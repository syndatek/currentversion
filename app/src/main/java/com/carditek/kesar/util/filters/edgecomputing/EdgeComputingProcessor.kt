package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Edge Computing Processor
 * 
 * Processes RAW ECG data (24000 bytes @ 1000 Hz) through:
 * 1. Decimation (1000 → 100 Hz)
 * 2. Baseline Filter (0.67-48 Hz) - Filter is integral part of edge computing
 * 3. Calculate HR (uses filtered data)
 * 4. Calculate SNR (uses filtered data)
 * 5. Detect Saturation (uses filtered data)
 * 
 * Outputs LiveData for UI display
 * 
 * Note: Filter is NOT separate - it's part of the edge computing processing pipeline.
 * When edge computing is enabled, filtering is automatically applied.
 */
class EdgeComputingProcessor {
    
    @Volatile
    private var enabled: Boolean = false
    
    // Processing components
    private val ecgFilter = ECGFilter()
    private val heartRateCalculator = HeartRateCalculator()
    private val snrCalculator = SNRCalculator()
    private val saturationDetector = SaturationDetector()
    
    // Expose LiveData from components
    val heartRateLive: LiveData<Int> = heartRateCalculator.heartRateLive
    val snrValuesLive: LiveData<Pair<Double?, Double?>> = snrCalculator.snrValuesPairLive
    val lowSNRLeadsLive: LiveData<List<Pair<Int, Double>>> = snrCalculator.lowSNRLeadsLive
    val sensorCheckWarningLive: LiveData<Boolean> = snrCalculator.sensorCheckWarningLive
    val saturatedLeadsLive: LiveData<List<Int>> = saturationDetector.saturatedLeadsLive
    
    /**
     * Enable or disable edge computing processing
     * When enabled, filter is always applied as part of the processing pipeline
     */
    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        // Filter is always enabled when edge computing is enabled (it's part of the pipeline)
        ecgFilter.setFilteringEnabled(enabled)
        if (!enabled) {
            reset()
        }
        Log.d("EdgeComputingProcessor", "Edge computing ${if (enabled) "enabled" else "disabled"} (filter is part of edge computing)")
    }
    
    /**
     * Check if edge computing is enabled
     */
    fun isEnabled(): Boolean = enabled
    
    /**
     * Process RAW ECG data (24000 bytes @ 1000 Hz)
     * 
     * @param rawBuffer RAW data buffer (24000 bytes: 1000 samples × 8 channels × 3 bytes)
     */
    fun processRawData(rawBuffer: ByteArray) {
        if (!enabled) {
            return
        }
        
        if (rawBuffer.size != 24 * 1000) {
            Log.e("EdgeComputingProcessor", "Invalid buffer size: ${rawBuffer.size}, expected ${24 * 1000}")
            return
        }
        
        // Step 1: Decimate (1000 → 100 Hz)
        val sampled = ByteArray(2400) // 100 samples × 8 channels × 3 bytes
        for (i in 0 until 100) {
            rawBuffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
        }
        
        // Step 2: Apply Baseline Filter (0.67-48 Hz) - Filter is part of edge computing processing
        ecgFilter.applyFiltering(sampled, ::read24, ::write24)
        
        // Step 3: Calculate HR (Lead II) - uses filtered data
        heartRateCalculator.updateHeartRateWindow(sampled, ::read24)
        heartRateCalculator.calculateHeartRateFromWindow()
        
        // Step 4: Calculate SNR for selected leads - uses filtered data
        snrCalculator.calculateSNRForLeads(sampled, ::read24)
        
        // Step 5: Detect Saturation for selected leads - uses filtered data
        saturationDetector.detectSaturation(sampled, ::read24)
    }
    
    /**
     * Set selected leads for SNR and Saturation calculation
     */
    fun setSelectedLeads(leads: Set<Int>) {
        snrCalculator.setSelectedLeads(leads)
        saturationDetector.setSelectedLeads(leads)
    }
    
    /**
     * Get currently selected leads
     */
    fun getSelectedLeads(): Set<Int> {
        return snrCalculator.getSelectedLeads()
    }
    
    /**
     * Reset all processing components
     */
    fun reset() {
        heartRateCalculator.reset()
        snrCalculator.reset()
        saturationDetector.reset()
    }
    
    /**
     * Helper function to read 24-bit signed integer from byte array
     */
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
    
    /**
     * Helper function to write 24-bit signed integer to byte array
     */
    private fun write24(bytes: ByteArray, offset: Int, v: Int) {
        var value = v.coerceIn(-0x800000, 0x7FFFFF)
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
    }
}

