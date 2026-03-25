//package com.carditek.kesar
//
//import android.annotation.SuppressLint
//import android.app.Activity.RESULT_OK
//import android.content.Intent
//import android.os.Bundle
//import android.provider.ContactsContract
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.fragment.app.Fragment
//import com.carditek.kesar.databinding.FragmentRecordBinding
//import com.carditek.kesar.module.Patient
//import com.carditek.kesar.cloud.Uploader
//import com.google.android.material.snackbar.Snackbar
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//import androidx.appcompat.app.AlertDialog
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.net.HttpURLConnection
//import java.net.URL
//import java.net.URLEncoder
//
//@AndroidEntryPoint
//class RecordFragment : Fragment() {
//    @Inject
//    lateinit var patient: Patient
//
//    @Inject
//    lateinit var device: Device
//
//    @Inject
//    lateinit var uploader: Uploader
//
//    @Inject
//    lateinit var noteDao: NoteDao
//
//    private val intent = Intent(
//        Intent.ACTION_PICK,
//        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
//    )
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val binding = FragmentRecordBinding.inflate(inflater, container, false)
//        binding.lifecycleOwner = this
//        binding.patient = patient
//        binding.device = device
//        val view = binding.root
//
//        val medicalHistoryTextView = view.findViewById<TextView>(R.id.medical_history_text)
//
//        // Load and display saved medical history (only unuploaded notes)
//        lifecycleScope.launch {
//            try {
//                val savedNoteData = noteDao.getUnuploadedNote()
//                if (savedNoteData != null && !savedNoteData.noteText.isNullOrEmpty()) {
//                    medicalHistoryTextView.text = "Medical History: ${savedNoteData.noteText.take(50)}${if (savedNoteData.noteText.length > 50) "..." else ""}"
//                    medicalHistoryTextView.visibility = View.VISIBLE
//                } else {
//                    medicalHistoryTextView.visibility = View.GONE
//                }
//            } catch (e: Exception) {
//                medicalHistoryTextView.visibility = View.GONE
//            }
//        }
//
//        view.findViewById<Button>(R.id.select_or_clear_patient).apply {
//            setOnClickListener {
//                if (patient.empty.value!!)
//                    startActivityForResult(intent, REQUEST_CONTACT)
//                else
//                    patient.clear()
//            }
//        }
//
//        view.findViewById<Button>(R.id.add_note_button).setOnClickListener {
//            val dialog = AddNoteDialog()
//            activity?.supportFragmentManager?.let {
//                dialog.show(it, "Add Note")
//            }
//        }
//
//        // Refresh medical history when fragment resumes (after dialog is dismissed)
//        viewLifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
//            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
//                refreshMedicalHistory(medicalHistoryTextView)
//            }
//        })
//
//        view.findViewById<Button>(R.id.record_or_stop).setOnClickListener {
//            val isRecording = device.recording.value == true
//            if (!isRecording) {
//                if (device.firstTimestamp == null) {
//                    device.firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                }
//                // Upload saved medical history from database (stored procedure) if exists
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val savedNoteData = AddNoteDialog.getSavedNoteWithId(requireContext(), noteDao)
//                        if (savedNoteData != null && !savedNoteData.noteText.isNullOrEmpty() && !savedNoteData.uploaded) {
//                            val stamp = device.firstTimestamp!!
//                            val savedNote = savedNoteData.noteText
//                            // uploader.note() queues WorkManager job (not suspend function)
//                            uploader.note(stamp, savedNote)
//                            // Mark note as uploaded (but keep it in database for retry if needed)
//                            AddNoteDialog.markNoteAsUploaded(requireContext(), noteDao, savedNoteData.id)
//                            android.util.Log.d("RecordFragment", "Medical history queued for upload, note ID: ${savedNoteData.id}")
//                        } else if (savedNoteData != null && savedNoteData.uploaded) {
//                            android.util.Log.d("RecordFragment", "Medical history already uploaded, skipping")
//                        }
//                    } catch (e: Exception) {
//                        // Log error but continue with recording
//                        android.util.Log.e("RecordFragment", "Error uploading note: ${e.message}", e)
//                    }
//                    // Ensure recording starts after note is processed
//                    withContext(Dispatchers.Main) {
//                        device.setRecording(true)
//                        // Clear medical history display after upload
//                        medicalHistoryTextView.visibility = View.GONE
//                    }
//                }
//            } else {
//                val firstTs = device.firstTimestamp
//                val lastTs = ((System.currentTimeMillis() / 15000) * 15).toInt()
//                if (firstTs != null) {
//                    val macId = device.address.value ?: "Unknown"
//                    CoroutineScope(Dispatchers.IO).launch {
//                        sendMacIdToTelegram(macId, firstTs, lastTs)
//                    }
//                }
//                device.setRecording(false)
//                device.firstTimestamp = null
//            }
//        }
//        return view
//    }
//
//    @SuppressLint("Range")
//    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
//        super.onActivityResult(request, result, data)
//        when {
//            result != RESULT_OK -> return
//            request == REQUEST_CONTACT && data != null -> {
//                data.data?.let {
//                    requireActivity().contentResolver.query(it, null, null, null, null)
//                        .use { cursor ->
//                            cursor?.use {
//                                if (cursor.count == 0) return
//                                cursor.moveToFirst()
//                                val name = cursor.getString(
//                                    cursor.getColumnIndex(
//                                        ContactsContract.Contacts.DISPLAY_NAME
//                                    )
//                                )
//                                val phone = cursor.getString(
//                                    cursor.getColumnIndex(
//                                        ContactsContract.CommonDataKinds.Phone.NUMBER
//                                    )
//                                )
//
//                                // TODO(vjn): find a better way than duplicating these checks.
//                                if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
//                                    Snackbar.make(
//                                        this.requireView(),
//                                        "Invalid phone number: '$phone'",
//                                        Snackbar.LENGTH_LONG
//                                    ).setAction("Action", null).show()
//
//                                } else if (name.length < 3) {
//                                    Snackbar.make(
//                                        this.requireView(),
//                                        "Name should be at least 3 letters: '$name'",
//                                        Snackbar.LENGTH_LONG
//                                    ).setAction("Action", null).show()
//                                } else {
//                                    patient.set(name, phone)
//                                }
//
//                            }
//                        }
//                }
//            }
//        }
//    }
//
////
////    @SuppressLint("Range")
////    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
////        super.onActivityResult(request, result, data)
////        when {
////            result != RESULT_OK -> return
////            request == REQUEST_CONTACT && data != null -> {
////                data.data?.let {
////                    requireActivity().contentResolver.query(it, null, null, null, null)
////                        .use { cursor ->
////                            cursor?.use {
////                                if (cursor.count == 0) return
////                                cursor.moveToFirst()
////                                val name = cursor.getString(
////                                    cursor.getColumnIndex(
////                                        ContactsContract.Contacts.DISPLAY_NAME
////                                    )
////                                )
////                                val phone = cursor.getString(
////                                    cursor.getColumnIndex(
////                                        ContactsContract.CommonDataKinds.Phone.NUMBER
////                                    )
////                                ).replace("\\s".toRegex(), "") // remove spaces
////
////                                val combined = "$name $phone"
////
////                                // Name format check
////                                val patientPattern = Regex(
////                                    """^[A-Za-z]{3}\d{1,10}\s+([A-Za-z]+(\s+[A-Za-z]+){0,5})\s+([1-9][0-9]?|1[01][0-9]|120)/(m|f)$""",
////                                    RegexOption.IGNORE_CASE
////                                )
////
////                                // Phone format check (Indian number)
////                                val phonePattern = Regex("^[6-9][0-9]{9}$")
////
////                                if (!patientPattern.matches(combined.trim())) {
////                                    showInvalidFormatDialog(
////                                        "Please follow the format:\n" +
////                                                "XXX123456789 Name Age/m\n" +
////                                                "Example: CKS7000123456 John Doe 45/m"
////                                    )
////                                } else if (!phonePattern.matches(phone)) {
////                                    showInvalidFormatDialog(
////                                        "Phone number is in incorrect format.\n" +
////                                                "Example: 9876543210"
////                                    )
////                                } else {
////                                    patient.set(name, phone)
////                                }
////                            }
////                        }
////                }
////            }
////        }
////    }
////
//////    private fun showInvalidFormatDialog(message: String) {
//////        val dialogView = LayoutInflater.from(requireContext())
//////            .inflate(R.layout.dialog_invalid_format, null)
//////
//////        val messageText = dialogView.findViewById<TextView>(R.id.txtMessage)
//////        messageText.text = message
//////        messageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
//////
//////        val dialog = AlertDialog.Builder(requireContext())
//////            .setView(dialogView)
//////            .setCancelable(false)
//////            .create()
//////
//////        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
//////            dialog.dismiss()
//////        }
//////
//////        dialog.show()
//////    }
////
////    private fun showInvalidFormatDialog(message: String) {
////        val dialogView = LayoutInflater.from(requireContext())
////            .inflate(R.layout.dialog_invalid_format, null)
////
////        val messageText = dialogView.findViewById<TextView>(R.id.txtMessage)
////        messageText.text = message
////        messageText.setTextColor(
////            ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
////        )
////
////        val dialog = AlertDialog.Builder(requireContext())
////            .setView(dialogView)
////            .create()
////
////        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
////            dialog.dismiss()
////        }
////
////        dialog.show()
////    }
////
////
//
//
//
//    private fun refreshMedicalHistory(textView: TextView) {
//        lifecycleScope.launch {
//            try {
//                val savedNoteData = noteDao.getUnuploadedNote()
//                if (savedNoteData != null && !savedNoteData.noteText.isNullOrEmpty()) {
//                    textView.text = "Medical History: ${savedNoteData.noteText.take(50)}${if (savedNoteData.noteText.length > 50) "..." else ""}"
//                    textView.visibility = View.VISIBLE
//                } else {
//                    textView.visibility = View.GONE
//                }
//            } catch (e: Exception) {
//                textView.visibility = View.GONE
//            }
//        }
//    }
//
//    companion object {
//        private const val REQUEST_CONTACT = 1001
//    }
//
//    private fun sendMacIdToTelegram(macId: String, firstTimestamp: Int, lastTimestamp: Int) {
//        try {
//            val botToken = "7597526068:AAGVJwkXbUO3R93UH4yWHtW5En-pYDf9Dl8"
//            val chatId = "738070910"
//
//            val message = """
//                📡 ECG Recording Completed
//                🔌 MAC ID: $macId
//                🕒 Start: $firstTimestamp
//                🕒 End:   $lastTimestamp
//            """.trimIndent()
//
//            val encodedMessage = URLEncoder.encode(message, "UTF-8")
//            val urlString =
//                "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
//
//            val url = URL(urlString)
//            with(url.openConnection() as HttpURLConnection) {
//                requestMethod = "GET"
//                connectTimeout = 5000
//                readTimeout = 5000
//                responseCode
//                disconnect()
//            }
//        } catch (_: Exception) {
//        }

