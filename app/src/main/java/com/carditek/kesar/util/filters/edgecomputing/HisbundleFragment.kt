//
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.carditek.kesar.Cache
//import com.carditek.kesar.R
//import com.carditek.kesar.module.Patient
//import com.google.android.material.card.MaterialCardView
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import android.graphics.Bitmap
//
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class HisbundleFragment : Fragment(R.layout.hisbundle) {
//
//    @Inject
//    lateinit var patient: Patient
//
//    @Inject
//    lateinit var appCache: Cache
//
//    // -------------------- UI --------------------
//
//    private lateinit var tvPatientName: TextView
//    private lateinit var tvHR: TextView
//
//    private lateinit var tvBeatNumber: TextView
//    private lateinit var tvPOnsetLocation: TextView
//    private lateinit var tvPPeakLocation: TextView
//    private lateinit var tvQRSOnLocation: TextView
//    private lateinit var tvQRSOffLocation: TextView
//    private lateinit var tvRPeakLocation: TextView
//    private lateinit var tvPRDuration: TextView
//    private lateinit var tvQRSDuration: TextView
//    private lateinit var tvPADuration: TextView
//    private lateinit var tvAHDuration: TextView
//    private lateinit var tvHVDuration: TextView
//
//    private lateinit var imgHisBundle: ImageView
//    //private lateinit var imgHisBundle: com.github.chrisbanes.photoview.PhotoView
//
//    // -------------------- Measurement Cards --------------------
//
//    private lateinit var cardBeatNumber: MaterialCardView
//    private lateinit var cardPOnset: MaterialCardView
//    private lateinit var cardPPeak: MaterialCardView
//    private lateinit var cardQRSOn: MaterialCardView
//    private lateinit var cardQRSOff: MaterialCardView
//    private lateinit var cardRPeak: MaterialCardView
//    private lateinit var cardPRDuration: MaterialCardView
//    private lateinit var cardQRSDuration: MaterialCardView
//    private lateinit var cardPADuration: MaterialCardView
//    private lateinit var cardAHDuration: MaterialCardView
//    private lateinit var cardHVDuration: MaterialCardView
//
//    // -------------------- Range Colors --------------------
//
//    private val colorNormal = Color.parseColor("#4CAF50")
//    private val colorAbnormal = Color.parseColor("#F44336")
//
//    // -------------------- Bitmap Cache --------------------
//
//    private var lastGraphPath: String? = null
//    private var lastModifiedTime: Long = -1L
//
//    // -------------------- CSV --------------------
//
//    private lateinit var csvManager: CsvManager
//
//
//    private var lastPatientName: String = "--"
//    private var lastHR: String = "-- BPM"
//
//    private var lastBeatNumber: String = "--"
//    private var lastPOnsetLocation: String = "--"
//    private var lastPPeakLocation: String = "--"
//    private var lastQRSOnLocation: String = "--"
//    private var lastQRSOffLocation: String = "--"
//    private var lastRPeakLocation: String = "--"
//    private var lastPRDuration: String = "-- ms"
//    private var lastQRSDuration: String = "-- ms"
//    private var lastPADuration: String = "-- ms"
//    private var lastAHDuration: String = "-- ms"
//    private var lastHVDuration: String = "-- ms"
//
//    override fun onViewCreated(
//        view: View,
//        savedInstanceState: Bundle?
//    ) {
//        csvManager = CsvManager(requireContext())
//
//        super.onViewCreated(view, savedInstanceState)
//
//        //-------------------- Find Views --------------------
//
//        tvPatientName = view.findViewById(R.id.tvPatientName)
//        tvHR = view.findViewById(R.id.tvHR)
//
//        tvBeatNumber = view.findViewById(R.id.tvBeatNumber)
//        tvPOnsetLocation = view.findViewById(R.id.tvPOnsetLocation)
//        tvPPeakLocation = view.findViewById(R.id.tvPPeakLocation)
//        tvQRSOnLocation = view.findViewById(R.id.tvQRSOnLocation)
//        tvQRSOffLocation = view.findViewById(R.id.tvQRSOffLocation)
//        tvRPeakLocation = view.findViewById(R.id.tvRPeakLocation)
//        tvPRDuration = view.findViewById(R.id.tvPRDuration)
//        tvQRSDuration = view.findViewById(R.id.tvQRSDuration)
//        tvPADuration = view.findViewById(R.id.tvPADuration)
//        tvAHDuration = view.findViewById(R.id.tvAHDuration)
//        tvHVDuration = view.findViewById(R.id.tvHVDuration)
//
//        imgHisBundle = view.findViewById(R.id.imgHisBundle)
//
//        cardBeatNumber = view.findViewById(R.id.cardBeatNumber)
//        cardPOnset = view.findViewById(R.id.cardPOnset)
//        cardPPeak = view.findViewById(R.id.cardPPeak)
//        cardQRSOn = view.findViewById(R.id.cardQRSOn)
//        cardQRSOff = view.findViewById(R.id.cardQRSOff)
//        cardRPeak = view.findViewById(R.id.cardRPeak)
//        cardPRDuration = view.findViewById(R.id.cardPRDuration)
//        cardQRSDuration = view.findViewById(R.id.cardQRSDuration)
//        cardPADuration = view.findViewById(R.id.cardPADuration)
//        cardAHDuration = view.findViewById(R.id.cardAHDuration)
//        cardHVDuration = view.findViewById(R.id.cardHVDuration)
//
//
//
//        patient.name.observe(viewLifecycleOwner) { patientData ->
//
//            val displayName = if (patientData.isNullOrBlank()) {
//                "--"
//            } else {
//
//                val parts = patientData
//                    .trim()
//                    .split("\\s+".toRegex())
//
//                if (parts.size >= 3) {
//                    parts
//                        .subList(1, parts.size - 1)
//                        .joinToString(" ")
//                } else {
//                    patientData
//                }
//            }
//
//            tvPatientName.text = displayName
//            lastPatientName = displayName
//        }
//
//        //-------------------- Heart Rate --------------------
//        // NOTE: This fires roughly once per second (whenever a new
//        // packet updates the HR window). It only updates the on-screen
//        // text now. It must NOT call saveSnapshot() — that was the
//        // cause of a new CSV row every second.
//
//        appCache.heartRateLive.observe(viewLifecycleOwner) { hr ->
//
//            val hrText = if (hr in 30..220) "$hr BPM" else "-- BPM"
//
//            tvHR.text = hrText
//            lastHR = hrText
//        }
//
//        //-------------------- Beat Number --------------------
//
//        appCache.beatNumberLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvBeatNumber.text = text
//            lastBeatNumber = text
//        }
//
//        //-------------------- P Onset Location --------------------
//
//        appCache.pOnsetLocationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvPOnsetLocation.text = text
//            lastPOnsetLocation = text
//        }
//
//        //-------------------- P Peak Location --------------------
//
//        appCache.pPeakLocationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvPPeakLocation.text = text
//            lastPPeakLocation = text
//        }
//
//        //-------------------- QRS On Location --------------------
//
//        appCache.qrsOnLocationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvQRSOnLocation.text = text
//            lastQRSOnLocation = text
//        }
//
//        //-------------------- QRS Off Location --------------------
//
//        appCache.qrsOffLocationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvQRSOffLocation.text = text
//            lastQRSOffLocation = text
//        }
//
//
//        //-------------------- R Peak Location --------------------
//
//        appCache.rPeakLocationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value"
//            tvRPeakLocation.text = text
//            lastRPeakLocation = text
//        }
//
//        //-------------------- PR Duration --------------------
//
//        appCache.prDurationLive.observe(viewLifecycleOwner) { value ->
//
//            val start = System.nanoTime()
//
//            val text = "$value ms"
//            tvPRDuration.text = text
//            lastPRDuration = text
//
//            applyRangeColor(cardPRDuration, tvPRDuration, value.toDouble(), 120.0, 200.0)
//
//            val end = System.nanoTime()
//
//            Log.d(
//                "UI_LATENCY",
//                "PR Duration UI Update = %.3f ms".format(
//                    (end - start) / 1_000_000.0
//                )
//            )
//        }
//
//        //-------------------- QRS Duration --------------------
//
//        appCache.qrsDurationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value ms"
//            tvQRSDuration.text = text
//            lastQRSDuration = text
//
//            applyRangeColor(cardQRSDuration, tvQRSDuration, value.toDouble(), 60.0, 110.0)
//        }
//
//        //-------------------- PA Duration --------------------
//
//        appCache.paDurationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value ms"
//            tvPADuration.text = text
//            lastPADuration = text
//
//            applyRangeColor(cardPADuration, tvPADuration, value.toDouble(), 25.0, 65.0)
//        }
//
//        //-------------------- AH Duration --------------------
//
//        appCache.ahDurationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value ms"
//            tvAHDuration.text = text
//            lastAHDuration = text
//
//            applyRangeColor(cardAHDuration, tvAHDuration, value.toDouble(), 55.0, 125.0)
//        }
//
//        //-------------------- HV Duration --------------------
//
//        appCache.hvDurationLive.observe(viewLifecycleOwner) { value ->
//
//            val text = "$value ms"
//            tvHVDuration.text = text
//            lastHVDuration = text
//
//            applyRangeColor(cardHVDuration, tvHVDuration, value.toDouble(), 35.0, 55.0)
//        }
//
//        //-------------------- Graph --------------------
//
//        HisBundleData.graphPathLive.observe(viewLifecycleOwner) { path ->
//
//            loadBitmap(path)
//        }
//
//        //-------------------- Initial State --------------------
//
//        loadBitmap(HisBundleData.graphPath)
//
//
//        appCache.lastUpdatedLive.observe(viewLifecycleOwner) {
//
//            saveSnapshot()
//        }
//
//        appCache.bleConnectedLive.observe(viewLifecycleOwner) { connected ->
//
//            if (!connected) {
//
//                resetScreen()
//
//            }
//
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Save a full snapshot of the latest known values to CSV
//    // ------------------------------------------------------------
//
//    private fun saveSnapshot() {
//
//        csvManager.saveData(
//            patient = lastPatientName,
//            hr = lastHR,
//            beatNumber = lastBeatNumber,
//            pOnsetLocation = lastPOnsetLocation,
//            pPeakLocation = lastPPeakLocation,
//            qrsOnLocation = lastQRSOnLocation,
//            qrsOffLocation = lastQRSOffLocation,
//            rPeakLocation = lastRPeakLocation,
//            prDuration = lastPRDuration,
//            qrsDuration = lastQRSDuration,
//            paDuration = lastPADuration,
//            ahDuration = lastAHDuration,
//            hvDuration = lastHVDuration
//        )
//    }
//
//    // ------------------------------------------------------------
//    // Apply Red/Green Color Based On Normal Range
//    // ------------------------------------------------------------
//
//    private fun applyRangeColor(
//        card: MaterialCardView,
//        textView: TextView,
//        value: Double,
//        min: Double,
//        max: Double
//    ) {
//
//        val inRange = value in min..max
//
//        val color = if (inRange) colorNormal else colorAbnormal
//
//        card.strokeColor = color
//        textView.setTextColor(color)
//    }
//
//    // ------------------------------------------------------------
//    // Load His Bundle Graph
//    // ------------------------------------------------------------
//
//    private fun loadBitmap(path: String) {
//
//        if (path.isBlank()) {
//            clearImage()
//            return
//        }
//
//        val file = File(path)
//
//        if (!file.exists()) {
//
//            Log.w(
//                "HIS_FRAGMENT",
//                "Graph file not found : $path"
//            )
//
//            clearImage()
//            return
//        }
//
//        val modified = file.lastModified()
//
//        // Skip loading only if the file is exactly the same
//        if (path == lastGraphPath &&
//            modified == lastModifiedTime
//        ) {
//            return
//        }
//
//        lastGraphPath = path
//        lastModifiedTime = modified
//
//        lifecycleScope.launch {
//
//            try {
//
//                val bitmap = withContext(Dispatchers.IO) {
//
//                    val options = BitmapFactory.Options().apply {
//
//                        // Helps reduce memory usage for large images
//                        inPreferredConfig = Bitmap.Config.RGB_565
//                    }
//
//                    BitmapFactory.decodeFile(
//                        path,
//                        options
//                    )
//                }
//
//                if (!isAdded) return@launch
//
//                withContext(Dispatchers.Main) {
//
//                    if (bitmap != null) {
//
//                        imgHisBundle.setImageBitmap(bitmap)
//
//                        Log.d(
//                            "HIS_FRAGMENT",
//                            "Graph Loaded Successfully"
//                        )
//
//                    } else {
//
//                        Log.e(
//                            "HIS_FRAGMENT",
//                            "Bitmap decode failed"
//                        )
//
//                        clearImage()
//                    }
//                }
//
//            } catch (e: Exception) {
//
//                Log.e(
//                    "HIS_FRAGMENT",
//                    "Error Loading Bitmap",
//                    e
//                )
//
//                if (isAdded) {
//                    clearImage()
//                }
//            }
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Clear Image
//    // ------------------------------------------------------------
//
//    private fun clearImage() {
//
//        if (::imgHisBundle.isInitialized) {
//            imgHisBundle.setImageDrawable(null)
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Fragment Destroy
//    // ------------------------------------------------------------
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        clearImage()
//
//        lastGraphPath = null
//        lastModifiedTime = -1L
//
//
//    }
////        super.onDestroyView()
//        private fun resetCard(
//            card: MaterialCardView,
//            textView: TextView
//        ) {
//
//            card.strokeColor = Color.GRAY
//
//            textView.setTextColor(Color.GRAY)
//        }
//
//        private fun resetScreen() {
//
//            tvPatientName.text = "--"
//            tvHR.text = "-- BPM"
//
//            tvBeatNumber.text = "--"
//            tvPOnsetLocation.text = "--"
//            tvPPeakLocation.text = "--"
//            tvQRSOnLocation.text = "--"
//            tvQRSOffLocation.text = "--"
//            tvRPeakLocation.text = "--"
//
//            tvPRDuration.text = "-- ms"
//            tvQRSDuration.text = "-- ms"
//            tvPADuration.text = "-- ms"
//            tvAHDuration.text = "-- ms"
//            tvHVDuration.text = "-- ms"
//
//            clearImage()
//
//            resetCard(cardPRDuration, tvPRDuration)
//            resetCard(cardQRSDuration, tvQRSDuration)
//            resetCard(cardPADuration, tvPADuration)
//            resetCard(cardAHDuration, tvAHDuration)
//            resetCard(cardHVDuration, tvHVDuration)
//        }
//
//}





////working hisbundle
//
//
package com.carditek.kesar.util.filters.edgecomputing

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carditek.kesar.Cache
import com.carditek.kesar.R
import com.carditek.kesar.module.Patient
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.graphics.Bitmap
import com.carditek.kesar.bluetooth.State
import javax.inject.Inject
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import android.os.Environment
import android.os.Build
import android.provider.DocumentsContract
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import android.database.Cursor
import androidx.core.content.FileProvider



@AndroidEntryPoint
class HisbundleFragment : Fragment(R.layout.hisbundle) {

    @Inject
    lateinit var patient: Patient

    @Inject
    lateinit var appCache: Cache
    @Inject
    lateinit var state: State
    // -------------------- UI --------------------
    private lateinit var tvMacAddress: TextView
    private lateinit var tvPatientName: TextView
    private lateinit var tvHR: TextView

    private lateinit var tvBeatNumber: TextView
    private lateinit var tvPOnsetLocation: TextView
    private lateinit var tvPPeakLocation: TextView
    private lateinit var tvQRSOnLocation: TextView
    private lateinit var tvQRSOffLocation: TextView
    private lateinit var tvRPeakLocation: TextView
    private lateinit var tvPRDuration: TextView
    private lateinit var tvQRSDuration: TextView
    private lateinit var tvPADuration: TextView
    private lateinit var tvAHDuration: TextView
    private lateinit var tvHVDuration: TextView

    private lateinit var tvHAmplitude: TextView

    private lateinit var imgHisBundle: PhotoView

    // -------------------- Measurement Cards --------------------

    private lateinit var cardBeatNumber: MaterialCardView
    private lateinit var cardPOnset: MaterialCardView
    private lateinit var cardPPeak: MaterialCardView
    private lateinit var cardQRSOn: MaterialCardView
    private lateinit var cardQRSOff: MaterialCardView
    private lateinit var cardRPeak: MaterialCardView
    private lateinit var cardPRDuration: MaterialCardView
    private lateinit var cardQRSDuration: MaterialCardView
    private lateinit var cardPADuration: MaterialCardView
    private lateinit var cardAHDuration: MaterialCardView
    private lateinit var cardHVDuration: MaterialCardView

    private lateinit var cardHAmplitude: MaterialCardView

    // -------------------- Range Colors --------------------

    private val colorNormal = Color.parseColor("#4CAF50")
    private val colorAbnormal = Color.parseColor("#F44336")

    // -------------------- Bitmap Cache --------------------

    private var lastGraphPath: String? = null
    private var lastModifiedTime: Long = -1L

    // -------------------- CSV --------------------
    // CSV saving is now handled in EdgeComputingProcessor, not in Fragment
    // This ensures data is saved even when the HIS Bundle screen is not open

    private var lastPatientName: String = "--"
    private var lastHR: String = "-- BPM"

    private var lastBeatNumber: String = "--"
    private var lastPOnsetLocation: String = "--"
    private var lastPPeakLocation: String = "--"
    private var lastQRSOnLocation: String = "--"
    private var lastQRSOffLocation: String = "--"
    private var lastRPeakLocation: String = "--"
    private var lastPRDuration: String = "-- ms"
    private var lastQRSDuration: String = "-- ms"
    private var lastPADuration: String = "-- ms"
    private var lastAHDuration: String = "-- ms"
    private var lastHVDuration: String = "-- ms"

    private var lastHAmplitude: Double = 0.0

    // -------------------- SAF for CSV Download/Share --------------------
    private lateinit var saveCsvLauncher: ActivityResultLauncher<String>
    private var csvFileToShare: File? = null




    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        //-------------------- Setup SAF Launcher --------------------

        saveCsvLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("*/*")
        ) { uri: Uri? ->
            uri?.let {
                copyCsvToUri(it)
            }
        }

        //-------------------- Find Views --------------------

        tvPatientName = view.findViewById(R.id.tvPatientName)
        tvHR = view.findViewById(R.id.tvHR)
        tvMacAddress = view.findViewById(R.id.tvMacAddress)

        tvBeatNumber = view.findViewById(R.id.tvBeatNumber)
        tvPOnsetLocation = view.findViewById(R.id.tvPOnsetLocation)
        tvPPeakLocation = view.findViewById(R.id.tvPPeakLocation)
        tvQRSOnLocation = view.findViewById(R.id.tvQRSOnLocation)
        tvQRSOffLocation = view.findViewById(R.id.tvQRSOffLocation)
        tvRPeakLocation = view.findViewById(R.id.tvRPeakLocation)
        tvPRDuration = view.findViewById(R.id.tvPRDuration)
        tvQRSDuration = view.findViewById(R.id.tvQRSDuration)
        tvPADuration = view.findViewById(R.id.tvPADuration)
        tvAHDuration = view.findViewById(R.id.tvAHDuration)
        tvHVDuration = view.findViewById(R.id.tvHVDuration)
        tvHAmplitude = view.findViewById(R.id.tvHAmplitude)

        imgHisBundle = view.findViewById(R.id.imgHisBundle)

        cardBeatNumber = view.findViewById(R.id.cardBeatNumber)
        cardPOnset = view.findViewById(R.id.cardPOnset)
        cardPPeak = view.findViewById(R.id.cardPPeak)
        cardQRSOn = view.findViewById(R.id.cardQRSOn)
        cardQRSOff = view.findViewById(R.id.cardQRSOff)
        cardRPeak = view.findViewById(R.id.cardRPeak)
        cardPRDuration = view.findViewById(R.id.cardPRDuration)
        cardQRSDuration = view.findViewById(R.id.cardQRSDuration)
        cardPADuration = view.findViewById(R.id.cardPADuration)
        cardAHDuration = view.findViewById(R.id.cardAHDuration)
        cardHVDuration = view.findViewById(R.id.cardHVDuration)
        cardHAmplitude = view.findViewById(R.id.cardHAmplitude)

        //-------------------- Setup Download & Share Buttons --------------------

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDownloadCSV).setOnClickListener {
            openSaveCsvDialog()
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnShareCSV).setOnClickListener {
            shareCsvFile()
        }



        patient.name.observe(viewLifecycleOwner) { patientData ->

            val displayName = if (patientData.isNullOrBlank()) {
                "--"
            } else {

                val parts = patientData
                    .trim()
                    .split("\\s+".toRegex())

                if (parts.size >= 3) {
                    parts
                        .subList(1, parts.size - 1)
                        .joinToString(" ")
                } else {
                    patientData
                }
            }

            tvPatientName.text = displayName
            lastPatientName = displayName
        }
