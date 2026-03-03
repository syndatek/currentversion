package com.carditek.kesar.util.filters.edgecomputing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Finite Impulse Response (FIR) Filter
 * Provides linear phase response and guaranteed stability
 * Better for ECG processing due to no phase distortion
 * 
 * Part of Edge Computing processing pipeline
 */
class FIRFilter(
    private val sampleRateHz: Int,
    private val lowCutoffHz: Double? = null,  // Low-pass cutoff (null = no low-pass)
    private val highCutoffHz: Double? = null, // High-pass cutoff (null = no high-pass)
    private val order: Int = 64  // Filter order (higher = sharper, more computation)
) {
    private val coefficients: DoubleArray
    private val delayLine: DoubleArray
    private var writeIndex = 0

    init {
        coefficients = designFilter()
        delayLine = DoubleArray(coefficients.size)
    }

    /**
     * Design FIR filter coefficients using windowed sinc method
     */
    private fun designFilter(): DoubleArray {
        val n = order
        val h = DoubleArray(n + 1)
        val fc1 = highCutoffHz?.let { it / sampleRateHz } ?: 0.0
        val fc2 = lowCutoffHz?.let { it / sampleRateHz } ?: 0.5

        // Design bandpass filter (high-pass + low-pass)
        if (fc1 > 0.0 && fc2 < 0.5) {
            // Bandpass: low-pass minus high-pass
            for (i in 0..n) {
                val m = i - n / 2.0
                if (m == 0.0) {
                    h[i] = 2.0 * (fc2 - fc1)
                } else {
                    h[i] = (sin(2.0 * PI * fc2 * m) - sin(2.0 * PI * fc1 * m)) / (PI * m)
                }
                // Apply Hamming window for better frequency response
                h[i] *= (0.54 - 0.46 * cos(2.0 * PI * i / n))
            }
        } else if (fc1 > 0.0) {
            // High-pass only
            for (i in 0..n) {
                val m = i - n / 2.0
                if (m == 0.0) {
                    h[i] = 1.0 - 2.0 * fc1
                } else {
                    h[i] = -sin(2.0 * PI * fc1 * m) / (PI * m)
                }
                h[i] *= (0.54 - 0.46 * cos(2.0 * PI * i / n))
            }
        } else if (fc2 < 0.5) {
            // Low-pass only
            for (i in 0..n) {
                val m = i - n / 2.0
                if (m == 0.0) {
                    h[i] = 2.0 * fc2
                } else {
                    h[i] = sin(2.0 * PI * fc2 * m) / (PI * m)
                }
                h[i] *= (0.54 - 0.46 * cos(2.0 * PI * i / n))
            }
        } else {
            // No filter (all-pass)
            h.fill(if (n == 0) 1.0 else 0.0)
            if (n > 0) h[n / 2] = 1.0
        }

        // Normalize coefficients
        val sum = h.sum()
        if (sum != 0.0) {
            for (i in h.indices) {
                h[i] /= sum
            }
        }

        return h
    }

    fun reset() {
        delayLine.fill(0.0)
        writeIndex = 0
    }

    /**
     * Process a single sample through the FIR filter
     */
    fun processSample(x: Double): Double {
        // Add new sample to delay line
        delayLine[writeIndex] = x

        // Convolve with filter coefficients
        var y = 0.0
        for (i in coefficients.indices) {
            val idx = (writeIndex - i + delayLine.size) % delayLine.size
            y += coefficients[i] * delayLine[idx]
        }

        // Update write index (circular buffer)
        writeIndex = (writeIndex + 1) % delayLine.size

        return y
    }

    /**
     * Process an array of samples
     */
    fun processArray(data: DoubleArray): DoubleArray {
        return DoubleArray(data.size) { i -> processSample(data[i]) }
    }
}

