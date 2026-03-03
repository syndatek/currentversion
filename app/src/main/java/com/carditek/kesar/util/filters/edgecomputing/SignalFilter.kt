package com.carditek.kesar.util.filters.edgecomputing

/**
 * Streaming ECG filter for real-time signal processing
 * - High-pass: 0.67 Hz (baseline wander removal)
 * - Low-pass: 48 Hz (noise removal)
 * 
 * Part of Edge Computing processing pipeline
 * Note: R-peak detection is now handled by HeartRateCalculator.kt
 * This class focuses solely on signal filtering.
 */
class SignalFilter(private val sampleRateHz: Int) {

    private val hp = Biquad(BiquadType.HIGHPASS, sampleRateHz, 0.67)
    private val lp = Biquad(BiquadType.LOWPASS, sampleRateHz, 48.0)

    fun reset() {
        hp.reset()
        lp.reset()
    }

    /** Process single sample, return filtered value */
    fun process(sample: Double): Double {
        val highPassed = hp.processSample(sample)
        return lp.processSample(highPassed)
    }

    /** Process an array of samples */
    fun processArray(data: DoubleArray): DoubleArray {
        return DoubleArray(data.size) { i -> process(data[i]) }
    }

    companion object {
        /** Convenience function to filter raw ECG */
        fun applyFilters(rawData: DoubleArray, fs: Int = 1000): DoubleArray {
            val filter = SignalFilter(fs)
            return filter.processArray(rawData)
        }
    }
}

