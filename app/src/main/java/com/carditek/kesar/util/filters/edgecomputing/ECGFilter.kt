package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log

/**
 * ECG Filter Manager
 * Handles filtering of ECG signals for all 8 leads
 * Supports multiple filter types: Biquad, FIR, and Butterworth
 * 
 * Part of Edge Computing processing pipeline
 */
class ECGFilter {
    
    @Volatile
    private var filteringEnabled: Boolean = false
    
    @Volatile
    private var filterType: FilterType = FilterType.BIQUAD
    
    // Filter instances for each lead (8 leads)
    private val biquadFilters: Array<SignalFilter> = Array(8) { SignalFilter(100) }
    private val firFilters: Array<FIRFilter> = Array(8) { 
        FIRFilter(
            sampleRateHz = 100,
            highCutoffHz = 0.67,  // High-pass: 0.67 Hz (baseline wander removal)
            lowCutoffHz = 48.0,   // Low-pass: 48 Hz (noise removal)
            order = 64             // FIR order (higher = sharper, more computation)
        )
    }
    private val butterworthFilters: Array<ButterworthFilter> = Array(8) {
        ButterworthFilter(
            sampleRateHz = 100,
            highCutoffHz = 0.67,  // High-pass: 0.67 Hz
            lowCutoffHz = 48.0,   // Low-pass: 48 Hz
            order = 6              // 6th order Butterworth (good balance)
        )
    }
    
    /**
     * Enable or disable filtering
     */
    fun setFilteringEnabled(enabled: Boolean) {
        filteringEnabled = enabled
        if (!enabled) {
            resetFilters()
        }
    }
    
    /**
     * Check if filtering is enabled
     */
    fun isFilteringEnabled(): Boolean = filteringEnabled
    
    /**
     * Set filter type
     * @param type FilterType (BIQUAD, FIR, or BUTTERWORTH)
     */
    fun setFilterType(type: FilterType) {
        if (filterType != type) {
            filterType = type
            resetFilters()
            Log.d("ECGFilter", "Filter type changed to: $type")
        }
    }
    
    /**
     * Get current filter type
     */
    fun getFilterType(): FilterType = filterType
    
    /**
     * Reset all filters (clear state)
     */
    private fun resetFilters() {
        when (filterType) {
            FilterType.BIQUAD -> biquadFilters.forEach { it.reset() }
            FilterType.FIR -> firFilters.forEach { it.reset() }
            FilterType.BUTTERWORTH -> butterworthFilters.forEach { it.reset() }
        }
    }
    
    /**
     * Apply filtering to ECG data if enabled
     * 
     * @param sampled Decimated ECG data (2400 bytes: 100 samples × 8 leads × 3 bytes)
     * @param read24 Function to read 24-bit signed integer from byte array
     * @param write24 Function to write 24-bit signed integer to byte array
     */
    fun applyFiltering(
        sampled: ByteArray,
        read24: (ByteArray, Int) -> Int,
        write24: (ByteArray, Int, Int) -> Unit
    ) {
        if (!filteringEnabled) {
            return
        }
        
        // Apply filtering to all 8 leads based on selected filter type
        for (i in 0 until 100) {
            val base = i * 24
            var lead = 0
            while (lead < 8) {
                val off = base + lead * 3
                val raw = read24(sampled, off)
                val filtered = when (filterType) {
                    FilterType.BIQUAD -> biquadFilters[lead].process(raw.toDouble())
                    FilterType.FIR -> firFilters[lead].processSample(raw.toDouble())
                    FilterType.BUTTERWORTH -> butterworthFilters[lead].processSample(raw.toDouble())
                }
                write24(sampled, off, filtered.toInt())
                lead++
            }
        }
        
        Log.d("ECGFilter", "Filtering applied to all 8 leads using $filterType filter")
    }
}

