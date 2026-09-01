# Sydäntek - ECG Monitoring Application

## 📱 Overview

Sydäntek is an Android application for real-time ECG (Electrocardiogram) monitoring and recording. The app connects to Bluetooth-enabled ECG sensors, processes ECG data using edge computing, calculates vital parameters (heart rate, SNR, saturation), and uploads recordings to cloud storage. It also sends recording completion notifications to Telegram.

**App Name:** Sydäntek  
**Package:** `com.carditek.kesar`  
**Version:** 4.0.0  
**Min SDK:** 21 (Android 5.0)  
**Target SDK:** 33 (Android 13)

---

## 🎯 Key Features

### 1. **Bluetooth ECG Sensor Connection**
- Connect to Bluetooth Low Energy (BLE) ECG sensors
- Real-time data streaming at 1000 Hz
- Support for 8 ECG leads (Lead I, Lead II, V1-V6)
- Automatic reconnection handling
- Battery level monitoring

### 2. **Edge Computing Processing**
- Real-time ECG signal filtering (0.67-48 Hz bandpass)
- Three filter types: Biquad, FIR, and Butterworth
- Heart rate calculation using Pan-Tompkins algorithm
- Signal-to-Noise Ratio (SNR) calculation
- Saturation detection for ADC clipping
- Live display of processed parameters

### 3. **ECG Recording & Cloud Upload**
- Start/stop recording functionality
- Automatic cloud upload of RAW ECG data
- Medical history/notes attachment
- Timestamp tracking (15-second intervals)
- Telegram notifications on recording completion

### 4. **Patient Management**
- Patient selection from contacts
- Medical history/notes storage
- Patient information linked to recordings

### 5. **Real-time Monitoring**
- Live ECG waveform display (WebView)
- Heart rate display (BPM)
- SNR values for selected leads
- Saturation warnings
- Sensor quality alerts

---

## 🏗️ Architecture

### Application Structure

```
com.carditek.kesar/
├── MainActivity.kt              # Main activity with navigation
├── Application.kt               # Application class with Hilt
├── Device.kt                    # Device interface
├── DeviceImpl.kt                # Device implementation
│
├── bluetooth/                   # Bluetooth communication
│   ├── Connection.kt           # BLE connection management
│   ├── DataHandler.kt           # Data packet processing
│   ├── State.kt                # Connection state management
│   ├── Protocol.kt             # Communication protocol
│   └── Parameters.kt           # Bluetooth parameters
│
├── service/                     # Background services
│   ├── BluetoothService.kt     # Foreground service for BLE
│   └── Controller.kt           # Service controller
│
├── cloud/                       # Cloud upload
│   └── Uploader.kt             # WorkManager-based upload
│
├── module/                      # Dependency injection modules
│   ├── BluetoothDevice.kt      # Device module
│   ├── CacheModule.kt          # Cache module
│   ├── StateModule.kt          # State module
│   ├── Patient.kt              # Patient module
│   └── GoogleAccount.kt        # Google account module
│
├── util/                        # Utilities
│   ├── filters/edgecomputing/  # Edge computing filters
│   │   ├── EdgeComputingProcessor.kt
│   │   ├── ECGFilter.kt
│   │   ├── HeartRateCalculator.kt
│   │   ├── SNRCalculator.kt
│   │   ├── SaturationDetector.kt
│   │   ├── Biquad.kt
│   │   ├── FIRFilter.kt
│   │   └── ButterworthFilter.kt
│   ├── Bluetooth.kt            # Bluetooth utilities
│   ├── CloudBackend.kt          # Backend communication
│   └── Configuration.kt         # App configuration
│
├── ui/                          # UI components
│   └── device_list/
│       └── PatchesDialog.kt    # Device selection dialog
│
├── fragments/                   # Main fragments
│   ├── LiveFragment.kt         # Live monitoring
│   ├── RecordFragment.kt        # Recording interface
│   ├── StatusFragment.kt        # Connection status
│   └── SettingsFragment.kt      # App settings
│
├── Database.kt                  # Room database
├── Cache.kt                     # LRU cache for display
└── WebViewFragment.kt          # Base WebView fragment
```

---

## 📊 Data Flow

### Complete Processing Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│              BLUETOOTH ECG SENSOR (1000 Hz)                  │
│          Sends packets: 244 bytes each, 10 packets/sec      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    DataHandler.handle()                     │
│  • Accumulates packets to 1 second                          │
│  • Extracts RAW data: 24000 bytes                           │
│    (1000 samples × 8 leads × 3 bytes)                       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               │ PATH 1: Edge Computing       │ PATH 2: Cache/Upload
               ▼                              ▼
