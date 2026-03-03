# Cache.kt Changes Comparison

## Overview
This document shows the key changes between the old Cache.kt and the new implementation after moving to edge computing architecture.

---

## Key Changes Summary

### ❌ REMOVED from Cache.kt:
1. **Filtering logic** - Completely removed
2. **SignalFilter instances** - No longer in Cache
3. **filteringEnabled flag** - Moved to EdgeComputingProcessor
4. **Filter application in put()** - Removed conditional filtering

### ✅ ADDED to Cache.kt:
1. **EdgeComputingProcessor dependency** - Injected via constructor
2. **LiveData exposure** - Exposes LiveData from EdgeComputingProcessor
3. **Delegation methods** - setFilteringEnabled() and isFilteringEnabled() delegate to EdgeComputingProcessor

---

## Detailed Comparison

### 1. Constructor Changes

**OLD:**
```kotlin
class Cache @Inject constructor(
    private val context: Context, 
    private val device: Device
) {
    // Filtering control
    @Volatile private var filteringEnabled: Boolean = false
    private val filters: Array<SignalFilter> = Array(8) { SignalFilter(100) }
}
```

**NEW:**
```kotlin
class Cache @Inject constructor(
    private val context: Context,
    private val device: Device,
    private val edgeComputingProcessor: EdgeComputingProcessor
) {
    // No filtering here - delegated to EdgeComputingProcessor
}
```

---

### 2. Filtering Control Methods

**OLD:**
```kotlin
fun setFilteringEnabled(enabled: Boolean) {
    filteringEnabled = enabled
    if (!enabled) resetFilters()
}

fun isFilteringEnabled(): Boolean = filteringEnabled

private fun resetFilters() { 
    filters.forEach { it.reset() } 
}
```

**NEW:**
```kotlin
fun setFilteringEnabled(enabled: Boolean) {
    edgeComputingProcessor.setEnabled(enabled)  // Delegates to EdgeComputingProcessor
}

fun isFilteringEnabled(): Boolean = edgeComputingProcessor.isEnabled()  // Delegates
```

---

### 3. put() Method - CRITICAL CHANGE

**OLD (Had Filtering):**
```kotlin
fun put(stamp: Int, buffer: ByteArray) {
    if (buffer.size != 24 * 1000)
        throw Exception("Expected 24000 bytes, got ${buffer.size}")
    
    // Decimation (same as before)
    val sampled = ByteArray(2400)
    for (i in 0 until 100)
        buffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)

    // ❌ FILTERING WAS HERE - REMOVED
    if (filteringEnabled) {
        // Apply baseline wander high-pass per-lead at 100 Hz on decimated samples
        for (i in 0 until 100) {
            val base = i * 24
            var lead = 0
            while (lead < 8) {
                val off = base + lead * 3
                val raw = read24(sampled, off)
                val filtered = filters[lead].process(raw.toDouble())  // ❌ REMOVED
                write24(sampled, off, filtered.toInt())  // ❌ REMOVED
                lead++
            }
        }
    }
    
    lru.put(stamp, sampled)
}
```

**NEW (No Filtering):**
```kotlin
fun put(stamp: Int, buffer: ByteArray) {
    if (buffer.size != 24 * 1000)
        throw Exception("Expected 24000 bytes, got ${buffer.size}")
    
    // Decimation (same as before - UNCHANGED)
    val sampled = ByteArray(2400)
    for (i in 0 until 100)
        buffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)

    // ✅ NO FILTERING - Just store decimated data
    lru.put(stamp, sampled)
    
    // Note: RAW data upload happens in DataHandler.store() - no filtering applied
}
```

---

### 4. What Stayed the Same

✅ **Unchanged:**
- `get()` method - Same logic
- `helper()` method - Same logic
- `load()` method - Same logic
- `read24()` method - Same logic
- `write24()` method - Removed (no longer needed in Cache)
- Decimation algorithm - **EXACTLY THE SAME**
- LRU cache - Same size and behavior

---

## Data Flow Impact

### OLD Flow:
```
RAW Data → Cache.put()
    ↓
Decimate (1000 → 100 Hz)
    ↓
[IF filteringEnabled] Apply Filter
    ↓
Store in LRU (filtered or unfiltered depending on flag)
    ↓
Upload RAW (but cache had filtered data)
```

**Problem**: Filtering in Cache affected the stored data, which could cause confusion.

---

### NEW Flow:
```
RAW Data → DataHandler
    ├─→ EdgeComputingProcessor.processRawData()
    │       ↓
    │   Decimate → Filter → HR/SNR/Sat → LiveData
    │
    └─→ Cache.put()
            ↓
        Decimate → Store (NO FILTERING)
            ↓
        Upload RAW (unfiltered)
```

**Solution**: 
- ✅ Filtering completely separated to EdgeComputingProcessor
- ✅ Cache only stores decimated data (no filtering)
- ✅ Cloud always gets RAW unfiltered data
- ✅ Clear separation of concerns

---

## Benefits of New Architecture

1. **Clear Separation**: Filtering is part of edge computing, not cache
2. **No Side Effects**: Cache doesn't modify data with filtering
3. **RAW Data Preserved**: Cloud always gets unfiltered RAW data
4. **Better Organization**: All edge computing logic in one place
5. **Independent Paths**: Edge computing and cache/upload are completely separate

---

## Migration Notes

- `setFilteringEnabled()` and `isFilteringEnabled()` still work (backward compatible)
- They now delegate to `EdgeComputingProcessor` instead of local filtering
- UI code doesn't need changes - same API
- Filter button still works the same way

---

## Summary

| Aspect | OLD | NEW |
|--------|-----|-----|
| **Filtering Location** | In Cache.put() | In EdgeComputingProcessor |
| **Filtering Control** | Cache.filteringEnabled | EdgeComputingProcessor.enabled |
| **Cache Data** | Could be filtered | Always unfiltered (decimated only) |
| **Cloud Upload** | RAW data | RAW data (unchanged) |
| **Decimation** | Same | Same (unchanged) |
| **Filter Types** | SignalFilter only | Biquad/FIR/Butterworth (in EdgeComputingProcessor) |

**Key Point**: The decimation algorithm is **EXACTLY THE SAME**. Only the filtering logic was moved out of Cache.

