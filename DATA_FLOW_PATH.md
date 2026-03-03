# ECG Data Flow Path

## Overview
The system processes RAW ECG data through two separate paths:
1. **Edge Computing Path**: For real-time processing and UI display
2. **Cache/Upload Path**: For storage and cloud upload

---

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        BLUETOOTH DEVICE                          │
│                    (ECG Sensor @ 1000 Hz)                        │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ Packets (244 bytes each)
                               │ Accumulated to 1 second
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DataHandler                               │
│  • Accumulates packets                                          │
│  • Extracts 1 second of RAW data                                │
│  • Output: 24000 bytes (1000 samples × 8 leads × 3 bytes)       │
└──────────────┬──────────────────────────────┬───────────────────┘
               │                              │
               │ RAW Data                     │ RAW Data
               │ (24000 bytes @ 1000 Hz)      │ (24000 bytes @ 1000 Hz)
               │                              │
               ▼                              ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│   PATH 1: EDGE COMPUTING │    │   PATH 2: CACHE          │
│                          │    │                          │
│  EdgeComputingProcessor  │    │  Cache                   │
└───────────┬──────────────┘    └───────────┬──────────────┘
            │                               │
            │ Step 1: Decimate              │ Step 1: Decimate
            │ (1000 → 100 Hz)               │ (1000 → 100 Hz)
            │ 24000 → 2400 bytes             │ 24000 → 2400 bytes
            │                               │
            ▼                               ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│  Step 2: Filter          │    │  Step 2: Store          │
│  (0.67-48 Hz)           │    │  • LRU Cache            │
│  • Biquad/FIR/Butterworth│    │  • For display          │
│  • Applied to all 8 leads│    │  • No filtering         │
└───────────┬──────────────┘    └───────────┬──────────────┘
            │                               │
            │ Filtered Data                 │ Decimated Data
            │ (2400 bytes @ 100 Hz)        │ (2400 bytes @ 100 Hz)
            │                               │
            ▼                               │
┌──────────────────────────┐              │
│  Step 3: Calculate HR     │              │
│  • Lead II (channel 1)   │              │
│  • 10-second rolling window│             │
│  • Pan-Tompkins algorithm│              │
└───────────┬──────────────┘              │
            │                               │
            ▼                               │
┌──────────────────────────┐              │
│  Step 4: Calculate SNR   │              │
│  • Selected leads        │              │
│  • 5-second rolling window│             │
│  • Signal-to-Noise Ratio │              │
└───────────┬──────────────┘              │
            │                               │
            ▼                               │
┌──────────────────────────┐              │
│  Step 5: Detect Saturation│              │
│  • Selected leads        │              │
│  • ADC limit checking    │              │
└───────────┬──────────────┘              │
            │                               │
            ▼                               │
┌──────────────────────────┐              │
│  LiveData (Observable)   │              │
│  • heartRateLive         │              │
│  • snrValuesLive         │              │
│  • saturatedLeadsLive    │              │
│  • lowSNRLeadsLive       │              │
│  • sensorCheckWarningLive│              │
└───────────┬──────────────┘              │
            │                               │
            ▼                               │
┌──────────────────────────┐              │
│      UI (LiveFragment)   │              │
│  • Display Heart Rate    │              │
│  • Display SNR           │              │
│  • Show Saturation Alert │              │
│  • Show SNR Alert        │              │
└──────────────────────────┘              │
                                          │
                                          ▼
                              ┌──────────────────────────┐
                              │  If Recording:           │
                              │  • Uploader.upload()     │
                              │  • RAW Data (24000 bytes)│
                              │  • No filtering applied  │
                              └───────────┬──────────────┘
                                          │
                                          ▼
                              ┌──────────────────────────┐
                              │      Cloud Storage        │
                              │  • RAW ECG data           │
                              │  • 1000 Hz, 8 leads       │
                              │  • Unfiltered            │
                              └──────────────────────────┘
