//package com.carditek.kesar
//
//import android.content.Context
//import android.util.Log
//import android.util.LruCache
//import com.carditek.kesar.util.filters.SignalFilter
//import javax.inject.Inject
//
//class Cache @Inject constructor(private val context: Context, private val device: Device) {
//
//    // Filtering control
//    @Volatile private var filteringEnabled: Boolean = false
//    private val filters: Array<SignalFilter> = Array(8) { SignalFilter(100) }
//    fun setFilteringEnabled(enabled: Boolean) {
//        filteringEnabled = enabled
//        if (!enabled) resetFilters()
//    }
//    fun isFilteringEnabled(): Boolean = filteringEnabled
//    private fun resetFilters() { filters.forEach { it.reset() } }
//
//    fun get(address: String, stamp: Int, frequency: Int): ByteArray? {
//        // Ensure that we're behind at least a little.  The current timestamp was fetched
//        // by JavaScript code, and can be assumed not to be ahead of real time .
//        return helper(address, stamp - 2, frequency)
//    }
//
//    private fun helper(address: String, stamp: Int, frequency: Int): ByteArray? {
//        return if (address != device.address.value || frequency != 100) {
//            null
//        } else {
//            val buffer = lru.get(stamp) ?: return null
//
//            if (buffer.size != 2400)
//                throw Exception("Expected 2400 bytes, got ${buffer.size}")
//
//            return if (address != "54:6C:0E:83:3E:49") {
//                buffer
//            } else {
//                val begin = (stamp % 15) * 2400
//                load().sliceArray(begin until begin + 2400)
//            }
//        }
//    }
//
//    fun put(stamp: Int, buffer: ByteArray) {
//        // We expect incoming data to be sampled at 1000 per second.  For the display, we only
//        // want 100.  Do the transformation here.  Use the simplest sort of decimation, make
//        // assumptions on the ratio, don't filter, etc., etc.
//        if (buffer.size != 24 * 1000)
//            throw Exception("Expected 24000 bytes, got ${buffer.size}")
//        val sampled = ByteArray(2400)
//        for (i in 0 until 100)
//            buffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
//
//        if (filteringEnabled) {
//            // Apply baseline wander high-pass per-lead at 100 Hz on decimated samples
//            for (i in 0 until 100) {
//                val base = i * 24
//                var lead = 0
//                while (lead < 8) {
//                    val off = base + lead * 3
//                    val raw = read24(sampled, off)
//                    val filtered = filters[lead].process(raw.toDouble())
//                    write24(sampled, off, filtered.toInt())
//                    lead++
//                }
//            }
//        }
//        lru.put(stamp, sampled)
//    }
//
//    @Synchronized
//    fun load(): ByteArray {
//        if (fake == null) {
//            val bytes = context.resources.openRawResource(R.raw.chunkdata).readBytes()
//            fake = bytes
//        }
//        return fake!!
//    }
//
//    // Local 5 minute cache.  At 2400 bytes per, works out to 0.72MB.
//    private val lru = LruCache<Int, ByteArray>(300)
//
//    companion object {
//        private var fake: ByteArray? = null
//    }
//
//    private fun read24(bytes: ByteArray, offset: Int): Int {
//        val b0 = bytes[offset].toInt() and 0xFF
//        val b1 = bytes[offset + 1].toInt() and 0xFF
//        val b2 = bytes[offset + 2].toInt() and 0xFF
//        var value = b0 or (b1 shl 8) or (b2 shl 16)
//        if (value and 0x800000 != 0) {
//            value = value or -0x1000000
//        }
//        return value
//    }
//
//    private fun write24(bytes: ByteArray, offset: Int, v: Int) {
//        var value = v.coerceIn(-0x800000, 0x7FFFFF)
//        bytes[offset] = (value and 0xFF).toByte()
//        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
//        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
//    }
//}



package com.carditek.kesar

import android.content.Context
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.LiveData
import com.carditek.kesar.util.filters.edgecomputing.EdgeComputingProcessor
import javax.inject.Inject

