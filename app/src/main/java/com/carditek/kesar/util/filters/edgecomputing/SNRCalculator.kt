
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import kotlin.math.log10
//import javax.inject.Inject
//
//class SNRCalculator @Inject constructor() {
//
//    // Hardware Channels
//    private val CHANNEL_1 = 0
//    private val CHANNEL_2 = 1
//
//    //  LiveData Outputs
//    private val _snrValuesLive = MutableLiveData<Pair<Double?, Double?>>()
//    val snrValuesLive: LiveData<Pair<Double?, Double?>> get() = _snrValuesLive
//
//    private val _lowSNRWarningLive = MutableLiveData<Boolean>()
//    val lowSNRWarningLive: LiveData<Boolean> get() = _lowSNRWarningLive
//
//    // True sample rate: `EdgeComputingProcessor` passes the raw 1000 Hz buffer here.
//    private val sampleRate = 1000
//    private val windowSeconds = 5                         // Faster response
//    private val windowSize = windowSeconds * sampleRate    // 5000 samples
//
//    // Buffers
//    private val ch1Window = DoubleArray(windowSize)
//    private val ch2Window = DoubleArray(windowSize)
//
//    private var writeIndex = 0
//    private var validSamples = 0
//
//    // Thresholds (can tune later)
//    private val snrLowThresholdDb = 0.0
//    private val snrHighThresholdDb = 0.0
//
//    // EMA smoothing (at 1000 Hz)
//    private val smoothingWindowSamples = 100   // ~100ms smoothing at 1000 Hz
//    private val minValidSamples = 200          // ~200ms of data before reporting SNR
//
//    private var lowSNRState = false
//
//    /**
//     * Call this for every incoming packet
//     */
//    fun calculateSNR(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
//
//        // Accept either 100 Hz decimated buffers (2400 bytes) or true 1000 Hz buffers (24000 bytes)
//        // by deriving sample count from the payload size.
//        val numSamples = sampled.size / 24
//        if (numSamples <= 0) {
//            _snrValuesLive.postValue(Pair(null, null))
//            _lowSNRWarningLive.postValue(false)
//            lowSNRState = false
//            return
//        }
//
//        //  Extract channel data
//        val ch1Data = DoubleArray(numSamples) {
//            read24(sampled, it * 24 + CHANNEL_1 * 3).toDouble()
//        }
//
//        val ch2Data = DoubleArray(numSamples) {
//            read24(sampled, it * 24 + CHANNEL_2 * 3).toDouble()
//        }
//
//        // Update rolling window
//        for (i in 0 until numSamples) {
//            ch1Window[writeIndex] = ch1Data[i]
//            ch2Window[writeIndex] = ch2Data[i]
//
//            writeIndex = (writeIndex + 1) % windowSize
//            if (validSamples < windowSize) validSamples++
//        }
//
//        if (validSamples < minValidSamples) {
//            _snrValuesLive.postValue(Pair(null, null))
//            lowSNRState = false
//            _lowSNRWarningLive.postValue(false)
//            return
//        }
//
//        val window1 = getWindow(ch1Window)
//        val window2 = getWindow(ch2Window)
//
//        val snr1 = computeSNR(window1)
//        val snr2 = computeSNR(window2)
//
//        _snrValuesLive.postValue(Pair(snr1, snr2))
//
//        val minSNR = minOf(snr1, snr2)
//        lowSNRState = when {
//            !lowSNRState && minSNR <= snrLowThresholdDb -> true
//            lowSNRState && minSNR >= snrHighThresholdDb -> false
//            else -> lowSNRState
//        }
//
//        _lowSNRWarningLive.postValue(lowSNRState)
//
//        Log.d("SNR_DEBUG", "CH1=$snr1 dB, CH2=$snr2 dB, LOW=$lowSNRState")
//    }
//
//    // Get ordered window
//    private fun getWindow(buffer: DoubleArray): DoubleArray {
//        return DoubleArray(validSamples) {
//            val idx = (writeIndex - validSamples + it + windowSize) % windowSize
//            buffer[idx]
//        }
//    }
//
//    //  UPDATED SNR CALCULATION (MORE ACCURATE)
//    private fun computeSNR(data: DoubleArray): Double {
//
//        if (data.size < 2) return Double.NEGATIVE_INFINITY
//
//        val alpha = 2.0 / (smoothingWindowSamples + 1)
//
//        //  EMA smoothing (signal estimate)
//        val smooth = DoubleArray(data.size)
//        smooth[0] = data[0]
//
//        for (i in 1 until data.size) {
//            smooth[i] = alpha * data[i] + (1.0 - alpha) * smooth[i - 1]
//        }
//
//        //  Signal Power (FULL POWER, not just variance)
//        var signalPowerSum = 0.0
//        for (v in smooth) {
//            signalPowerSum += v * v
//        }
//        val signalPower = signalPowerSum / smooth.size
//
//        // Noise Power (residual)
//        var noisePowerSum = 0.0
//        for (i in data.indices) {
//            val noise = data[i] - smooth[i]
//            noisePowerSum += noise * noise
//        }
//        val noisePower = noisePowerSum / data.size
//
//        val eps = 1e-12
//
//        if (signalPower <= 0) return Double.NEGATIVE_INFINITY
//        if (noisePower <= 0) return Double.POSITIVE_INFINITY
//
//        val snr = 10 * log10(signalPower / (noisePower + eps))
//
//        return if (snr.isFinite()) snr else Double.NEGATIVE_INFINITY
//    }
//
//    // ✅ Reset
//    fun reset() {
//        writeIndex = 0
//        validSamples = 0
//        lowSNRState = false
//        ch1Window.fill(0.0)
//        ch2Window.fill(0.0)
//
//        _snrValuesLive.postValue(Pair(null, null))
//        _lowSNRWarningLive.postValue(false)
//    }
//}