┌──────────────────────────────┐  ┌──────────────────────────────┐
│ EdgeComputingProcessor       │  │ Cache.put()                  │
│                               │  │ • Decimate: 1000→100 Hz      │
│ 1. Decimate (1000→100 Hz)    │  │ • Store in LRU cache         │
│ 2. Filter (0.67-48 Hz)       │  │ • (if recording) Upload RAW │
│ 3. Calculate HR              │  │                              │
│ 4. Calculate SNR              │  │ Uploader.upload()           │
│ 5. Detect Saturation          │  │ • RAW data (24000 bytes)     │
│ 6. Update LiveData           │  │ • No filtering               │
└──────────────┬───────────────┘  └──────────────┬───────────────┘
               │                                  │
               ▼                                  ▼
┌──────────────────────────────┐  ┌──────────────────────────────┐
│ UI Display (LiveFragment)    │  │ Cloud Storage                │
│ • Heart Rate (BPM)            │  │ • RAW ECG data               │
│ • SNR values (dB)            │  │ • 1000 Hz, 8 leads           │
│ • Saturation warnings        │  │ • Unfiltered                 │
│ • ECG waveform (WebView)     │  └──────────────────────────────┘
└──────────────────────────────┘
```

---

## 🔧 Technical Details

### Edge Computing Processing

#### 1. **Decimation**
- **Input:** 24000 bytes @ 1000 Hz
- **Output:** 2400 bytes @ 100 Hz
- **Method:** Every 10th sample (1 sample per 10ms)
- **Formula:** `sampled[i] = raw[i * 10]` for i = 0 to 99

#### 2. **Filtering**
- **Type:** Bandpass filter (0.67-48 Hz)
- **Purpose:** Remove baseline wander and high-frequency noise
- **Filter Options:**
  - **Biquad** (Default): Fast, low latency, 2nd order IIR
  - **FIR**: Linear phase, 64th order, better for analysis
  - **Butterworth**: 6th order, maximally flat passband

**Filter Parameters:**
- Sample Rate: 100 Hz (after decimation)
- High-pass Cutoff: 0.67 Hz (baseline wander removal)
- Low-pass Cutoff: 48 Hz (noise removal)
- Number of Leads: 8 (separate filter per lead)

**Filter Formulas:**

**Biquad Filter:**
```
High-pass (0.67 Hz):
w0 = 2π × frequencyHz / sampleRateHz
alpha = sin(w0) / (2 × q)  [q = 0.707]
cosw = cos(w0)

b0 = (1 + cosw) / 2
b1 = -(1 + cosw)
b2 = (1 + cosw) / 2
a0 = 1 + alpha
a1 = -2 × cosw
a2 = 1 - alpha

Normalized coefficients:
b0' = b0 / a0
b1' = b1 / a0
b2' = b2 / a0
a1' = a1 / a0
a2' = a2 / a0

Processing equation:
y[n] = b0 × x[n] + z1
z1 = b1 × x[n] - a1 × y[n] + z2
z2 = b2 × x[n] - a2 × y[n]

Low-pass (48 Hz):
b0 = (1 - cosw) / 2
b1 = 1 - cosw
b2 = (1 - cosw) / 2
[Same a0, a1, a2 as high-pass]
```

**FIR Filter:**
```
Coefficient design (bandpass):
Normalized frequencies:
fc1 = highCutoffHz / sampleRateHz  (0.67 / 100 = 0.0067)
fc2 = lowCutoffHz / sampleRateHz   (48 / 100 = 0.48)

For each coefficient i (0 to n, where n=64):
m = i - n/2

If m == 0:
    h[i] = 2 × (fc2 - fc1)
Else:
    h[i] = (sin(2π × fc2 × m) - sin(2π × fc1 × m)) / (π × m)

Apply Hamming window:
w[i] = 0.54 - 0.46 × cos(2π × i / n)
h[i] = h[i] × w[i]

Normalize:
sum = Σ h[i]
h[i] = h[i] / sum

Processing equation (convolution):
y[n] = Σ (h[i] × x[n-i]) for i = 0 to order
```

**Butterworth Filter:**
```
Pre-warp frequency:
wc = 2 × sampleRateHz × tan(π × fc / sampleRateHz)
k = wc / sampleRateHz

For each biquad section i:
angle = π × (2i + 1) / (2 × order)
alpha = sin(angle)
beta = cos(angle)

High-pass:
a0 = 1 + 2 × alpha × k + k²
a1 = 2 × (k² - 1) / a0
a2 = (1 - 2 × alpha × k + k²) / a0
b0 = k² / a0
b1 = -2 × k² / a0
b2 = k² / a0

Low-pass:
[Same a0, a1, a2]
b0 = k² / a0
b1 = 2 × k² / a0
b2 = k² / a0

