package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.log10

/**
 * SNR (Signal-to-Noise Ratio) Calculator
 * Calculates SNR for selected leads using 5-second rolling window
 * Threshold: 0.0 dB (positive threshold - higher is better)
 */
class SNRCalculator {
    
    // --- Selected leads for SNR calculation (0-7, where 0=Lead1, 1=Lead2, 2-7=V1-V6) ---
    @Volatile
    private var selectedLeads: Set<Int> = setOf(0, 1) // Default: Lead 1 and Lead 2
    
    // --- Live SNR Values for Display (Observable) ---
    // Map: Lead index (0-7) -> SNR value in dB
    private val _snrValuesLive = MutableLiveData<Map<Int, Double?>>()
    val snrValuesLive: LiveData<Map<Int, Double?>> get() = _snrValuesLive
    
    // --- Legacy: Pair format for backward compatibility (Lead 1, Lead 2) ---
    private val _snrValuesPairLive = MutableLiveData<Pair<Double?, Double?>>()
    val snrValuesPairLive: LiveData<Pair<Double?, Double?>> get() = _snrValuesPairLive
    
    // --- Live Low SNR Leads (Observable) ---
    // Pair: (Lead number (1-8), SNR value in dB)
    private val _lowSNRLeadsLive = MutableLiveData<List<Pair<Int, Double>>>()
    val lowSNRLeadsLive: LiveData<List<Pair<Int, Double>>> get() = _lowSNRLeadsLive
    
    // --- Live Sensor Check Warning (Observable) ---
    private val _sensorCheckWarningLive = MutableLiveData<Boolean>()
    val sensorCheckWarningLive: LiveData<Boolean> get() = _sensorCheckWarningLive
    
    // --- SNR rolling window state (average over 5 seconds) ---
    private val snrWindowSeconds = 5
    private val snrSamplesPerSecond = 100
    private val snrWindowSize = snrWindowSeconds * snrSamplesPerSecond // 500 samples
    
    // Windows for all 8 leads (index 0-7)
    private val leadWindows = Array(8) { DoubleArray(snrWindowSize) }
    private var snrWriteIndex = 0          // Next position to write into the window
    private var snrValidSamples = 0        // How many valid samples are currently in the window
    
    // SNR threshold: 0.0 dB (positive - higher is better)
    private val snrThresholdDb = 0.0
    
    /**
     * Set which leads to calculate SNR for
     * @param leads Set of lead indices (0-7, where 0=Lead1, 1=Lead2, 2-7=V1-V6)
     */
    fun setSelectedLeads(leads: Set<Int>) {
        selectedLeads = leads.filter { it in 0..7 }.toSet()
        // Reset windows for deselected leads
        (0..7).forEach { lead ->
            if (lead !in selectedLeads) {
                leadWindows[lead].fill(0.0)
            }
        }
        snrValidSamples = 0
        snrWriteIndex = 0
        Log.d("SNRCalculator", "Selected leads for SNR: ${selectedLeads.map { it + 1 }.sorted()}")
    }
    
    /**
     * Get currently selected leads
     */
    fun getSelectedLeads(): Set<Int> = selectedLeads
    
