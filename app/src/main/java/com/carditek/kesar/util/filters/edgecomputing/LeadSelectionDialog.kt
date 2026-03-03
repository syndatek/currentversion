package com.carditek.kesar.util.filters.edgecomputing

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.carditek.kesar.R

class LeadSelectionDialog(
    private val initialSelectedLeads: Set<Int>,
    private val onLeadsSelected: (Set<Int>) -> Unit
) : DialogFragment() {

    private val leadCheckboxes = mutableMapOf<Int, CheckBox>()
    private lateinit var checkboxSelectAll: CheckBox
    private lateinit var buttonCancel: Button
    private lateinit var buttonApply: Button

    companion object {
        // Lead mapping: Display name -> Lead number (0-indexed internally, 1-indexed for display)
        // Lead 1 = 0, Lead 2 = 1, V1 = 2, V2 = 3, V3 = 4, V4 = 5, V5 = 6, V6 = 7
        private val LEAD_MAPPING = mapOf(
            R.id.checkbox_lead1 to 0,  // Lead 1
            R.id.checkbox_lead2 to 1,  // Lead 2
            R.id.checkbox_v1 to 2,     // V1
            R.id.checkbox_v2 to 3,     // V2
            R.id.checkbox_v3 to 4,     // V3
            R.id.checkbox_v4 to 5,     // V4
            R.id.checkbox_v5 to 6,     // V5
            R.id.checkbox_v6 to 7      // V6
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle("Select Leads for SNR & Saturation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.dialog_lead_selection, container, false)

        // Get references to checkboxes
        checkboxSelectAll = root.findViewById(R.id.checkbox_select_all)
        buttonCancel = root.findViewById(R.id.button_cancel)
        buttonApply = root.findViewById(R.id.button_apply)

        // Map checkboxes to lead numbers
        LEAD_MAPPING.forEach { (checkboxId, leadIndex) ->
            val checkbox = root.findViewById<CheckBox>(checkboxId)
            leadCheckboxes[leadIndex] = checkbox
        }

        // Set initial state
        initialSelectedLeads.forEach { leadIndex ->
            leadCheckboxes[leadIndex]?.isChecked = true
        }

        // Update "Select All" checkbox
        updateSelectAllCheckbox()

        // Handle "Select All" checkbox
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            leadCheckboxes.values.forEach { it.isChecked = isChecked }
        }

        // Handle individual checkbox changes
        leadCheckboxes.values.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                updateSelectAllCheckbox()
            }
        }

        // Handle buttons
        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonApply.setOnClickListener {
            val selectedLeads = leadCheckboxes
                .filter { it.value.isChecked }
                .map { it.key }
                .toSet()

            if (selectedLeads.isEmpty()) {
                // Show warning if no leads selected
                android.widget.Toast.makeText(
                    requireContext(),
                    "Please select at least one lead",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                onLeadsSelected(selectedLeads)
                dismiss()
            }
        }

        return root
    }

    private fun updateSelectAllCheckbox() {
        val allChecked = leadCheckboxes.values.all { it.isChecked }
        checkboxSelectAll.isChecked = allChecked
    }
}