//-------------------- BLE MAC Address --------------------

        state.address.observe(viewLifecycleOwner) { macAddress ->

            tvMacAddress.text = if (macAddress.isNullOrBlank()) {
                "MACID: "
            } else {
                "MACID: $macAddress"
            }

        }




        //-------------------- Heart Rate --------------------
        // NOTE: This fires roughly once per second (whenever a new
        // packet updates the HR window). It only updates the on-screen
        // text now. It must NOT call saveSnapshot() — that was the
        // cause of a new CSV row every second.

        appCache.heartRateLive.observe(viewLifecycleOwner) { hr ->

            val hrText = if (hr in 30..220) "$hr BPM" else "-- BPM"

            tvHR.text = hrText
            lastHR = hrText
        }

        //-------------------- Beat Number --------------------

        appCache.beatNumberLive.observe(viewLifecycleOwner) { value ->

//            val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvBeatNumber.text = text
            lastBeatNumber = text
        }

        //-------------------- P Onset Location --------------------

        appCache.pOnsetLocationLive.observe(viewLifecycleOwner) { value ->

          //  val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvPOnsetLocation.text = text
            lastPOnsetLocation = text
        }

        //-------------------- P Peak Location --------------------

        appCache.pPeakLocationLive.observe(viewLifecycleOwner) { value ->

           // val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvPPeakLocation.text = text
            lastPPeakLocation = text
        }

        //-------------------- QRS On Location --------------------

        appCache.qrsOnLocationLive.observe(viewLifecycleOwner) { value ->

          //  val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvQRSOnLocation.text = text
            lastQRSOnLocation = text
        }

        //-------------------- QRS Off Location --------------------

        appCache.qrsOffLocationLive.observe(viewLifecycleOwner) { value ->

           // val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvQRSOffLocation.text = text
            lastQRSOffLocation = text
        }


        //-------------------- R Peak Location --------------------

        appCache.rPeakLocationLive.observe(viewLifecycleOwner) { value ->

           // val text = "$value"
            val text = if (value == 0) "--" else "$value"
            tvRPeakLocation.text = text
            lastRPeakLocation = text
        }

        //-------------------- PR Duration --------------------

        appCache.prDurationLive.observe(viewLifecycleOwner) { value ->

            val start = System.nanoTime()

//            val text = "$value ms"
            val text = if (value == 0) "-- ms" else "$value ms"
            tvPRDuration.text = text
            lastPRDuration = text

            applyRangeColor(cardPRDuration, tvPRDuration, value.toDouble(), 120.0, 200.0)

            val end = System.nanoTime()

            Log.d(
                "UI_LATENCY",
                "PR Duration UI Update = %.3f ms".format(
                    (end - start) / 1_000_000.0
                )
            )
        }

        //-------------------- QRS Duration --------------------

        appCache.qrsDurationLive.observe(viewLifecycleOwner) { value ->

//            val text = "$value ms"
            val text = if (value == 0) "-- ms" else "$value ms"
            tvQRSDuration.text = text
            lastQRSDuration = text

            applyRangeColor(cardQRSDuration, tvQRSDuration, value.toDouble(), 60.0, 110.0)
        }

        //-------------------- PA Duration --------------------

        appCache.paDurationLive.observe(viewLifecycleOwner) { value ->

            //val text = "$value ms"
            val text = if (value == 0) "-- ms" else "$value ms"
            tvPADuration.text = text
            lastPADuration = text

            applyRangeColor(cardPADuration, tvPADuration, value.toDouble(), 25.0, 65.0)
        }

        //-------------------- AH Duration --------------------

        appCache.ahDurationLive.observe(viewLifecycleOwner) { value ->

           // val text = "$value ms"
            val text = if (value == 0) "-- ms" else "$value ms"
            tvAHDuration.text = text
            lastAHDuration = text

            applyRangeColor(cardAHDuration, tvAHDuration, value.toDouble(), 55.0, 125.0)
        }

        //-------------------- HV Duration --------------------

        appCache.hvDurationLive.observe(viewLifecycleOwner) { value ->

           // val text = "$value ms"
            val text = if (value == 0) "-- ms" else "$value ms"
            tvHVDuration.text = text
            lastHVDuration = text

            applyRangeColor(cardHVDuration, tvHVDuration, value.toDouble(), 35.0, 55.0)
        }

        //-------------------- H Amplitude --------------------

