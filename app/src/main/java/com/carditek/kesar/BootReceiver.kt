package com.carditek.kesar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.carditek.kesar.service.Controller

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Controller.ensure(context)
        }
    }
}
