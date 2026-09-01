
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
//import java.io.File
//
//class OfflineProcessor(private val context: Context) {
//
//    companion object {
//        private const val TAG = "OfflineProcessor"
//    }
//
//    private var pythonInitialized = false
//
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    // Python's analyzelead4() never returns a beat number (it only prints
//    // "Beat Number : best_idx + 1" internally). So we track it locally —
//    // it increments by 1 every time a beat is successfully processed.
//    private var beatCounter = 0
//
//    private fun getDefaultValues(): Map<String, Double> {
//        return mapOf(
//            "beatNumber" to beatCounter.toDouble(),
//            "pOnsetLocation" to 0.0,
//            "pPeakLocation" to 0.0,
//            "qrsOnLocation" to 0.0,
//            "qrsOffLocation" to 0.0,
//            "rPeakLocation" to 0.0,
//            "prDuration" to 0.0,
//            "qrsDuration" to 0.0,
//            "paDuration" to 0.0
//        )
//    }
//
//    private fun initPython() {
//        if (!pythonInitialized) {
//            synchronized(this) {
//                if (!pythonInitialized && !Python.isStarted()) {
//                    try {
//                        Python.start(AndroidPlatform(context))
//                        pythonInitialized = true
//                        Log.d(TAG, "Python started successfully")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Failed to start Python", e)
//                    }
//                } else if (Python.isStarted()) {
//                    pythonInitialized = true
//                }
//            }
//        }
//    }
//
//    fun process(signal: IntArray): Map<String, Double> {
//
//        val startTime = System.currentTimeMillis()
//
//        Log.d(TAG, "====================================")
//        Log.d(TAG, "OfflineProcessor Started")
//        Log.d(TAG, "Signal Length : ${signal.size}")
//        Log.d(TAG, "====================================")
//
//        if (signal.isEmpty()) {
//            Log.e(TAG, "Signal Empty")
//            return getDefaultValues()
//        }
//
//        if (signal.all { it == 0 }) {
//            Log.e(TAG, "Signal contains only zeros")
//            return getDefaultValues()
//        }
//
//        try {
//
//            initPython()
//
//            val py = Python.getInstance()
//
//            val module = py.getModule("ecg_analysis")
//
//            val graphFile = File(context.filesDir, "hisbundle.png")
//
//            val graphPath = graphFile.absolutePath
//
//            val result = module.callAttr(
//                "analyzelead4",
//                signal.toList(),
//                graphPath
//            )
//
//            // Python return order:
//            // [0] P_on_idx   [1] P_peak_idx   [2] QRS_on_idx   [3] QRS_off_idx  [4] R_peak_idx
//            // [5] P_on_val   [6] P_peak_val   [7] QRS_on_val   [8] QRS_off_val  [9] R_peak_val
//            // [10] PR_duration  [11] QRS_duration  [12] graph_path
//            val list = result.asList()
//
//            val pOnsetLocation =
//                if (list.size > 0) list[0].toInt() else 0
//
//            val pPeakLocation =
//                if (list.size > 1) list[1].toInt() else 0
//
//            val qrsOnLocation =
//                if (list.size > 2) list[2].toInt() else 0
//
//            val qrsOffLocation =
//                if (list.size > 3) list[3].toInt() else 0
//
//            val rPeakLocation =
//                if (list.size > 4) list[4].toInt() else 0
//
//
//            // Indices 5-9 (P_on_val, P_peak_val, QRS_on_val, QRS_off_val, R_peak_val)
//            // are the raw amplitude values at those locations — not needed
//            // for this UI, so they're skipped here.
//
//            val prDuration =
//                if (list.size > 10) list[10].toInt() else 0
//
//            val qrsDuration =
//                if (list.size > 11) list[11].toInt() else 0
//            val paduration=
//                if(list.size > 12) list[12].toInt() else 0
//
//            val returnedGraphPath =
//                if (list.size > 13) list[13].toString() else ""
//
//            // Successfully processed a beat — increment the counter.
//            beatCounter += 1
//
//            Log.d(TAG, "==============================")
//            Log.d(TAG, "BEST BEAT RESULTS")
//            Log.d(TAG, "==============================")
//
//            Log.d(TAG, "Beat Number       : $beatCounter")
//            Log.d(TAG, "P Onset Location  : $pOnsetLocation")
//            Log.d(TAG, "P Peak Location   : $pPeakLocation")
//            Log.d(TAG, "QRS On Location   : $qrsOnLocation")
//            Log.d(TAG, "QRS Off Location  : $qrsOffLocation")
//            Log.d(TAG, "R Peak Location   : $rPeakLocation")
//            Log.d(TAG, "PR Duration       : $prDuration ms")
//            Log.d(TAG, "QRS Duration      : $qrsDuration ms")
//            Log.d(TAG,    "PA Duration      : $paduration ms")
//            Log.d(TAG, "Graph Path        : $returnedGraphPath")
//
//            Log.d(TAG, "==============================")
//
//            mainHandler.post {
//
//                HisBundleData.graphPath = returnedGraphPath
//
//                HisBundleData.graphPathLive.postValue(returnedGraphPath)
//
//            }
//
//            val endTime = System.currentTimeMillis()
//
//            Log.d(
//                TAG,
//                "Offline Processing Time : ${endTime - startTime} ms"
//            )
//
//            return mapOf(
//                "beatNumber" to beatCounter.toDouble(),
//                "pOnsetLocation" to pOnsetLocation.toDouble(),
//                "pPeakLocation" to pPeakLocation.toDouble(),
//                "qrsOnLocation" to qrsOnLocation.toDouble(),
//                "qrsOffLocation" to qrsOffLocation.toDouble(),
//                "rPeakLocation" to rPeakLocation.toDouble(),
//                "prDuration" to prDuration.toDouble(),
//                "qrsDuration" to qrsDuration.toDouble(),
//                "paduration" to paduration.toDouble()
//            )
//
//        } catch (e: Exception) {
//
//            Log.e(TAG, "OfflineProcessor Error", e)
//
//            return getDefaultValues()
//        }
//    }
//
//    fun resetBeatCounter() {
//        beatCounter = 0
//    }
//}