//        appCache.hAmplitudeLive.observe(viewLifecycleOwner) { value ->
//
//            //  val text = "$value"
//            val text = if (value == 0) "--" else "$value"
//            tvHAmplitude.text = text
//            lastHAmplitude = text
//        }
        appCache.hAmplitudeLive.observe(viewLifecycleOwner) { value ->

            lastHAmplitude = value.toDouble()

            tvHAmplitude.text = if (value == 0.0) {
                "-- mV"
            } else {
                "${value} mV"
            }
        }

        //-------------------- Graph --------------------

        HisBundleData.graphPathLive.observe(viewLifecycleOwner) { path ->

            loadBitmap(path)
        }

        //-------------------- Initial State --------------------

        loadBitmap(HisBundleData.graphPath)

        appCache.bleConnectedLive.observe(viewLifecycleOwner) { connected ->
            Log.e("RESET_TEST", "BLE = $connected")
            if (!connected) {

                resetScreen()

            }

        }
    }

    // ------------------------------------------------------------
    // Apply Red/Green Color Based On Normal Range
    // ------------------------------------------------------------

    private fun applyRangeColor(
        card: MaterialCardView,
        textView: TextView,
        value: Double,
        min: Double,
        max: Double
    ) {

        val inRange = value in min..max

        val color = if (inRange) colorNormal else colorAbnormal

        card.strokeColor = color
        textView.setTextColor(color)
    }

    // ------------------------------------------------------------
    // Load His Bundle Graph
    // ------------------------------------------------------------

    private fun loadBitmap(path: String) {

        if (path.isBlank()) {
            clearImage()
            return
        }

        val file = File(path)

        if (!file.exists()) {

            Log.w(
                "HIS_FRAGMENT",
                "Graph file not found : $path"
            )

            clearImage()
            return
        }

        val modified = file.lastModified()

        // Skip loading only if the file is exactly the same
        if (path == lastGraphPath &&
            modified == lastModifiedTime
        ) {
            return
        }

        lastGraphPath = path
        lastModifiedTime = modified

        lifecycleScope.launch {

            try {

                val bitmap = withContext(Dispatchers.IO) {

                    val options = BitmapFactory.Options().apply {

                        // Helps reduce memory usage for large images
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }

                    BitmapFactory.decodeFile(
                        path,
                        options
                    )
                }

                if (!isAdded) return@launch

                withContext(Dispatchers.Main) {

                    if (bitmap != null) {

                        // Reset zoom/pan so each new graph starts unzoomed
                        imgHisBundle.setImageBitmap(bitmap)
                        imgHisBundle.scale = 1.0f

                        Log.d(
                            "HIS_FRAGMENT",
                            "Graph Loaded Successfully"
                        )

                    } else {

                        Log.e(
                            "HIS_FRAGMENT",
                            "Bitmap decode failed"
                        )

                        clearImage()
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    "HIS_FRAGMENT",
                    "Error Loading Bitmap",
                    e
                )

                if (isAdded) {
                    clearImage()
                }
            }
        }
    }

    // ------------------------------------------------------------
    // Clear Image
    // ------------------------------------------------------------

    private fun clearImage() {

        if (::imgHisBundle.isInitialized) {
            imgHisBundle.setImageDrawable(null)
        }
    }

    // ------------------------------------------------------------
    // Fragment Destroy
    // ------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        clearImage()

        lastGraphPath = null
        lastModifiedTime = -1L


    }

    private fun resetCard(
        card: MaterialCardView,
        textView: TextView
    ) {

        card.strokeColor = Color.GRAY

        textView.setTextColor(Color.GRAY)
    }

    private fun resetScreen() {
        Log.e("RESET_TEST", "resetScreen() called")

        tvPatientName.text = "--"
        tvHR.text = "-- BPM"
        tvMacAddress.text = "MACID: "

        tvBeatNumber.text = "--"
        tvPOnsetLocation.text = "--"
        tvPPeakLocation.text = "--"
        tvQRSOnLocation.text = "--"
        tvQRSOffLocation.text = "--"
        tvRPeakLocation.text = "--"

        tvPRDuration.text = "-- ms"
        tvQRSDuration.text = "-- ms"
        tvPADuration.text = "-- ms"
        tvAHDuration.text = "-- ms"
        tvHVDuration.text = "-- ms"
        tvHAmplitude.text = "-- mv"

        clearImage()

        resetCard(cardPRDuration, tvPRDuration)
        resetCard(cardQRSDuration, tvQRSDuration)
        resetCard(cardPADuration, tvPADuration)
        resetCard(cardAHDuration, tvAHDuration)
        resetCard(cardHVDuration, tvHVDuration)
        resetCard(cardHAmplitude, tvHAmplitude)
    }

    // ------------------------------------------------------------
    // CSV Download & Share Functions using SAF
    // ------------------------------------------------------------

    private fun openSaveCsvDialog() {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val fileName = "His_Bundle_$timestamp.csv"

        saveCsvLauncher.launch(fileName)
    }

    private fun copyCsvToUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val sourceFile = getCsvFile()
                if (sourceFile == null || !sourceFile.exists()) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "CSV file not found. Please ensure data has been recorded first.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val contentResolver = requireContext().contentResolver
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                android.widget.Toast.makeText(
                    requireContext(),
                    "CSV saved successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("CSV_DOWNLOAD", "Error copying CSV to URI", e)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Failed to save CSV: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareCsvFile() {
        lifecycleScope.launch {
            try {
                val csvFile = getCsvFile()
                if (csvFile == null || !csvFile.exists()) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "CSV file not found. Please ensure data has been recorded first.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        csvFile
                    )
                } else {
                    Uri.fromFile(csvFile)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, "Share CSV File")
                startActivity(chooser)
            } catch (e: Exception) {
                Log.e("CSV_SHARE", "Error sharing CSV", e)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Failed to share CSV: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getCsvFile(): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, try to find the file in MediaStore Downloads
            getCsvFileFromMediaStore()
        } else {
            // For Android 9 and below, look in public Downloads folder
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, "His_Bundlecmc.csv")
        }
    }

    private fun getCsvFileFromMediaStore(): File? {
        // Return the original CSV file from app-specific Downloads
        val appSpecificFile = File(requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS), "His_Bundlecmc.csv")
        if (appSpecificFile.exists()) {
            Log.d("CSV_FILE", "Found CSV in app-specific Downloads: ${appSpecificFile.absolutePath}")
            return appSpecificFile
        }

        Log.d("CSV_FILE", "CSV file not found")
        return null
    }

}







