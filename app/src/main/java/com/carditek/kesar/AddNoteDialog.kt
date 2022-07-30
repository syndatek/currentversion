package com.carditek.kesar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.carditek.kesar.databinding.AddNoteDialogBinding

class AddNoteDialog(private val handler: (String) -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = AddNoteDialogBinding.inflate(inflater)
        binding.saveText.setOnClickListener {
            handler(binding.notes.text.toString())
            dismiss()
        }
        return binding.root
    }
}