///pa values is correct
//
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
//import java.io.File
//
//class OfflineProcessor(private val context: Context) {
//
//    companion object {
//        private const val TAG = "OfflineProcessor"
//    }
//
//    private var pythonInitialized = false
//
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    // Python's analyzelead4() never returns a beat number (it only prints
//    // "Beat Number : best_idx + 1" internally). So we track it locally —
//    // it increments by 1 every time a beat is successfully processed.
//    private var beatCounter = 0
//
//    private fun getDefaultValues(): Map<String, Double> {
//        return mapOf(
//            "beatNumber" to beatCounter.toDouble(),
//            "pOnsetLocation" to 0.0,
//            "pPeakLocation" to 0.0,
//            "qrsOnLocation" to 0.0,
//            "qrsOffLocation" to 0.0,
//            "rPeakLocation" to 0.0,
//            "prDuration" to 0.0,
//            "qrsDuration" to 0.0,
//            "paDuration" to 0.0,
//            "ahDuration" to 0.0,
//            "hvDuration" to 0.0
//
//        )
//    }
//
//    private fun initPython() {
//        if (!pythonInitialized) {
//            synchronized(this) {
//                if (!pythonInitialized && !Python.isStarted()) {
//                    try {
//                        Python.start(AndroidPlatform(context))
//                        pythonInitialized = true
//                        Log.d(TAG, "Python started successfully")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Failed to start Python", e)
//                    }
//                } else if (Python.isStarted()) {
//                    pythonInitialized = true
//                }
//            }
//        }
//    }
//
//    fun process(signal: IntArray): Map<String, Double> {
//
//        val startTime = System.currentTimeMillis()
//
//        Log.d(TAG, "====================================")
//        Log.d(TAG, "OfflineProcessor Started")
//        Log.d(TAG, "Signal Length : ${signal.size}")
//        Log.d(TAG, "====================================")
//
//        if (signal.isEmpty()) {
//            Log.e(TAG, "Signal Empty")
//            return getDefaultValues()
//        }
//
//        if (signal.all { it == 0 }) {
//            Log.e(TAG, "Signal contains only zeros")
//            return getDefaultValues()
//        }
//
//        try {
//
//            initPython()
//
//            val py = Python.getInstance()
//
//            val module = py.getModule("ecg_analysis")
//
//            val graphFile = File(context.filesDir, "hisbundle.png")
//
//            val graphPath = graphFile.absolutePath
//
//            val result = module.callAttr(
//                "analyzelead4",
//                signal.toList(),
//                graphPath
//            )
//
//            // Python (analyzelead4) return order — 16 elements total:
//            // [0]  P_on_idx        [1]  P_peak_idx      [2]  A_wave_idx
//            // [3]  QRS_on_idx      [4]  QRS_off_idx     [5]  R_peak_idx
//            // [6]  P_on_val        [7]  P_peak_val      [8]  A_wave_val
//            // [9]  QRS_on_val      [10] QRS_off_val     [11] R_peak_val
//            // [12] PA_duration     [13] PR_duration     [14] QRS_duration
//            // [15] graph_path
//            val list = result.asList()
//
//            val pOnsetLocation =
//                if (list.size > 0) list[0].toInt() else 0
//
//            val pPeakLocation =
//                if (list.size > 1) list[1].toInt() else 0
//
//            // Index 2 is A_wave_idx — not needed for this UI, skipped.
//
//            val qrsOnLocation =
//                if (list.size > 3) list[3].toInt() else 0
//
//            val qrsOffLocation =
//                if (list.size > 4) list[4].toInt() else 0
//
//            val rPeakLocation =
//                if (list.size > 5) list[5].toInt() else 0
//
//            // Indices 6-11 (P_on_val, P_peak_val, A_wave_val, QRS_on_val,
//            // QRS_off_val, R_peak_val) are raw amplitude values — not
//            // needed for this UI, so they're skipped here.
//
//            val paDuration =
//                if (list.size > 12) list[12].toInt() else 0
//
//            val prDuration =
//                if (list.size > 13) list[13].toInt() else 0
//
//            val qrsDuration =
//                if (list.size > 14) list[14].toInt() else 0
//
//            val ahDuration =
//                if (list.size > 15) list[15].toInt() else 0
//            val hvDuration =
//                if (list.size > 16) list[16].toInt() else 0
//
//
//            val returnedGraphPath =
//                if (list.size > 17) list[17].toString() else ""
//
//            // Successfully processed a beat — increment the counter.
//            beatCounter += 1
//
//            Log.d(TAG, "==============================")
//            Log.d(TAG, "BEST BEAT RESULTS")
//            Log.d(TAG, "==============================")
//
//            Log.d(TAG, "Beat Number       : $beatCounter")
//            Log.d(TAG, "P Onset Location  : $pOnsetLocation")
//            Log.d(TAG, "P Peak Location   : $pPeakLocation")
//            Log.d(TAG, "QRS On Location   : $qrsOnLocation")
//            Log.d(TAG, "QRS Off Location  : $qrsOffLocation")
//            Log.d(TAG, "R Peak Location   : $rPeakLocation")
//            Log.d(TAG, "PA Duration       : $paDuration ms")
//            Log.d(TAG, "PR Duration       : $prDuration ms")
//            Log.d(TAG, "QRS Duration      : $qrsDuration ms")
//            Log.d(TAG, "AH Duration      : $ahDuration ms")
//            Log.d(TAG, "HV Duration      : $hvDuration ms")
//            Log.d(TAG, "Graph Path        : $returnedGraphPath")
//
//            Log.d(TAG, "==============================")
//
//            mainHandler.post {
//
//                HisBundleData.graphPath = returnedGraphPath
//
//                HisBundleData.graphPathLive.postValue(returnedGraphPath)
//
//            }
//
//            val endTime = System.currentTimeMillis()
//
//            Log.d(
//                TAG,
//                "Offline Processing Time : ${endTime - startTime} ms"
//            )
//
//            return mapOf(
//                "beatNumber" to beatCounter.toDouble(),
//                "pOnsetLocation" to pOnsetLocation.toDouble(),
//                "pPeakLocation" to pPeakLocation.toDouble(),
//                "qrsOnLocation" to qrsOnLocation.toDouble(),
//                "qrsOffLocation" to qrsOffLocation.toDouble(),
//                "rPeakLocation" to rPeakLocation.toDouble(),
//                "prDuration" to prDuration.toDouble(),
//                "qrsDuration" to qrsDuration.toDouble(),
//                "paDuration" to paDuration.toDouble(),
//                "ahDuration" to ahDuration.toDouble(),
//                "hvDuration" to hvDuration.toDouble()
//            )
//
//        } catch (e: Exception) {
//
//            Log.e(TAG, "OfflineProcessor Error", e)
//
//            return getDefaultValues()
//        }
//    }
//
//    /**
//     * Resets the beat counter back to zero.
//     * Call this when starting a new recording session for a new patient.
//     */
//    fun resetBeatCounter() {
//        beatCounter = 0
//    }
//}




