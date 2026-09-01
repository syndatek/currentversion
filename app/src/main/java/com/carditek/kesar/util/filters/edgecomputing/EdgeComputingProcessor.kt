//package com.carditek.kesar.util.filters.edgecomputing
//
//
//
//import android.R.attr.end
//
//import android.content.Context
//
//import android.os.Handler
//
//import android.os.Looper
//
//import android.util.Log
//
//import androidx.lifecycle.LiveData
//
//import androidx.lifecycle.MutableLiveData
//
//import java.util.concurrent.atomic.AtomicBoolean
//class EdgeComputingProcessor(
//
//    private val context: Context
//
//) {
//    companion object {
//
//        private const val TAG = "EdgeProcessor"
//        private const val SAMPLE_RATE = 1000
//        private const val MAX_SAMPLES = 15000
//
//    }
//
//    private val offlineProcessor =
//
//        OfflineProcessor(context)
//    // Application Context
//    private val appContext = context.applicationContext
//
//    // ECG Components
//    private val ecgFilter = ECGFilter()
//    private val heartRateCalculator =
//
//        HeartRateCalculator()
//
//    // PR Interval
//
//    private val _prIntervalLive = MutableLiveData<Int>()
//    val prIntervalLive: LiveData<Int> = _prIntervalLive
//    // PA Interval
//    private val _paIntervalLive = MutableLiveData<Int>()
//
//    val paIntervalLive: LiveData<Int> = _paIntervalLive
//    // AH Interval
//    private val _ahIntervalLive = MutableLiveData<Int>()
//    val ahIntervalLive: LiveData<Int> = _ahIntervalLive
//    // HV Interval
//    private val _hvIntervalLive = MutableLiveData<Int>()
//    val hvIntervalLive: LiveData<Int> = _hvIntervalLive
//    // QRS Duration
//    private val _qrsDurationLive = MutableLiveData<Int>()
//    val qrsDurationLive: LiveData<Int> = _qrsDurationLive
//    // SNR
//    private val _snrLive = MutableLiveData<Double>()
//    val snrLive: LiveData<Double> = _snrLive
//    // Last Updated
//    private val _lastUpdatedLive = MutableLiveData<String>()
//
//    val lastUpdatedLive: LiveData<String> = _lastUpdatedLive
//    val heartRateLive =
//
//        heartRateCalculator.heartRateLive
//    // Enable Flag
//    private var enabled = false
//    private var packetCounter = 0
//    private var writeIndex = 0
//    private var totalSamples = 0
//
//
//    // Main Thread Handler
//    private val handler =
//
//        Handler(Looper.getMainLooper())
//
//
//    // Initialization
//
//    init {
//
//
//
//        Log.d(
//
//            TAG,
//
//            "========================================"
//
//        )
//
//
//
//        Log.d(
//
//            TAG,
//
//            "EdgeComputingProcessor Initialized"
//
//        )
//
//        Log.d(
//
//            TAG,
//
//            "Buffer Size = $MAX_SAMPLES"
//
//        )
//
//
//
//        Log.d(
//
//            TAG,
//
//            "Sampling Rate = $SAMPLE_RATE Hz"
//
//        )
//
//
//
//        Log.d(
//
//            TAG,
//
//            "Buffer Window = 15 Seconds"
//
//        )
//
//
//
//        Log.d(
//
//            TAG,
//
//            "Buffer2 Hold = 5 Seconds"
//
//        )
//
//
//
//        Log.d(
//
//            TAG,
//
//            "========================================"
//
//        )
//
//    }
//
//
//
//    // =====================================================
//
//    // Enable / Disable
//
//    // =====================================================
//
//
//
//    fun setEnabled(enable: Boolean) {
//
//        val changed =
//
//            enabled != enable
//
//        enabled = enable
//
//        ecgFilter.setFilteringEnabled(enable)
//
//        if (!enable && changed) {
//
//
//
//            Log.d(
//
//                TAG,
//
//                "Edge Computing Disabled"
//
//            )
//
//
//
//            reset()
//
//        }
//
//
//
//        if (enable && changed) {
//
//
//
//            Log.d(
//
//                TAG,
//
//                "Edge Computing Enabled"
//
//            )
//
//        }
//
//    }
//
//
//
//    fun isEnabled(): Boolean {
//
//
//
//        return enabled
//
//    }
//
//
//    // =====================================================
//
//    // Reset
//
//    // =====================================================
//
//
//
//    fun reset() {
//        heartRateCalculator.reset()
//        Log.d(
//
//            TAG,
//
//            "Edge Processor Reset"
//
//        )
//
//    }
//
//
//private val bufferManager = BufferManager(
//
//    object : BufferManager.BufferListener {
//
//
//override fun onBufferReady(samples: IntArray, cycle: Int) {
//
//    val start = System.nanoTime()
//
//    val totalStart = System.nanoTime()
//
//    Log.d(TAG, "Buffer Ready : Cycle=$cycle")
//
//    // Process on background thread to avoid blocking callback
//    Thread {
//        try {
//            val result = offlineProcessor.process(samples)
//
//            val end = System.nanoTime()
//
//            Log.d(
//                TAG,
//                "OfflineProcessor Time = %.2f ms"
//                    .format((end - start) / 1_000_000.0)
//            )
//
//            // Update UI on main thread
//            handler.post {
//
//                _paIntervalLive.value =
//                    (result["PA"] as? Number)?.toInt()
//
//                _ahIntervalLive.value =
//                    (result["AH"] as? Number)?.toInt()
//
//                _hvIntervalLive.value =
//                    (result["HV"] as? Number)?.toInt()
//
//                _prIntervalLive.value =
//                    (result["PR"] as? Number)?.toInt()
//
//                _qrsDurationLive.value =
//                    (result["QRS"] as? Number)?.toInt()
//
//                _snrLive.value =
//                    (result["SNR"] as? Number)?.toDouble()
//
//                _lastUpdatedLive.value =
//                    java.text.SimpleDateFormat(
//                        "HH:mm:ss",
//                        java.util.Locale.getDefault()
//                    ).format(java.util.Date())
//
//                val totalEnd = System.nanoTime()
//
//                Log.d(
//                    TAG,
//                    "TOTAL PIPELINE TIME = %.2f ms".format(
//                        (totalEnd-totalStart)/1_000_000.0
//                    )
//                )
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error processing buffer", e)
//        }
//    }.start()
//}
//        override fun onLog(message: String) {
//
//
//
//            Log.d("BufferManager", message)
//
//        }
//
//    }
//
//)
//
//
//    fun processRawData(raw: ByteArray) {
//
//        if (!enabled) {
//
//            return
//        }
//        if (raw.size != 24000) {
//
//            Log.w(
//
//                TAG,
//
//                "Invalid packet size = ${raw.size}"
//
//            )
//            return
//
//        }
//
//
//        // Read Lead4 Samples
//
//        for (i in 0 until 1000) {
//            val base = i * 24
//            val lead4Offset = base + 9
//            val lead4 =
//
//                read24(
//
//                    raw,
//
//                    lead4Offset
//
//                )
//
//            // Buffer1
//            bufferManager.addSample(lead4)
//
//        }
//
//
//
//
//
//
//
//        val sampled =
//
//            ByteArray(2400)
//
//        for (i in 0 until 100) {
//
//            raw.copyInto(
//                sampled,
//                i * 24,
//                i * 240,
//                i * 240 + 24
//
//            )
//
//        }
//
//        ecgFilter.applyFiltering(
//            sampled,
//            ::read24,
//            ::write24
//
//        )
//
//        // Heart Rate
//        heartRateCalculator.updateHeartRateWindow(
//            sampled,
//            ::read24
//
//        )
//        heartRateCalculator.calculateHeartRateFromWindow()
//        /*
//
//        if (totalSamples == MAX_SAMPLES) {
//
//
//
//            analyzeLead4()
//
//
//
//        }
//
//        */
//
//    }
//
//
//
//// Read Signed 24-bit Integer
//
//
//    private fun read24(
//
//        buffer: ByteArray,
//
//        offset: Int
//
//    ): Int {
//        var value =
//
//            (buffer[offset].toInt() and 0xFF) or
//
//                    ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
//
//                    ((buffer[offset + 2].toInt() and 0xFF) shl 16)
//
//        if (value and 0x800000 != 0) {
//            value = value or -0x1000000
//
//        }
//
//        return value
//
//    }
//
//
//// Write Signed 24-bit Integer
//
//
//    private fun write24(
//
//        buffer: ByteArray,
//
//        offset: Int,
//
//        value: Int
//
//    ) {
//
//
//
//        val sample =
//
//            value.coerceIn(
//
//                -0x800000,
//
//                0x7FFFFF
//
//            )
//
//
//
//        buffer[offset] =
//
//            (sample and 0xFF).toByte()
//
//
//
//        buffer[offset + 1] =
//
//            ((sample shr 8) and 0xFF).toByte()
//
//
//
//        buffer[offset + 2] =
//
//            ((sample shr 16) and 0xFF).toByte()
//
//    }
//
//
//// Destroy
//
//    fun destroy() {
////        clearAllBuffers()
//        handler.removeCallbacksAndMessages(null)
//
//        heartRateCalculator.reset()
//        Log.d(
//
//            TAG,
//
//            "Edge Processor Destroyed"
//
//        )
//
//    }
//
//}
//


