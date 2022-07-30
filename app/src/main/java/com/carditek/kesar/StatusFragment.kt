package com.carditek.kesar

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.databinding.FragmentStatusBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StatusFragment : Fragment() {
    private lateinit var binding: FragmentStatusBinding
    private val handler = Handler()
    private var previousTimestamp: Long = 0  // previous value
    private var previousBytes: Int = 0      // previous value

    @Inject
    lateinit var state: State

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.state = state
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(this::periodic)
    }

    override fun onResume() {
        super.onResume()
        previousTimestamp = System.currentTimeMillis()
        handler.post(this::periodic)
    }

    @SuppressLint("SetTextI18n")
    private fun periodic() {
        val current = System.currentTimeMillis()
        val bytes = state.stats.bytes.total
        val kbps = if (current > previousTimestamp) {
            (bytes - previousBytes).toFloat() / (current - previousTimestamp)
        } else 0.0F
        previousBytes = bytes
        previousTimestamp = current

        val packets = state.stats.packets
        val cloud = state.stats.cloud
        val disconnects = state.stats.connections.disconnects
        var address: String = state.address.value!!
        address = if (address == "") "(none)" else address
        binding.textMessage.text = "MAC: $address\n" +
                "Connections: $disconnects disconnects.\n" +
                "Packets: ${packets.total} total.\n" +
                "    ${packets.short} short, ${packets.skips} skips, " +
                "${packets.error} errors.\n" +
                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
                        + "\n\n\n") +
                "Version ${BuildConfig.VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
        handler.postDelayed(this::periodic, 5000)
    }
}