//  AH AND HV CODE
//
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import com.chaquo.python.Python
//import com.chaquo.python.android.AndroidPlatform
//import java.io.File
//
//class OfflineProcessor(private val context: Context) {
//
//    companion object {
//        private const val TAG = "OfflineProcessor"
//    }
//
//    private var pythonInitialized = false
//
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    // Python's analyzelead4() never returns a beat number (it only prints
//    // "Beat Number : best_idx + 1" internally). So we track it locally —
//    // it increments by 1 every time a beat is successfully processed.
//    private var beatCounter = 0
//
//    private fun getDefaultValues(): Map<String, Double> {
//        return mapOf(
//            "beatNumber" to beatCounter.toDouble(),
//            "pOnsetLocation" to 0.0,
//            "pPeakLocation" to 0.0,
//            "qrsOnLocation" to 0.0,
//            "qrsOffLocation" to 0.0,
//            "rPeakLocation" to 0.0,
//            "prDuration" to 0.0,
//            "qrsDuration" to 0.0,
//            "paDuration" to 0.0,
//            "ahDuration" to 0.0,
//            "hvDuration" to 0.0,
//            "HAmplitude" to 0.0
//        )
//    }
//
//    private fun initPython() {
//        if (!pythonInitialized) {
//            synchronized(this) {
//                if (!pythonInitialized && !Python.isStarted()) {
//                    try {
//                        Python.start(AndroidPlatform(context))
//                        pythonInitialized = true
//                        Log.d(TAG, "Python started successfully")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Failed to start Python", e)
//                    }
//                } else if (Python.isStarted()) {
//                    pythonInitialized = true
//                }
//            }
//        }
//    }
//
//    fun process(signal: IntArray): Map<String, Double> {
//
//        val startTime = System.currentTimeMillis()
//
//        Log.d(TAG, "====================================")
//        Log.d(TAG, "OfflineProcessor Started")
//        Log.d(TAG, "Signal Length : ${signal.size}")
//        Log.d(TAG, "====================================")
//
//        if (signal.isEmpty()) {
//            Log.e(TAG, "Signal Empty")
//            return getDefaultValues()
//        }
//
//        if (signal.all { it == 0 }) {
//            Log.e(TAG, "Signal contains only zeros")
//            return getDefaultValues()
//        }
//
//        try {
//
//            initPython()
//
//            val py = Python.getInstance()
//
//            val module = py.getModule("ecg_analysis")
//
//            val graphFile = File(context.filesDir, "hisbundle.png")
//
//            val graphPath = graphFile.absolutePath
//
//            val result = module.callAttr(
//                "analyzelead4",
//                signal.toList(),
//                graphPath
//            )
//
//            // Python (analyzelead4) return order — 20 elements total:
//            // [0]  P_on_idx        [1]  P_peak_idx      [2]  A_wave_idx     [3]  H_idx
//            // [4]  QRS_on_idx      [5]  QRS_off_idx     [6]  R_peak_idx
//            // [7]  P_on_val        [8]  P_peak_val      [9]  A_wave_val     [10] H_val
//            // [11] QRS_on_val      [12] QRS_off_val     [13] R_peak_val
//            // [14] PA_duration     [15] AH_duration     [16] HV_duration
//            // [17] PR_duration     [18] QRS_duration    [19] graph_path
//            val list = result.asList()
//
//            val pOnsetLocation =
//                if (list.size > 0) list[0].toInt() else 0
//
//            val pPeakLocation =
//                if (list.size > 1) list[1].toInt() else 0
//
//            // Index 2 is A_wave_idx, index 3 is H_idx — not needed for
//            // this UI, skipped.
//
//            val qrsOnLocation =
//                if (list.size > 4) list[4].toInt() else 0
//
//            val qrsOffLocation =
//                if (list.size > 5) list[5].toInt() else 0
//
//            val rPeakLocation =
//                if (list.size > 6) list[6].toInt() else 0
//
//            // Indices 7-13 (P_on_val, P_peak_val, A_wave_val, H_val,
//            // QRS_on_val, QRS_off_val, R_peak_val) are raw amplitude
//            // values — not needed for this UI, so they're skipped here.
//            val hAmplitude =
//                if (list.size > 10) list[10].toInt() else 0
//
//            val paDuration =
//                if (list.size > 14) list[14].toInt() else 0
//
//            val ahDuration =
//                if (list.size > 15) list[15].toInt() else 0
//
//            val hvDuration =
//                if (list.size > 16) list[16].toInt() else 0
//
//            val prDuration =
//                if (list.size > 17) list[17].toInt() else 0
//
//            val qrsDuration =
//                if (list.size > 18) list[18].toInt() else 0
//
//
//            val returnedGraphPath =
//                if (list.size > 19) list[19].toString() else ""
//
//            // Successfully processed a beat — increment the counter.
//            beatCounter += 1
//
//            Log.d(TAG, "==============================")
//            Log.d(TAG, "BEST BEAT RESULTS")
//            Log.d(TAG, "==============================")
//
//            Log.d(TAG, "Beat Number       : $beatCounter")
//            Log.d(TAG, "P Onset Location  : $pOnsetLocation")
//            Log.d(TAG, "P Peak Location   : $pPeakLocation")
//            Log.d(TAG, "QRS On Location   : $qrsOnLocation")
//            Log.d(TAG, "QRS Off Location  : $qrsOffLocation")
//            Log.d(TAG, "R Peak Location   : $rPeakLocation")
//            Log.d(TAG, "PA Duration       : $paDuration ms")
//            Log.d(TAG, "AH Duration       : $ahDuration ms")
//            Log.d(TAG, "HV Duration       : $hvDuration ms")
//            Log.d(TAG, "PR Duration       : $prDuration ms")
//            Log.d(TAG, "QRS Duration      : $qrsDuration ms")
//            Log.d(TAG, "H  Amplitude      : $hAmplitude mv")
//            Log.d(TAG, "Graph Path        : $returnedGraphPath")
//
//            Log.d(TAG, "==============================")
//
//            mainHandler.post {
//
//                HisBundleData.graphPath = returnedGraphPath
//
//                HisBundleData.graphPathLive.postValue(returnedGraphPath)
//
//
//
//            }
//
//            val endTime = System.currentTimeMillis()
//
//            Log.d(
//                TAG,
//                "Offline Processing Time : ${endTime - startTime} ms"
//            )
//
//            return mapOf(
//                "beatNumber" to beatCounter.toDouble(),
//                "pOnsetLocation" to pOnsetLocation.toDouble(),
//                "pPeakLocation" to pPeakLocation.toDouble(),
//                "qrsOnLocation" to qrsOnLocation.toDouble(),
//                "qrsOffLocation" to qrsOffLocation.toDouble(),
//                "rPeakLocation" to rPeakLocation.toDouble(),
//                "prDuration" to prDuration.toDouble(),
//                "qrsDuration" to qrsDuration.toDouble(),
//                "paDuration" to paDuration.toDouble(),
//                "ahDuration" to ahDuration.toDouble(),
//                "hvDuration" to hvDuration.toDouble(),
//                "hAmplitude" to hAmplitude.toDouble()
//            )
//
//        } catch (e: Exception) {
//
//            Log.e(TAG, "OfflineProcessor Error", e)
//
//            return getDefaultValues()
//        }
//    }
//
//    /**
//     * Resets the beat counter back to zero.
//     * Call this when starting a new recording session for a new patient.
//     */
//    fun resetBeatCounter() {
//        beatCounter = 0
//    }
//    fun clearResults() {
//
//        beatCounter = 0
//
//        mainHandler.post {
//            HisBundleData.graphPath = ""
//            HisBundleData.graphPathLive.postValue("")
//        }
//
//        Log.d(TAG, "OfflineProcessor Cleared")
//    }
//
//}




