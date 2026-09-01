package com.carditek.kesar.service

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.carditek.kesar.BuildConfig
import com.carditek.kesar.MainActivity
import com.carditek.kesar.R
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Controller {
    private const val CHANNEL_ID = "Carditek"
    private const val NOTIFICATION_ID = 1001

    private lateinit var appContext: Context
    private lateinit var manager: NotificationManager
    private lateinit var contentIntent: PendingIntent
    private var initialized = false

    private fun buildNotification(text: CharSequence): Notification {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bluetooth_black)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build()
    }

    fun start(service: Service) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            //   ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(service, NOTIFICATION_ID, buildNotification("Select ECG/EKG Patch"), type)
    }

    fun ensure(context: Context): Controller {
        if (initialized) return this
        appContext = context.applicationContext
        firebase.setCustomKey("GIT_HASH", BuildConfig.GIT_HASH)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE else 0
        contentIntent = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java),
            flags
        )

        manager = ContextCompat.getSystemService(appContext, NotificationManager::class.java)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Foreground",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, BluetoothService::class.java)
        )

        initialized = true
        return this
    }

    fun notify(message: String) {
        if (!canPostNotifications()) {
            Log.w(TAG, "Skipping notification update: POST_NOTIFICATIONS not granted")
            return
        }
        try {
            manager.notify(NOTIFICATION_ID, buildNotification(message))
        } catch (e: SecurityException) {
            Log.w(TAG, "notify failed", e)
        }
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val firebase = FirebaseCrashlytics.getInstance()
    private const val TAG = "Controller"
}


//
//
//
//package com.carditek.kesar.service
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.PendingIntent.FLAG_IMMUTABLE
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.pm.ServiceInfo
//import android.os.Build
//import android.util.Log
//import androidx.core.app.NotificationCompat
//import androidx.core.app.ServiceCompat
//import androidx.core.content.ContextCompat
//import com.carditek.kesar.BuildConfig
//import com.carditek.kesar.MainActivity
//import com.carditek.kesar.R
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//
//object Controller {
//
//    private const val CHANNEL_ID = "Carditek"
//    private const val NOTIFICATION_ID = 1001
//
//    private lateinit var appContext: Context
//    private lateinit var manager: NotificationManager
//    private lateinit var contentIntent: PendingIntent
//
//    private var initialized = false
//
//    private fun buildNotification(text: CharSequence): Notification {
//        return NotificationCompat.Builder(appContext, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_bluetooth_black)
//            .setContentTitle("Carditek ECG")
//            .setContentText(text)
//            .setContentIntent(contentIntent)
//            .setOngoing(true)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
//
//    /**
//     * Makes the BluetoothService a foreground service.
//     *
//     * Call this ONLY when BLE/ECG session actually needs to run.
//     */
//    fun start(service: Service) {
//
//        val type =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
//            } else {
//                0
//            }
//
//        ServiceCompat.startForeground(
//            service,
//            NOTIFICATION_ID,
//            buildNotification("ECG/BLE active"),
//            type
//        )
//
//        Log.i(TAG, "Bluetooth foreground service started")
//    }
//
//    /**
//     * Initializes notification infrastructure.
//     *
//     * IMPORTANT:
//     * This function does NOT start BluetoothService.
//     */
//    fun ensure(context: Context): Controller {
//
//        if (initialized) {
//            return this
//        }
//
//        appContext = context.applicationContext
//
//        firebase.setCustomKey(
//            "GIT_HASH",
//            BuildConfig.GIT_HASH
//        )
//
//        val flags =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                FLAG_IMMUTABLE
//            } else {
//                0
//            }
//
//        contentIntent = PendingIntent.getActivity(
//            appContext,
//            0,
//            Intent(appContext, MainActivity::class.java),
//            flags
//        )
//
//        manager =
//            ContextCompat.getSystemService(
//                appContext,
//                NotificationManager::class.java
//            )!!
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            manager.createNotificationChannel(
//                NotificationChannel(
//                    CHANNEL_ID,
//                    "Carditek Bluetooth",
//                    NotificationManager.IMPORTANCE_LOW
//                )
//            )
//        }
//
//        initialized = true
//
//        Log.i(TAG, "Controller initialized")
//
//        return this
//    }
//
//    /**
//     * Start BluetoothService.
//     *
//     * Call this only when you actually want BLE/ECG running.
//     */
//    fun startBluetoothService() {
//
//        check(initialized) {
//            "Controller.ensure(context) must be called before startBluetoothService()"
//        }
//
//        val intent = Intent(
//            appContext,
//            BluetoothService::class.java
//        )
//
//        ContextCompat.startForegroundService(
//            appContext,
//            intent
//        )
//
//        Log.i(TAG, "BluetoothService requested to start")
//    }
//
//    /**
//     * Stop BluetoothService.
//     *
//     * This should be called when BLE is disconnected
//     * and ECG acquisition is no longer required.
//     */
//    fun stopBluetoothService() {
//
//        if (!initialized) {
//            return
//        }
//
//        val intent = Intent(
//            appContext,
//            BluetoothService::class.java
//        )
//
//        appContext.stopService(intent)
//
//        Log.i(TAG, "BluetoothService requested to stop")
//    }
//
//    fun notify(message: String) {
//
//        if (!canPostNotifications()) {
//            Log.w(
//                TAG,
//                "Skipping notification update: POST_NOTIFICATIONS not granted"
//            )
//            return
//        }
//
//        try {
//
//            manager.notify(
//                NOTIFICATION_ID,
//                buildNotification(message)
//            )
//
//        } catch (e: SecurityException) {
//
//            Log.w(
//                TAG,
//                "Notification update failed",
//                e
//            )
//        }
//    }
//
//    private fun canPostNotifications(): Boolean {
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            return true
//        }
//
//        return ContextCompat.checkSelfPermission(
//            appContext,
//            android.Manifest.permission.POST_NOTIFICATIONS
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private val firebase =
//        FirebaseCrashlytics.getInstance()
//
//    private const val TAG = "Controller"
//}