Cascaded processing:
y = x
For each high-pass section: y = section.process(y)
For each low-pass section: y = section.process(y)
```

#### 3. **Heart Rate Calculation**
- **Algorithm:** Pan-Tompkins R-peak detection
- **Lead:** Lead II (channel 1)
- **Window:** 10 seconds (1000 samples @ 100 Hz)
- **Update:** Every 5-10 seconds
- **Range:** 30-200 BPM (clamped)
- **Smoothing:** Median of last 3 values

**Complete Process:**
```
Step 1: Normalization
mean = (Σ ecg[i]) / N
norm[i] = ecg[i] - mean

Step 2: Derivative (slope)
diff[i] = norm[i+1] - norm[i]

Step 3: Squaring
squared[i] = diff[i]²

Step 4: Moving Average (150 ms window)
win = 0.150 × fs = 15 samples
ma[i] = (Σ squared[j]) / win  for j = i-win to i

Step 5: Adaptive Threshold
threshold = average(ma) × 0.5
If threshold ≤ 0: set to 0.000001

Step 6: Peak Detection
For each sample i:
    if (ma[i] > threshold) AND
       (ma[i] > ma[i-1]) AND
       (ma[i] > ma[i+1]) AND
       (i - lastPeak) ≥ refractory_period (300ms = 30 samples):
        → R-peak detected at i

Step 7: RR Interval Filtering
For each pair of consecutive R-peaks:
    interval = rPeaks[i] - rPeaks[i-1]  (in samples)
    bpm = (60 × fs) / interval
    
    If bpm in [30, 300]:
        → Valid RR interval

Step 8: Median RR → Heart Rate
medianRR = median(sorted RR intervals)
HR = (60 × fs) / medianRR

Step 9: Smoothing & Clamping
Add HR to history (keep last 3 values)
smoothedHR = median(hrHistory)
finalHR = clamp(smoothedHR, 30, 200)
```

#### 4. **SNR Calculation**
- **Window:** 5 seconds (500 samples @ 100 Hz)
- **Threshold:** 0.0 dB (positive = good signal)
- **Default Leads:** Lead 1 and Lead 2
- **Alerts:** Shows dialog when SNR ≤ 0.0 dB

**Complete Formula:**
```
Step 1: Signal Power Calculation
mean = (Σ leadData[i]) / N
signal_variance = (Σ (leadData[i] - mean)²) / N
signal_power = signal_variance

Step 2: Noise Power Calculation (First Difference Method)
First difference (high-frequency component):
noise_data[i] = leadData[i+1] - leadData[i]

noise_mean = (Σ noise_data[i]) / (N-1)
noise_variance = (Σ (noise_data[i] - noise_mean)²) / (N-1)
noise_power = 0.5 × noise_variance

Why 0.5?
- First difference amplifies high-frequency components (noise)
- 0.5 factor compensates for variance increase from differentiation
- Provides better estimate of actual noise power

Step 3: SNR Ratio & dB Conversion
eps = 1e-12  (guard against division by zero)
snr_ratio = signal_power / (noise_power + eps)
snr_dB = 10 × log₁₀(snr_ratio)

Step 4: Threshold Check
If snr_dB ≤ 0.0 dB:
    → Lead has low SNR (sensor check needed)
```

**Edge Cases:**
- If `snrValidSamples < 2`: Return -∞ dB (need at least 2 samples for difference)
- If `signal_power ≤ 0` or `noise_power ≤ 0`: Return -∞ dB
- If `snr_dB` is NaN: Treat as below threshold

#### 5. **Saturation Detection**
- **Check:** All 8 leads (configurable)
- **Window:** 1 second (100 samples)
- **Threshold:** 99.9% of ADC limits
- **Alert:** Shows dialog when saturation detected

**Complete Detection Logic:**
```
Step 1: Define Saturation Limits
MAX_VALUE = 2²³ - 1 = 8,388,607   (0x7FFFFF)
MIN_VALUE = -2²³ = -8,388,608     (0x800000)

SATURATION_THRESHOLD_POSITIVE = MAX_VALUE × 0.999
                                = 8,388,607 × 0.999
                                = 8,380,358

SATURATION_THRESHOLD_NEGATIVE = MIN_VALUE × 0.999
                                = -8,388,608 × 0.999
                                = -8,380,358

Why 99.9%?
- Accounts for ADC quantization near limits
- Detects near-saturation (within 0.1% of limits)
- Prevents false positives from normal signal variations

Step 2: Check Each Sample
For each selected lead:
    For each sample in 1-second window (100 samples):
        value = read24(sampled, offset)
        
        if (value ≥ 8,380,358) OR (value ≤ -8,380,358):
            → Lead is saturated
            → Break (no need to check more samples)

