//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import javax.inject.Inject
//class SaturationDetector @Inject constructor()  {
//
//    // --- Only Lead 1 & Lead 2 ---
//    private val LEAD_1 = 0
//    private val LEAD_2 = 1
//
//    // --- Live Data ---
//    private val _saturatedLeadsLive = MutableLiveData<List<Int>>()
//    val saturatedLeadsLive: LiveData<List<Int>> get() = _saturatedLeadsLive
//
//    /**
//     * Detect saturation for Lead 1 & Lead 2 only
//     */
//    fun detectSaturation(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
//
//        val numSamples = 100
//
//        val MAX_VALUE = 1388607
//        val MIN_VALUE = -1388608
//
//        val POS_THRESHOLD = (MAX_VALUE * 0.999).toInt()
//        val NEG_THRESHOLD = (MIN_VALUE * 0.999).toInt()
//
//        val saturatedLeads = mutableListOf<Int>()
//
//        // --- Check Lead 1 ---
//        var lead1Sat = false
//        for (i in 0 until numSamples) {
//            val offset = i * 24 + LEAD_1 * 3
//            val value = read24(sampled, offset)
//
//            if (value >= POS_THRESHOLD || value <= NEG_THRESHOLD) {
//                lead1Sat = true
//                break
//            }
//        }
//        if (lead1Sat) saturatedLeads.add(1)
//
//        // --- Check Lead 2 ---
//        var lead2Sat = false
//        for (i in 0 until numSamples) {
//            val offset = i * 24 + LEAD_2 * 3
//            val value = read24(sampled, offset)
//
//            if (value >= POS_THRESHOLD || value <= NEG_THRESHOLD) {
//                lead2Sat = true
//                break
//            }
//        }
//        if (lead2Sat) saturatedLeads.add(2)
//
//        _saturatedLeadsLive.postValue(saturatedLeads)
//
//        if (saturatedLeads.isNotEmpty()) {
//            Log.w("Saturation", "Saturation in Leads: $saturatedLeads")
//        }
//    }
//
//    fun reset() {
//        _saturatedLeadsLive.postValue(emptyList())
//    }
//}
package com.carditek.kesar.util.filters.edgecomputing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SaturationDetector @Inject constructor() {

    private val CHANNEL_1  = 0
    private val CHANNEL_2 = 1

    // Stability: don't trigger saturation on a single sample touching the ADC rail.
    private val minSaturatedSamplesInWindow = 2

    private val _saturatedLeadsLive = MutableLiveData<List<Int>>()
    val saturatedLeadsLive: LiveData<List<Int>> get() = _saturatedLeadsLive

    fun detectSaturation(sampled: ByteArray, read24: (ByteArray, Int) -> Int) {
        // 1 second of data at 100 Hz after decimation
        val numSamples = 100

        // 24-bit signed ADC range
        val maxValue = 0x7FFFFF
        val minValue = -0x800000

        // "Near full-scale" thresholds (24-bit signed).
        // If any lead stays at/near the rail for multiple samples, we treat it as saturated.
        val posThreshold = (maxValue * 0.999).toInt()
        val negThreshold = (minValue * 0.999).toInt()

        val saturatedLeads = mutableListOf<Int>()
        var lead1SaturatedCount = 0
        var lead2SaturatedCount = 0

        for (i in 0 until numSamples) {

            // Lead 1
            val v1 = read24(sampled, i * 24 + CHANNEL_1 * 3)
            if (v1 >= posThreshold || v1 <= negThreshold) lead1SaturatedCount++

            // Lead 2
            val v2 = read24(sampled, i * 24 + CHANNEL_2 * 3)
            if (v2 >= posThreshold || v2 <= negThreshold) lead2SaturatedCount++

            if (lead1SaturatedCount >= minSaturatedSamplesInWindow &&
                lead2SaturatedCount >= minSaturatedSamplesInWindow
            ) break
        }

        if (lead1SaturatedCount >= minSaturatedSamplesInWindow) saturatedLeads.add(1)
        if (lead2SaturatedCount >= minSaturatedSamplesInWindow) saturatedLeads.add(2)

        _saturatedLeadsLive.postValue(saturatedLeads)

        if (saturatedLeads.isNotEmpty()) {
            Log.w("SaturationDetector", "Saturation detected in leads: $saturatedLeads")
        }
    }

    fun reset() {
        _saturatedLeadsLive.postValue(emptyList())
    }
}
