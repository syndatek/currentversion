package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.log10
import javax.inject.Inject

class SNRCalculator @Inject constructor() {

    // ✅ Hardware Channels (RAW DATA)
    private val CHANNEL_1 = 0
    private val CHANNEL_2 = 1

    // ✅ LiveData Outputs
    private val _snrValuesLive = MutableLiveData<Pair<Double?, Double?>>()
    val snrValuesLive: LiveData<Pair<Double?, Double?>> get() = _snrValuesLive

    private val _lowSNRWarningLive = MutableLiveData<Boolean>()
    val lowSNRWarningLive: LiveData<Boolean> get() = _lowSNRWarningLive

    // ✅ Config
    private val windowSeconds = 5
    private val sampleRate = 100
    private val windowSize = windowSeconds * sampleRate // 500 samples

    // ✅ Buffers
    private val ch1Window = DoubleArray(windowSize)
    private val ch2Window = DoubleArray(windowSize)

    private var writeIndex = 0
    private var validSamples = 0

    // Thresholds for popup gating (hysteresis reduces flicker).
    private val snrLowThresholdDb = 6.0
    private val snrHighThresholdDb = 8.0

    // Signal/noise split: compute a smoothed "signal" using EMA, and treat residual as noise.
    private val smoothingWindowSamples = 20
    private val minValidSamples = 10

    private var lowSNRState = false

    /**
     * Call this for every incoming packet
     */
    fun calculateSNR(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {

        val numSamples = 100

        // ✅ Extract channel data
        val ch1Data = DoubleArray(numSamples) {
            read24(sampled, it * 24 + CHANNEL_1 * 3).toDouble()
        }

        val ch2Data = DoubleArray(numSamples) {
            read24(sampled, it * 24 + CHANNEL_2 * 3).toDouble()
        }

        // ✅ Update rolling window
        for (i in 0 until numSamples) {
            ch1Window[writeIndex] = ch1Data[i]
            ch2Window[writeIndex] = ch2Data[i]

            writeIndex = (writeIndex + 1) % windowSize
            if (validSamples < windowSize) validSamples++
        }

        if (validSamples < minValidSamples) {
            _snrValuesLive.postValue(Pair(null, null))
            lowSNRState = false
            _lowSNRWarningLive.postValue(false)
            return
        }

        val window1 = getWindow(ch1Window)
        val window2 = getWindow(ch2Window)

        val snr1 = computeSNR(window1)
        val snr2 = computeSNR(window2)

        _snrValuesLive.postValue(Pair(snr1, snr2))

        val minSNR = minOf(snr1, snr2)
        lowSNRState = when {
            !lowSNRState && minSNR <= snrLowThresholdDb -> true
            lowSNRState && minSNR >= snrHighThresholdDb -> false
            else -> lowSNRState
        }

        _lowSNRWarningLive.postValue(lowSNRState)

        Log.d("SNR_DEBUG", "CH1=$snr1 dB, CH2=$snr2 dB, LOW=$lowSNRState")
    }

    // ✅ Get ordered window
    private fun getWindow(buffer: DoubleArray): DoubleArray {
        return DoubleArray(validSamples) {
            val idx = (writeIndex - validSamples + it + windowSize) % windowSize
            buffer[idx]
        }
    }

    // ✅ Core SNR logic (stable)
    private fun computeSNR(data: DoubleArray): Double {

        if (data.size < 2) return Double.NEGATIVE_INFINITY

        val denom = (smoothingWindowSamples + 1).coerceAtLeast(1)
        val alpha = 2.0 / denom.toDouble()

        // EMA-smoothed signal
        val smooth = DoubleArray(data.size)
        smooth[0] = data[0]
        for (i in 1 until data.size) {
            smooth[i] = alpha * data[i] + (1.0 - alpha) * smooth[i - 1]
        }

        // Signal power = variance of the smoothed signal
        var sumSmooth = 0.0
        for (v in smooth) sumSmooth += v
        val meanSmooth = sumSmooth / smooth.size

        var signalPowerSum = 0.0
        for (v in smooth) {
            val d = v - meanSmooth
            signalPowerSum += d * d
        }
        val signalPower = signalPowerSum / smooth.size

        // Noise power = variance of residual (data - smooth)
        var sumResidual = 0.0
        for (i in data.indices) {
            sumResidual += (data[i] - smooth[i])
        }
        val meanResidual = sumResidual / data.size

        var noisePowerSum = 0.0
        for (i in data.indices) {
            val r = (data[i] - smooth[i]) - meanResidual
            noisePowerSum += r * r
        }
        val noisePower = noisePowerSum / data.size

        val eps = 1e-12
        if (signalPower <= 0) return Double.NEGATIVE_INFINITY
        if (noisePower <= 0) return Double.POSITIVE_INFINITY

        val snr = 10 * log10(signalPower / (noisePower + eps))
        return if (snr.isFinite()) snr else Double.NEGATIVE_INFINITY
    }

    // ✅ Reset
    fun reset() {
        writeIndex = 0
        validSamples = 0
        lowSNRState = false
        ch1Window.fill(0.0)
        ch2Window.fill(0.0)

        _snrValuesLive.postValue(Pair(null, null))
        _lowSNRWarningLive.postValue(false)
    }
}