Step 3: Report Saturated Leads
saturated_leads = [lead_numbers where saturation detected]
```

---

## 📱 User Interface

### Main Screens

#### 1. **Live Fragment** (`LiveFragment.kt`)
- **Purpose:** Real-time ECG monitoring
- **Features:**
  - ECG waveform display (WebView)
  - Heart rate display (BPM) - only shown if Lead 2 has good quality
  - SNR values for Lead 1 and Lead 2
  - Edge computing toggle (red/green button)
    - Red: Edge computing OFF
    - Green: Edge computing ON
  - Recording start/stop button (FAB)
  - Timestamp counter (TS=1, TS=2, etc., shows "✓" at TS=15)
  - Sensor quality alerts:
    - SNR warning dialog (when SNR ≤ 0.0 dB)
    - Saturation warning dialog (when ADC clipping detected)

#### 2. **Record Fragment** (`RecordFragment.kt`)
- **Purpose:** Patient selection and recording
- **Features:**
  - Patient selection from contacts
  - Medical history/notes input dialog
  - Medical history display (if unuploaded note exists)
  - Recording start/stop button
  - Automatic medical history upload on recording start
  - Telegram notification on recording stop

#### 3. **Status Fragment** (`StatusFragment.kt`)
- **Purpose:** Connection and device status
- **Features:**
  - Bluetooth connection status
  - Device MAC address
  - Data transfer statistics (bytes, packets)
  - Connection/disconnection count
  - Data rate (KB/s)

#### 4. **Settings Fragment** (`SettingsFragment.kt`)
- **Purpose:** App configuration
- **Features:**
  - App preferences
  - Device selection
  - Account settings

### Navigation Drawer

- **Record:** Patient selection and recording
- **Status:** Connection status
- **Live:** Real-time monitoring
- **Settings:** App settings
- **Device List:** Bluetooth device selection
- **SNR & Saturation:** Lead selection for SNR/saturation calculation
- **Sign Out:** Google account sign out

---

## 🔌 Bluetooth Communication

### Connection Process

1. **Device Selection**
   - User opens navigation drawer
   - Selects "Device List"
   - `PatchesDialog` shows available BLE devices
   - User selects ECG sensor
   - MAC address stored in SharedPreferences (`"device"` key)
   - `BluetoothService` receives preference change

2. **BLE Connection**
   - `BluetoothService` creates `Connection` object
   - GATT connection established via `BluetoothGatt`
   - Characteristics discovered
   - Notifications enabled for data reception
   - Connection state tracked in `State` object

3. **Data Reception**
   - Packets received via `BluetoothGattCallback.onCharacteristicChanged()`
   - Each packet: 244 bytes
   - 10 packets per second
   - Accumulated to 1 second (24000 bytes) in `DataHandler`
   - Processed by `DataHandler.handle()`

### Data Format

- **Packet Size:** 244 bytes
- **Header:** 4 bytes (timestamp, serial number)
- **Payload:** 240 bytes (244 - 4 header bytes)
- **Samples per Packet:** 10 samples (240 / 24 bytes)
- **Bytes per Sample:** 24 bytes (8 leads × 3 bytes)
- **Packets per Second:** 10 packets
- **Total Data Rate:** 2400 bytes/second (after decimation)

### Protocol Details

- **Sample Format:** 24-bit signed integers (3 bytes per sample)
- **Endianness:** Little-endian
- **Channels:** 8 leads (Lead I, Lead II, V1-V6)
- **Sample Rate:** 1000 Hz (RAW), 100 Hz (processed)
- **Buffer Size:** 15 seconds (360 KB @ 1000 Hz)

---

## ☁️ Cloud Upload

### Upload Process

1. **Recording Start**
   - User clicks record button in `LiveFragment` or `RecordFragment`
   - `firstTimestamp` captured: `((System.currentTimeMillis() / 15000) * 15).toInt()`
   - Medical history uploaded if available (unuploaded note from database)
   - Recording flag set to `true` via `device.setRecording(true)`
   - `DataHandler.recording` flag set to `true`

2. **Data Upload**
   - RAW data (24000 bytes @ 1000 Hz) uploaded every second
   - **No filtering applied** (preserves RAW data for analysis)
   - Uploaded via WorkManager (background task)
   - Chunked into 15-second intervals
   - Uploaded via `Uploader.upload(stamp, LEADS, FREQUENCY, buffer)`

3. **Recording Stop**
   - User clicks record button again
   - `lastTimestamp` calculated: `((System.currentTimeMillis() / 15000) * 15).toInt()`
   - Telegram notification sent automatically
   - `firstTimestamp` cleared (set to null)
   - Recording flag set to `false`
   - `DataHandler.recording` flag set to `false`

### Telegram Integration

**Notification on Recording Stop:**
```
ECG Recording Completed
MAC ID: 54:6C:0E:83:3E:49
StartTimestamp: 1700000000
LastTimestamp: 1700000450
```

**API Endpoint:**
```
https://api.telegram.org/bot{BOT_TOKEN}/sendMessage?chat_id={CHAT_ID}&text={MESSAGE}
```

**Configuration:**
- Bot Token: use `TELEGRAM_BOT_TOKEN` (build-time Gradle property; do not hardcode in repo)
- Chat ID: use `TELEGRAM_CHAT_ID` (build-time Gradle property; do not hardcode in repo)
- Enable flag: `TELEGRAM_ENABLED=true` to send Telegram (disable for production; use backend)

**Complete Process:**
```
1. Calculate timestamps (rounded to 15-second intervals)
   firstTimestamp = (System.currentTimeMillis() / 15000) * 15
   lastTimestamp = (System.currentTimeMillis() / 15000) * 15

