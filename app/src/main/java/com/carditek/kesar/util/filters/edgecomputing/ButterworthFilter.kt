package com.carditek.kesar.util.filters.edgecomputing

import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan

/**
 * Butterworth Filter (IIR)
 * Provides maximally flat frequency response in passband
 * Higher order = sharper rolloff, better noise rejection
 * 
 * For ECG: Typically use 4th-8th order for optimal balance
 * Part of Edge Computing processing pipeline
 */
class ButterworthFilter(
    private val sampleRateHz: Int,
    private val lowCutoffHz: Double? = null,  // Low-pass cutoff
    private val highCutoffHz: Double? = null,  // High-pass cutoff
    private val order: Int = 4  // Filter order (2, 4, 6, 8 recommended)
) {
    private val highPassSections: List<BiquadSection>
    private val lowPassSections: List<BiquadSection>

    init {
        highPassSections = if (highCutoffHz != null) designHighPass(highCutoffHz!!) else emptyList()
        lowPassSections = if (lowCutoffHz != null) designLowPass(lowCutoffHz!!) else emptyList()
    }

    /**
     * Biquad section for cascaded implementation
     */
    private data class BiquadSection(
        val b0: Double, val b1: Double, val b2: Double,
        val a1: Double, val a2: Double,
        var x1: Double = 0.0, var x2: Double = 0.0,
        var y1: Double = 0.0, var y2: Double = 0.0
    ) {
        fun process(x: Double): Double {
            val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1
            x1 = x
            y2 = y1
            y1 = y
            return y
        }

        fun reset() {
            x1 = 0.0
            x2 = 0.0
            y1 = 0.0
            y2 = 0.0
        }
    }

    /**
     * Design Butterworth high-pass filter using bilinear transform
     */
    private fun designHighPass(fc: Double): List<BiquadSection> {
        val sections = mutableListOf<BiquadSection>()
        val sectionsPerOrder = order / 2
        
        // Pre-warp frequency
        val wc = 2.0 * sampleRateHz * tan(PI * fc / sampleRateHz)
        val k = wc / sampleRateHz

        for (i in 0 until sectionsPerOrder) {
            val angle = PI * (2.0 * i + 1.0) / (2.0 * order)
            val alpha = sin(angle)
            
            val a0 = 1.0 + 2.0 * alpha * k + k * k
            val a1 = 2.0 * (k * k - 1.0) / a0
            val a2 = (1.0 - 2.0 * alpha * k + k * k) / a0
            val b0 = k * k / a0
            val b1 = -2.0 * k * k / a0
            val b2 = k * k / a0

            sections.add(BiquadSection(b0, b1, b2, a1, a2))
        }

        return sections
    }

    /**
     * Design Butterworth low-pass filter using bilinear transform
     */
    private fun designLowPass(fc: Double): List<BiquadSection> {
        val sections = mutableListOf<BiquadSection>()
        val sectionsPerOrder = order / 2
        
        // Pre-warp frequency
        val wc = 2.0 * sampleRateHz * tan(PI * fc / sampleRateHz)
        val k = wc / sampleRateHz

        for (i in 0 until sectionsPerOrder) {
            val angle = PI * (2.0 * i + 1.0) / (2.0 * order)
            val alpha = sin(angle)
            
            val a0 = 1.0 + 2.0 * alpha * k + k * k
            val a1 = 2.0 * (k * k - 1.0) / a0
            val a2 = (1.0 - 2.0 * alpha * k + k * k) / a0
            val b0 = k * k / a0
            val b1 = 2.0 * k * k / a0
            val b2 = k * k / a0

            sections.add(BiquadSection(b0, b1, b2, a1, a2))
        }

        return sections
    }

    fun reset() {
        highPassSections.forEach { it.reset() }
        lowPassSections.forEach { it.reset() }
    }

    /**
     * Process a single sample through cascaded biquad sections
     * First applies high-pass, then low-pass
     */
    fun processSample(x: Double): Double {
        var y = x
        
        // Apply high-pass sections
        for (section in highPassSections) {
            y = section.process(y)
        }
        
        // Apply low-pass sections
        for (section in lowPassSections) {
            y = section.process(y)
        }
        
        return y
    }

    /**
     * Process an array of samples
     */
    fun processArray(data: DoubleArray): DoubleArray {
        return DoubleArray(data.size) { i -> processSample(data[i]) }
    }
}

