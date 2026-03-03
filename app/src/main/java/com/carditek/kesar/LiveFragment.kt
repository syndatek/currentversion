
// recording timestamp correction removed

package com.carditek.kesar
import androidx.fragment.app.Fragment

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
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject



@AndroidEntryPoint
class LiveFragment : WebViewFragment() {

    @Inject
    lateinit var uploader: Uploader
    private lateinit var binding: FragmentLiveBinding

    @Inject
    lateinit var device: Device
    @Inject
    lateinit var appCache: com.carditek.kesar.Cache
    @Inject
    lateinit var patient: Patient
    @Inject
    lateinit var noteDao: NoteDao


    private var timestampCounterJob: Job? = null
    private var lastSensorCheckState: Boolean = false
    private var sensorCheckDialog: AlertDialog? = null
    private var snrMonitoringJob: Job? = null
    private var ecgSettled: Boolean = false // Track if ECG has settled (SNR was good at least once)
    private var saturationDialog: AlertDialog? = null
    private var saturationMonitoringJob: Job? = null

    // Track Lead 2 quality status for heart rate display
    private var lead2SNR: Double? = null
    private var lead2Saturated: Boolean = false



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
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLiveBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.device = device

        device.address.observe(viewLifecycleOwner) { maybeEnable() }
        patient.empty.observe(viewLifecycleOwner) { maybeEnable() }

        //  LIVE PAGE NAME ONLY
        patient.name.observe(viewLifecycleOwner) { fullName ->
            binding.tvPatientName.text =
                if (!fullName.isNullOrBlank())
                    " ${extractOnlyName(fullName)}"
                else
                    " : --"
        }