package com.carditek.kesar.util.filters.edgecomputing

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.module.Patient
import java.util.concurrent.atomic.AtomicBoolean

class EdgeComputingProcessor(
    private val context: Context,
    private val csvManager: CsvManager,
    private val patient: Patient,
    private val state: State
) {
    companion object {

        private const val TAG = "EdgeProcessor"
        private const val SAMPLE_RATE = 1000
        private const val MAX_SAMPLES = 15000
    }

    private val offlineProcessor = OfflineProcessor(context)
    // Application Context
    private val appContext = context.applicationContext

    // ECG Components
    private val ecgFilter = ECGFilter()
    private val heartRateCalculator =

        HeartRateCalculator()

    // Beat Number
    private val _beatNumberLive = MutableLiveData<Int>()
    val beatNumberLive: LiveData<Int> = _beatNumberLive

    // P Onset Location
    private val _pOnsetLocationLive = MutableLiveData<Int>()
    val pOnsetLocationLive: LiveData<Int> = _pOnsetLocationLive

    // P Peak Location
    private val _pPeakLocationLive = MutableLiveData<Int>()
    val pPeakLocationLive: LiveData<Int> = _pPeakLocationLive

    // QRS On Location
    private val _qrsOnLocationLive = MutableLiveData<Int>()
    val qrsOnLocationLive: LiveData<Int> = _qrsOnLocationLive

    // QRS Off Location
    private val _qrsOffLocationLive = MutableLiveData<Int>()
    val qrsOffLocationLive: LiveData<Int> = _qrsOffLocationLive

    // R Peak Location
    private val _rPeakLocationLive = MutableLiveData<Int>()
    val rPeakLocationLive: LiveData<Int> = _rPeakLocationLive

    // PR Duration
    private val _prDurationLive = MutableLiveData<Int>()
    val prDurationLive: LiveData<Int> = _prDurationLive

    // QRS Duration
    private val _qrsDurationLive = MutableLiveData<Int>()
    val qrsDurationLive: LiveData<Int> = _qrsDurationLive

    // PA Duration
    private val _paDurationLive = MutableLiveData<Int>()
    val paDurationLive: LiveData<Int> = _paDurationLive

    // AH Duration
    private val _ahDurationLive = MutableLiveData<Int>()
    val ahDurationLive: LiveData<Int> = _ahDurationLive

    // HV Duration
    private val _hvDurationLive = MutableLiveData<Int>()
    val hvDurationLive: LiveData<Int> = _hvDurationLive

    // H Amplitude
    private val _hAmplitudeLive = MutableLiveData<Double>()
    val hAmplitudeLive: LiveData<Double> = _hAmplitudeLive

    // Last Updated
    private val _lastUpdatedLive = MutableLiveData<String>()
    val lastUpdatedLive: LiveData<String> = _lastUpdatedLive

    val heartRateLive =

        heartRateCalculator.heartRateLive
    // Enable Flag
    private var enabled = false
    private var packetCounter = 0
    private var writeIndex = 0
    private var totalSamples = 0


    // Main Thread Handler
    private val handler =

        Handler(Looper.getMainLooper())


    // Initialization

    init {



        Log.d(

            TAG,

            "========================================"

        )



        Log.d(

            TAG,

            "EdgeComputingProcessor Initialized"

        )

        Log.d(

            TAG,

            "Buffer Size = $MAX_SAMPLES"

        )



        Log.d(

            TAG,

            "Sampling Rate = $SAMPLE_RATE Hz"

        )



        Log.d(

            TAG,

            "Buffer Window = 15 Seconds"

        )



        Log.d(

            TAG,

            "Buffer2 Hold = 5 Seconds"

        )



        Log.d(

            TAG,

            "========================================"

        )

    }



    // =====================================================

    // Enable / Disable

    // =====================================================



    fun setEnabled(enable: Boolean) {

        val changed =

            enabled != enable

        enabled = enable

        ecgFilter.setFilteringEnabled(enable)

        if (!enable && changed) {



            Log.d(

                TAG,

                "Edge Computing Disabled"

            )



            reset()

        }



        if (enable && changed) {



            Log.d(

                TAG,

                "Edge Computing Enabled"

            )

        }

    }



    fun isEnabled(): Boolean {



        return enabled

    }


    // =====================================================

    // Reset

    // =====================================================



//    fun reset() {  // block hor his bunlde ui clear add below
//        heartRateCalculator.reset()
//        offlineProcessor.resetBeatCounter()
//        Log.d(
//
//            TAG,
//
//            "Edge Processor Reset"
//
//        )
//
//    }
fun reset() {

    heartRateCalculator.reset()

    offlineProcessor.clearResults()

    handler.removeCallbacksAndMessages(null)

    handler.post {

        _beatNumberLive.value = 0
        _pOnsetLocationLive.value = 0
        _pPeakLocationLive.value = 0
        _qrsOnLocationLive.value = 0
        _qrsOffLocationLive.value = 0
        _rPeakLocationLive.value = 0

        _prDurationLive.value = 0
        _qrsDurationLive.value = 0
        _paDurationLive.value = 0
        _ahDurationLive.value = 0
        _hvDurationLive.value = 0
        _hAmplitudeLive.value = 0.0

        _lastUpdatedLive.value = ""

    }

    Log.d(TAG, "Edge Processor Reset")

}


    private val bufferManager = BufferManager(

        object : BufferManager.BufferListener {


            override fun onBufferReady(samples: IntArray, cycle: Int) {

                val start = System.nanoTime()

                val totalStart = System.nanoTime()

                Log.d(TAG, "Buffer Ready : Cycle=$cycle")

                // Process on background thread to avoid blocking callback
                // Use single thread executor to reduce battery drain
                Thread {
                    try {
                        val result = offlineProcessor.process(samples)

                        val end = System.nanoTime()

                        Log.d(
                            TAG,
                            "OfflineProcessor Time = %.2f ms"
                                .format((end - start) / 1_000_000.0)
                        )

                        // Save data to CSV (happens regardless of UI visibility)
                        val heartRate = heartRateCalculator.heartRateLive.value ?: 0
                        val patientName = patient.name.value ?: "--"
                        val macAddress = state.address.value ?: "--"

                        Log.d(TAG, "Saving CSV data - Patient: $patientName, MAC: $macAddress, HR: $heartRate")

                        csvManager.saveData(
                            patient = patientName,
                            macAddress = macAddress,
                            hr = if (heartRate in 30..220) "$heartRate BPM" else "-- BPM",
                            beatNumber = (result["beatNumber"] as? Number)?.toInt()?.toString() ?: "--",
                            pOnsetLocation = (result["pOnsetLocation"] as? Number)?.toInt()?.toString() ?: "--",
                            pPeakLocation = (result["pPeakLocation"] as? Number)?.toInt()?.toString() ?: "--",
                            qrsOnLocation = (result["qrsOnLocation"] as? Number)?.toInt()?.toString() ?: "--",
                            qrsOffLocation = (result["qrsOffLocation"] as? Number)?.toInt()?.toString() ?: "--",
                            rPeakLocation = (result["rPeakLocation"] as? Number)?.toInt()?.toString() ?: "--",
                            prDuration = (result["prDuration"] as? Number)?.toInt()?.toString() ?: "-- ms",
                            qrsDuration = (result["qrsDuration"] as? Number)?.toInt()?.toString() ?: "-- ms",
                            paDuration = (result["paDuration"] as? Number)?.toInt()?.toString() ?: "-- ms",
                            ahDuration = (result["ahDuration"] as? Number)?.toInt()?.toString() ?: "-- ms",
                            hvDuration = (result["hvDuration"] as? Number)?.toInt()?.toString() ?: "-- ms",
                            hAmplitude = (result["hAmplitude"] as? Number)?.toDouble() ?: 0.0
                        )

                        // Update UI on main thread
                        handler.post {

                            _beatNumberLive.value =
                                (result["beatNumber"] as? Number)?.toInt()

                            _pOnsetLocationLive.value =
                                (result["pOnsetLocation"] as? Number)?.toInt()

                            _pPeakLocationLive.value =
                                (result["pPeakLocation"] as? Number)?.toInt()

                            _qrsOnLocationLive.value =
                                (result["qrsOnLocation"] as? Number)?.toInt()

                            _qrsOffLocationLive.value =
                                (result["qrsOffLocation"] as? Number)?.toInt()

                            _rPeakLocationLive.value =
                                (result["rPeakLocation"] as? Number)?.toInt()

                            _prDurationLive.value =
                                (result["prDuration"] as? Number)?.toInt()

                            _qrsDurationLive.value =
                                (result["qrsDuration"] as? Number)?.toInt()

                            _paDurationLive.value =
                                (result["paDuration"] as? Number)?.toInt()
                            _ahDurationLive.value =
                                (result["ahDuration"] as? Number)?.toInt()
                            _hvDurationLive.value =
                                (result["hvDuration"] as? Number)?.toInt()
                            _hAmplitudeLive.value =
                                (result["hAmplitude"] as? Number)?.toDouble()?:0.0

                            _lastUpdatedLive.value =
                                java.text.SimpleDateFormat(
                                    "HH:mm:ss",
                                    java.util.Locale.getDefault()
                                ).format(java.util.Date())

                            val totalEnd = System.nanoTime()

                            Log.d(
                                TAG,
                                "TOTAL PIPELINE TIME = %.2f ms".format(
                                    (totalEnd-totalStart)/1_000_000.0
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing buffer", e)
                    }
                }.start()
            }
            override fun onLog(message: String) {



                Log.d("BufferManager", message)

            }

        }

    )


    fun processRawData(raw: ByteArray) {

        if (!enabled) {

            return
        }
        if (raw.size != 24000) {

            Log.w(

                TAG,

                "Invalid packet size = ${raw.size}"

            )
            return

        }


        // Read Lead4 Samples

        for (i in 0 until 1000) {
            val base = i * 24
           // val lead4Offset = base + 9
//            val lead4Offset = base + 15
            val lead4Offset = base + 3
            val lead4 =

                read24(

                    raw,

                    lead4Offset

                )

            // Buffer1
            bufferManager.addSample(lead4)

        }







        val sampled =

            ByteArray(2400)

        for (i in 0 until 100) {

            raw.copyInto(
                sampled,
                i * 24,
                i * 240,
                i * 240 + 24

            )

        }

        ecgFilter.applyFiltering(
            sampled,
            ::read24,
            ::write24

        )

        // Heart Rate
        heartRateCalculator.updateHeartRateWindow(
            sampled,
            ::read24

        )
        heartRateCalculator.calculateHeartRateFromWindow()
        /*

        if (totalSamples == MAX_SAMPLES) {



            analyzeLead4()



        }

        */

    }



// Read Signed 24-bit Integer


    private fun read24(

        buffer: ByteArray,

        offset: Int

    ): Int {
        var value =

            (buffer[offset].toInt() and 0xFF) or

                    ((buffer[offset + 1].toInt() and 0xFF) shl 8) or

                    ((buffer[offset + 2].toInt() and 0xFF) shl 16)

        if (value and 0x800000 != 0) {
            value = value or -0x1000000

        }

        return value

    }


// Write Signed 24-bit Integer


    private fun write24(

        buffer: ByteArray,

        offset: Int,

        value: Int

    ) {



        val sample =

            value.coerceIn(

                -0x800000,

                0x7FFFFF

            )



        buffer[offset] =

            (sample and 0xFF).toByte()



        buffer[offset + 1] =

            ((sample shr 8) and 0xFF).toByte()



        buffer[offset + 2] =

            ((sample shr 16) and 0xFF).toByte()

    }


// Destroy

    fun destroy() {
//        clearAllBuffers()
        handler.removeCallbacksAndMessages(null)

        heartRateCalculator.reset()
        Log.d(

            TAG,

            "Edge Processor Destroyed"

        )

    }

}



//
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.content.Context
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.carditek.kesar.module.Patient
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
///**
// * App-scoped (Hilt @Singleton) ECG processing pipeline.
// *
// * IMPORTANT: This class now owns CSV persistence directly (via CsvManager),
// * instead of exposing lastUpdatedLive for a Fragment to observe and save.
// * That is what previously caused rows to stop being written whenever the
// * His Bundle screen was not on screen: LiveData observers registered with
// * viewLifecycleOwner are automatically removed on onDestroyView(), so the
// * save call was silently dropped when the user navigated away.
// *
// * Because saving now happens inside this singleton's own background
// * callback (onBufferReady), it runs for as long as the app process is
// * alive and edge computing is enabled — regardless of which page the
// * user currently has open.
// */
//class EdgeComputingProcessor(
//    private val context: Context,
//    private val patient: Patient,
//    private val csvManager: CsvManager
//) {
//
//    companion object {
//        private const val TAG = "EdgeProcessor"
//        private const val SAMPLE_RATE = 1000
//        private const val MAX_SAMPLES = 15000
//    }
//
//    private val offlineProcessor = OfflineProcessor(context)
//
//    // Application Context
//    private val appContext = context.applicationContext
//
//    // ECG Components
//    private val ecgFilter = ECGFilter()
//    private val heartRateCalculator = HeartRateCalculator()
//
//    // -------------------- LiveData (UI display only) --------------------
//
//    private val _beatNumberLive = MutableLiveData<Int>()
//    val beatNumberLive: LiveData<Int> = _beatNumberLive
//
//    private val _pOnsetLocationLive = MutableLiveData<Int>()
//    val pOnsetLocationLive: LiveData<Int> = _pOnsetLocationLive
//
//    private val _pPeakLocationLive = MutableLiveData<Int>()
//    val pPeakLocationLive: LiveData<Int> = _pPeakLocationLive
//
//    private val _qrsOnLocationLive = MutableLiveData<Int>()
//    val qrsOnLocationLive: LiveData<Int> = _qrsOnLocationLive
//
//    private val _qrsOffLocationLive = MutableLiveData<Int>()
//    val qrsOffLocationLive: LiveData<Int> = _qrsOffLocationLive
//
//    private val _rPeakLocationLive = MutableLiveData<Int>()
//    val rPeakLocationLive: LiveData<Int> = _rPeakLocationLive
//
//    private val _prDurationLive = MutableLiveData<Int>()
//    val prDurationLive: LiveData<Int> = _prDurationLive
//
//    private val _qrsDurationLive = MutableLiveData<Int>()
//    val qrsDurationLive: LiveData<Int> = _qrsDurationLive
//
//    private val _paDurationLive = MutableLiveData<Int>()
//    val paDurationLive: LiveData<Int> = _paDurationLive
//
//    private val _ahDurationLive = MutableLiveData<Int>()
//    val ahDurationLive: LiveData<Int> = _ahDurationLive
//
//    private val _hvDurationLive = MutableLiveData<Int>()
//    val hvDurationLive: LiveData<Int> = _hvDurationLive
//
//    private val _hAmplitudeLive = MutableLiveData<Double>()
//    val hAmplitudeLive: LiveData<Double> = _hAmplitudeLive
//
//    // Kept for UI display of "last updated" timestamp only.
//    // No longer used to trigger CSV saves.
//    private val _lastUpdatedLive = MutableLiveData<String>()
//    val lastUpdatedLive: LiveData<String> = _lastUpdatedLive
//
//    val heartRateLive = heartRateCalculator.heartRateLive
//
//    // -------------------- State --------------------
//
//    private var enabled = false
//
//    private val handler = Handler(Looper.getMainLooper())
//
//    init {
//        Log.d(TAG, "========================================")
//        Log.d(TAG, "EdgeComputingProcessor Initialized")
//        Log.d(TAG, "Buffer Size = $MAX_SAMPLES")
//        Log.d(TAG, "Sampling Rate = $SAMPLE_RATE Hz")
//        Log.d(TAG, "Buffer Window = 15 Seconds")
//        Log.d(TAG, "========================================")
//    }
//
//    // =====================================================
//    // Enable / Disable
//    // =====================================================
//
//    fun setEnabled(enable: Boolean) {
//        val changed = enabled != enable
//        enabled = enable
//        ecgFilter.setFilteringEnabled(enable)
//
//        if (!enable && changed) {
//            Log.d(TAG, "Edge Computing Disabled")
//            reset()
//        }
//
//        if (enable && changed) {
//            Log.d(TAG, "Edge Computing Enabled")
//        }
//    }
//
//    fun isEnabled(): Boolean = enabled
//
//    // =====================================================
//    // Reset
//    // =====================================================
//
//    fun reset() {
//        heartRateCalculator.reset()
//        offlineProcessor.clearResults()
//
//        handler.post {
//            _beatNumberLive.value = 0
//            _pOnsetLocationLive.value = 0
//            _pPeakLocationLive.value = 0
//            _qrsOnLocationLive.value = 0
//            _qrsOffLocationLive.value = 0
//            _rPeakLocationLive.value = 0
//
//            _prDurationLive.value = 0
//            _qrsDurationLive.value = 0
//            _paDurationLive.value = 0
//            _ahDurationLive.value = 0
//            _hvDurationLive.value = 0
//            _hAmplitudeLive.value = 0.0
//
//            _lastUpdatedLive.value = ""
//        }
//
//        Log.d(TAG, "Edge Processor Reset")
//    }
//
//    // =====================================================
//    // Buffer Manager Callback
//    // =====================================================
//
//    private val bufferManager = BufferManager(
//        object : BufferManager.BufferListener {
//
//            override fun onBufferReady(samples: IntArray, cycle: Int) {
//
//                val start = System.nanoTime()
//
//                Log.d(TAG, "Buffer Ready : Cycle=$cycle")
//
//                // Process on background thread to avoid blocking callback
//                Thread {
//                    try {
//                        val result = offlineProcessor.process(samples)
//
//                        val end = System.nanoTime()
//                        Log.d(
//                            TAG,
//                            "OfflineProcessor Time = %.2f ms".format((end - start) / 1_000_000.0)
//                        )
//
//                        val beatNumber = (result["beatNumber"] as? Number)?.toInt() ?: 0
//                        val pOnsetLocation = (result["pOnsetLocation"] as? Number)?.toInt() ?: 0
//                        val pPeakLocation = (result["pPeakLocation"] as? Number)?.toInt() ?: 0
//                        val qrsOnLocation = (result["qrsOnLocation"] as? Number)?.toInt() ?: 0
//                        val qrsOffLocation = (result["qrsOffLocation"] as? Number)?.toInt() ?: 0
//                        val rPeakLocation = (result["rPeakLocation"] as? Number)?.toInt() ?: 0
//                        val prDuration = (result["prDuration"] as? Number)?.toInt() ?: 0
//                        val qrsDuration = (result["qrsDuration"] as? Number)?.toInt() ?: 0
//                        val paDuration = (result["paDuration"] as? Number)?.toInt() ?: 0
//                        val ahDuration = (result["ahDuration"] as? Number)?.toInt() ?: 0
//                        val hvDuration = (result["hvDuration"] as? Number)?.toInt() ?: 0
//                        val hAmplitude = (result["hAmplitude"] as? Number)?.toDouble() ?: 0.0
//                        val hrValue = heartRateLive.value ?: 0
//                        val timestamp = SimpleDateFormat(
//                            "HH:mm:ss",
//                            Locale.getDefault()
//                        ).format(Date())
//
//                        // ---- Update UI-facing LiveData on main thread ----
//                        handler.post {
//                            _beatNumberLive.value = beatNumber
//                            _pOnsetLocationLive.value = pOnsetLocation
//                            _pPeakLocationLive.value = pPeakLocation
//                            _qrsOnLocationLive.value = qrsOnLocation
//                            _qrsOffLocationLive.value = qrsOffLocation
//                            _rPeakLocationLive.value = rPeakLocation
//                            _prDurationLive.value = prDuration
//                            _qrsDurationLive.value = qrsDuration
//                            _paDurationLive.value = paDuration
//                            _ahDurationLive.value = ahDuration
//                            _hvDurationLive.value = hvDuration
//                            _hAmplitudeLive.value = hAmplitude
//                            _lastUpdatedLive.value = timestamp
//                        }
//
//                        // ---- Persist to CSV regardless of visible screen ----
//                        // This runs unconditionally on this background thread,
//                        // so it does NOT depend on any Fragment being alive.
//                        saveToCsv(
//                            beatNumber = beatNumber,
//                            pOnsetLocation = pOnsetLocation,
//                            pPeakLocation = pPeakLocation,
//                            qrsOnLocation = qrsOnLocation,
//                            qrsOffLocation = qrsOffLocation,
//                            rPeakLocation = rPeakLocation,
//                            prDuration = prDuration,
//                            qrsDuration = qrsDuration,
//                            paDuration = paDuration,
//                            ahDuration = ahDuration,
//                            hvDuration = hvDuration,
//                            hAmplitude = hAmplitude,
//                            hr = hrValue
//                        )
//
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Error processing buffer", e)
//                    }
//                }.start()
//            }
//
//            override fun onLog(message: String) {
//                Log.d("BufferManager", message)
//            }
//        }
//    )
//
//    // =====================================================
//    // CSV Persistence (app-scoped, independent of UI)
//    // =====================================================
//
//    private fun saveToCsv(
//        beatNumber: Int,
//        pOnsetLocation: Int,
//        pPeakLocation: Int,
//        qrsOnLocation: Int,
//        qrsOffLocation: Int,
//        rPeakLocation: Int,
//        prDuration: Int,
//        qrsDuration: Int,
//        paDuration: Int,
//        ahDuration: Int,
//        hvDuration: Int,
//        hAmplitude: Double,
//        hr: Int
//    ) {
//        try {
//            csvManager.saveData(
//                patient = formatPatientName(patient.name.value),
//                hr = if (hr in 30..220) "$hr BPM" else "-- BPM",
//                beatNumber = if (beatNumber == 0) "--" else "$beatNumber",
//                pOnsetLocation = if (pOnsetLocation == 0) "--" else "$pOnsetLocation",
//                pPeakLocation = if (pPeakLocation == 0) "--" else "$pPeakLocation",
//                qrsOnLocation = if (qrsOnLocation == 0) "--" else "$qrsOnLocation",
//                qrsOffLocation = if (qrsOffLocation == 0) "--" else "$qrsOffLocation",
//                rPeakLocation = if (rPeakLocation == 0) "--" else "$rPeakLocation",
//                prDuration = if (prDuration == 0) "-- ms" else "$prDuration ms",
//                qrsDuration = if (qrsDuration == 0) "-- ms" else "$qrsDuration ms",
//                paDuration = if (paDuration == 0) "-- ms" else "$paDuration ms",
//                ahDuration = if (ahDuration == 0) "-- ms" else "$ahDuration ms",
//                hvDuration = if (hvDuration == 0) "-- ms" else "$hvDuration ms",
//                hAmplitude = hAmplitude
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saving CSV row", e)
//        }
//    }
//
//    /**
//     * Mirrors the display-name trimming logic previously done in
//     * HisbundleFragment (drops the first token / trailing token,
//     * e.g. an ID prefix and age/sex suffix, keeping the middle name parts).
//     */
//    private fun formatPatientName(raw: String?): String {
//        if (raw.isNullOrBlank()) return "--"
//
//        val parts = raw.trim().split("\\s+".toRegex())
//
//        return if (parts.size >= 3) {
//            parts.subList(1, parts.size - 1).joinToString(" ")
//        } else {
//            raw
//        }
//    }
//
//    // =====================================================
//    // Raw Data Ingestion
//    // =====================================================
//
//    fun processRawData(raw: ByteArray) {
//
//        if (!enabled) return
//
//        if (raw.size != 24000) {
//            Log.w(TAG, "Invalid packet size = ${raw.size}")
//            return
//        }
//
//        // Feed Lead4 samples into the beat buffer
//        for (i in 0 until 1000) {
//            val base = i * 24
//            val lead4Offset = base + 9
//            val lead4 = read24(raw, lead4Offset)
//            bufferManager.addSample(lead4)
//        }
//
//        val sampled = ByteArray(2400)
//
//        for (i in 0 until 100) {
//            raw.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
//        }
//
//        ecgFilter.applyFiltering(sampled, ::read24, ::write24)
//
//        heartRateCalculator.updateHeartRateWindow(sampled, ::read24)
//        heartRateCalculator.calculateHeartRateFromWindow()
//    }
//
//    // Read Signed 24-bit Integer
//    private fun read24(buffer: ByteArray, offset: Int): Int {
//        var value =
//            (buffer[offset].toInt() and 0xFF) or
//                    ((buffer[offset + 1].toInt() and 0xFF) shl 8) or
//                    ((buffer[offset + 2].toInt() and 0xFF) shl 16)
//
//        if (value and 0x800000 != 0) {
//            value = value or -0x1000000
//        }
//
//        return value
//    }
//
//    // Write Signed 24-bit Integer
//    private fun write24(buffer: ByteArray, offset: Int, value: Int) {
//        val sample = value.coerceIn(-0x800000, 0x7FFFFF)
//
//        buffer[offset] = (sample and 0xFF).toByte()
//        buffer[offset + 1] = ((sample shr 8) and 0xFF).toByte()
//        buffer[offset + 2] = ((sample shr 16) and 0xFF).toByte()
//    }
//
//    // =====================================================
//    // Destroy
//    // =====================================================
//
//    fun destroy() {
//        handler.removeCallbacksAndMessages(null)
//        heartRateCalculator.reset()
//        Log.d(TAG, "Edge Processor Destroyed")
//    }
//}
