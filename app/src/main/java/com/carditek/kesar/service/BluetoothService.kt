package com.carditek.kesar.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import com.carditek.kesar.Cache
import com.carditek.kesar.Device
import com.carditek.kesar.bluetooth.Connection
import com.carditek.kesar.bluetooth.DataHandler
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.cloud.Uploader
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var state: State

    @Inject
    lateinit var uploader: Uploader

    @Inject
    lateinit var cache: Cache

    @Inject
    lateinit var dataHandler: DataHandler

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(preferences, "device")

        Controller.start(this)
        return START_STICKY
    }

    private var connection: Connection? = null
    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        if (key != "device" && key != "recording") return

        val address: String = preferences.getString("device", "")!!
        if (key == "device") {
            if (address == "") {
                connection?.close()
                connection = null
                currentConnection = null// added this line sept 15 for switch
            } else if (address != connection?.address) {
                connection?.close()
                connection = Connection(this, address, state, dataHandler,cache)
                currentConnection = connection//add this line sept 15 for switch
            }
        }

        val recording: Boolean = preferences.getBoolean("recording", false)
        connection?.handler?.recording = recording

        var message = ""
        if (address != "") {
            message += "Patch $address selected."
            if (recording) message += " (recording)"
        } else
            message = "Select ECG/EKG Patch"
        firebase.setCustomKey("MAC", address)
        Log.i(TAG, message)

        Controller.ensure(this).notify(message)
        state.address.postValue(address)
    }

    companion object {
        private const val TAG = "Bluetooth"
        private val firebase = FirebaseCrashlytics.getInstance()
        // Static reference to current connection for GATT write access added 15 september for switch
        @Volatile
        private var currentConnection: Connection? = null

        fun getCurrentConnection(): Connection? = currentConnection//till now
    }
}




//
//package com.carditek.kesar.service
//
//import android.app.Service
//import android.content.Intent
//import android.content.SharedPreferences
//import android.os.IBinder
//import android.util.Log
//import androidx.preference.PreferenceManager
//import com.carditek.kesar.Cache
//import com.carditek.kesar.Device
//import com.carditek.kesar.bluetooth.Connection
//import com.carditek.kesar.bluetooth.DataHandler
//import com.carditek.kesar.bluetooth.State
//import com.carditek.kesar.cloud.Uploader
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class BluetoothService :
//    Service(),
//    SharedPreferences.OnSharedPreferenceChangeListener {
//
//    @Inject
//    lateinit var device: Device
//
//    @Inject
//    lateinit var state: State
//
//    @Inject
//    lateinit var uploader: Uploader
//
//    @Inject
//    lateinit var cache: Cache
//
//    @Inject
//    lateinit var dataHandler: DataHandler
//
//    private var connection: Connection? = null
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(
//        intent: Intent?,
//        flags: Int,
//        startId: Int
//    ): Int {
//
//        super.onStartCommand(intent, flags, startId)
//
//        val preferences =
//            PreferenceManager.getDefaultSharedPreferences(
//                applicationContext
//            )
//
//        preferences.registerOnSharedPreferenceChangeListener(this)
//
//        /*
//         * Check the current BLE device.
//         */
//        val address =
//            preferences.getString("device", "") ?: ""
//
//        if (address.isEmpty()) {
//
//            Log.i(
//                TAG,
//                "No BLE device selected. Stopping BluetoothService."
//            )
//
//            stopSelf()
//
//            return START_NOT_STICKY
//        }
//
//        /*
//         * Start foreground only when a BLE device exists.
//         */
//        Controller.start(this)
//
//        /*
//         * Initialize the current device connection.
//         */
//        onSharedPreferenceChanged(
//            preferences,
//            "device"
//        )
//
//        return START_NOT_STICKY
//    }
//
//    override fun onSharedPreferenceChanged(
//        preferences: SharedPreferences,
//        key: String?
//    ) {
//
//        if (key != "device" && key != "recording") {
//            return
//        }
//
//        val address =
//            preferences.getString("device", "") ?: ""
//
//        /*
//         * No BLE device.
//         */
//        if (address.isEmpty()) {
//
//            Log.i(
//                TAG,
//                "BLE device removed/disconnected."
//            )
//
//            connection?.close()
//            connection = null
//            currentConnection = null
//
//            /*
//             * IMPORTANT:
//             * Stop foreground Bluetooth service.
//             */
//            stopSelf()
//
//            return
//        }
//
//        /*
//         * Device changed.
//         */
//        if (key == "device") {
//
//            if (address != connection?.address) {
//
//                Log.i(
//                    TAG,
//                    "Connecting to BLE device: $address"
//                )
//
//                connection?.close()
//
//                connection = Connection(
//                    this,
//                    address,
//                    state,
//                    dataHandler,
//                    cache
//                )
//
//                currentConnection = connection
//            }
//        }
//
//        /*
//         * Recording state.
//         */
//        val recording =
//            preferences.getBoolean(
//                "recording",
//                false
//            )
//
//        connection?.handler?.recording = recording
//
//        var message =
//            if (address.isNotEmpty()) {
//                "Patch $address selected."
//            } else {
//                "Select ECG/EKG Patch"
//            }
//
//        if (recording) {
//            message += " (recording)"
//        }
//
//        firebase.setCustomKey(
//            "MAC",
//            address
//        )
//
//        Log.i(
//            TAG,
//            message
//        )
//
//        Controller.ensure(this).notify(message)
//
//        state.address.postValue(address)
//    }
//
//    override fun onDestroy() {
//
//        Log.i(
//            TAG,
//            "BluetoothService onDestroy"
//        )
//
//        try {
//            connection?.close()
//        } catch (e: Exception) {
//
//            Log.e(
//                TAG,
//                "Error closing BLE connection",
//                e
//            )
//        }
//
//        connection = null
//        currentConnection = null
//
//        /*
//         * Cancel DataHandler coroutines.
//         */
//        dataHandler.cleanup()
//
//        /*
//         * Remove SharedPreferences listener.
//         */
//        val preferences =
//            PreferenceManager.getDefaultSharedPreferences(
//                applicationContext
//            )
//
//        preferences.unregisterOnSharedPreferenceChangeListener(
//            this
//        )
//
//        super.onDestroy()
//    }
//
//    companion object {
//
//        private const val TAG = "Bluetooth"
//
//        private val firebase =
//            FirebaseCrashlytics.getInstance()
//
//        @Volatile
//        private var currentConnection: Connection? = null
//
//        fun getCurrentConnection(): Connection? =
//            currentConnection
//    }
//}
