package com.carditek.kesar.service

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.carditek.kesar.BuildConfig
import com.carditek.kesar.MainActivity
import com.carditek.kesar.R
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Controller {
    private lateinit var manager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var notification: Notification
    private var initialized = false

    fun start(service: Service) {
        service.startForeground(NOTIFICATION_ID, notification)
    }

    fun ensure(context: Context): Controller {
        if (initialized) return this
        firebase.setCustomKey("GIT_HASH", BuildConfig.GIT_HASH)

        val pending: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                run {
                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        FLAG_IMMUTABLE else 0
                    PendingIntent.getActivity(context, 0, notificationIntent, flags)
                }
            }

        manager = ContextCompat.getSystemService(context, NotificationManager::class.java)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "Carditek", "Foreground",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        builder = NotificationCompat.Builder(context, "Carditek")
            .setSmallIcon(R.drawable.ic_bluetooth_black)
            .setContentText("Select ECG/EKG Patch")
            .setContentIntent(pending)

        notification = builder.build()

        ContextCompat.startForegroundService(
            context,
            Intent(context, BluetoothService::class.java)
        )

        initialized = true
        return this
    }

    fun notify(message: String) {
        notification = builder.setContentText(message).build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private const val NOTIFICATION_ID = 1001
    private val firebase = FirebaseCrashlytics.getInstance()
}
