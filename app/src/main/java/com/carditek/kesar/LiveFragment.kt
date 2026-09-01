package com.carditek.kesar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.carditek.kesar.cloud.Uploader
import com.carditek.kesar.databinding.FragmentLiveBinding
import com.carditek.kesar.module.Patient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class LiveFragment : WebViewFragment() {

    @Inject
    lateinit var uploader: Uploader
    @Inject
    lateinit var device: Device
    @Inject
    lateinit var appCache: Cache
    @Inject
    lateinit var patient: Patient
    @Inject
    lateinit var noteDao: NoteDao
    @Inject
    lateinit var medicalHistoryUploadNotifier: MedicalHistoryUploadNotifier

    private lateinit var binding: FragmentLiveBinding
//    private var maxTimestampsLimit: Int? = null    // this is for the autorecord off
    private var timestampCounterJob: Job? = null
    private var historyUploadJob: Job? = null
    private var sensorCheckDialog: AlertDialog? = null
    private var snrMonitoringJob: Job? = null
    private var saturationDialog: AlertDialog? = null
    private var saturationMonitoringJob: Job? = null

    private var lead2SNR: Double? = null
    private var lead2Saturated: Boolean = false

    // Keep HR gating consistent with SNR popup thresholds.
    private val snrGoodThresholdDb = 6.0

    override fun url(): String {
        var address = device.address.value
        if (address.isNullOrEmpty()) {
            address = "54:6C:0E:83:3E:49"
        }
        return "https://ecg.carditek.com/#/tail/$address"
    }

    override fun webView(): WebView = binding.liveWebview

    private fun maybeEnable() {
        binding.fabRecord.isEnabled =
            device.address.value?.isNotEmpty() == true && patient.empty.value == false
        binding.fabAddNote.isEnabled =
            device.address.value?.isNotEmpty() == true &&
                patient.empty.value == false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLiveBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.device = device

        device.address.observe(viewLifecycleOwner) { maybeEnable() }
        patient.empty.observe(viewLifecycleOwner) { maybeEnable() }
        device.recording.observe(viewLifecycleOwner) { maybeEnable() }

        patient.name.observe(viewLifecycleOwner) { fullName ->
            binding.tvPatientName.text =
                if (!fullName.isNullOrBlank()) " ${extractOnlyName(fullName)}" else " : --"
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        lead2SNR = null
        lead2Saturated = false
        startHistoryUploadStatus()

        appCache.heartRateLive.observe(viewLifecycleOwner) { hr ->
            binding.heartRateTextView.text = if (shouldShowHr(hr)) "$hr bpm" else "-- bpm"
        }

//        appCache.snrValuesLive.observe(viewLifecycleOwner) { (lead1SNR, lead2SNRValue) ->
//            lead2SNR = lead2SNRValue
//            if (appCache.isFilteringEnabled()) {
//                val lead1Text = if (lead1SNR != null && lead1SNR != Double.NEGATIVE_INFINITY) {
//                    String.format("%.1f", lead1SNR)
//                } else {
//                    "--"
//                }
//                val lead2Text = if (lead2SNRValue != null && lead2SNRValue != Double.NEGATIVE_INFINITY) {
//                    String.format("%.1f", lead2SNRValue)
//                } else {
//                    "--"
//                }
//                binding.snrTextView.text = "SNR: L1:$lead1Text dB  L2:$lead2Text dB"
//            } else {
//                binding.snrTextView.text = "SNR: --"
//            }
//        }

//        appCache.saturatedLeadsLive.observe(viewLifecycleOwner) { saturatedLeads ->
//            lead2Saturated = saturatedLeads.contains(2)
//        }

        if (device.recording.value == true) {
            startLiveTimestampCounter()
        }

        binding.fabRecord.setOnClickListener {
            if (device.recording.value == true) stopRecordingAndSendTelegram() else startRecording()
        }

        binding.fabAddNote.setOnClickListener {
            AddNoteDialog().show(parentFragmentManager, "AddNoteDialog")
        }

//        binding.btnStop.setOnClickListener {
//            val currentlyEnabled = appCache.isFilteringEnabled()
//            appCache.setFilteringEnabled(!currentlyEnabled)//UNCONNNENT YESTERDAY
//            appCache.setFilteringEnabled(true)
//            updateFilterButton()
//            applyEdgeComputingUiState()
//            Toast.makeText(
//                requireContext(),
//                if (!currentlyEnabled) "Edge Computing ON" else "Edge Computing OFF",
//                Toast.LENGTH_SHORT
//            ).show()
////                requireContext(),
////                "Edge Computing ON",
////                Toast.LENGTH_SHORT
////            ).show()
//        }
        binding.btnStop.setOnClickListener {

            val currentlyEnabled = appCache.isFilteringEnabled()

            // Toggle the current state
            appCache.setFilteringEnabled(!currentlyEnabled)

            updateFilterButton()
            applyEdgeComputingUiState()

            Toast.makeText(
                requireContext(),
                if (!currentlyEnabled) {
                    "Edge Computing ON"
                } else {
                    "Edge Computing OFF"
                },
                Toast.LENGTH_SHORT
            ).show()
        }

        updateFilterButton()
        applyEdgeComputingUiState()
    }

    private fun startHistoryUploadStatus() {
        historyUploadJob?.cancel()
        // Use event-based approach with much longer polling interval to save battery
        historyUploadJob = viewLifecycleOwner.lifecycleScope.launch {
            // Initial check
            checkAndShowHistoryStatus()

            // Listen for upload completion events
            medicalHistoryUploadNotifier.medicalHistorySavedEvents.collect {
                // Hide syncing status when upload completes
                binding.historyUploadToggle.visibility = View.GONE
            }
        }
    }

    private suspend fun checkAndShowHistoryStatus() {
        try {
            val pending = withContext(Dispatchers.IO) { noteDao.getUnuploadedNote() }
            if (pending != null) {
                binding.historyUploadToggle.setText(R.string.history_status_syncing)
                binding.historyUploadToggle.visibility = View.VISIBLE
            } else {
                binding.historyUploadToggle.visibility = View.GONE
            }
        } catch (_: Exception) {
            binding.historyUploadToggle.visibility = View.GONE
        }
    }

    private fun shouldShowHr(hr: Int): Boolean {
        return hr > 0
    }

    private fun applyEdgeComputingUiState() {
        if (appCache.isFilteringEnabled()) {
            startSNRMonitoring()
            startSaturationMonitoring()
        } else {
//            binding.snrTextView.text = "SNR: --"
            stopSNRMonitoring()
            stopSaturationMonitoring()
        }
   }

    private fun startRecording() {
        if (device.firstTimestamp == null) {
            device.firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
            Log.d(TAG, "Start timestamp saved: ${device.firstTimestamp}")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedNoteData = AddNoteDialog.getSavedNoteWithId(requireContext(), noteDao)
                if (savedNoteData != null && !savedNoteData.noteText.isNullOrEmpty() && !savedNoteData.uploaded) {
                    val stamp = device.firstTimestamp!!
                    uploader.note(stamp, savedNoteData.noteText, savedNoteData.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading note: ${e.message}", e)
            }
            withContext(Dispatchers.Main) {
                device.setRecording(true)
                startLiveTimestampCounter()
                Toast.makeText(requireContext(), "Recording Started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecordingAndSendTelegram() {
        device.setRecording(false)
        val firstTs = device.firstTimestamp
        val lastTs = ((System.currentTimeMillis() / 15000) * 15).toInt()
        val macId = device.address.value ?: "Unknown"

        if (firstTs != null) {
            CoroutineScope(Dispatchers.IO).launch {
                sendMacIdToTelegram(macId, firstTs, lastTs)
            }
        }

        device.firstTimestamp = null
        stopLiveTimestampCounter()
        Toast.makeText(requireContext(), "Recording Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun sendMacIdToTelegram(macId: String, firstTimestamp: Int, lastTimestamp: Int) {
        try {
            // Telegram alerts are controlled by build-time config.
            // For production, disable this and use your backend instead.
            if (!BuildConfig.TELEGRAM_ENABLED) return
            val botToken = BuildConfig.TELEGRAM_BOT_TOKEN
            val chatId = BuildConfig.TELEGRAM_CHAT_ID
            if (botToken.isBlank() || chatId.isBlank()) return
            val message = """
                 ECG Recording Completed
                 MAC ID: $macId
                 StartTimestamp: $firstTimestamp
                 LastTimestamp:  $lastTimestamp
            """.trimIndent()

            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val urlString =
                "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

            val url = URL(urlString)
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                val code = responseCode
                if (code != 200) {
                    Log.e(TAG, "Telegram send failed. HTTP $code")
                }
                disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Telegram send error: ${e.message}", e)
        }
    }

    private fun startLiveTimestampCounter() {
        binding.timestampToggle.visibility = View.VISIBLE
        timestampCounterJob?.cancel()
        timestampCounterJob = lifecycleScope.launch {
            while (device.recording.value == true) {
                val firstTs = device.firstTimestamp
                if (firstTs != null) {
                    val nowTs = ((System.currentTimeMillis() / 15000) * 15).toInt()
                    val intervalNumber = (nowTs - firstTs) / 15
                    binding.timestampToggle.text = "TS=$intervalNumber"
                }
                delay(1000)
            }
        }
    }

//    // autostop logic
//    private fun startLiveTimestampCounter() {
//        binding.timestampToggle.visibility = View.VISIBLE
//        timestampCounterJob?.cancel()
//
//        timestampCounterJob = lifecycleScope.launch {
//            while (device.recording.value == true) {
//
//                val firstTs = device.firstTimestamp
//                if (firstTs != null) {
//
//                    val nowTs = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                    val intervalNumber = (nowTs - firstTs) / 15
//
//                    binding.timestampToggle.text = "TS=$intervalNumber"
//
//                    //  ADD THIS BLOCK (AUTO STOP LOGIC)
//                    device.maxTimestamps?.let { max ->
//                        if (intervalNumber >= max) {
//                            stopRecordingAndSendTelegram()
//                            return@launch  // important to stop loop
//                        }
//                    }
//                }
//
//                delay(1000)
//            }
//        }
//    }
    private fun stopLiveTimestampCounter() {
        timestampCounterJob?.cancel()
        timestampCounterJob = null
        binding.timestampToggle.visibility = View.GONE
    }

    private fun startSNRMonitoring() {
        // Disabled - logic is commented out, was causing battery drain
        // Re-enable if needed in the future
    }

    private fun stopSNRMonitoring() {
        snrMonitoringJob?.cancel()
        snrMonitoringJob = null
        sensorCheckDialog?.dismiss()
        sensorCheckDialog = null
    }

    private fun startSaturationMonitoring() {
        // Disabled - logic is commented out, was causing battery drain
        // Re-enable if needed in the future
    }

    private fun stopSaturationMonitoring() {
        saturationMonitoringJob?.cancel()
        saturationMonitoringJob = null
        saturationDialog?.dismiss()
        saturationDialog = null
    }

    private fun showSensorCheckDialog() {
        if (!isAdded || context == null) return
        val message = "Low signal quality detected (SNR low <= 0.0 dB).\n\nPlease check electrodes and sensor contact."
        if (sensorCheckDialog?.isShowing == true) {
            sensorCheckDialog?.setMessage(message)
            return
        }
        sensorCheckDialog = AlertDialog.Builder(requireContext())
            .setTitle("Sensor Check Required")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                sensorCheckDialog?.dismiss()
                sensorCheckDialog = null
            }
            .create()
        sensorCheckDialog?.show()
    }

    private fun showSaturationDialog(saturatedLeads: List<Int>) {
        if (!isAdded || context == null) return
        val leadList = saturatedLeads.joinToString(", ") { "Lead $it" }
        val message = "Saturation is high - signal is clipped.\n\nAffected Lead(s): $leadList"
        if (saturationDialog?.isShowing == true) {
            saturationDialog?.setMessage(message)
            return
        }
        saturationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Saturation Detected")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                saturationDialog?.dismiss()
                saturationDialog = null
            }
            .create()
        saturationDialog?.show()
    }

    private fun updateFilterButton() {
        binding.btnStop.text = "S"
        val color = if (appCache.isFilteringEnabled()) {
            android.R.color.holo_green_dark
        } else {
            android.R.color.holo_red_dark
        }
        @Suppress("DEPRECATION")
        binding.btnStop.setBackgroundColor(resources.getColor(color))
    }

    override fun onStop() {
        super.onStop()
        stopSNRMonitoring()
        stopSaturationMonitoring()
        historyUploadJob?.cancel()
        historyUploadJob = null
    }

    private fun extractOnlyName(fullName: String): String {
        val parts = fullName.trim().split("\\s+".toRegex())
        if (parts.size < 3) return fullName
        return parts.subList(1, parts.size - 1).joinToString(" ")
    }

    companion object {
        private const val TAG = "LiveFragment"
    }
}