//
//package com.carditek.kesar.util.filters.edgecomputing
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.carditek.kesar.Cache
//import com.carditek.kesar.R
//import com.carditek.kesar.module.Patient
//import com.github.chrisbanes.photoview.PhotoView
//import com.google.android.material.card.MaterialCardView
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import javax.inject.Inject
//
///**
// * Pure UI renderer for the His Bundle screen.
// *
// * NOTE: This fragment does NOT write to CSV. Persistence is handled by
// * EdgeComputingProcessor (an app-scoped singleton), so measurements keep
// * being logged even when this screen is not visible. This fragment only
// * reflects the latest values on screen while it is alive.
// */
//@AndroidEntryPoint
//class HisbundleFragment : Fragment(R.layout.hisbundle) {
//
//    @Inject
//    lateinit var patient: Patient
//
//    @Inject
//    lateinit var appCache: Cache
//
//    // -------------------- UI --------------------
//
//    private lateinit var tvPatientName: TextView
//    private lateinit var tvHR: TextView
//
//    private lateinit var tvBeatNumber: TextView
//    private lateinit var tvPOnsetLocation: TextView
//    private lateinit var tvPPeakLocation: TextView
//    private lateinit var tvQRSOnLocation: TextView
//    private lateinit var tvQRSOffLocation: TextView
//    private lateinit var tvRPeakLocation: TextView
//    private lateinit var tvPRDuration: TextView
//    private lateinit var tvQRSDuration: TextView
//    private lateinit var tvPADuration: TextView
//    private lateinit var tvAHDuration: TextView
//    private lateinit var tvHVDuration: TextView
//    private lateinit var tvHAmplitude: TextView
//
//    private lateinit var imgHisBundle: PhotoView
//
//    // -------------------- Measurement Cards --------------------
//
//    private lateinit var cardPRDuration: MaterialCardView
//    private lateinit var cardQRSDuration: MaterialCardView
//    private lateinit var cardPADuration: MaterialCardView
//    private lateinit var cardAHDuration: MaterialCardView
//    private lateinit var cardHVDuration: MaterialCardView
//    private lateinit var cardHAmplitude: MaterialCardView
//
//    // -------------------- Range Colors --------------------
//
//    private val colorNormal = Color.parseColor("#4CAF50")
//    private val colorAbnormal = Color.parseColor("#F44336")
//
//    // -------------------- Bitmap Cache --------------------
//
//    private var lastGraphPath: String? = null
//    private var lastModifiedTime: Long = -1L
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        //-------------------- Find Views --------------------
//
//        tvPatientName = view.findViewById(R.id.tvPatientName)
//        tvHR = view.findViewById(R.id.tvHR)
//
//        tvBeatNumber = view.findViewById(R.id.tvBeatNumber)
//        tvPOnsetLocation = view.findViewById(R.id.tvPOnsetLocation)
//        tvPPeakLocation = view.findViewById(R.id.tvPPeakLocation)
//        tvQRSOnLocation = view.findViewById(R.id.tvQRSOnLocation)
//        tvQRSOffLocation = view.findViewById(R.id.tvQRSOffLocation)
//        tvRPeakLocation = view.findViewById(R.id.tvRPeakLocation)
//        tvPRDuration = view.findViewById(R.id.tvPRDuration)
//        tvQRSDuration = view.findViewById(R.id.tvQRSDuration)
//        tvPADuration = view.findViewById(R.id.tvPADuration)
//        tvAHDuration = view.findViewById(R.id.tvAHDuration)
//        tvHVDuration = view.findViewById(R.id.tvHVDuration)
//        tvHAmplitude = view.findViewById(R.id.tvHAmplitude)
//
//        imgHisBundle = view.findViewById(R.id.imgHisBundle)
//
//        cardPRDuration = view.findViewById(R.id.cardPRDuration)
//        cardQRSDuration = view.findViewById(R.id.cardQRSDuration)
//        cardPADuration = view.findViewById(R.id.cardPADuration)
//        cardAHDuration = view.findViewById(R.id.cardAHDuration)
//        cardHVDuration = view.findViewById(R.id.cardHVDuration)
//        cardHAmplitude = view.findViewById(R.id.cardHAmplitude)
//
//        //-------------------- Patient Name --------------------
//
//        patient.name.observe(viewLifecycleOwner) { patientData ->
//            tvPatientName.text = formatPatientName(patientData)
//        }
//
//        //-------------------- Heart Rate --------------------
//        // Fires roughly once per second. UI display only — must NOT
//        // trigger any CSV write (that caused a row every second before).
//
//        appCache.heartRateLive.observe(viewLifecycleOwner) { hr ->
//            tvHR.text = if (hr in 30..220) "$hr BPM" else "-- BPM"
//        }
//
//        //-------------------- Beat Number --------------------
//
//        appCache.beatNumberLive.observe(viewLifecycleOwner) { value ->
//            tvBeatNumber.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- P Onset Location --------------------
//
//        appCache.pOnsetLocationLive.observe(viewLifecycleOwner) { value ->
//            tvPOnsetLocation.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- P Peak Location --------------------
//
//        appCache.pPeakLocationLive.observe(viewLifecycleOwner) { value ->
//            tvPPeakLocation.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- QRS On Location --------------------
//
//        appCache.qrsOnLocationLive.observe(viewLifecycleOwner) { value ->
//            tvQRSOnLocation.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- QRS Off Location --------------------
//
//        appCache.qrsOffLocationLive.observe(viewLifecycleOwner) { value ->
//            tvQRSOffLocation.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- R Peak Location --------------------
//
//        appCache.rPeakLocationLive.observe(viewLifecycleOwner) { value ->
//            tvRPeakLocation.text = if (value == 0) "--" else "$value"
//        }
//
//        //-------------------- PR Duration --------------------
//
//        appCache.prDurationLive.observe(viewLifecycleOwner) { value ->
//            tvPRDuration.text = if (value == 0) "-- ms" else "$value ms"
//            applyRangeColor(cardPRDuration, tvPRDuration, value.toDouble(), 120.0, 200.0)
//        }
//
//        //-------------------- QRS Duration --------------------
//
//        appCache.qrsDurationLive.observe(viewLifecycleOwner) { value ->
//            tvQRSDuration.text = if (value == 0) "-- ms" else "$value ms"
//            applyRangeColor(cardQRSDuration, tvQRSDuration, value.toDouble(), 60.0, 110.0)
//        }
//
//        //-------------------- PA Duration --------------------
//
//        appCache.paDurationLive.observe(viewLifecycleOwner) { value ->
//            tvPADuration.text = if (value == 0) "-- ms" else "$value ms"
//            applyRangeColor(cardPADuration, tvPADuration, value.toDouble(), 25.0, 65.0)
//        }
//
//        //-------------------- AH Duration --------------------
//
//        appCache.ahDurationLive.observe(viewLifecycleOwner) { value ->
//            tvAHDuration.text = if (value == 0) "-- ms" else "$value ms"
//            applyRangeColor(cardAHDuration, tvAHDuration, value.toDouble(), 55.0, 125.0)
//        }
//
//        //-------------------- HV Duration --------------------
//
//        appCache.hvDurationLive.observe(viewLifecycleOwner) { value ->
//            tvHVDuration.text = if (value == 0) "-- ms" else "$value ms"
//            applyRangeColor(cardHVDuration, tvHVDuration, value.toDouble(), 35.0, 55.0)
//        }
//
//        //-------------------- H Amplitude --------------------
//
//        appCache.hAmplitudeLive.observe(viewLifecycleOwner) { value ->
//            tvHAmplitude.text = if (value == 0.0) "-- mV" else "$value mV"
//        }
//
//        //-------------------- Graph --------------------
//
//        HisBundleData.graphPathLive.observe(viewLifecycleOwner) { path ->
//            loadBitmap(path)
//        }
//
//        //-------------------- Initial State --------------------
//
//        loadBitmap(HisBundleData.graphPath)
//
//        //-------------------- BLE Connection --------------------
//
//        appCache.bleConnectedLive.observe(viewLifecycleOwner) { connected ->
//            if (!connected) {
//                resetScreen()
//            }
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Patient display-name formatting (drops ID prefix / age-sex suffix)
//    // ------------------------------------------------------------
//
//    private fun formatPatientName(patientData: String?): String {
//        if (patientData.isNullOrBlank()) return "--"
//
//        val parts = patientData.trim().split("\\s+".toRegex())
//
//        return if (parts.size >= 3) {
//            parts.subList(1, parts.size - 1).joinToString(" ")
//        } else {
//            patientData
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Apply Red/Green Color Based On Normal Range
//    // ------------------------------------------------------------
//
//    private fun applyRangeColor(
//        card: MaterialCardView,
//        textView: TextView,
//        value: Double,
//        min: Double,
//        max: Double
//    ) {
//        val inRange = value in min..max
//        val color = if (inRange) colorNormal else colorAbnormal
//
//        card.strokeColor = color
//        textView.setTextColor(color)
//    }
//
//    // ------------------------------------------------------------
//    // Load His Bundle Graph
//    // ------------------------------------------------------------
//
//    private fun loadBitmap(path: String) {
//
//        if (path.isBlank()) {
//            clearImage()
//            return
//        }
//
//        val file = File(path)
//
//        if (!file.exists()) {
//            Log.w("HIS_FRAGMENT", "Graph file not found : $path")
//            clearImage()
//            return
//        }
//
//        val modified = file.lastModified()
//
//        // Skip loading only if the file is exactly the same
//        if (path == lastGraphPath && modified == lastModifiedTime) {
//            return
//        }
//
//        lastGraphPath = path
//        lastModifiedTime = modified
//
//        lifecycleScope.launch {
//            try {
//                val bitmap = withContext(Dispatchers.IO) {
//                    val options = BitmapFactory.Options().apply {
//                        // Helps reduce memory usage for large images
//                        inPreferredConfig = Bitmap.Config.RGB_565
//                    }
//                    BitmapFactory.decodeFile(path, options)
//                }
//
//                if (!isAdded) return@launch
//
//                withContext(Dispatchers.Main) {
//                    if (bitmap != null) {
//                        // Reset zoom/pan so each new graph starts unzoomed
//                        imgHisBundle.setImageBitmap(bitmap)
//                        imgHisBundle.scale = 1.0f
//                        Log.d("HIS_FRAGMENT", "Graph Loaded Successfully")
//                    } else {
//                        Log.e("HIS_FRAGMENT", "Bitmap decode failed")
//                        clearImage()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("HIS_FRAGMENT", "Error Loading Bitmap", e)
//                if (isAdded) {
//                    clearImage()
//                }
//            }
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Clear Image
//    // ------------------------------------------------------------
//
//    private fun clearImage() {
//        if (::imgHisBundle.isInitialized) {
//            imgHisBundle.setImageDrawable(null)
//        }
//    }
//
//    // ------------------------------------------------------------
//    // Fragment Destroy
//    // ------------------------------------------------------------
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        clearImage()
//
//        lastGraphPath = null
//        lastModifiedTime = -1L
//    }
//
//    // ------------------------------------------------------------
//    // Reset UI (does NOT touch CSV / processing state)
//    // ------------------------------------------------------------
//
//    private fun resetCard(card: MaterialCardView, textView: TextView) {
//        card.strokeColor = Color.GRAY
//        textView.setTextColor(Color.GRAY)
//    }
//
//    private fun resetScreen() {
//        tvPatientName.text = "--"
//        tvHR.text = "-- BPM"
//
//        tvBeatNumber.text = "--"
//        tvPOnsetLocation.text = "--"
//        tvPPeakLocation.text = "--"
//        tvQRSOnLocation.text = "--"
//        tvQRSOffLocation.text = "--"
//        tvRPeakLocation.text = "--"
//
//        tvPRDuration.text = "-- ms"
//        tvQRSDuration.text = "-- ms"
//        tvPADuration.text = "-- ms"
//        tvAHDuration.text = "-- ms"
//        tvHVDuration.text = "-- ms"
//        tvHAmplitude.text = "-- mV"
//
//        clearImage()
//
//        resetCard(cardPRDuration, tvPRDuration)
//        resetCard(cardQRSDuration, tvQRSDuration)
//        resetCard(cardPADuration, tvPADuration)
//        resetCard(cardAHDuration, tvAHDuration)
//        resetCard(cardHVDuration, tvHVDuration)
//        resetCard(cardHAmplitude, tvHAmplitude)
//    }
//}