package com.carditek.kesar.util.filters.edgecomputing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File

class OfflineProcessor(private val context: Context) {

    companion object {
        private const val TAG = "OfflineProcessor"
    }

    private var pythonInitialized = false

    private val mainHandler = Handler(Looper.getMainLooper())

    // Python's analyzelead4() never returns a beat number (it only prints
    // "Beat Number : best_idx + 1" internally). So we track it locally —
    // it increments by 1 every time a beat is successfully processed.
    private var beatCounter = 0

    private fun getDefaultValues(): Map<String, Double> {
        return mapOf(
            "beatNumber" to beatCounter.toDouble(),
            "pOnsetLocation" to 0.0,
            "pPeakLocation" to 0.0,
            "qrsOnLocation" to 0.0,
            "qrsOffLocation" to 0.0,
            "rPeakLocation" to 0.0,
            "prDuration" to 0.0,
            "qrsDuration" to 0.0,
            "paDuration" to 0.0,
            "ahDuration" to 0.0,
            "hvDuration" to 0.0,
            "hAmplitude" to 0.0
        )
    }

    private fun initPython() {
        if (!pythonInitialized) {
            synchronized(this) {
                if (!pythonInitialized && !Python.isStarted()) {
                    try {
                        Python.start(AndroidPlatform(context))
                        pythonInitialized = true
                        Log.d(TAG, "Python started successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start Python", e)
                    }
                } else if (Python.isStarted()) {
                    pythonInitialized = true
                }
            }
        }
    }