2. Get MAC ID from device address
   macId = device.address.value ?: "Unknown"

3. Build message
   message = """
        ECG Recording Completed
        MAC ID: $macId
        StartTimestamp: $firstTimestamp
        LastTimestamp: $lastTimestamp
   """

4. URL encode message
   encodedMessage = URLEncoder.encode(message, "UTF-8")

5. Build Telegram API URL
   urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

6. Send HTTP GET request
   - Method: GET
   - Timeout: 5 seconds (connect & read)
   - Success: HTTP 200
   - Error handling: Logs error if request fails
```

**Implementation Locations:**
- `LiveFragment.sendMacIdToTelegram()` (lines 235-267)
- `RecordFragment.sendMacIdToTelegram()` (lines 317-343)

---

## 💾 Database

### Room Database (`ChunkDatabase`)

#### Tables

1. **Chunk Table**
   - **Purpose:** Stores ECG data chunks for local caching
   - **Primary Key:** `(address, timestamp)`
   - **Fields:**
     - `address`: String (MAC address)
     - `timestamp`: Int (15-second interval)
     - `data`: ByteArray (ECG data)

2. **Medical Note Table**
   - **Purpose:** Stores patient medical history/notes
   - **Primary Key:** `id` (auto-generated)
   - **Fields:**
     - `id`: Long (auto-generated)
     - `noteText`: String (medical history text)
     - `createdAt`: Long (timestamp in milliseconds)
     - `uploaded`: Boolean (upload status)
     - `uploadTimestamp`: Long? (when uploaded, nullable)

### Database Operations

**Medical Note Operations:**
- `getLatestNote()`: Get most recent note
- `getUnuploadedNote()`: Get unuploaded note for upload
- `getNoteById(id)`: Get note by ID
- `insert(note)`: Insert new note
- `markAsUploaded(id, timestamp)`: Mark note as uploaded
- `deleteNote(id)`: Delete specific note
- `deleteUploadedNotes()`: Cleanup uploaded notes
- `clearAllNotes()`: Clear all notes

### Cache System

- **Type:** LRU (Least Recently Used) cache
- **Size:** 5 minutes of data
- **Purpose:** Local display buffer for WebView
- **Data:** Decimated (100 Hz), unfiltered
- **Implementation:** `Cache.kt`

**Cache Operations:**
- `put(stamp, buffer)`: Store decimated data
- `get(stamp)`: Retrieve data for specific timestamp
- `clear()`: Clear all cached data

---

## 🔐 Permissions

### Required Permissions

```xml
<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

<!-- Location (required for BLE scanning) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- System -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### Features Required

```xml
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
```

---

## 🛠️ Dependencies

### Key Libraries

**Dependency Injection:**
- `com.google.dagger:hilt-android:$hilt_version` - Dagger Hilt
- `androidx.hilt:hilt-work:1.0.0` - Hilt for WorkManager

**Database:**
- `androidx.room:room-runtime:$room_version` - Room database
- `androidx.room:room-ktx:$room_version` - Room Kotlin extensions

**Background Tasks:**
- `androidx.work:work-runtime-ktx:$work_version` - WorkManager

**Networking:**
- `com.squareup.okhttp3:okhttp:4.12.0` - HTTP client for Telegram
- `com.squareup.okhttp3:okhttp-urlconnection:5.0.0-alpha.2` - URL connection

**Firebase:**
- `com.google.firebase:firebase-analytics-ktx` - Analytics
- `com.google.firebase:firebase-crashlytics-ktx` - Crashlytics

**UI:**
- `androidx.navigation:navigation-fragment-ktx:$nav_version` - Navigation
- `com.google.android.material:material:1.4.0` - Material Design
- `androidx.lifecycle:lifecycle-livedata-ktx:2.4.0` - LiveData
- `androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0` - ViewModel

