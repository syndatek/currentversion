//package com.carditek.kesar
//
//import com.carditek.kesar.bluetooth.DataHandler
//import android.annotation.SuppressLint
//import android.os.BatteryManager
//import android.content.Context
//import android.content.res.ColorStateList
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import com.carditek.kesar.bluetooth.State
//import com.carditek.kesar.databinding.FragmentStatusBinding
//import android.util.Log
//import androidx.core.content.ContextCompat
////import androidx.databinding.ktx.BuildConfig
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//import com.carditek.kesar.BuildConfig
//import androidx.navigation.fragment.findNavController
//import android.view.animation.AnimationUtils
//
//
//
//
//@AndroidEntryPoint
//class StatusFragment : Fragment() {
//
//
//    private lateinit var binding: FragmentStatusBinding
//    //private val handler = Handler()
//    private val handler = Handler(Looper.getMainLooper())//add by uday
//
//    private var previousTimestamp: Long = 0  // previous timestamp value
//    private var previousBytes: Int = 0       // previous bytes value
//    private var isUpdating = false  // to keep track of updates
//    private var jumpedToLive = false
//
//
//    @Inject
//    lateinit var state: State
//
//    @Inject
//    lateinit var dataHandler: DataHandler // Inject DataHandler
//
//    //@Inject
//    //lateinit var connection: Connection  // Inject Connection class
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentStatusBinding.inflate(layoutInflater)
//        binding.lifecycleOwner = this
//        binding.state = state
//
//        // Toggle button click listener
//        binding.buttonToggle.setOnClickListener {
//            if (isUpdating) {
//                stopPeriodicUpdates()
//                //connection.writeStopCommand()  // Write to characteristic to stop
//                binding.buttonToggle.text = "Start"
//            } else {
//                startPeriodicUpdates()
//                //connection.writeStartCommand()  // Write to characteristic to start
//                binding.buttonToggle.text = "Stop"
//            }
//        }
//
//        return binding.root
//    }
//
//    override fun onPause() {
//        super.onPause()
//        stopPeriodicUpdates()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        startPeriodicUpdates()
//    }
//
//    private fun startPeriodicUpdates() {
//        isUpdating = true
//        previousTimestamp = System.currentTimeMillis()
//        handler.post { periodic() }
//    }
//
//    private fun stopPeriodicUpdates() {
//        isUpdating = false
//        handler.removeCallbacksAndMessages(null)
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun periodic() {
//        if (!isUpdating) return
//
//
//
//
//        val current = System.currentTimeMillis()
//        val bytes = state.stats.bytes.total
//        val kbps = if (current > previousTimestamp) {
//            (bytes - previousBytes).toFloat() / (current - previousTimestamp)
//        } else 0.0F
//        previousBytes = bytes
//        previousTimestamp = current
//
//        val packets = state.stats.packets
//        val cloud = state.stats.cloud
//        val disconnects = state.stats.connections.disconnects
//        var address: String = state.address.value!!
//        address = if (address == "") "(none)" else address
//
//
//
//        binding.textMessage.text = "MAC: $address\n" +
//                "Connections: $disconnects disconnects.\n" +
//                "Packets: ${packets.total} total.\n" +
//                "    ${packets.short} short, ${packets.skips} skips, " +
//                "${packets.error} errors.\n" +
//                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
//                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
//                        + "\n\n\n") +
////               "Version ${BuildConfig.VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
//               "Version ${BuildConfig.APP_VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
//        //updateReadyBanner(bytes, state.state.value == "Connected")////add by uday
//         //updateReadyBanner(kbps, state.state.value == "Connected")//add by uday
//        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
//        updateReadyBanner(kbps, isConnected)            // <── new call
//
//        handler.postDelayed({ periodic() }, 1_000)
//       // handler.postDelayed({ periodic() }, 30000)
//    }
//
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Observe battery percentage LiveData from DataHandler
//        dataHandler.batteryPercentage.observe(viewLifecycleOwner) { percentage ->
//            updateBatteryPercentage(percentage)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun updateBatteryPercentage(batteryPercentage: Int) {
//        binding.textBattery.text = "SYDANTEK LIPO BAT: $batteryPercentage%"
//        Log.i("Battery", "Battery Percentage: $batteryPercentage%")
//
//
//
//    }
//    /** Show “Ready to Go” when bytes ∈ 24–25 KB and device is connected,//added by uday
//     *  hide the banner at ≥ 30 KB or when disconnected. */
//    @SuppressLint("SetTextI18n")
//    //private fun updateReadyBanner(kbPerSec: Float, isConnected: Boolean) {
//  private fun updateReadyBanner(kbPerSec: Float, isConnected: Boolean) {
//
//
//        // Lazy‑create a banner view (one TextView added in XML with @id/text_ready_banner)
//        val banner = binding.textReadyBanner   // make sure this TextView exists in XML
//        val show   = isConnected && kbPerSec in 24f..25.9f
////       // if (isConnected && kbPerSec in 14f..30f) {
//        if (show) {
//            banner.text = "Ready to Go"
//            banner.visibility = View.VISIBLE
//            //uday added
//            if (!jumpedToLive) {
//                jumpedToLive = true                // don’t jump repeatedly
//
//
//                handler.postDelayed({
//                    // Double check still ready before navigating
//                    if (isVisible && banner.visibility == View.VISIBLE) {
//                findNavController().navigate(R.id.nav_live)
//            }
//        }, 50000)// 5000 ms = 5 seconds
//            }//till now
//
//        } else {
//            banner.visibility = View.GONE
//            jumpedToLive = false//it working
//            handler.removeCallbacksAndMessages(null) // Cancel pending jump it added by uday
//        }
//        Log.d("READY",
//            "connected=$isConnected  kbps=$kbPerSec  show=$show")
//    }//TILL ADD UDAY
//}




//
/////*
////class StatusFragment : Fragment() {
////    private lateinit var binding: FragmentStatusBinding
////    private val handler = Handler()
////    private var previousTimestamp: Long = 0  // previous timestamp value
////    private var previousBytes: Int = 0       // previous bytes value
////    private var isUpdating = false  // to keep track of updates
////
////    @Inject
////    lateinit var state: State
////
////    @Inject
////    lateinit var dataHandler: DataHandler // Inject DataHandler
////
////    override fun onCreateView(
////        inflater: LayoutInflater,
////        container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View {
////        binding = FragmentStatusBinding.inflate(layoutInflater)
////        binding.lifecycleOwner = this
////        binding.state = state
////
////        // No need to find views by ID, use view binding directly
////        binding.buttonToggle.setOnClickListener {
////            if (isUpdating) {
////                stopPeriodicUpdates()
////                binding.buttonToggle.text = "Start"
////            } else {
////                startPeriodicUpdates()
////                binding.buttonToggle.text = "Stop"
////            }
////        }
////
////        return binding.root
////    }
////
////    override fun onPause() {
////        super.onPause()
////        stopPeriodicUpdates()
////    }
////
////    override fun onResume() {
////        super.onResume()
////        startPeriodicUpdates()
////        // Update battery percentage using LiveData from DataHandler
////        //updateBatteryPercentage(dataHandler.batteryPercentage.value ?: 0)
////
////    }
////
////    private fun startPeriodicUpdates() {
////        isUpdating = true
////        previousTimestamp = System.currentTimeMillis()
////        handler.post { periodic() }
////    }
////
////    private fun stopPeriodicUpdates() {
////        isUpdating = false
////        handler.removeCallbacksAndMessages(null)
////    }
////
////    @SuppressLint("SetTextI18n")
////    private fun periodic() {
////        if (!isUpdating) return
////
////        val current = System.currentTimeMillis()
////        val bytes = state.stats.bytes.total
////        val kbps = if (current > previousTimestamp) {
////            (bytes - previousBytes).toFloat() / (current - previousTimestamp)
////        } else 0.0F
////        previousBytes = bytes
////        previousTimestamp = current
////
////        val packets = state.stats.packets
////        val cloud = state.stats.cloud
////        val disconnects = state.stats.connections.disconnects
////        var address: String = state.address.value!!
////        address = if (address == "") "(none)" else address
////
////        binding.textMessage.text = "MAC: $address\n" +
////                "Connections: $disconnects disconnects.\n" +
////                "Packets: ${packets.total} total.\n" +
////                "    ${packets.short} short, ${packets.skips} skips, " +
////                "${packets.error} errors.\n" +
////                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
////                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
////                        + "\n\n\n") +
////                "Version ${BuildConfig.VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
////
////        // Update battery percentage using LiveData from DataHandler
////        //updateBatteryPercentage(dataHandler.batteryPercentage.value ?: 0)
////
////        // Post the next periodic update in 30 seconds
////        handler.postDelayed({ periodic() }, 30000)
////    }
////
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////
////        // Observe battery percentage LiveData from DataHandler
////        dataHandler.batteryPercentage.observe(viewLifecycleOwner) { percentage ->
////            updateBatteryPercentage(percentage)
////
////        }
////    }
////
////     //Update method to take an Int for battery percentage
////    @SuppressLint("SetTextI18n")
////    private fun updateBatteryPercentage(batteryPercentage: Int) {
////        //Update the TextView with the battery percentage
////       binding.textBattery.text = "SYDANTEK LIPO BAT: $batteryPercentage%"
////
////        // Set the ProgressBar to reflect the battery level
////        //binding.progressBarBattery.progress = batteryPercentage
////
////        // Log the battery percentage
////        Log.i("Battery", "Battery Percentage: $batteryPercentage%")
////    }
////}







//  ready to go
//package com.carditek.kesar
//
//import com.carditek.kesar.bluetooth.DataHandler
//import android.annotation.SuppressLint
//import android.os.BatteryManager
//import android.content.Context
//import android.content.res.ColorStateList
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import com.carditek.kesar.bluetooth.State
//import com.carditek.kesar.databinding.FragmentStatusBinding
//import android.util.Log
//import androidx.core.content.ContextCompat
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//import com.carditek.kesar.BuildConfig
//import androidx.navigation.fragment.findNavController
//import com.google.android.material.snackbar.Snackbar
//
//@AndroidEntryPoint
//class StatusFragment : Fragment() {
//
//    private lateinit var binding: FragmentStatusBinding
//    private val handler = Handler(Looper.getMainLooper())
//
//    private var previousTimestamp: Long = 0
//    private var previousBytes: Int = 0
//    private var isUpdating = false
//    private var jumpedToLive = false
//    private var lastKnownKbps: Float = 0f
//
//    @Inject
//    lateinit var state: State
//
//    @Inject
//    lateinit var dataHandler: DataHandler
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentStatusBinding.inflate(layoutInflater)
//        binding.lifecycleOwner = this
//        binding.state = state
//
//        binding.buttonToggle.setOnClickListener {
//            if (isUpdating) {
//                stopPeriodicUpdates()
//                binding.buttonToggle.text = "Start"
//            } else {
//                startPeriodicUpdates()
//                binding.buttonToggle.text = "Stop"
//
//                // ✅ Check if device is ready and navigate
//                val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
//                if (isConnected && lastKnownKbps in 24f..25.9f) {
//                    findNavController().navigate(R.id.nav_live)
//                } else {
//                    Snackbar.make(binding.root, "Device not ready yet. Please wait...", Snackbar.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        return binding.root
//    }
//
//    override fun onPause() {
//        super.onPause()
//        stopPeriodicUpdates()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        startPeriodicUpdates()
//    }
//
//    private fun startPeriodicUpdates() {
//        isUpdating = true
//        previousTimestamp = System.currentTimeMillis()
//        handler.post { periodic() }
//    }
//
//    private fun stopPeriodicUpdates() {
//        isUpdating = false
//        handler.removeCallbacksAndMessages(null)
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun periodic() {
//        if (!isUpdating) return
//
//        val current = System.currentTimeMillis()
//        val bytes = state.stats.bytes.total
//        val kbps = if (current > previousTimestamp) {
//            (bytes - previousBytes).toFloat() / (current - previousTimestamp)
//        } else 0.0F
//        lastKnownKbps = kbps
//
//        previousBytes = bytes
//        previousTimestamp = current
//
//        val packets = state.stats.packets
//        val cloud = state.stats.cloud
//        val disconnects = state.stats.connections.disconnects
//        var address: String = state.address.value!!
//        address = if (address == "") "(none)" else address
//
//        binding.textMessage.text = "MAC: $address\n" +
//                "Connections: $disconnects disconnects.\n" +
//                "Packets: ${packets.total} total.\n" +
//                "    ${packets.short} short, ${packets.skips} skips, " +
//                "${packets.error} errors.\n" +
//                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
//                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
//                        + "\n\n\n") +
//                "Version ${BuildConfig.APP_VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"
//
//        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
//        updateReadyBanner(kbps, isConnected)
//
//        handler.postDelayed({ periodic() }, 1_000)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        dataHandler.batteryPercentage.observe(viewLifecycleOwner) { percentage ->
//            updateBatteryPercentage(percentage)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun updateBatteryPercentage(batteryPercentage: Int) {
//        binding.textBattery.text = "SYDANTEK LIPO BAT: $batteryPercentage%"
//        Log.i("Battery", "Battery Percentage: $batteryPercentage%")
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun updateReadyBanner(kbPerSec: Float, isConnected: Boolean) {
//        val banner = binding.textReadyBanner
//        val show = isConnected && kbPerSec in 24f..25.9f
//
//        if (show) {
//            banner.text = "Ready to Go"
//            banner.visibility = View.VISIBLE
//        } else {
//            banner.visibility = View.GONE
//        }
//
//        Log.d("READY", "connected=$isConnected  kbps=$kbPerSec  show=$show")
//    }
//}



// remove the stop option

package com.carditek.kesar

import com.carditek.kesar.bluetooth.DataHandler
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

        binding.textMessage.text = "MAC: $address\n" +
                "Connections: $disconnects disconnects.\n" +
                "Packets: ${packets.total} total.\n" +
                "    ${packets.short} short, ${packets.skips} skips, " +
                "${packets.error} errors.\n" +
                "Bytes: $bytes, " + "%.2fKB/s".format(kbps) + "\n" +
                ("Cloud: ${cloud.upload} uploads, ${cloud.pending} pending."
                        + "\n\n\n") +
                "Version ${BuildConfig.APP_VERSION_NAME}, commit: ${BuildConfig.GIT_HASH}"

        val isConnected = state.state.value.equals(State.CONNECTED, ignoreCase = true)
        updateReadyBanner(kbps, isConnected)

        handler.postDelayed({ periodic() }, 1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataHandler.batteryPercentage.observe(viewLifecycleOwner) { percentage ->
            updateBatteryPercentage(percentage)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBatteryPercentage(batteryPercentage: Int) {
        binding.textBattery.text = "SYDANTEK LIPO BAT: $batteryPercentage%"
        Log.i("Battery", "Battery Percentage: $batteryPercentage%")
    }

    @SuppressLint("SetTextI18n")
    private fun updateReadyBanner(kbPerSec: Float, isConnected: Boolean) {
        val banner = binding.textReadyBanner
        val show = isConnected && kbPerSec in 24f..25.9f

        if (show) {
            banner.text = "Ready to Go"
            banner.visibility = View.VISIBLE
        } else {
            banner.visibility = View.GONE
        }

        Log.d("READY", "connected=$isConnected  kbps=$kbPerSec  show=$show")
    }
}