    fun process(signal: IntArray): Map<String, Double> {

        val startTime = System.currentTimeMillis()

        Log.d(TAG, "====================================")
        Log.d(TAG, "OfflineProcessor Started")
        Log.d(TAG, "Signal Length : ${signal.size}")
        Log.d(TAG, "====================================")

        if (signal.isEmpty()) {
            Log.e(TAG, "Signal Empty")
            return getDefaultValues()
        }

        if (signal.all { it == 0 }) {
            Log.e(TAG, "Signal contains only zeros")
            return getDefaultValues()
        }

        try {

            initPython()

            val py = Python.getInstance()

            val module = py.getModule("ecg_analysis")

            val graphFile = File(context.filesDir, "hisbundle.png")

            val graphPath = graphFile.absolutePath

            val result = module.callAttr(
                "analyzelead4",
                signal.toList(),
                graphPath
            )

            // Python (analyzelead4) return order — 20 elements total (indices 0-19):
            // [0]  P_on_idx        [1]  P_peak_idx      [2]  A_wave_idx     [3]  H_idx
            // [4]  QRS_on_idx      [5]  QRS_off_idx     [6]  R_peak_idx
            // [7]  P_on_val        [8]  P_peak_val      [9]  A_wave_val     [10] H_val (H amplitude)
            // [11] QRS_on_val      [12] QRS_off_val     [13] R_peak_val
            // [14] PA_duration     [15] AH_duration     [16] HV_duration
            // [17] PR_duration     [18] QRS_duration    [19] graph_path
            val list = result.asList()

            val pOnsetLocation =
                if (list.size > 0) list[0].toInt() else 0

            val pPeakLocation =
                if (list.size > 1) list[1].toInt() else 0

            // Index 2 is A_wave_idx, index 3 is H_idx — not needed for
            // this UI, skipped.

            val qrsOnLocation =
                if (list.size > 4) list[4].toInt() else 0

            val qrsOffLocation =
                if (list.size > 5) list[5].toInt() else 0

            val rPeakLocation =
                if (list.size > 6) list[6].toInt() else 0

            // Indices 7-9, 11-13 (P_on_val, P_peak_val, A_wave_val,
            // QRS_on_val, QRS_off_val, R_peak_val) are raw amplitude
            // values — not needed for this UI, so they're skipped here.

            // Index 10 is H_val (H amplitude, mV) — a float, so read as
            // Double rather than Int (previously misread from index 19,
            // which is actually the graph path string).
            val hAmplitude =
                if (list.size > 10) list[10].toDouble() else 0.0

            val paDuration =
                if (list.size > 14) list[14].toInt() else 0

            val ahDuration =
                if (list.size > 15) list[15].toInt() else 0

            val hvDuration =
                if (list.size > 16) list[16].toInt() else 0

            val prDuration =
                if (list.size > 17) list[17].toInt() else 0

            val qrsDuration =
                if (list.size > 18) list[18].toInt() else 0

            // Index 19 is the graph path string — was previously read at
            // index 20, which is out of bounds (list only has 20
            // elements, indices 0-19), so returnedGraphPath was always "".
            val returnedGraphPath =
                if (list.size > 19) list[19].toString() else ""

            // Successfully processed a beat — increment the counter.
            beatCounter += 1

            Log.d(TAG, "==============================")
            Log.d(TAG, "BEST BEAT RESULTS")
            Log.d(TAG, "==============================")

            Log.d(TAG, "Beat Number       : $beatCounter")
            Log.d(TAG, "P Onset Location  : $pOnsetLocation")
            Log.d(TAG, "P Peak Location   : $pPeakLocation")
            Log.d(TAG, "QRS On Location   : $qrsOnLocation")
            Log.d(TAG, "QRS Off Location  : $qrsOffLocation")
            Log.d(TAG, "R Peak Location   : $rPeakLocation")
            Log.d(TAG, "PA Duration       : $paDuration ms")
            Log.d(TAG, "AH Duration       : $ahDuration ms")
            Log.d(TAG, "HV Duration       : $hvDuration ms")
            Log.d(TAG, "PR Duration       : $prDuration ms")
            Log.d(TAG, "QRS Duration      : $qrsDuration ms")
            Log.d(TAG, "H  Amplitude      : $hAmplitude mV")
            Log.d(TAG, "Graph Path        : $returnedGraphPath")

            Log.d(TAG, "==============================")

            mainHandler.post {

                HisBundleData.graphPath = returnedGraphPath

                HisBundleData.graphPathLive.postValue(returnedGraphPath)

            }

            val endTime = System.currentTimeMillis()

            Log.d(
                TAG,
                "Offline Processing Time : ${endTime - startTime} ms"
            )

            return mapOf(
                "beatNumber" to beatCounter.toDouble(),
                "pOnsetLocation" to pOnsetLocation.toDouble(),
                "pPeakLocation" to pPeakLocation.toDouble(),
                "qrsOnLocation" to qrsOnLocation.toDouble(),
                "qrsOffLocation" to qrsOffLocation.toDouble(),
                "rPeakLocation" to rPeakLocation.toDouble(),
                "prDuration" to prDuration.toDouble(),
                "qrsDuration" to qrsDuration.toDouble(),
                "paDuration" to paDuration.toDouble(),
                "ahDuration" to ahDuration.toDouble(),
                "hvDuration" to hvDuration.toDouble(),
                "hAmplitude" to hAmplitude
            )

        } catch (e: Exception) {

            Log.e(TAG, "OfflineProcessor Error", e)

            return getDefaultValues()
        }
    }

    /**
     * Resets the beat counter back to zero.
     * Call this when starting a new recording session for a new patient.
     */
    fun resetBeatCounter() {
        beatCounter = 0
    }

    fun clearResults() {

        beatCounter = 0

        mainHandler.post {
            HisBundleData.graphPath = ""
            HisBundleData.graphPathLive.postValue("")
        }

        Log.d(TAG, "OfflineProcessor Cleared")
    }

}