**Coroutines:**
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2` - Coroutines

**Other:**
- `com.googlecode.libphonenumber:libphonenumber:8.12.21` - Phone number validation
- `com.google.android.gms:play-services-auth:20.0.0` - Google Sign-In

---

## 📈 Performance Characteristics

### Data Processing

- **Input Rate:** 24000 bytes/second (1000 Hz)
- **Processing Rate:** 2400 bytes/second (100 Hz after decimation)
- **Filter Latency:**
  - Biquad: ~2 samples (20ms @ 100 Hz)
  - FIR: ~64 samples (640ms @ 100 Hz)
  - Butterworth: ~12 samples (120ms @ 100 Hz)

### Memory Usage

- **Cache Size:** ~720 KB (5 minutes @ 100 Hz)
- **Buffer Size:** ~360 KB (15 seconds @ 1000 Hz)
- **SNR Window:** ~4 KB per lead (500 samples × 8 bytes)
- **HR Window:** ~8 KB (1000 samples × 8 bytes)

### CPU Usage

- **Edge Computing:** Moderate (filtering + calculations)
- **Bluetooth:** Low (data reception only)
- **Upload:** Background (WorkManager)

---

## 🚀 Build & Installation

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 8 or later
- Android SDK 31+
- Gradle 7.0+
- Git (for version tracking)

### Build Steps

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd "Telegram android app"
   ```

2. **Configure Firebase**
   - Create Firebase project
   - Add `google-services.json` to `app/` directory
   - Configure Firebase Analytics and Crashlytics

3. **Build APK**
   ```bash
   ./gradlew assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/sydantek.apk`

