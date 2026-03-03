package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Saturation Detector
 * Detects when ECG signals are clipped (saturated) at ADC limits
 * Checks selected leads for saturation
 */
class SaturationDetector {
    
    // --- Selected leads for saturation detection (0-7, where 0=Lead1, 1=Lead2, 2-7=V1-V6) ---
    @Volatile
    private var selectedLeads: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6, 7) // Default: All leads
    
    // --- Live Saturated Leads (Observable) ---
    // List of lead numbers (1-8) that are currently saturated
    private val _saturatedLeadsLive = MutableLiveData<List<Int>>()
    val saturatedLeadsLive: LiveData<List<Int>> get() = _saturatedLeadsLive
    
    /**
     * Set which leads to check for saturation
     * @param leads Set of lead indices (0-7, where 0=Lead1, 1=Lead2, 2-7=V1-V6)
     */
    fun setSelectedLeads(leads: Set<Int>) {
        selectedLeads = leads.filter { it in 0..7 }.toSet()
        Log.d("SaturationDetector", "Selected leads for saturation: ${selectedLeads.map { it + 1 }.sorted()}")
    }
    
    /**
     * Get currently selected leads
     */
    fun getSelectedLeads(): Set<Int> = selectedLeads
    
    /**
     * Detects saturation (clipping) for selected ECG leads.
     * Saturation occurs when the signal reaches the ADC limits, indicating the signal is clipped.
     * 
     * For 24-bit signed integers:
     * - Maximum value: 8,388,607 (0x7FFFFF)
     * - Minimum value: -8,388,608 (0x800000)
     * 
     * A lead is considered saturated if any sample is at or near these limits.
     * 
     * @param sampled Decimated ECG data (2400 bytes: 100 samples × 8 channels × 3 bytes)
     * @param read24 Function to read 24-bit signed integer from byte array
     */
    fun detectSaturation(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
        if (selectedLeads.isEmpty()) {
            Log.w("SaturationDetector", "No leads selected for saturation detection")
            _saturatedLeadsLive.postValue(emptyList())
            return
        }
        
        val numSamples = 100 // 1 second of data at 100 Hz
        
        // 24-bit signed integer limits
        val MAX_VALUE = 8388607   // 0x7FFFFF (maximum positive)
        val MIN_VALUE = -8388608   // 0x800000 (minimum negative)
        
        // Threshold for saturation detection (within 0.1% of limits)
        val SATURATION_THRESHOLD_POSITIVE = (MAX_VALUE * 0.999).toInt()
        val SATURATION_THRESHOLD_NEGATIVE = (MIN_VALUE * 0.999).toInt()
        
        // Track which leads have saturation
        val saturatedLeads = mutableSetOf<Int>()
        
        // Check each selected lead for saturation
        selectedLeads.forEach { lead ->
            var leadSaturated = false
            
            // Check all samples in this lead
            for (sample in 0 until numSamples) {
                val offset = sample * 24 + lead * 3
                val value = read24(sampled, offset)
                
                // Check if value is at or near saturation limits
                if (value >= SATURATION_THRESHOLD_POSITIVE || value <= SATURATION_THRESHOLD_NEGATIVE) {
                    leadSaturated = true
                    break // Lead is saturated, no need to check more samples
                }
            }
            
            if (leadSaturated) {
                // Lead numbers are 1-indexed (Lead 1 = channel 0, Lead 2 = channel 1, etc.)
                saturatedLeads.add(lead + 1)
            }
        }
        
        // Update LiveData with saturated leads
        val saturatedLeadsList = saturatedLeads.sorted()
        _saturatedLeadsLive.postValue(saturatedLeadsList)
        
        // Log saturation status
        if (saturatedLeadsList.isNotEmpty()) {
            Log.w("SaturationDetector", "ECG Saturation detected in leads: ${saturatedLeadsList.joinToString(", ")}")
        } else {
            Log.d("SaturationDetector", "ECG Saturation check: No saturation detected in selected leads")
        }
    }
    
    /**
     * Reset saturation detector state
     */
    fun reset() {
        _saturatedLeadsLive.postValue(emptyList())
    }
}