//    }
//}
//
//
//





//new code
package com.carditek.kesar

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.carditek.kesar.databinding.FragmentRecordBinding
import com.carditek.kesar.module.Patient
import com.carditek.kesar.cloud.Uploader
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class RecordFragment : Fragment() {

    @Inject
    lateinit var patient: Patient

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var uploader: Uploader

    @Inject
    lateinit var noteDao: NoteDao

    private lateinit var binding: FragmentRecordBinding

    private val contactIntent = Intent(
        Intent.ACTION_PICK,
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.patient = patient
        binding.device = device

        setupButtons()
        refreshMedicalHistory()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshMedicalHistory()
    }

    // -------------------------------------------------
    // Button Setup
    // -------------------------------------------------

    private fun setupButtons() {

        // Select / Clear patient
        binding.selectOrClearPatient.setOnClickListener {
            if (patient.empty.value == true) {
                startActivityForResult(contactIntent, REQUEST_CONTACT)
            } else {
                patient.clear()
            }
        }

        // Add Medical History
        binding.addNoteButton.setOnClickListener {
            val dialog = AddNoteDialog()
            dialog.show(parentFragmentManager, "AddNoteDialog")
        }

        // Start / Stop Recording
        binding.recordOrStop.setOnClickListener {

            val recording = device.recording.value == true

            if (!recording) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    // -------------------------------------------------
    // Start Recording
    // -------------------------------------------------

    private fun startRecording() {

        if (device.firstTimestamp == null) {
            device.firstTimestamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
        }

        device.setRecording(true)

        // Upload medical history in background
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            try {

                val savedNote = AddNoteDialog.getSavedNoteWithId(requireContext(), noteDao)

                if (savedNote != null &&
                    !savedNote.noteText.isNullOrEmpty() &&
                    !savedNote.uploaded
                ) {

                    val stamp = device.firstTimestamp!!

                    uploader.note(stamp, savedNote.noteText)

                    AddNoteDialog.markNoteAsUploaded(
                        requireContext(),
                        noteDao,
                        savedNote.id
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("RecordFragment", "Note upload error", e)
            }

            withContext(Dispatchers.Main) {
                binding.medicalHistoryText.visibility = View.GONE
            }
        }
    }

    // -------------------------------------------------
    // Stop Recording
    // -------------------------------------------------

    private fun stopRecording() {

        val firstTs = device.firstTimestamp
        val lastTs = ((System.currentTimeMillis() / 15000) * 15).toInt()

        device.setRecording(false)
        device.firstTimestamp = null

        if (firstTs != null) {

            val macId = device.address.value ?: "Unknown"

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                sendMacIdToTelegram(macId, firstTs, lastTs)
            }
        }
    }

    // -------------------------------------------------
    // Refresh Medical History UI
    // -------------------------------------------------

    private fun refreshMedicalHistory() {

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val note = noteDao.getUnuploadedNote()

                if (note != null && !note.noteText.isNullOrEmpty()) {

                    binding.medicalHistoryText.text =
                        "Medical History: ${note.noteText.take(120)}"

                    binding.medicalHistoryText.visibility = View.VISIBLE

                } else {

                    binding.medicalHistoryText.visibility = View.GONE
                }

            } catch (_: Exception) {

                binding.medicalHistoryText.visibility = View.GONE
            }
        }
    }

    // -------------------------------------------------
    // Contact Selection
    // -------------------------------------------------

    @SuppressLint("Range")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {

        super.onActivityResult(request, result, data)

        if (result != RESULT_OK) return

        if (request == REQUEST_CONTACT && data != null) {

            data.data?.let {

                requireActivity().contentResolver.query(it, null, null, null, null)
                    .use { cursor ->

                        cursor?.let {

                            if (cursor.count == 0) return

                            cursor.moveToFirst()

                            val name = cursor.getString(
                                cursor.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME
                                )
                            )

                            val phone = cursor.getString(
                                cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )

                            if (!android.util.Patterns.PHONE.matcher(phone).matches()) {

                                Snackbar.make(
                                    requireView(),
                                    "Invalid phone number: $phone",
                                    Snackbar.LENGTH_LONG
                                ).show()

                            } else if (name.length < 3) {

                                Snackbar.make(
                                    requireView(),
                                    "Name must be at least 3 characters",
                                    Snackbar.LENGTH_LONG
                                ).show()

                            } else {

                                patient.set(name, phone)
                            }
                        }
                    }
            }
        }
    }

    // -------------------------------------------------
    // Telegram Notification
    // -------------------------------------------------

    private fun sendMacIdToTelegram(
        macId: String,
        firstTimestamp: Int,
        lastTimestamp: Int
    ) {

        try {

            val botToken = "YOUR_BOT_TOKEN"
            val chatId = "YOUR_CHAT_ID"

            val message = """
ECG Recording Completed
MAC ID: $macId
Start: $firstTimestamp
End: $lastTimestamp
""".trimIndent()

            val encodedMessage = URLEncoder.encode(message, "UTF-8")

            val url = URL(
                "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
            )

            with(url.openConnection() as HttpURLConnection) {

                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                responseCode
                disconnect()
            }

        } catch (_: Exception) {
        }
    }

    companion object {
        private const val REQUEST_CONTACT = 1001
    }
}