class Cache @Inject constructor(
    private val context: Context,
    private val device: Device,
    private val edgeComputingProcessor: EdgeComputingProcessor
) {

    // Expose LiveData from EdgeComputingProcessor
    val heartRateLive: LiveData<Int> = edgeComputingProcessor.heartRateLive
    val snrValuesLive: LiveData<Pair<Double?, Double?>> = edgeComputingProcessor.snrValuesLive
    val lowSNRLeadsLive: LiveData<List<Pair<Int, Double>>> = edgeComputingProcessor.lowSNRLeadsLive
    val sensorCheckWarningLive: LiveData<Boolean> = edgeComputingProcessor.sensorCheckWarningLive
    val saturatedLeadsLive: LiveData<List<Int>> = edgeComputingProcessor.saturatedLeadsLive

    fun setFilteringEnabled(enabled: Boolean) {
        edgeComputingProcessor.setEnabled(enabled)
    }
    
    fun isFilteringEnabled(): Boolean = edgeComputingProcessor.isEnabled()
    
    /**
     * Set selected leads for SNR and Saturation calculation
     * @param leads Set of lead indices (0-7, where 0=Lead1, 1=Lead2, 2-7=V1-V6)
     */
    fun setSelectedLeadsForSNRAndSaturation(leads: Set<Int>) {
        edgeComputingProcessor.setSelectedLeads(leads)
        Log.d("Cache", "Set selected leads for SNR and Saturation: ${leads.map { it + 1 }.sorted()}")
    }
    
    /**
     * Get currently selected leads for SNR and Saturation
     */
    fun getSelectedLeadsForSNRAndSaturation(): Set<Int> {
        return edgeComputingProcessor.getSelectedLeads()
    }

    fun get(address: String, stamp: Int, frequency: Int): ByteArray? {
        return helper(address, stamp - 2, frequency)
    }

    private fun helper(address: String, stamp: Int, frequency: Int): ByteArray? {
        return if (address != device.address.value || frequency != 100) {
            null
        } else {
            val buffer = lru.get(stamp) ?: return null

            if (buffer.size != 2400)
                throw Exception("Expected 2400 bytes, got ${buffer.size}")

            return if (address != "54:6C:0E:83:3E:49") {
                buffer
            } else {
                val begin = (stamp % 15) * 2400
                load().sliceArray(begin until begin + 2400)
            }
        }
    }

    /**
     * Path 2: Cache → Upload (Storage)
     * 
     * RAW Data (24000 bytes @ 1000 Hz)
     *   ↓
     * Cache
     *   ├─→ Decimate (1000 → 100 Hz)
     *   ├─→ Store in LRU (for display)
     *   └─→ If recording:
     *       └─→ Uploader → Cloud (RAW data)
     * 
     * Note: No filtering applied here - RAW data is uploaded to cloud
     */
    fun put(stamp: Int, buffer: ByteArray) {
        if (buffer.size != 24 * 1000)
            throw Exception("Expected 24000 bytes, got ${buffer.size}")
        
        // Decimate (1000 → 100 Hz) for display cache only
        val sampled = ByteArray(2400)
        for (i in 0 until 100)
            buffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)

        // Store decimated data in LRU cache (for display)
        lru.put(stamp, sampled)
        
        // Note: RAW data upload happens in DataHandler.store() - no filtering applied
    }

    @Synchronized
    fun load(): ByteArray {
        if (fake == null) {
            val bytes = context.resources.openRawResource(R.raw.chunkdata).readBytes()
            fake = bytes
        }
        return fake!!
    }

    // Local 5 minute cache
    private val lru = LruCache<Int, ByteArray>(300)

    companion object {
        private var fake: ByteArray? = null
    }

    private fun read24(bytes: ByteArray, offset: Int): Int {
        val b0 = bytes[offset].toInt() and 0xFF
        val b1 = bytes[offset + 1].toInt() and 0xFF
        val b2 = bytes[offset + 2].toInt() and 0xFF
        var value = b0 or (b1 shl 8) or (b2 shl 16)
        if (value and 0x800000 != 0) {
            value = value or -0x1000000
        }
        return value
    }

}