```

---

## Detailed Path Descriptions

### PATH 1: Edge Computing (Processing & Display)

**Purpose**: Real-time processing for UI display

**Data Flow**:
1. **Input**: RAW data (24000 bytes @ 1000 Hz)
2. **Decimation**: 1000 Hz → 100 Hz (24000 → 2400 bytes)
3. **Filtering**: Baseline filter (0.67-48 Hz) using selected filter type
   - Filter types: Biquad, FIR, or Butterworth
   - Applied to all 8 leads
4. **Heart Rate Calculation**:
   - Uses Lead II (channel 1)
   - 10-second rolling window
   - Pan-Tompkins R-peak detection
   - Updates every 5-10 seconds
5. **SNR Calculation**:
   - Uses selected leads
   - 5-second rolling window
   - Signal-to-Noise Ratio in dB
6. **Saturation Detection**:
   - Checks selected leads for ADC saturation
   - Detects clipping at ±8,388,607 limits
7. **Output**: LiveData observables for UI

**Key Characteristics**:
- ✅ Filtering is **integral** to edge computing
- ✅ All calculations use **filtered data**
- ✅ Only processes when edge computing is **enabled** (red/green button)
- ✅ Data is **NOT** sent to cloud from this path

---

### PATH 2: Cache → Upload (Storage)

**Purpose**: Storage for display and cloud upload

**Data Flow**:
1. **Input**: RAW data (24000 bytes @ 1000 Hz)
2. **Decimation**: 1000 Hz → 100 Hz (24000 → 2400 bytes)
   - Only for LRU cache (display purposes)
3. **Storage**: 
   - Decimated data stored in LRU cache (5-minute window)
   - Used for web view display
4. **Upload** (if recording):
   - RAW data (24000 bytes @ 1000 Hz)
   - **NO filtering applied**
   - Uploaded directly to cloud via Uploader

**Key Characteristics**:
- ✅ **NO filtering** in this path
- ✅ RAW data is preserved for cloud upload
- ✅ Decimation only for local display cache
- ✅ Upload happens in `DataHandler.store()` method

---

## Code Locations

### Entry Point
- **File**: `DataHandler.kt`
- **Method**: `handle(packet: ByteArray)`
- **Location**: Lines 86-103

### Path 1: Edge Computing
- **File**: `EdgeComputingProcessor.kt`
- **Method**: `processRawData(rawBuffer: ByteArray)`
- **Location**: Lines 64-91

### Path 2: Cache
- **File**: `Cache.kt`
- **Method**: `put(stamp: Int, buffer: ByteArray)`
- **Location**: Lines 188-200

### Upload
- **File**: `DataHandler.kt`
- **Method**: `store(buffer: ByteArray, stamp: Int)`
- **Location**: Lines 190-195

---

## Data Sizes

| Stage | Size | Rate | Description |
|-------|------|------|-------------|
| RAW Input | 24000 bytes | 1000 Hz | 1000 samples × 8 leads × 3 bytes |
| Decimated | 2400 bytes | 100 Hz | 100 samples × 8 leads × 3 bytes |
| Filtered | 2400 bytes | 100 Hz | Same size, values filtered |

---

## Control Flow

### Edge Computing Enable/Disable
- **Control**: Filter button in `LiveFragment` (red/green)
- **Red**: Edge computing OFF (no processing)
- **Green**: Edge computing ON (processing active)
- **Location**: `LiveFragment.kt` line 173-182

### Recording Control
- **Control**: Record button (FAB)
- **When recording**: RAW data uploaded to cloud
- **Location**: `DataHandler.kt` line 190-195

---

## Important Notes

1. **Filter is part of Edge Computing**: The filter is NOT a separate component - it's integrated into the edge computing pipeline.

2. **Two Independent Paths**: Edge computing and cache/upload are completely separate. Edge computing does NOT affect cloud uploads.

3. **RAW Data to Cloud**: Cloud always receives unfiltered RAW data (24000 bytes @ 1000 Hz).

4. **Filtered Data for UI**: UI displays use filtered data from edge computing path.

5. **No Cross-Contamination**: Filtering in edge computing does NOT affect the cache/upload path.

6. **Decimation Logic Unchanged**: The decimation algorithm (1000 Hz → 100 Hz) is **exactly the same** as before:
   ```kotlin
   // Same algorithm in both paths:
   for (i in 0 until 100) {
       rawBuffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
   }
   ```
   - Takes every 10th sample (1000 Hz → 100 Hz)
   - Copies 24 bytes (8 channels × 3 bytes) per sample
   - No changes to the decimation algorithm itself

---

## LiveData Observables

All observables are exposed through `Cache` and originate from `EdgeComputingProcessor`:

- `heartRateLive: LiveData<Int>` - Current heart rate in BPM
- `snrValuesLive: LiveData<Pair<Double?, Double?>>` - SNR for Lead 1 and Lead 2
- `lowSNRLeadsLive: LiveData<List<Pair<Int, Double>>>` - Leads with low SNR
- `saturatedLeadsLive: LiveData<List<Int>>` - Leads with saturation
- `sensorCheckWarningLive: LiveData<Boolean>` - Sensor check needed flag

---

## Summary

```
RAW Data (24000 bytes @ 1000 Hz)
    │
    ├─→ Edge Computing → Filter → HR/SNR/Sat → LiveData → UI
    │
    └─→ Cache → Decimate → LRU → (if recording) Upload RAW → Cloud
```

**Key Principle**: Edge computing processes and displays filtered data locally. Cloud receives unfiltered RAW data for analysis.

