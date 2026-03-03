

// remove the stop option

package com.carditek.kesar

import com.carditek.kesar.bluetooth.DataHandler
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan




import com.carditek.kesar.bluetooth.Connection
import android.widget.Switch
import android.annotation.SuppressLint
import android.os.BatteryManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.databinding.FragmentStatusBinding
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.carditek.kesar.BuildConfig
import androidx.navigation.fragment.findNavController
import com.carditek.kesar.service.BluetoothService
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class StatusFragment : Fragment() {

    private lateinit var binding: FragmentStatusBinding
    private val handler = Handler(Looper.getMainLooper())

    private var previousTimestamp: Long = 0
    private var previousBytes: Int = 0
    private var isUpdating = false
    private var lastKnownKbps: Float = 0f

    @Inject
    lateinit var state: State

    @Inject
    lateinit var dataHandler: DataHandler
    //add september 15 fow switch
    // Device control state
//    private var isDeviceActive = true//till now
    private var isDeviceActive = true
    private var pendingDeviceCommand: Boolean? = null // Store pending command when device is not connected//till now

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatusBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.state = state

        binding.buttonToggle.text = "Start" // Always show "Start"

        binding.buttonToggle.setOnClickListener {
            val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)

            if (isConnected && lastKnownKbps in 24f..25.9f) {
                findNavController().navigate(R.id.nav_live)
            } else {
                Snackbar.make(binding.root, "Device not ready yet. Please wait...", Snackbar.LENGTH_SHORT).show()
            }
        }
        //add 15 th september switch
        // Device mode switch setup
        binding.switchDeviceMode.setOnCheckedChangeListener { _, isChecked ->
            Log.d("DeviceControl", "Switch toggled: $isChecked (${if (isChecked) "Active" else "Sleep"})")
            isDeviceActive = isChecked
            updateSwitchAppearance()
            sendDeviceCommand(isChecked)
        }//till now
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startPeriodicUpdates()
    }

    override fun onPause() {
        super.onPause()
        // We don’t stop updates anymore, you can keep it if needed
        // stopPeriodicUpdates()
    }

    private fun startPeriodicUpdates() {
        if (isUpdating) return
        isUpdating = true
        previousTimestamp = System.currentTimeMillis()
        handler.post { periodic() }
    }
    /**
     * Check and send pending device command when device connects
     */
    private fun checkPendingDeviceCommand() {
        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
        if (isConnected && pendingDeviceCommand != null) {
            val pendingCommand = pendingDeviceCommand!!
            Log.i("DeviceControl", "Device connected - sending pending command: ${if (pendingCommand) "Active" else "Sleep"}")

            // Send the pending command
            sendDeviceCommand(pendingCommand)

            // Clear pending command
            pendingDeviceCommand = null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun periodic() {
        if (!isUpdating) return

        val current = System.currentTimeMillis()
        val bytes = state.stats.bytes.total
        val kbps = if (current > previousTimestamp) {
            (bytes - previousBytes).toFloat() / (current - previousTimestamp)
        } else 0.0F
        lastKnownKbps = kbps

        previousBytes = bytes
        previousTimestamp = current

        val packets = state.stats.packets
        val cloud = state.stats.cloud
        val disconnects = state.stats.connections.disconnects
        var address: String = state.address.value ?: ""
        address = if (address == "") "(none)" else address

//        binding.textMessage.text = "MAC: $address\n" +
//                "Connections: $disconnects disconnects.\n" +
//                "Packets: ${packets.total} total.\n" +
//                "    ${packets.short} short, ${packets.skips} skips, " +
//                "${packets.error} errors.\n" +
//                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
//                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
//                        + "\n\n\n") +
//                "Version ${BuildConfig.APP_VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
        val textBuilder = SpannableStringBuilder()

        textBuilder.append("MAC: $address\n")
        textBuilder.append("Connections: $disconnects disconnects.\n")
        textBuilder.append("Packets: ${packets.total} total.\n")
        textBuilder.append("    ${packets.short} short, ${packets.skips} skips, ${packets.error} errors.\n")

// Format KB/s line
        val kbpsText = "%.2fKB/s".format(kbps)
        val bytesLine = "Bytes: $bytes, $kbpsText\n"

// Append and track position of KB/s text
        val startKbps = textBuilder.length + bytesLine.indexOf(kbpsText)
        val endKbps = startKbps + kbpsText.length

        textBuilder.append(bytesLine)

// Apply green color if kbps > 24
//        if (kbps > 24) {
        if (kbps in 24.0..25.9) {
            textBuilder.setSpan(
                ForegroundColorSpan(Color.GREEN),
                startKbps,
                endKbps,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textBuilder.append("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending.\n\n\n")
        textBuilder.append("Version ${BuildConfig.APP_VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}")

        binding.textMessage.setText(textBuilder, TextView.BufferType.SPANNABLE)





//        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
//        updateReadyBanner(kbps, isConnected)

        handler.postDelayed({ periodic() }, 1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataHandler.batteryPercentage.observe(viewLifecycleOwner) { percentage ->
            updateBatteryPercentage(percentage)
        }
        // Initialize switch appearance
//        updateSwitchAppearance()
        initializeSwitchState()
    }

    @SuppressLint("SetTextI18n")
    private fun updateBatteryPercentage(batteryPercentage: Int) {
        binding.textBattery.text = "SYDANTEK LIPO BAT: $batteryPercentage%"
        Log.i("Battery", "Battery Percentage: $batteryPercentage%")
    }
//this ready to go banner code

//    @SuppressLint("SetTextI18n")
//    private fun updateReadyBanner(kbPerSec: Float, isConnected: Boolean) {
//        val banner = binding.textReadyBanner
//        val show = isConnected && kbPerSec in 24f..28.9f
//
//        if (show) {
//            banner.text = "Ready to Go"
//            banner.visibility = View.VISIBLE
//        } else {
//            banner.visibility = View.GONE
//        }
//        // Check for pending device commands when device connects
//        checkPendingDeviceCommand()
//        Log.d("READY", "connected=$isConnected  kbps=$kbPerSec  show=$show")
//    }
    // september 15 added for switch
    /**
     * Initialize switch state and appearance
     */
    private fun initializeSwitchState() {
        val switch = binding.switchDeviceMode
        // Set initial state based on isDeviceActive (defaults to true)
        switch.isChecked = isDeviceActive
        updateSwitchAppearance()
        Log.d("DeviceControl", "Switch initialized: ${if (isDeviceActive) "Active" else "Sleep"}")
    }
    /**
     * Update switch appearance based on device state
     */
    private fun updateSwitchAppearance() {
        val switch = binding.switchDeviceMode
        val modeText = binding.textDeviceMode
        if (isDeviceActive) {
            // Active state - Green colors
            switch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.device_active_color))
            switch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.device_active_track_color))
//            switch.text = "Active"
            modeText.text="Active"
            modeText.setTextColor(ContextCompat.getColor(requireContext(),R.color.device_active_color))
            Log.d("DeviceControl", "Switch set to Active mode (Green)")
        } else {
            // Sleep state - Red colors
            switch.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.device_sleep_color))
            switch.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.device_sleep_track_color))
