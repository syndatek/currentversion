//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//
//class EdgeComputingProcessor {
//
//    @Volatile
//    private var enabled: Boolean = false
//
//    // Components
//    private val ecgFilter = ECGFilter()
//    private val heartRateCalculator = HeartRateCalculator()
//    private val snrCalculator = SNRCalculator()
//    private val saturationDetector = SaturationDetector()
//
//    // ✅ Clean LiveData (UPDATED)
//    val heartRateLive: LiveData<Int> = heartRateCalculator.heartRateLive
//    val snrValuesLive: LiveData<Pair<Double?, Double?>> = snrCalculator.snrValuesLive
//    val lowSNRWarningLive: LiveData<Boolean> = snrCalculator.lowSNRWarningLive
//    val saturatedLeadsLive: LiveData<List<Int>> = saturationDetector.saturatedLeadsLive
//
//    fun setEnabled(enabled: Boolean) {
//        this.enabled = enabled
//        ecgFilter.setFilteringEnabled(enabled)
//
//        if (!enabled) reset()
//
//        Log.d("EdgeComputingProcessor", "Edge computing ${if (enabled) "enabled" else "disabled"}")
//    }
//
//    fun isEnabled(): Boolean = enabled
//
//    fun processRawData(rawBuffer: ByteArray) {
//        if (!enabled) return
//
//        if (rawBuffer.size != 24 * 1000) {
//            Log.e("EdgeComputingProcessor", "Invalid buffer size: ${rawBuffer.size}")
//            return
//        }
//
//        // --- Step 1: Decimation (1000 → 100 Hz) ---
//        val sampled = ByteArray(2400)
//        for (i in 0 until 100) {
//            rawBuffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
//        }
//
//        // --- Step 2: Filtering ---
//        ecgFilter.applyFiltering(sampled, ::read24, ::write24)
//
//        // --- Step 3: Heart Rate ---
//        heartRateCalculator.updateHeartRateWindow(sampled, ::read24)
//        heartRateCalculator.calculateHeartRateFromWindow()
//
//        // --- Step 4: SNR (Lead 1 & 2 only) ---
//        snrCalculator.calculateSNR(sampled, ::read24)
//
//        // --- Step 5: Saturation (Lead 1 & 2 only) ---
//        saturationDetector.detectSaturation(sampled, ::read24)
//    }
//
//    fun reset() {
//        heartRateCalculator.reset()
//        snrCalculator.reset()
//        saturationDetector.reset()
//    }
//
//    // --- Read 24-bit ---
//    private fun read24(bytes: ByteArray, offset: Int): Int {
//        val b0 = bytes[offset].toInt() and 0xFF
//        val b1 = bytes[offset + 1].toInt() and 0xFF
//        val b2 = bytes[offset + 2].toInt() and 0xFF
//
//        var value = b0 or (b1 shl 8) or (b2 shl 16)
//
//        if (value and 0x800000 != 0) {
//            value = value or -0x1000000
//        }
//        return value
//    }
//
//    // --- Write 24-bit ---
//    private fun write24(bytes: ByteArray, offset: Int, v: Int) {
//        val value = v.coerceIn(-0x800000, 0x7FFFFF)
//
//        bytes[offset] = (value and 0xFF).toByte()
//        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
//        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
//    }
//}
package com.carditek.kesar.util.filters.edgecomputing
import android.util.Log

class EdgeComputingProcessor {

    private var enabled = false

    private val ecgFilter = ECGFilter()
    private val heartRateCalculator = HeartRateCalculator()
    private val snrCalculator = SNRCalculator()
    private val saturationDetector = SaturationDetector()

    val heartRateLive = heartRateCalculator.heartRateLive
    val snrValuesLive = snrCalculator.snrValuesLive
    val lowSNRWarningLive = snrCalculator.lowSNRWarningLive
    val saturatedLeadsLive = saturationDetector.saturatedLeadsLive

    fun setEnabled(enabled: Boolean) {
        val changed = this.enabled != enabled
        this.enabled = enabled
        ecgFilter.setFilteringEnabled(enabled)
        if (!enabled && changed) {
            // Prevent stale HR/SNR/saturation values when edge computing is turned off.
            reset()
        }
    }

    fun isEnabled() = enabled

    fun processRawData(raw: ByteArray) {
        if (!enabled) return
        if (raw.size != 24 * 1000) {
            Log.w("EdgeComputingProcessor", "Skipping invalid raw packet size: ${raw.size}")
            return
        }

        val sampled = ByteArray(2400)

        for (i in 0 until 100)
            raw.copyInto(sampled, i * 24, i * 240, i * 240 + 24)

        ecgFilter.applyFiltering(sampled, ::read24, ::write24)

        heartRateCalculator.updateHeartRateWindow(sampled, ::read24)
        heartRateCalculator.calculateHeartRateFromWindow()

        snrCalculator.calculateSNR(sampled, ::read24)
        saturationDetector.detectSaturation(sampled, ::read24)
    }

    fun reset() {
        heartRateCalculator.reset()
        snrCalculator.reset()
        saturationDetector.reset()
    }

    private fun read24(b: ByteArray, o: Int): Int {
        var v = (b[o].toInt() and 0xFF) or
                ((b[o + 1].toInt() and 0xFF) shl 8) or
                ((b[o + 2].toInt() and 0xFF) shl 16)
        if (v and 0x800000 != 0) v = v or -0x1000000
        return v
    }

    private fun write24(b: ByteArray, o: Int, v: Int) {
        val value = v.coerceIn(-0x800000, 0x7FFFFF)
        b[o] = (value and 0xFF).toByte()
        b[o + 1] = ((value shr 8) and 0xFF).toByte()
        b[o + 2] = ((value shr 16) and 0xFF).toByte()
    }
}
