////package com.carditek.kesar
////
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import androidx.fragment.app.DialogFragment
////import com.carditek.kesar.databinding.AddNoteDialogBinding
////
////class AddNoteDialog(private val handler: (String) -> Unit) : DialogFragment() {
////    override fun onCreateView(
////        inflater: LayoutInflater,
////        container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View {
////        val binding = AddNoteDialogBinding.inflate(inflater)
////        binding.saveText.setOnClickListener {
////            handler(binding.notes.text.toString())
////            dismiss()
////        }
////        return binding.root
////    }
////}
//
//
//
////  second stage update
//
//
package com.carditek.kesar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.carditek.kesar.databinding.AddNoteDialogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AddNoteDialog : DialogFragment() {
    
    @Inject
    lateinit var noteDao: NoteDao
    
    companion object {
        suspend fun getSavedNote(context: Context, noteDao: NoteDao): String? {
            // Get the latest unuploaded note first, if none, get the latest note
            return noteDao.getUnuploadedNote()?.noteText ?: noteDao.getLatestNote()?.noteText
        }
        
        suspend fun getSavedNoteWithId(context: Context, noteDao: NoteDao): MedicalNote? {
            // Get the latest unuploaded note first, if none, get the latest note
            return noteDao.getUnuploadedNote() ?: noteDao.getLatestNote()
        }
        
        suspend fun markNoteAsUploaded(context: Context, noteDao: NoteDao, noteId: Long) {
            noteDao.markAsUploaded(noteId, System.currentTimeMillis())
            // Clean up old uploaded notes (keep only recent ones)
            noteDao.deleteUploadedNotes()
        }
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
            "Short Holter",
            "Long Holter"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, scanTypes)
        binding.scanTypeSpinner.adapter = adapter

        binding.saveText.setOnClickListener {
            val noteText = binding.notes.text.toString().trim()

            // Collect selected options
            val selectedConditions = mutableListOf<String>()
            if (root.findViewById<CheckBox>(R.id.option_PostEcg).isChecked) selectedConditions.add("Post Ecg")
            if (root.findViewById<CheckBox>(R.id.option_Thyroid).isChecked) selectedConditions.add("Thyroid")
            if (root.findViewById<CheckBox>(R.id.option_Hypertension).isChecked) selectedConditions.add("Hypertension")
            if (root.findViewById<CheckBox>(R.id.option_DiabetesMellitu).isChecked) selectedConditions.add("Diabetes Mellitus")
            if (root.findViewById<CheckBox>(R.id.option_HeartAttack).isChecked) selectedConditions.add("Heart Attack")
//            if (root.findViewById<CheckBox>(R.id.option_Palpitations).isChecked) selectedConditions.add("Palpitations")
            if (root.findViewById<CheckBox>(R.id.option_ChestPain).isChecked) selectedConditions.add("Chest Pain")
            if (root.findViewById<CheckBox>(R.id.option_HistoryofStroke).isChecked) selectedConditions.add("History of Stroke")
            if (root.findViewById<CheckBox>(R.id.option_CoronaryStent).isChecked) selectedConditions.add("Coronary Stent")
            if (root.findViewById<CheckBox>(R.id.option_BypassSurgery).isChecked) selectedConditions.add("Bypass Surgery")
            if (root.findViewById<CheckBox>(R.id.option_LegSwelling).isChecked) selectedConditions.add("Leg Swelling")
            if (root.findViewById<CheckBox>(R.id.option_generalcheckup).isChecked) selectedConditions.add("General Checkup")
            if (root.findViewById<CheckBox>(R.id.option_BreathingProblems).isChecked) selectedConditions.add("Breathing Problems")
            if (root.findViewById<CheckBox>(R.id.option_Hcm).isChecked) selectedConditions.add("Hcm")
            if (root.findViewById<CheckBox>(R.id.option_Dcm).isChecked) selectedConditions.add("Dcm")
            if (root.findViewById<CheckBox>(R.id.option_Holter).isChecked) selectedConditions.add("Holter")



            // Collect scan type from Spinner
            val scanTypeSelected = binding.scanTypeSpinner.selectedItem?.toString()?.takeIf { it != "Select Scan Type" }

            // Combine note and selections
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

            // Ensure it's not empty
            if (finalNote.isEmpty()) {
                binding.notes.error = "Please enter a note or select a history option"
            } else {
                // Save to database (stored procedure)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Delete any existing uploaded notes (cleanup)
                        noteDao.deleteUploadedNotes()
                        // Clear any existing unuploaded note first (replace with new one)
                        val existingNote = noteDao.getUnuploadedNote()
                        existingNote?.let {
                            noteDao.deleteNote(it.id)
                        }
                        // Insert new note (not uploaded yet)
                        val note = MedicalNote(noteText = finalNote, uploaded = false)
                        noteDao.insert(note)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Medical History saved successfully", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error saving medical history: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
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














