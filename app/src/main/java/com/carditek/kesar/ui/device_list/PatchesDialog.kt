package com.carditek.kesar.ui.device_list

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.carditek.kesar.R

class PatchesDialog(
    private val devices: Array<String>,
    private val selected: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.dialog_title_patches)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    Log.i(TAG, "Canceling dialog")
                }
                .setItems(devices) { _, index -> selected(devices[index]) }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        private const val TAG = "Patches"
    }
}