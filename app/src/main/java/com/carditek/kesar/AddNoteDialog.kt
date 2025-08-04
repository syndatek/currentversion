//package com.carditek.kesar
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.DialogFragment
//import com.carditek.kesar.databinding.AddNoteDialogBinding
//
//class AddNoteDialog(private val handler: (String) -> Unit) : DialogFragment() {
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val binding = AddNoteDialogBinding.inflate(inflater)
//        binding.saveText.setOnClickListener {
//            handler(binding.notes.text.toString())
//            dismiss()
//        }
//        return binding.root
//    }
//}



//  second stage update


package com.carditek.kesar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.carditek.kesar.databinding.AddNoteDialogBinding

class AddNoteDialog(private val handler: (String) -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AddNoteDialogBinding.inflate(inflater)
        val root = binding.root

        binding.saveText.setOnClickListener {
            val noteText = binding.notes.text.toString().trim()

            // Collect selected options
            val selectedConditions = mutableListOf<String>()
            if (root.findViewById<CheckBox>(R.id.option_PostEcg).isChecked) selectedConditions.add("Post Ecg")
            if (root.findViewById<CheckBox>(R.id.option_Thyroid).isChecked) selectedConditions.add("Thyroid")
            if (root.findViewById<CheckBox>(R.id.option_Hypertension).isChecked) selectedConditions.add("Hypertension")
            if (root.findViewById<CheckBox>(R.id.option_DiabetesMellitu).isChecked) selectedConditions.add("Diabetes Mellitus")
            if (root.findViewById<CheckBox>(R.id.option_HeartAttack).isChecked) selectedConditions.add("Heart Attack")
            if (root.findViewById<CheckBox>(R.id.option_Palpitations).isChecked) selectedConditions.add("Palpitations")
            if (root.findViewById<CheckBox>(R.id.option_ChestPain).isChecked) selectedConditions.add("Chest Pain")
            if (root.findViewById<CheckBox>(R.id.option_HistoryofStroke).isChecked) selectedConditions.add("History of Stroke")
            if (root.findViewById<CheckBox>(R.id.option_CoronaryStent).isChecked) selectedConditions.add("Coronary Stent")
            if (root.findViewById<CheckBox>(R.id.option_BypassSurgery).isChecked) selectedConditions.add("Bypass Surgery")
            if (root.findViewById<CheckBox>(R.id.option_LegSwelling).isChecked) selectedConditions.add("Leg Swelling")
            if (root.findViewById<CheckBox>(R.id.option_generalcheckup).isChecked) selectedConditions.add("General Checkup")
            if (root.findViewById<CheckBox>(R.id.option_BreathingProblems).isChecked) selectedConditions.add("Breathing Problems")
            if (root.findViewById<CheckBox>(R.id.option_Allergy).isChecked) selectedConditions.add("Allergy")
            if (root.findViewById<CheckBox>(R.id.option_Holter).isChecked) selectedConditions.add("Holter")

            // Combine note and selections
            val finalNote = buildString {
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
                handler(finalNote)
                dismiss()
            }
        }

        return root
    }
}









