package com.carditek.kesar.util.filters.edgecomputing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class BiquadType { LOWPASS, HIGHPASS, NOTCH }

/** Simple biquad IIR filter for real-time streaming (per-sample). 
 * Part of Edge Computing processing pipeline */
class Biquad(
    private val type: BiquadType,
    private val sampleRateHz: Int,
    private val frequencyHz: Double,
    private val q: Double = 0.7071067811865476
) {
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a0 = 1.0
    private var a1 = 0.0
    private var a2 = 0.0

    // State
    private var z1 = 0.0
    private var z2 = 0.0

    init { design() }

    private fun design() {
        val w0 = 2.0 * PI * frequencyHz / sampleRateHz
        val alpha = sin(w0) / (2.0 * q)
        val cosw = cos(w0)

        when (type) {
            BiquadType.LOWPASS -> {
                b0 = (1 - cosw) / 2
                b1 = 1 - cosw
                b2 = (1 - cosw) / 2
                a0 = 1 + alpha
                a1 = -2 * cosw
                a2 = 1 - alpha
            }
            BiquadType.HIGHPASS -> {
                b0 = (1 + cosw) / 2
                b1 = -(1 + cosw)
                b2 = (1 + cosw) / 2
                a0 = 1 + alpha
                a1 = -2 * cosw
                a2 = 1 - alpha
            }
            BiquadType.NOTCH -> {
                b0 = 1.0
                b1 = -2 * cosw
                b2 = 1.0
                a0 = 1 + alpha
                a1 = -2 * cosw
                a2 = 1 - alpha
            }
        }

        // Normalize coefficients
        b0 /= a0
        b1 /= a0
        b2 /= a0
        a1 /= a0
        a2 /= a0
        a0 = 1.0
    }

    fun reset() {
        z1 = 0.0
        z2 = 0.0
    }

    /** Process a single ECG sample */
    fun processSample(x: Double): Double {
        val y = b0 * x + z1
        z1 = b1 * x - a1 * y + z2
        z2 = b2 * x - a2 * y
        return y
    }
}

