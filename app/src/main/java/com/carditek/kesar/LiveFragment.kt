package com.carditek.kesar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.carditek.kesar.cloud.Uploader
import com.carditek.kesar.databinding.FragmentLiveBinding
import com.carditek.kesar.module.Patient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LiveFragment : WebViewFragment() {
    @Inject
    lateinit var uploader: Uploader

    private lateinit var binding: FragmentLiveBinding

    override fun url(): String {
        var address = device.address.value
        if (address == null || address == "")
            address = "54:6C:0E:83:3E:49"
        return "https://ecg.carditek.com/#/tail/$address"
    }

    override fun webView(): WebView = binding.liveWebview

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var patient: Patient

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
        device.address.observe(viewLifecycleOwner, { maybeEnable() })
        patient.empty.observe(viewLifecycleOwner, { maybeEnable() })
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.fabRecord.setOnClickListener {
            device.setRecording(!device.recording.value!!)
        }
        binding.fabAddNote.setOnClickListener {
            Log.i(TAG, "Adding a note")
            val dialog = AddNoteDialog() {
                // This is a sloppier computation than the one in DataHandler.  TODO(vjn): tie the
                // two up to use the same, monotonically-increasing one.
                val stamp = ((System.currentTimeMillis() / 15000) * 15).toInt()
                GlobalScope.launch {
                    uploader.note(stamp, it)
                }
            }
            activity?.supportFragmentManager?.let {
                dialog.show(it, "Add Note")
            }
        }
    }

    companion object {
        private const val TAG = "LiveFragment"
    }
}