4. **Build Release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/sydantek.apk`

5. **Install via ADB**
   ```bash
   adb install app/build/outputs/apk/debug/sydantek.apk
   ```

### Build Configuration

- **Compile SDK:** 31
- **Min SDK:** 21
- **Target SDK:** 33
- **Build Tools:** 31.0.0
- **Kotlin:** 1.8
- **Java:** 1.8

---

## 📝 Usage Guide

### Starting a Recording

1. **Select Patient**
   - Open Record Fragment (navigation drawer → Record)
   - Click "Select Patient" button
   - Choose patient from contacts
   - (Optional) Click "Add Note" to add medical history
   - Enter medical history/notes in dialog
   - Click "Save"

2. **Connect Device**
   - Open navigation drawer
   - Select "Device List"
   - Wait for Bluetooth scan
   - Choose ECG sensor from list
   - Wait for connection (check Status Fragment)

3. **Start Recording**
   - Go to Live Fragment (navigation drawer → Live)
   - Ensure edge computing is ON (green button "S")
   - Click Record button (FAB - Floating Action Button)
   - Recording starts, timestamp counter appears (TS=1, TS=2, etc.)
   - Medical history automatically uploaded if available

4. **Monitor**
   - Watch heart rate (BPM) - displayed if Lead 2 has good quality
   - Check SNR values for Lead 1 and Lead 2
   - Monitor for saturation warnings (popup dialogs)
   - View ECG waveform in WebView
   - Check timestamp counter progress

5. **Stop Recording**
   - Click Record button again (FAB)
   - Recording stops
   - Telegram notification sent automatically
   - Data uploaded to cloud
   - Toast message: "Recording Stopped"

### Edge Computing Control

- **Enable:** Click "S" button (turns green)
- **Disable:** Click "S" button again (turns red)
- **When ON:** All processing active (filtering, HR, SNR, saturation)
- **When OFF:** No processing, no calculations

### Lead Selection for SNR/Saturation

1. Open navigation drawer
2. Select "SNR & Saturation"
3. Choose leads to monitor (default: Lead 1, Lead 2)
4. Click OK
5. Toast confirmation shown

---

## 🔍 Troubleshooting

### Common Issues

1. **Bluetooth Connection Fails**
   - **Symptoms:** Device not connecting, connection lost
   - **Solutions:**
     - Check Bluetooth is enabled on device
     - Ensure location permission granted (required for BLE scanning)
     - Restart Bluetooth service
     - Check device is in range (within 10 meters)
     - Restart app
     - Check device battery level

2. **No Heart Rate Display**
   - **Symptoms:** Heart rate shows "-- bpm"
   - **Solutions:**
     - Ensure edge computing is ON (green button)
     - Check Lead 2 SNR > 0.0 dB (check SNR display)
     - Verify Lead 2 is not saturated (check for saturation dialog)
     - Wait 10 seconds for initial calculation (needs 10-second window)
     - Check electrode contact for Lead 2

3. **SNR Warnings**
   - **Symptoms:** "Sensor Check Required" dialog appears
   - **Solutions:**
     - Check electrode contact (ensure good skin contact)
     - Verify sensor placement (correct lead positions)
     - Clean electrode contacts (remove dirt, oil, sweat)
     - Ensure proper skin preparation (clean, dry skin)
     - Check for loose connections
     - Verify sensor is properly attached

4. **Saturation Detected**
   - **Symptoms:** "Saturation Detected" dialog appears
   - **Solutions:**
     - Reduce sensor gain (if possible in sensor settings)
     - Check electrode contact (may be too tight or loose)
     - Verify sensor connection (check all connections)
     - Ensure proper sensor placement
     - Check for loose connections
     - Verify sensor is not damaged

5. **Telegram Not Sent**
   - **Symptoms:** No Telegram message after recording stop
   - **Solutions:**
     - Check internet connection
     - Verify bot token and chat ID are correct
     - Check app logs for errors (Logcat)
     - Ensure recording was properly stopped (not force-closed)
     - Check Telegram API is accessible
     - Verify HTTP request succeeded (check logs)

6. **No ECG Waveform Display**
   - **Symptoms:** WebView shows blank or error
   - **Solutions:**
     - Check internet connection (WebView loads from web)
     - Verify device is connected
     - Check WebView URL is accessible
     - Clear app cache
     - Restart app

7. **Recording Not Starting**
   - **Symptoms:** Record button doesn't start recording
   - **Solutions:**
     - Ensure patient is selected
     - Check device is connected
     - Verify device address is set
     - Check app permissions
     - Restart app

8. **Data Not Uploading**
   - **Symptoms:** Recording completes but data not in cloud
   - **Solutions:**
     - Check internet connection
     - Verify WorkManager is running
     - Check app logs for upload errors
     - Ensure recording flag was set correctly
     - Check cloud backend is accessible

---

## 📚 Code Structure

### Key Classes

#### `EdgeComputingProcessor`
- **Location:** `util/filters/edgecomputing/EdgeComputingProcessor.kt`
- **Purpose:** Main edge computing orchestrator
- **Responsibilities:**
  - Processes RAW data through pipeline
  - Manages filter, HR, SNR, saturation calculations
  - Exposes LiveData for UI
- **Key Methods:**
  - `processRawData(rawBuffer: ByteArray)` - Main processing entry point
  - `setEnabled(enabled: Boolean)` - Enable/disable edge computing
  - `setSelectedLeads(leads: Set<Int>)` - Set leads for SNR/saturation

#### `DataHandler`
- **Location:** `bluetooth/DataHandler.kt`
- **Purpose:** Receives and processes Bluetooth packets
- **Responsibilities:**
  - Accumulates packets to 1-second buffers
  - Routes data to edge computing and cache
  - Manages recording state
- **Key Methods:**
  - `handle(packet: ByteArray)` - Process incoming packet
  - `store(buffer: ByteArray, stamp: Int)` - Upload data to cloud

#### `HeartRateCalculator`
- **Location:** `util/filters/edgecomputing/HeartRateCalculator.kt`
- **Purpose:** Calculate heart rate from ECG signal
- **Algorithm:** Pan-Tompkins R-peak detection
- **Key Methods:**
  - `updateHeartRateWindow(sampled: ByteArray, read24: Function)` - Update data window
  - `calculateHeartRateFromWindow()` - Calculate HR from window
  - `detectRPeaksAndCalculateHR(ecg: DoubleArray, fs: Int)` - R-peak detection

#### `SNRCalculator`
- **Location:** `util/filters/edgecomputing/SNRCalculator.kt`
- **Purpose:** Calculate signal-to-noise ratio
- **Key Methods:**
  - `calculateSNRForLeads(sampled: ByteArray, read24: Function)` - Calculate SNR
  - `computeSNR(data: DoubleArray, leadNumber: Int)` - Compute SNR for one lead
  - `setSelectedLeads(leads: Set<Int>)` - Set leads to monitor

#### `SaturationDetector`
- **Location:** `util/filters/edgecomputing/SaturationDetector.kt`
- **Purpose:** Detect ADC saturation/clipping
- **Key Methods:**
  - `detectSaturation(sampled: ByteArray, read24: Function)` - Check for saturation
  - `setSelectedLeads(leads: Set<Int>)` - Set leads to check

#### `ECGFilter`
- **Location:** `util/filters/edgecomputing/ECGFilter.kt`
- **Purpose:** Filter manager for all 8 leads
- **Supports:** Biquad, FIR, Butterworth filters
- **Key Methods:**
  - `applyFiltering(sampled: ByteArray, read24: Function, write24: Function)` - Apply filters
  - `setFilterType(type: FilterType)` - Change filter type
  - `setFilteringEnabled(enabled: Boolean)` - Enable/disable filtering

#### `BluetoothService`
- **Location:** `service/BluetoothService.kt`
- **Purpose:** Foreground service for Bluetooth management
- **Responsibilities:**
  - Manages Bluetooth connections
  - Handles device selection
  - Manages recording state
- **Key Methods:**
  - `onSharedPreferenceChanged()` - Handle device/recording changes

#### `Connection`
- **Location:** `bluetooth/Connection.kt`
- **Purpose:** BLE connection management
- **Responsibilities:**
  - Establish GATT connection
  - Handle characteristic notifications
  - Manage connection state

#### `Uploader`
- **Location:** `cloud/Uploader.kt`
- **Purpose:** Cloud data upload
- **Implementation:** WorkManager-based background upload
- **Key Methods:**
  - `upload(stamp: Int, leads: Int, frequency: Int, buffer: ByteArray)` - Upload ECG data
  - `note(stamp: Int, note: String)` - Upload medical history

---

## 🔄 Data Formats

### Timestamp Format
- **Unit:** Seconds (rounded to 15-second intervals)
- **Calculation:** `(System.currentTimeMillis() / 15000) * 15`
- **Example:** `1700000000` = Jan 14, 2024, 00:00:00 UTC
- **Purpose:** Aligns with data chunking intervals (15 seconds)

### ECG Data Format
- **RAW:** 24-bit signed integers (3 bytes per sample)
- **Endianness:** Little-endian
- **Channels:** 8 leads (Lead I, Lead II, V1-V6)
- **Sample Rate:** 1000 Hz (RAW), 100 Hz (processed)
- **Data Size:** 24000 bytes/second (RAW), 2400 bytes/second (processed)

### Packet Format
- **Total Size:** 244 bytes
- **Header:** 4 bytes (timestamp, serial number)
- **Payload:** 240 bytes (10 samples × 24 bytes)
- **Samples per Packet:** 10
- **Packets per Second:** 10

---

## 🔐 Security & Privacy

### Data Handling
- **Local Storage:** ECG data cached locally (5 minutes)
- **Cloud Upload:** RAW ECG data uploaded to cloud
- **Medical History:** Stored locally, uploaded with recordings
- **Telegram:** Only MAC ID and timestamps sent (no patient data)

### Permissions
- **Bluetooth:** Required for sensor connection
- **Location:** Required for BLE scanning (Android requirement)
- **Internet:** Required for cloud upload and Telegram
- **Storage:** Not required (uses Room database)

---

## 📞 Support & Contact

For issues, questions, or contributions:
- Check app logs (Logcat) for error messages
- Review troubleshooting section above
- Contact development team
- Refer to project repository

---

## 📄 License

[Specify license here]

---

## 🎯 Future Enhancements

- [ ] Additional filter types (Elliptic, Chebyshev)
- [ ] Real-time arrhythmia detection
- [ ] Export ECG data to PDF/CSV
- [ ] Multi-language support
- [ ] Offline mode improvements
- [ ] Enhanced visualization options
- [ ] QRS complex analysis
- [ ] ST segment analysis
- [ ] Heart rate variability (HRV) calculation
- [ ] Custom filter parameter configuration

---

## 📊 Version History

### Version 4.0.0
- Edge computing implementation
- Heart rate calculation (Pan-Tompkins)
- SNR calculation
- Saturation detection
- Telegram integration
- Medical history/notes
- Multiple filter types (Biquad, FIR, Butterworth)

---

## 🧪 Testing

### Manual Testing Checklist

- [ ] Bluetooth connection/disconnection
- [ ] ECG data reception
- [ ] Edge computing enable/disable
- [ ] Heart rate calculation
- [ ] SNR calculation
- [ ] Saturation detection
- [ ] Recording start/stop
- [ ] Cloud upload
- [ ] Telegram notification
- [ ] Patient selection
- [ ] Medical history input
- [ ] Lead selection
- [ ] Filter type switching
- [ ] UI responsiveness
- [ ] Error handling

---

## 📖 References

### Algorithms
- **Pan-Tompkins Algorithm:** R-peak detection for ECG
- **FIR Filter Design:** Windowed sinc method
- **Butterworth Filter:** Bilinear transform design
- **Biquad Filter:** Direct form II implementation

### Standards
- **ECG Lead Configuration:** Standard 12-lead ECG (8 leads used)
- **Sample Rates:** 1000 Hz (RAW), 100 Hz (processed)
- **Filter Specifications:** 0.67-48 Hz bandpass (baseline wander + noise removal)

---

**Last Updated:** 2024  
**Version:** 4.0.0  
**Maintained by:** Carditek Team  
**Documentation Version:** 1.0