    /**
     * Calculates SNR (Signal-to-Noise Ratio) for selected leads.
     * SNR is calculated as: 10 * log10(signal_power / noise_power)
     *
     * Signal power: variance of the signal
     * Noise power: variance of the first difference (high-frequency component)
     *
     * @param sampled Decimated ECG data (2400 bytes: 100 samples × 8 channels × 3 bytes)
     * @param read24 Function to read 24-bit signed integer from byte array
     */
    fun calculateSNRForLeads(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
        if (selectedLeads.isEmpty()) {
            Log.w("SNRCalculator", "No leads selected for SNR calculation")
            return
        }
        
        val numSamples = 100 // 1 second of data at 100 Hz
        
        // Extract current 1-second data for selected leads
        val currentLeadData = Array(8) { lead ->
            if (lead in selectedLeads) {
                DoubleArray(numSamples) { sample ->
                    val offset = sample * 24 + lead * 3
                    read24(sampled, offset).toDouble()
                }
            } else {
                null
            }
        }
        
        // Update rolling window (5 seconds = 500 samples) for selected leads only
        for (i in 0 until numSamples) {
            selectedLeads.forEach { lead ->
                currentLeadData[lead]?.let { data ->
                    leadWindows[lead][snrWriteIndex] = data[i]
                }
            }
            
            snrWriteIndex = (snrWriteIndex + 1) % snrWindowSize
            if (snrValidSamples < snrWindowSize) {
                snrValidSamples++
            }
        }
        
        // Need at least 2 samples to compute noise (difference)
        if (snrValidSamples < 2) {
            val emptyMap = selectedLeads.associateWith { Double.NEGATIVE_INFINITY }
            _snrValuesLive.postValue(emptyMap)
            _snrValuesPairLive.postValue(Pair(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY))
            _lowSNRLeadsLive.postValue(selectedLeads.map { (it + 1) to Double.NEGATIVE_INFINITY })
            _sensorCheckWarningLive.postValue(true)
            Log.w("SNRCalculator", "SNR window not yet full - treating all selected leads as below threshold")
            return
        }
        
        // Build ordered arrays and compute SNR for each selected lead
        val windowLength = snrValidSamples
        val snrResults = mutableMapOf<Int, Double>()
        val leadSNRs = mutableListOf<Pair<Int, Double>>()
        
        selectedLeads.forEach { lead ->
            val leadData = DoubleArray(windowLength) { i ->
                val idx = (snrWriteIndex - windowLength + i + snrWindowSize) % snrWindowSize
                leadWindows[lead][idx]
            }
            
            val snr = computeSNR(leadData, lead + 1)
            snrResults[lead] = snr
            leadSNRs.add((lead + 1) to snr)
        }
        
        // Check for low SNR leads
        var hasLowSNR = false
        val lowSNRLeads = leadSNRs.filter { (leadNumber, snrDb) ->
            val isBelowThreshold = when {
                snrDb.isNaN() -> {
                    Log.w("SNRCalculator", "Lead $leadNumber SNR is NaN - treating as below threshold")
                    true
                }
                snrDb == Double.NEGATIVE_INFINITY -> {
                    Log.w("SNRCalculator", "Lead $leadNumber SNR = -∞ dB (very poor signal - below threshold)")
                    true
                }
                snrDb <= snrThresholdDb -> {
                    Log.w("SNRCalculator", "Lead $leadNumber SNR (5s avg) = ${String.format("%.2f", snrDb)} dB (below threshold ${snrThresholdDb} dB)")
                    true
                }
                else -> {
                    Log.d("SNRCalculator", "Lead $leadNumber SNR (5s avg) = ${String.format("%.2f", snrDb)} dB (above threshold)")
                    false
                }
            }
            
            if (isBelowThreshold) {
                hasLowSNR = true
            }
            isBelowThreshold
        }
        
        // Update LiveData
        _snrValuesLive.postValue(snrResults)
        
        // Legacy: Update pair format for backward compatibility
        val lead1SNR = snrResults[0]
        val lead2SNR = snrResults[1]
        _snrValuesPairLive.postValue(Pair(lead1SNR, lead2SNR))
        
        _lowSNRLeadsLive.postValue(lowSNRLeads)
        _sensorCheckWarningLive.postValue(hasLowSNR)
        
        // Log for debugging
        if (hasLowSNR) {
            val affectedLeads = lowSNRLeads.joinToString(", ") {
                "Lead ${it.first} (${String.format("%.2f", it.second)} dB)"
            }
            Log.w("SNRCalculator", "Sensor check needed! Low SNR leads (5s avg): $affectedLeads")
        } else {
            val allSNRs = leadSNRs.joinToString(", ") {
                "Lead ${it.first}: ${String.format("%.2f", it.second)} dB"
            }
            Log.d("SNRCalculator", "All selected leads SNR (5s avg): $allSNRs")
        }
    }
    
    /**
     * Helper to compute SNR from a window of samples
     */
    private fun computeSNR(data: DoubleArray, leadNumber: Int): Double {
        if (data.size < 2) {
            Log.w("SNRCalculator", "Insufficient data (${data.size} samples) - returning -∞")
            return Double.NEGATIVE_INFINITY
        }
        
        val mean = data.average()
        val signalVariance = data.map { (it - mean) * (it - mean) }.average()
        val signalPower = signalVariance
        
        // First difference (noise estimate)
        val noiseData = DoubleArray(data.size - 1) { i -> data[i + 1] - data[i] }
        val noiseMean = noiseData.average()
        val noiseVariance = noiseData.map { (it - noiseMean) * (it - noiseMean) }.average()
        val noisePower = 0.5 * noiseVariance
        
        // Guard against tiny values
        val eps = 1e-12
        if (!signalPower.isFinite() || !noisePower.isFinite() || signalPower <= 0.0 || noisePower <= 0.0) {
            Log.w("SNRCalculator", "Invalid power values (signal=$signalPower, noise=$noisePower) - returning -∞")
            return Double.NEGATIVE_INFINITY
        }
        
        val snrRatio = signalPower / (noisePower + eps)
        val snrDb = 10.0 * log10(snrRatio)
        
        Log.d("SNRCalculator", "Lead $leadNumber: SNR ratio = ${"%.6f".format(snrRatio)}, SNR (dB) = ${"%.2f".format(snrDb)}")
        
        return snrDb
    }
    
    /**
     * Reset SNR calculator state
     */
    fun reset() {
        snrWriteIndex = 0
        snrValidSamples = 0
        leadWindows.forEach { it.fill(0.0) }
        _snrValuesLive.postValue(emptyMap())
        _snrValuesPairLive.postValue(Pair(null, null))
        _lowSNRLeadsLive.postValue(emptyList())
        _sensorCheckWarningLive.postValue(false)
    }
}

