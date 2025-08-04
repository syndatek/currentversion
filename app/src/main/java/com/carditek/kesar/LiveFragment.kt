//package com.carditek.kesar
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.webkit.WebView
//import com.carditek.kesar.cloud.Uploader
//import com.carditek.kesar.databinding.FragmentLiveBinding
//import com.carditek.kesar.module.Patient
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class LiveFragment : WebViewFragment() {
//    @Inject
//    lateinit var uploader: Uploader
//
//    private lateinit var binding: FragmentLiveBinding
//
//
//    override fun url(): String {
//        var address = device.address.value
//        if (address == null || address == "")
//            address = "54:6C:0E:83:3E:49"
//        return "https://ecg.carditek.com/#/tail/$address"
//    }
//
//    override fun webView(): WebView = binding.liveWebview
//
//    @Inject
//    lateinit var device: Device
//
//    @Inject
//    lateinit var patient: Patient
//
//    private fun maybeEnable() {
//        binding.fabRecord.isEnabled =
//            device.address.value?.isNotEmpty() == true && patient.empty.value == false
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentLiveBinding.inflate(inflater)
//        binding.lifecycleOwner = this
//        binding.device = device
//        device.address.observe(viewLifecycleOwner, { maybeEnable() })
//        patient.empty.observe(viewLifecycleOwner, { maybeEnable() })
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//        binding.fabRecord.setOnClickListener {
//            device.setRecording(!device.recording.value!!)
//        }
//        binding.fabAddNote.setOnClickListener {
//            Log.i(TAG, "Adding a note")
//            val dialog = AddNoteDialog() {
//                // This is a sloppier computation than the one in DataHandler.  TODO(vjn): tie the
//                // two up to use the same, monotonically-increasing one.
//                val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                GlobalScope.launch {
//                    uploader.note(stamp, it)
//                }
//            }
//            activity?.supportFragmentManager?.let {
//                dialog.show(it, "Add Note")
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveFragment"
//    }
//}
//
//
//
//
//
//
//
//








////this is the working fine code
//package com.carditek.kesar
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.webkit.WebView
//import android.widget.Toast
//import com.carditek.kesar.cloud.Uploader
//import com.carditek.kesar.databinding.FragmentLiveBinding
//import com.carditek.kesar.module.Patient
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class LiveFragment : WebViewFragment() {
//
//    @Inject
//    lateinit var uploader: Uploader
//
//    private lateinit var binding: FragmentLiveBinding
//
//    @Inject
//    lateinit var device: Device
//
//    @Inject
//    lateinit var patient: Patient
//
//    // Track whether a valid note has been saved
//    private var isNoteSaved = false
//
//    override fun url(): String {
//        var address = device.address.value
//        if (address.isNullOrEmpty()) {
//            address = "54:6C:0E:83:3E:49"
//        }
//        return "https://ecg.carditek.com/#/tail/$address"
//    }
//
//    override fun webView(): WebView = binding.liveWebview
//
//    private fun updateRecordButtonState() {
//        val isDeviceConnected = device.address.value?.isNotEmpty() == true
//        val isPatientSelected = patient.empty.value == false
//        val canEnable = isDeviceConnected && isPatientSelected && isNoteSaved
//        binding.fabRecord.isEnabled = canEnable
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentLiveBinding.inflate(inflater)
//        binding.lifecycleOwner = this
//        binding.device = device
//
//        // Observe device & patient state
//        device.address.observe(viewLifecycleOwner) { updateRecordButtonState() }
//        patient.empty.observe(viewLifecycleOwner) {
//            isNoteSaved = false // New patient → require new note
//            updateRecordButtonState()
//        }
//
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        // 📍 Recording button logic
//        binding.fabRecord.setOnClickListener {
//            if (!isNoteSaved) {
//                Toast.makeText(requireContext(), "Please add history", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val isRecording = device.recording.value ?: false
//            device.setRecording(!isRecording)
//
//            val message = if (isRecording) {
//                // 🎯 After stopping: require new note next time
//                isNoteSaved = false
//                updateRecordButtonState()
//                "Recording Stopped"
//            } else {
//                "Recording Started"
//            }
//
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        }
//
//        // 📝 Add Note logic
//        binding.fabAddNote.setOnClickListener {
//            val dialog = AddNoteDialog { note ->
//                if (note.isNotBlank()) {
//                    val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                    GlobalScope.launch {
//                        uploader.note(stamp, note)
//                    }
//
//                    isNoteSaved = true
//                    updateRecordButtonState()
//                } else {
//                    Toast.makeText(requireContext(), "Please enter patient history", Toast.LENGTH_SHORT).show()
//                }
//            }
//            activity?.supportFragmentManager?.let {
//                dialog.show(it, "Add Note")
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveFragment"
//    }
//}






////telegram code
//
//package com.carditek.kesar
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.webkit.WebView
//import android.widget.Toast
//import com.carditek.kesar.cloud.Uploader
//import com.carditek.kesar.databinding.FragmentLiveBinding
//import com.carditek.kesar.module.Patient
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.net.HttpURLConnection
//import java.net.URL
//import java.net.URLEncoder
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class LiveFragment : WebViewFragment() {
//
//    @Inject
//    lateinit var uploader: Uploader
//
//    private lateinit var binding: FragmentLiveBinding
//
//    @Inject
//    lateinit var device: Device
//
//    @Inject
//    lateinit var patient: Patient
//
//    private var isNoteSaved = false
//
//    override fun url(): String {
//        var address = device.address.value
//        if (address.isNullOrEmpty()) {
//            address = "54:6C:0E:83:3E:49"
//        }
//        return "https://ecg.carditek.com/#/tail/$address"
//    }
//
//    override fun webView(): WebView = binding.liveWebview
//
//    private fun updateRecordButtonState() {
//        val isDeviceConnected = device.address.value?.isNotEmpty() == true
//        val isPatientSelected = patient.empty.value == false
//        val canEnable = isDeviceConnected && isPatientSelected && isNoteSaved
//        binding.fabRecord.isEnabled = canEnable
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentLiveBinding.inflate(inflater)
//        binding.lifecycleOwner = this
//        binding.device = device
//
//        // Observe device & patient state
//        device.address.observe(viewLifecycleOwner) { updateRecordButtonState() }
//        patient.empty.observe(viewLifecycleOwner) {
//            isNoteSaved = false // New patient → require new note
//            updateRecordButtonState()
//        }
//
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        // 📍 Recording button logic
//        binding.fabRecord.setOnClickListener {
//            if (!isNoteSaved) {
//                Toast.makeText(requireContext(), "Please add history", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val isRecording = device.recording.value ?: false
//            device.setRecording(!isRecording)
//
//            val message = if (isRecording) {
//                isNoteSaved = false
//                updateRecordButtonState()
//                "Recording Stopped"
//            } else {
//                // ✅ Send to Telegram on recording start
//                val timestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                val macId = device.address.value ?: "Unknown"
//                GlobalScope.launch {
//                    sendMacIdToTelegram(macId, timestamp)
//                }
//
//                "Recording Started"
//            }
//
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        }
//
//        // 📝 Add Note logic
//        binding.fabAddNote.setOnClickListener {
//            val dialog = AddNoteDialog { note ->
//                if (note.isNotBlank()) {
//                    val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                    GlobalScope.launch {
//                        uploader.note(stamp, note)
//                    }
//
//                    isNoteSaved = true
//                    updateRecordButtonState()
//                } else {
//                    Toast.makeText(requireContext(), "Please enter patient history", Toast.LENGTH_SHORT).show()
//                }
//            }
//            activity?.supportFragmentManager?.let {
//                dialog.show(it, "Add Note")
//            }
//        }
//    }
//
//    private suspend fun sendMacIdToTelegram(macId: String, timestamp: Int) {
//        val botToken = "7597526068:AAGVJwkXbUO3R93UH4yWHtW5En-pYDf9Dl8"
//        val chatId = "738070910"
//
//        val message = """
//            📡 ECG Recording Started
//            🕒 Timestamp: $timestamp
//            🔌 MAC ID: $macId
//        """.trimIndent()
//
//        val encodedMessage = URLEncoder.encode(message, "UTF-8")
//        val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
//
//        val url = URL(urlString)
//        with(url.openConnection() as HttpURLConnection) {
//            requestMethod = "GET"
//            connectTimeout = 5000
//            readTimeout = 5000
//
//            val responseCode = responseCode
//            if (responseCode == 200) {
//                Log.d("Telegram", "Message sent successfully")
//            } else {
//                Log.e("Telegram", "Failed to send message. Code: $responseCode")
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveFragment"
//    }
//}


////stop recording the timestamp send to telegram
//
//
//package com.carditek.kesar
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.webkit.WebView
//import android.widget.Toast
//import com.carditek.kesar.cloud.Uploader
//import com.carditek.kesar.databinding.FragmentLiveBinding
//import com.carditek.kesar.module.Patient
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.net.HttpURLConnection
//import java.net.URL
//import java.net.URLEncoder
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class LiveFragment : WebViewFragment() {
//
//    @Inject lateinit var uploader: Uploader
//    private lateinit var binding: FragmentLiveBinding
//    @Inject lateinit var device: Device
//    @Inject lateinit var patient: Patient
//
//    private var isNoteSaved = false
//    private var firstTimestamp: Int = 0
//
//    override fun url(): String {
//        var address = device.address.value
//        if (address.isNullOrEmpty()) {
//            address = "54:6C:0E:83:3E:49"
//        }
//        return "https://ecg.carditek.com/#/tail/$address"
//    }
//
//    override fun webView(): WebView = binding.liveWebview
//
//    private fun updateRecordButtonState() {
//        val isDeviceConnected = device.address.value?.isNotEmpty() == true
//        val isPatientSelected = patient.empty.value == false
//        val canEnable = isDeviceConnected && isPatientSelected && isNoteSaved
//        binding.fabRecord.isEnabled = canEnable
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentLiveBinding.inflate(inflater)
//        binding.lifecycleOwner = this
//        binding.device = device
//
//        device.address.observe(viewLifecycleOwner) { updateRecordButtonState() }
//        patient.empty.observe(viewLifecycleOwner) {
//            isNoteSaved = false
//            updateRecordButtonState()
//        }
//
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        // 📍 Recording button logic
//        binding.fabRecord.setOnClickListener {
//            if (!isNoteSaved) {
//                Toast.makeText(requireContext(), "Please add history", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val isRecording = device.recording.value ?: false
//            device.setRecording(!isRecording)
//
//            val macId = device.address.value ?: "Unknown"
//
//            val message = if (isRecording) {
//                // ✅ Recording stopped
//                isNoteSaved = false
//                updateRecordButtonState()
//
//                // Send MAC and timestamp to Telegram
//
//                if (firstTimestamp != 0) {
//                    GlobalScope.launch {
//                        sendMacIdToTelegram(macId, firstTimestamp)
//                    }
//                    firstTimestamp = 0
//                }
//
//                "Recording Stopped"
//            } else {
//                // ✅ Recording started
////                if (firstTimestamp == null) {//added
//                firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
////                    Log.d("LiveFragment", "First timestamp set: $firstTimestamp")//added
////                }//added
//                "Recording Started"
//            }
//
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        }
//
//        // 📝 Add Note logic
//        binding.fabAddNote.setOnClickListener {
//            val dialog = AddNoteDialog { note ->
//                if (note.isNotBlank()) {
//                    val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                    GlobalScope.launch {
//                        uploader.note(stamp, note)
//                    }
//
//                    isNoteSaved = true
//                    updateRecordButtonState()
//                } else {
//                    Toast.makeText(requireContext(), "Please enter patient history", Toast.LENGTH_SHORT).show()
//                }
//            }
//            activity?.supportFragmentManager?.let {
//                dialog.show(it, "Add Note")
//            }
//        }
//    }
//
//    private suspend fun sendMacIdToTelegram(macId: String, timestamp: Int) {
//        val botToken = "7597526068:AAGVJwkXbUO3R93UH4yWHtW5En-pYDf9Dl8"
//        val chatId = "738070910"
//
//        val message = """
//            📡 ECG Recording Completed
//            🕒 Start Timestamp: $timestamp
//            🔌 MAC ID: $macId
//        """.trimIndent()
//
//        val encodedMessage = URLEncoder.encode(message, "UTF-8")
//        val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
//
//        val url = URL(urlString)
//        with(url.openConnection() as HttpURLConnection) {
//            requestMethod = "GET"
//            connectTimeout = 5000
//            readTimeout = 5000
//
//            val responseCode = responseCode
//            if (responseCode == 200) {
//                Log.d("Telegram", "Message sent successfully")
//            } else {
//                Log.e("Telegram", "Failed to send message. Code: $responseCode")
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveFragment"
//    }
//}



///// i dontknow

package com.carditek.kesar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import com.carditek.kesar.cloud.Uploader
import com.carditek.kesar.databinding.FragmentLiveBinding
import com.carditek.kesar.module.Patient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class LiveFragment : WebViewFragment() {

    @Inject lateinit var uploader: Uploader
    private lateinit var binding: FragmentLiveBinding
    @Inject lateinit var device: Device
    @Inject lateinit var patient: Patient

    private var isNoteSaved = false
    private var firstTimestamp: Int? = null  // ✅ Save only when recording starts

    override fun url(): String {
        var address = device.address.value
        if (address.isNullOrEmpty()) {
            address = "54:6C:0E:83:3E:49"
        }
        return "https://ecg.carditek.com/#/tail/$address"
    }

    override fun webView(): WebView = binding.liveWebview

    private fun updateRecordButtonState() {
        val isDeviceConnected = device.address.value?.isNotEmpty() == true
        val isPatientSelected = patient.empty.value == false
        val canEnable = isDeviceConnected && isPatientSelected && isNoteSaved
        binding.fabRecord.isEnabled = canEnable
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLiveBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.device = device

        device.address.observe(viewLifecycleOwner) { updateRecordButtonState() }
        patient.empty.observe(viewLifecycleOwner) {
            isNoteSaved = false
            updateRecordButtonState()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 📍 Recording button logic
        binding.fabRecord.setOnClickListener {
            if (!isNoteSaved) {
                Toast.makeText(requireContext(), "Please add history", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isRecording = device.recording.value ?: false
            device.setRecording(!isRecording)

            val macId = device.address.value ?: "Unknown"

            val message = if (isRecording) {
                // ✅ Recording is stopping
                isNoteSaved = false
                updateRecordButtonState()

                // ✅ Send MAC and timestamp to Telegram (after stop)
                firstTimestamp?.let { timestamp ->
                    GlobalScope.launch {
                        sendMacIdToTelegram(macId, timestamp)
                    }
                } ?: Log.e("LiveFragment", "❌ First timestamp is null or not set!")
                firstTimestamp = null // Reset after sending

                "Recording Stopped"
            } else {
                // ✅ Recording started – set the first timestamp
                if (firstTimestamp == null) {
                    firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
                    Log.d("LiveFragment", "✅ First timestamp saved: $firstTimestamp")
                }
                "Recording Started"
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // 📝 Add Note logic
        binding.fabAddNote.setOnClickListener {
            val dialog = AddNoteDialog { note ->
                if (note.isNotBlank()) {
                    val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
                    GlobalScope.launch {
                        uploader.note(stamp, note)
                    }

                    isNoteSaved = true
                    updateRecordButtonState()
                } else {
                    Toast.makeText(requireContext(), "Please enter patient history", Toast.LENGTH_SHORT).show()
                }
            }
            activity?.supportFragmentManager?.let {
                dialog.show(it, "Add Note")
            }
        }
    }

    // ✅ Function to send data to Telegram
    private suspend fun sendMacIdToTelegram(macId: String, timestamp: Int) {
        val botToken = "7597526068:AAGVJwkXbUO3R93UH4yWHtW5En-pYDf9Dl8"
        val chatId = "738070910"

        val message = """
            📡 ECG Recording Completed
            🕒 Start Timestamp: $timestamp
            🔌 MAC ID: $macId
        """.trimIndent()

        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

        val url = URL(urlString)
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000

            val responseCode = responseCode
            if (responseCode == 200) {
                Log.d("Telegram", "✅ Message sent successfully to Telegram.")
            } else {
                Log.e("Telegram", "❌ Failed to send message. Response code: $responseCode")
            }
        }
    }

    companion object {
        private const val TAG = "LiveFragment"
    }
}

