package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs

/**
 * Improved Heart Rate Calculator (Pan–Tompkins + stability filters)
 * Works on 100 Hz decimated ECG from Lead II.
 */
class HeartRateCalculator {

    // Live ECG heart rate
    private val _heartRateLive = MutableLiveData<Int>()
    val heartRateLive = _heartRateLive

    // Rolling 10-second window (1000 samples)
    private val fs = 100
    private val windowSec = 10
    private val windowSize = windowSec * fs
    private val hrWindow = DoubleArray(windowSize)

    private var writeIndex = 0
    private var validSamples = 0
    private var lastUpdateTime = 0L

    // History smoothing (median of last 3 values)
    private val hrHistory = mutableListOf<Int>()
    private val historySize = 3

    /**
     * Called every second with 100 decimated samples.
     * Extracts Lead II and writes into 10-sec rolling buffer.
     */
    fun updateHeartRateWindow(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
        val numSamples = 100
        val channel = 1  // Lead II

        val lead2 = DoubleArray(numSamples) { i ->
            val offset = i * 24 + channel * 3
            read24(sampled, offset).toDouble()
        }

        // Insert into rolling buffer
        for (i in 0 until numSamples) {
            hrWindow[writeIndex] = lead2[i]
            writeIndex = (writeIndex + 1) % windowSize
            if (validSamples < windowSize) validSamples++
        }
    }

    /**
     * Calculates HR every 10 seconds using 1000-sample window.
     */
    fun calculateHeartRateFromWindow() {

        // Wait until window full
        if (validSamples < windowSize) return

        // Update only once every 10 seconds
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < 5000 && lastUpdateTime > 0) return

        // Build ordered window
        val signal = DoubleArray(windowSize) { i ->
            val idx = (writeIndex - windowSize + i + windowSize) % windowSize
            hrWindow[idx]
        }

        // Process heart rate
        val hr = detectRPeaksAndCalculateHR(signal, fs)

        if (hr > 0) {
            // Add to history for smoothing
            hrHistory.add(hr)
            if (hrHistory.size > historySize) hrHistory.removeAt(0)

            // Median of last values
            val sorted = hrHistory.sorted()
            val smoothed = sorted[sorted.size / 2]

            // Clamp to valid physiological range
            val finalHR = smoothed.coerceIn(30, 200)

            _heartRateLive.postValue(finalHR)
            lastUpdateTime = now

            Log.d("HeartRateCalculator", "HR (stable 5s): $finalHR bpm   (raw=$hr)")
        }
    }

    /**
     * Improved R-peak detection (Pan-Tompkins style)
     */
    private fun detectRPeaksAndCalculateHR(ecg: DoubleArray, fs: Int): Int {

        if (ecg.size < 20) return 0

        // -----------------------------
        // Step 1 — Normalize
        // -----------------------------
        val mean = ecg.average()
        val norm = DoubleArray(ecg.size) { ecg[it] - mean }

        // -----------------------------
        // Step 2 — Derivative
        // -----------------------------
        val diff = DoubleArray(norm.size - 1) { i -> norm[i + 1] - norm[i] }

        // -----------------------------
        // Step 3 — Squaring
        // -----------------------------
        val squared = DoubleArray(diff.size) { i -> diff[i] * diff[i] }

        // -----------------------------
        // Step 4 — Moving average 150 ms
        // -----------------------------
        val win = (0.150 * fs).toInt()
        val ma = DoubleArray(squared.size)
        var sum = 0.0

        for (i in squared.indices) {
            sum += squared[i]
            if (i >= win) sum -= squared[i - win]
            ma[i] = sum / win
        }

        // -----------------------------
        // Step 5 — Adaptive threshold
        // -----------------------------
        var th = ma.average() * 0.5
        if (th <= 0) th = 0.000001  // allow <=0 but avoid zero div

        val refractory = (0.3 * fs).toInt() // 300ms

        // -----------------------------
        // Step 6 — Peak detection
        // -----------------------------
        val rPeaks = mutableListOf<Int>()
        var lastPeak = -999

        for (i in 1 until ma.size - 1) {
            if (ma[i] > th &&
                ma[i] > ma[i - 1] &&
                ma[i] > ma[i + 1] &&
                (i - lastPeak) >= refractory
            ) {
                rPeaks.add(i)
                lastPeak = i
            }
        }

        if (rPeaks.size < 2) return 0

        // -----------------------------
        // Step 7 — RR interval filtering
        // -----------------------------
        val rr = mutableListOf<Int>()
        for (i in 1 until rPeaks.size) {
            val interval = rPeaks[i] - rPeaks[i - 1]
            val bpm = (60.0 * fs) / interval
            if (bpm in 30.0..300.0) rr.add(interval)
        }

        if (rr.isEmpty()) return 0

        // -----------------------------
        // Step 8 — Median RR → HR
        // -----------------------------
        val sortedRR = rr.sorted()
        val medianRR = sortedRR[sortedRR.size / 2]

        val hr = ((60.0 * fs) / medianRR).toInt()

        Log.d("HeartRateCalculator", "Detected R-peaks=${rPeaks.size}, medianRR=$medianRR → HR=$hr")

        return hr
    }

    /**
     * Reset state
     */
    fun reset() {
        writeIndex = 0
        validSamples = 0
        lastUpdateTime = 0
        hrHistory.clear()
        _heartRateLive.postValue(0)
    }
}