//            switch.text = "Sleep"
            modeText.text = "Sleep"
            modeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.device_sleep_color))
            Log.d("DeviceControl", "Switch set to Sleep mode (Red)")
        }
    }

    /**
     * Send GATT write command to device
     * @param isActive true for active mode (292), false for sleep mode (929)
     */
//    private fun sendDeviceCommand(isActive: Boolean) {
//        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
//
//        if (!isConnected) {
//            Snackbar.make(binding.root, "Device not connected", Snackbar.LENGTH_SHORT).show()
//            return
//        }
//
//        try {
//            // Convert command to byte array
//            val command = if (isActive) {
//                // Active mode command: 292
//                byteArrayOf(0x24.toByte(), 0x01.toByte()) // 292 in little-endian format
//            } else {
//                // Sleep mode command: 929
//                byteArrayOf(0xA1.toByte(), 0x03.toByte()) // 929 in little-endian format
//            }
//
//            // Get current connection and send GATT write
//            val connection = BluetoothService.getCurrentConnection()
//            if (connection != null) {
//                val success = connection.writeCharacteristic(command)
//                if (success) {
//                    val message = if (isActive) "Device set to Active mode" else "Device set to Sleep mode"
//                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//                    Log.i("DeviceControl", "GATT write successful: ${if (isActive) "292 (Active)" else "929 (Sleep)"}")
//                } else {
//                    Snackbar.make(binding.root, "Failed to send command to device", Snackbar.LENGTH_SHORT).show()
//                    Log.e("DeviceControl", "GATT write failed")
//                }
//            } else {
//                Snackbar.make(binding.root, "No active connection to device", Snackbar.LENGTH_SHORT).show()
//                Log.e("DeviceControl", "No connection available for GATT write")
//            }
//
//        } catch (e: Exception) {
//            Log.e("DeviceControl", "Error sending device command", e)
//            Snackbar.make(binding.root, "Error sending command to device", Snackbar.LENGTH_SHORT).show()
//        }
//    }
    /**
     * Send GATT write command to device
     * @param isActive true for active mode, false for sleep mode
     */
    private fun sendDeviceCommand(isActive: Boolean) {
        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)

        // Always update the switch state regardless of connection status
        val modeText = if (isActive) "Active" else "Sleep"
        val modeEmoji = if (isActive) "." else "."

        if (!isConnected) {
//            Snackbar.make(binding.root, "Device not connected", Snackbar.LENGTH_SHORT).show()
//            // Revert switch state if device is not connected
//            binding.switchDeviceMode.isChecked = !isActive
//            isDeviceActive = !isActive
//            updateSwitchAppearance()
//            return
            // Device not connected - switch still works but command is queued
            pendingDeviceCommand = isActive
            val message = "$modeEmoji Switch set to $modeText mode (Device not connected - command will be sent when connected)"
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            Log.i("DeviceControl", "Switch set to $modeText mode (device not connected - command queued)")
            return
        }

        try {
            // Use ASCII string commands instead of numeric byte arrays
            val commandString = if (isActive) {
                "2#9*2$"   // Active mode
            } else {
                "9#2*9$"   // Sleep mode
            }

            // Convert string to byte array (UTF-8 encoding)
            val command = commandString.toByteArray(Charsets.UTF_8)

            // Get current connection and send GATT write
            val connection = BluetoothService.getCurrentConnection()
            if (connection != null) {
                val success = connection.writeCharacteristic(command)
                if (success) {
//                    val message = if (isActive) "Device set to Active mode" else "Device set to Sleep mode"
//                    val message = if (isActive) " Device set to Active mode (Green)" else " Device set to Sleep mode (Red)"
                    val message = "$modeEmoji Device set to $modeText mode (Green/Red)"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//                    Log.i("DeviceControl", "GATT write successful: $commandString")
//                    Log.i("DeviceControl", "GATT write successful: $commandString - ${if (isActive) "Active" else "Sleep"}")
                    Log.i("DeviceControl", "GATT write successful: $commandString - $modeText")
                } else {
////                    Snackbar.make(binding.root, "Failed to send command to device", Snackbar.LENGTH_SHORT).show()
//                    Snackbar.make(binding.root, " Failed to send command to device", Snackbar.LENGTH_SHORT).show()
//                    Log.e("DeviceControl", "GATT write failed")
//                    // Revert switch state on failure
//                    binding.switchDeviceMode.isChecked = !isActive
//                    isDeviceActive = !isActive
//                    updateSwitchAppearance()
                    val message = "Switch set to $modeText mode but failed to send to device"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    Log.e("DeviceControl", "GATT write failed - switch state maintained")
                }
            } else {
////                Snackbar.make(binding.root, "No active connection to device", Snackbar.LENGTH_SHORT).show()
//                Snackbar.make(binding.root, " No active connection to device", Snackbar.LENGTH_SHORT).show()
//                Log.e("DeviceControl", "No connection available for GATT write")
//                // Revert switch state on failure
//                binding.switchDeviceMode.isChecked = !isActive
//                isDeviceActive = !isActive
//                updateSwitchAppearance()
                val message = " Switch set to $modeText mode but no connection available"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                Log.e("DeviceControl", "No connection available for GATT write - switch state maintained")
            }

        } catch (e: Exception) {
            Log.e("DeviceControl", "Error sending device command", e)
//            Snackbar.make(binding.root, "Error sending command to device", Snackbar.LENGTH_SHORT).show()
            Snackbar.make(binding.root, " Error sending command to device", Snackbar.LENGTH_SHORT).show()
            // Revert switch state on error
            binding.switchDeviceMode.isChecked = !isActive
            isDeviceActive = !isActive
            updateSwitchAppearance()
        }
    }

}//till now


