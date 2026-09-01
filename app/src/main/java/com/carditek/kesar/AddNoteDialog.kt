
package com.carditek.kesar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.carditek.kesar.cloud.Uploader
import com.carditek.kesar.databinding.AddNoteDialogBinding
import com.carditek.kesar.module.Patient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AddNoteDialog : DialogFragment() {

    @Inject
    lateinit var noteDao: NoteDao

    @Inject
    lateinit var uploader: Uploader

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var patient: Patient

    companion object {

        suspend fun getSavedNote(context: Context, noteDao: NoteDao): String? {
            return noteDao.getUnuploadedNote()?.noteText ?: noteDao.getLatestNote()?.noteText
        }

        suspend fun getSavedNoteWithId(context: Context, noteDao: NoteDao): MedicalNote? {
            return noteDao.getUnuploadedNote() ?: noteDao.getLatestNote()
        }

        // Intentionally do not mark uploaded here.
        // Upload success is confirmed inside `Uploader.NoteWorker`, which then marks the DB row.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = AddNoteDialogBinding.inflate(inflater)
        val root = binding.root

        val scanTypes = listOf(
            "Select Scan Type",
            "12 lead Pre Ecg",
            "12 Lead post Ecg",
            "Advance Ecg",
            "Long-Holter",
            "Short-Holter",
            "HFECG in Angioplasty",
            "Surface Hisbundle"


        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            scanTypes
        )

        binding.scanTypeSpinner.adapter = adapter

        binding.saveText.setOnClickListener {

//            val scanType =//add autostop
//                binding.scanTypeSpinner.selectedItem?.toString()
//                    ?.takeIf { it != "Select Scan Type" }
//
//// ✅ SET MAX TIMESTAMP BASED ON SELECTION
//            device.maxTimestamps = when (scanType) {
//
//                "12 lead Pre Ecg",
//                "12 Lead post Ecg" -> 17   // 5 timestamps (75 sec)
//
//                "Short-Holter" -> 30      // 7.5 min
//
//                "Long-Holter" -> 120      // 30 min
//
//                else -> null              // No auto-stop for other types
//            }// till added

            val noteText = binding.notes.text.toString().trim()

            val selectedConditions = mutableListOf<String>()

            fun addIfChecked(id: Int, text: String) {
                if (root.findViewById<CheckBox>(id).isChecked) {
                    selectedConditions.add(text)
                }
            }

            addIfChecked(R.id.option_Thyroid, "Thyroid")
            addIfChecked(R.id.option_Hypertension, "Hypertension")
            addIfChecked(R.id.option_DiabetesMellitus, "Diabetes Mellitus")
            addIfChecked(R.id.option_HeartAttack, "Heart Attack")
            addIfChecked(R.id.option_ChestPain, "Chest Pain")
            addIfChecked(R.id.option_HistoryofStroke, "History of Stroke")
            addIfChecked(R.id.option_CoronaryStent, "Coronary Stent")
            addIfChecked(R.id.option_BypassSurgery, "Bypass Surgery")
            addIfChecked(R.id.option_LegSwelling, "Leg Swelling")
            addIfChecked(R.id.option_Generalcheckup, "General Checkup")
            addIfChecked(R.id.option_BreathingProblems, "Breathing Problems")
            addIfChecked(R.id.option_Hcm, "Hcm")
            addIfChecked(R.id.option_Dcm, "Dcm")
            addIfChecked(R.id.option_PostEcg, "Post Ecg")
            addIfChecked(R.id.option_Holter, "Holter")


            val scanTypeSelected =
                binding.scanTypeSpinner.selectedItem?.toString()
                    ?.takeIf { it != "Select Scan Type" }

            val finalNote = buildString {

                if (!scanTypeSelected.isNullOrEmpty()) {
                    append("Scan Type: $scanTypeSelected\n")
                }

                if (selectedConditions.isNotEmpty()) {
                    append("Medical History: ${selectedConditions.joinToString(", ")}\n")
                }

                if (noteText.isNotEmpty()) {
                    append("Note: $noteText")
                }

            }.trim()

            if (finalNote.isEmpty()) {

                binding.notes.error = "Please enter a note or select a history option"
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

                try {
                    val note = MedicalNote(
                        noteText = finalNote,
                        uploaded = false
                    )

                    val id = noteDao.insert(note)

                    // Upload immediately
                    val timestamp =
                        device.firstTimestamp
                            ?: ((System.currentTimeMillis() / 15000) * 15).toInt()

                    // Queue upload; DB row is marked uploaded only on worker success.
                    uploader.note(timestamp, finalNote, id)

                    withContext(Dispatchers.Main) {
                        dismiss()
                    }

                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error saving medical history: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        return root
    }
}


// 12/08/2025
//
//package com.carditek.kesar
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.CheckBox
//import android.widget.Toast
//import androidx.fragment.app.DialogFragment
//import com.carditek.kesar.databinding.AddNoteDialogBinding
//
//class AddNoteDialog(private val handler: (String) -> Boolean) : DialogFragment() {
//    // handler now returns Boolean → true if saved, false if failed
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val binding = AddNoteDialogBinding.inflate(inflater)
//        val root = binding.root
//
//        binding.saveText.setOnClickListener {
//            val noteText = binding.notes.text.toString().trim()
//
//            // Collect selected options
//            val selectedConditions = mutableListOf<String>()
//            if (root.findViewById<CheckBox>(R.id.option_PostEcg).isChecked) selectedConditions.add("Post Ecg")
//            if (root.findViewById<CheckBox>(R.id.option_Thyroid).isChecked) selectedConditions.add("Thyroid")
//            if (root.findViewById<CheckBox>(R.id.option_Hypertension).isChecked) selectedConditions.add("Hypertension")
//            if (root.findViewById<CheckBox>(R.id.option_DiabetesMellitu).isChecked) selectedConditions.add("Diabetes Mellitus")
//            if (root.findViewById<CheckBox>(R.id.option_HeartAttack).isChecked) selectedConditions.add("Heart Attack")
//            if (root.findViewById<CheckBox>(R.id.option_ChestPain).isChecked) selectedConditions.add("Chest Pain")
//            if (root.findViewById<CheckBox>(R.id.option_HistoryofStroke).isChecked) selectedConditions.add("History of Stroke")
//            if (root.findViewById<CheckBox>(R.id.option_CoronaryStent).isChecked) selectedConditions.add("Coronary Stent")
//            if (root.findViewById<CheckBox>(R.id.option_BypassSurgery).isChecked) selectedConditions.add("Bypass Surgery")
//            if (root.findViewById<CheckBox>(R.id.option_LegSwelling).isChecked) selectedConditions.add("Leg Swelling")
//            if (root.findViewById<CheckBox>(R.id.option_generalcheckup).isChecked) selectedConditions.add("General Checkup")
//            if (root.findViewById<CheckBox>(R.id.option_BreathingProblems).isChecked) selectedConditions.add("Breathing Problems")
//            if (root.findViewById<CheckBox>(R.id.option_Holter).isChecked) selectedConditions.add("Holter")
//
//            // Combine note and selections
//            val finalNote = buildString {
//                if (selectedConditions.isNotEmpty()) {
//                    append("Medical History: ${selectedConditions.joinToString(", ")}\n")
//                }
//                if (noteText.isNotEmpty()) {
//                    append("Note: $noteText")
//                }
//            }.trim()
//
//            // Ensure it's not empty
//            if (finalNote.isEmpty()) {
//                binding.notes.error = "Please enter a note or select a history option"
//            } else {
//                val saved = handler(finalNote) // Now we know if it's saved or not
//                if (saved) {
//                    Toast.makeText(requireContext(), "✅ Data saved successfully", Toast.LENGTH_SHORT).show()
//                    dismiss()
//                } else {
//                    Toast.makeText(requireContext(), "❌ Data not saved", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        return root
//    }
//}
//
//