        return binding.root
    }


    override fun onStart() {
        super.onStart()

        // Initialize Lead 2 quality status
        lead2SNR = null
        lead2Saturated = false

        // Live Heart Rate Observation - Only display if Lead 2 has good data
        appCache.heartRateLive.observe(viewLifecycleOwner) { hr ->
            // Check if Lead 2 has good quality data
            val isLead2Good = lead2SNR != null &&
                             lead2SNR != Double.NEGATIVE_INFINITY &&
                             (lead2SNR ?: 0.0) > 0.0 &&  // SNR > 0.0 dB (good quality)
                             !lead2Saturated     // Not saturated

            binding.heartRateTextView.text = if (hr > 0 && isLead2Good) {
                "$hr bpm"
            } else {
                "-- bpm"
            }
        }

        // Live SNR Values Observation - Display SNR for Lead 1 and Lead 2
        appCache.snrValuesLive.observe(viewLifecycleOwner) { (lead1SNR, lead2SNRValue) ->
            lead2SNR = lead2SNRValue  // Store for heart rate quality check

            val lead1Text = if (lead1SNR != null && lead1SNR != Double.NEGATIVE_INFINITY) {
                String.format("%.1f", lead1SNR)
            } else {
                "--"
            }
            val lead2Text = if (lead2SNRValue != null && lead2SNRValue != Double.NEGATIVE_INFINITY) {
                String.format("%.1f", lead2SNRValue)
            } else {
                "--"
            }
            binding.snrTextView.text = "SNR: L1:$lead1Text dB  L2:$lead2Text dB"

            // Update heart rate display when SNR changes
            val currentHR = appCache.heartRateLive.value ?: 0
            val isLead2Good = lead2SNRValue != null &&
                             lead2SNRValue != Double.NEGATIVE_INFINITY &&
                             lead2SNRValue > 0.0 &&
                             !lead2Saturated
            binding.heartRateTextView.text = if (currentHR > 0 && isLead2Good) {
                "$currentHR bpm"
            } else {
                "-- bpm"
            }
        }

        // Monitor saturation status for Lead 2
        appCache.saturatedLeadsLive.observe(viewLifecycleOwner) { saturatedLeads ->
            lead2Saturated = saturatedLeads.contains(2)  // Check if Lead 2 is saturated

            // Update heart rate display when saturation changes
            val currentHR = appCache.heartRateLive.value ?: 0
            val isLead2Good = lead2SNR != null &&
                             lead2SNR != Double.NEGATIVE_INFINITY &&
                             (lead2SNR ?: 0.0) > 0.0 &&
                             !lead2Saturated
            binding.heartRateTextView.text = if (currentHR > 0 && isLead2Good) {
                "$currentHR bpm"
            } else {
                "-- bpm"
            }
        }

        // Live Sensor Check Warning Observation - Continuous monitoring
        startSNRMonitoring()

        // Live Saturation Detection Observation - Continuous monitoring
        startSaturationMonitoring()

        // Restart timestamp counter if recording is already in progress
        if (device.recording.value == true) {
            startLiveTimestampCounter()
        }


        binding.fabRecord.setOnClickListener {
            val currentlyRecording = device.recording.value == true
            if (!currentlyRecording) {
                startRecording()
            } else {
                stopRecordingAndSendTelegram()
            }
        }

        binding.btnStop.setOnClickListener {
            val enabled = appCache.isFilteringEnabled()
            appCache.setFilteringEnabled(!enabled)
            updateFilterButton()
            Toast.makeText(
                requireContext(),
                if (!enabled) "Edge Computing ON" else "Edge Computing OFF",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun startRecording() {
        if (device.firstTimestamp == null) {
            device.firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
            Log.d(TAG, " Start timestamp saved: ${device.firstTimestamp}")
        }
        // Upload saved medical history from database (stored procedure) if exists
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedNoteData = AddNoteDialog.getSavedNoteWithId(requireContext(), noteDao)
                if (savedNoteData != null && !savedNoteData.noteText.isNullOrEmpty() && !savedNoteData.uploaded) {
                    val stamp = device.firstTimestamp!!
                    val savedNote = savedNoteData.noteText
                    // uploader.note() queues WorkManager job (not suspend function)
                    uploader.note(stamp, savedNote)
                    // Mark note as uploaded (but keep it in database for retry if needed)
                    AddNoteDialog.markNoteAsUploaded(requireContext(), noteDao, savedNoteData.id)
                    Log.d(TAG, "Medical history queued for upload, note ID: ${savedNoteData.id}")
                } else if (savedNoteData != null && savedNoteData.uploaded) {
                    Log.d(TAG, "Medical history already uploaded, skipping")
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
            val botToken = "7597526068:AAGVJwkXbUO3R93UH4yWHtW5En-pYDf9Dl8"
            val chatId = "738070910"

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
                if (code == 200) {
                    Log.d(TAG, " Telegram message sent.")
                } else {
                    Log.e(TAG, " Telegram send failed. HTTP $code")
                }
                disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Telegram send error: ${e.message}", e)
        }
    }


    private fun startLiveTimestampCounter() {
        binding.timestampToggle.visibility = View.VISIBLE
        updateFilterButton()
        timestampCounterJob?.cancel()
        timestampCounterJob = CoroutineScope(Dispatchers.Main).launch {
            while (device.recording.value == true) {
                val firstTs = device.firstTimestamp
                if (firstTs != null) {
                    val nowTs = ((System.currentTimeMillis() / 15000) * 15).toInt()
                    val elapsedSeconds = nowTs - firstTs
                    val intervalNumber = (elapsedSeconds / 15)  // 0-15sec=1, 16-30sec=2, etc.
                    val displayText = if (intervalNumber == 15) {
                        "✓" // Show tick mark when reaching 15 or higher
                    } else if (intervalNumber > 15) {
                        "TS=$intervalNumber" // Show TS=1, TS=2, etc. for intervals below 15
                    }
                    else {
                        "TS=$intervalNumber" // Before 15
                    }
                    binding.timestampToggle.text = displayText
                }
                delay(1000)
            }
        }
    }

    private fun stopLiveTimestampCounter() {
        timestampCounterJob?.cancel()
        timestampCounterJob = null
        binding.timestampToggle.visibility = View.GONE
    }

    /**
     * Starts continuous SNR monitoring every 1 second for Lead 1 and Lead 2.
     *
     * POPUP APPEARS IF: ANY lead (Lead 1 OR Lead 2) has SNR <= 0.0 dB
     * POPUP DISAPPEARS IF: BOTH leads have SNR > 0.0 dB
     *
     * Dialog automatically disappears when SNR > 0.0 dB for both leads.
     * Dialog will show again if SNR drops below threshold again.
     */
    private fun startSNRMonitoring() {
        snrMonitoringJob?.cancel()
        snrMonitoringJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val needsCheck = appCache.sensorCheckWarningLive.value ?: false
                val lowSNRLeads = appCache.lowSNRLeadsLive.value ?: emptyList()

                if (needsCheck) {
                    // ANY lead (Lead 1 OR Lead 2) has SNR <= 0.0 dB - show/update dialog
                    // Always call to update message with latest SNR values (similar to heart rate)
                    Log.d(TAG, "Showing popup: ${lowSNRLeads.size} lead(s) below 0.0 dB threshold")
                    showSensorCheckDialog(lowSNRLeads)
                } else {
                    // BOTH leads have SNR >= 6.0 dB - dismiss dialog if showing
                    if (!ecgSettled) {
                        ecgSettled = true
                        Log.d(TAG, "ECG settled - Both Lead 1 and Lead 2 have SNR > 0.0 dB")
                    }
                    sensorCheckDialog?.dismiss()
                    sensorCheckDialog = null
                }

                delay(1000) // Check every 1 second continuously
            }
        }

        // Also observe LiveData for immediate updates (continuous calculation like heart rate)
        appCache.sensorCheckWarningLive.observe(viewLifecycleOwner) { needsCheck ->
            val lowSNRLeads = appCache.lowSNRLeadsLive.value ?: emptyList()
            val snrValues = appCache.snrValuesLive.value

            // Log current SNR values for debugging
            val lead1SNR = snrValues?.first
            val lead2SNR = snrValues?.second
            Log.d(TAG, "SNR Check - Lead1: ${if (lead1SNR != null) String.format("%.2f", lead1SNR) else "null"} dB, " +
                    "Lead2: ${if (lead2SNR != null) String.format("%.2f", lead2SNR) else "null"} dB, " +
                    "needsCheck: $needsCheck, lowSNRLeads: ${lowSNRLeads.size}")

            if (needsCheck) {
                // ANY lead (Lead 1 OR Lead 2) has SNR < 6.0 dB - show/update dialog
                // Always call to update message with latest SNR values (similar to heart rate)
                if (lowSNRLeads.isNotEmpty()) {
                    Log.w(TAG, "Showing popup: ${lowSNRLeads.size} lead(s) below 0.0 dB threshold")
                    showSensorCheckDialog(lowSNRLeads)
                } else {
                    // This shouldn't happen, but log it if it does
                    Log.e(TAG, "ERROR: needsCheck is true but lowSNRLeads is empty! Lead1: $lead1SNR, Lead2: $lead2SNR")
                    // Still show dialog with current SNR values
                    val currentLowLeads = mutableListOf<Pair<Int, Double>>()
                    if (lead1SNR != null && (lead1SNR <= 0.0 || lead1SNR.isNaN() || lead1SNR == Double.NEGATIVE_INFINITY)) {
                        currentLowLeads.add(Pair(1, lead1SNR))
                    }
                    if (lead2SNR != null && (lead2SNR <= 0.0 || lead2SNR.isNaN() || lead2SNR == Double.NEGATIVE_INFINITY)) {
                        currentLowLeads.add(Pair(2, lead2SNR))
                    }
                    if (currentLowLeads.isNotEmpty()) {
                        Log.w(TAG, "Showing popup with manually detected low SNR leads")
                        showSensorCheckDialog(currentLowLeads)
                    }
                }
            } else {
                // BOTH leads have SNR >= 6.0 dB - dismiss dialog if showing
                if (!ecgSettled) {
                    ecgSettled = true
                    Log.d(TAG, "ECG settled - Both Lead 1 and Lead 2 have SNR >= 6.0 dB")
                }
                sensorCheckDialog?.dismiss()
                sensorCheckDialog = null
            }
        }
    }

    private fun updateFilterButton() {
        binding.btnStop.text = "S"
        // Red = edge computing off, Green = edge computing on
        val color =
            if (appCache.isFilteringEnabled()) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        @Suppress("DEPRECATION")
        binding.btnStop.setBackgroundColor(resources.getColor(color))
    }

    /**
     * Shows a dialog popup when sensor check is needed (SNR ≤ 0.0 dB for Lead 1 or Lead 2).
     * This alerts the user to check the sensor connection/placement.
     * Shows which specific lead(s) have low SNR.
     * Dialog has no OK button and will automatically disappear when SNR > 0.0 dB for both leads.
     * Similar to heart rate monitoring - continuously calculated and displayed.
     *
     * @param lowSNRLeads List of pairs containing (Lead number: 1 or 2, SNR value in dB)
     */
    private fun showSensorCheckDialog(lowSNRLeads: List<Pair<Int, Double>>) {
        if (!isAdded || context == null) return

        // If dialog is already showing, update its message instead of recreating
        if (sensorCheckDialog != null && sensorCheckDialog!!.isShowing) {
            // Update the message with current SNR values
            val leadList = if (lowSNRLeads.isNotEmpty()) {
                lowSNRLeads.joinToString("\n") { (lead, snr) ->
                    "  • Lead $lead: ${String.format("%.2f", snr)} dB"
                }
            } else {
                "  • Lead 1 or Lead 2"
            }

            val message = "Low signal quality detected (SNR ≤ 0.0 dB).\n\n" +
                    "Affected Lead(s):\n$leadList\n\n" +
                    "Please check the sensor:\n" +
                    "• Ensure proper electrode contact\n" +
                    "• Check sensor placement\n" +
                    "• Verify sensor connection\n" +
                    "• Clean electrode contacts if needed\n\n" +
                    "This message will automatically disappear when signal quality improves (SNR > 0.0 dB)."

            // Update message in existing dialog
            sensorCheckDialog?.setMessage(message)
            return
        }

        // Dismiss existing dialog if any (shouldn't happen, but safety check)
        sensorCheckDialog?.dismiss()

        // Build lead list message
        val leadList = if (lowSNRLeads.isNotEmpty()) {
            lowSNRLeads.joinToString("\n") { (lead, snr) ->
                "  • Lead $lead: ${String.format("%.2f", snr)} dB"
            }
        } else {
            "  • Lead 1 or Lead 2"
        }

        val message = "Low signal quality detected (SNR ≤ 0.0 dB).\n\n" +
                "Affected Lead(s):\n$leadList\n\n" +
                "Please check the sensor:\n" +
                "• Ensure proper electrode contact\n" +
                "• Check sensor placement\n" +
                "• Verify sensor connection\n" +
                "• Clean electrode contacts if needed"

        // Create dialog with OK button - blocks other interactions
        sensorCheckDialog = AlertDialog.Builder(requireContext())
            .setTitle("Sensor Check Required")
            .setMessage(message)
            .setCancelable(false) // Cannot be dismissed by back button or outside touch
            .setPositiveButton("OK") { _, _ ->
                sensorCheckDialog?.dismiss()
                sensorCheckDialog = null
            }
            .create()

        sensorCheckDialog?.show()
    }

    /**
     * Starts continuous saturation monitoring every 1 second for all 8 leads.
     *
     * POPUP APPEARS IF: ANY lead (1-8) has saturation detected
     * POPUP DISAPPEARS IF: NO leads have saturation
     *
     * Dialog automatically disappears when saturation is no longer detected.
     */
    private fun startSaturationMonitoring() {
        saturationMonitoringJob?.cancel()
        saturationMonitoringJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                val saturatedLeads = appCache.saturatedLeadsLive.value ?: emptyList()

                if (saturatedLeads.isNotEmpty()) {
                    // Saturation detected - show/update dialog
                    Log.w(TAG, "Showing saturation popup: ${saturatedLeads.size} lead(s) saturated")
                    showSaturationDialog(saturatedLeads)
                } else {
                    // No saturation - dismiss dialog if showing
                    saturationDialog?.dismiss()
                    saturationDialog = null
                }

                delay(1000) // Check every 1 second continuously
            }
        }

        // Also observe LiveData for immediate updates
        appCache.saturatedLeadsLive.observe(viewLifecycleOwner) { saturatedLeads ->
            if (saturatedLeads.isNotEmpty()) {
                // Saturation detected - show/update dialog
                Log.w(TAG, "Showing saturation popup: ${saturatedLeads.size} lead(s) saturated")
                showSaturationDialog(saturatedLeads)
            } else {
                // No saturation - dismiss dialog if showing
                saturationDialog?.dismiss()
                saturationDialog = null
            }
        }
    }

    /**
     * Shows a dialog popup when saturation is detected in any lead.
     * This alerts the user that the signal is clipped and sensor needs checking.
     * Dialog has no OK button and will automatically disappear when saturation is no longer detected.
     *
     * @param saturatedLeads List of lead numbers (1-8) that are saturated
     */
    private fun showSaturationDialog(saturatedLeads: List<Int>) {
        if (!isAdded || context == null) return

        // If dialog is already showing, update its message instead of recreating
        if (saturationDialog != null && saturationDialog!!.isShowing) {
            // Update the message with current saturated leads
            val leadList = saturatedLeads.joinToString(", ") { "Lead $it" }

            val message = "Saturation is high - signal is clipped!\n\n" +
                    "Affected Lead(s): $leadList\n\n" +
                    "Please check the sensor:\n" +
                    "• Reduce sensor gain if possible\n" +
                    "• Check electrode contact\n" +
                    "• Verify sensor connection\n" +
                    "• Ensure proper sensor placement\n" +
                    "• Check for loose connections\n\n" +
                    "This message will automatically disappear when saturation is resolved."

            // Update message in existing dialog
            saturationDialog?.setMessage(message)
            return
        }

        // Dismiss existing dialog if any (shouldn't happen, but safety check)
        saturationDialog?.dismiss()

        // Build lead list message
        val leadList = saturatedLeads.joinToString(", ") { "Lead $it" }

        val message = "Saturation is high - signal is clipped!\n\n" +
                "Affected Lead(s): $leadList\n\n" +
                "Please check the sensor:\n" +
                "• Reduce sensor gain if possible\n" +
                "• Check electrode contact\n" +
                "• Verify sensor connection\n" +
                "• Ensure proper sensor placement\n" +
                "• Check for loose connections"

        // Create dialog with OK button - blocks other interactions
        saturationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Saturation Detected")
            .setMessage(message)
            .setCancelable(false) // Cannot be dismissed by back button or outside touch
            .setPositiveButton("OK") { _, _ ->
                saturationDialog?.dismiss()
                saturationDialog = null
            }
            .create()

        saturationDialog?.show()
    }

    override fun onStop() {
        super.onStop()
        snrMonitoringJob?.cancel()
        snrMonitoringJob = null
        sensorCheckDialog?.dismiss()
        sensorCheckDialog = null
        saturationMonitoringJob?.cancel()
        saturationMonitoringJob = null
        saturationDialog?.dismiss()
        saturationDialog = null
    }

    override fun onResume() {
        super.onResume()
        // Reset ECG settled status when fragment resumes (new session)
        ecgSettled = false
    }

    //  patient name dislay function start
        private fun extractOnlyName(fullName: String): String {
        val parts = fullName.trim().split("\\s+".toRegex())
        if (parts.size < 3) return fullName
        return parts.subList(1, parts.size - 1).joinToString(" ")
    }// end

    companion object {
        private const val TAG = "LiveFragment"
    }
}



