package com.carditek.kesar.util

import android.os.Build
import java.util.*

class Configuration {
    val isEmulator = (Build.MANUFACTURER.contains("Genymotion")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.lowercase(Locale.getDefault()).contains("droid4x")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.HARDWARE == "goldfish"
            || Build.HARDWARE == "vbox86"
            || Build.HARDWARE.lowercase(Locale.getDefault()).contains("nox")
            || Build.FINGERPRINT.startsWith("generic")
            || Build.PRODUCT == "sdk"
            || Build.PRODUCT == "google_sdk"
            || Build.PRODUCT == "sdk_x86"
            || Build.PRODUCT == "vbox86p"
            || Build.PRODUCT.lowercase(Locale.getDefault()).contains("nox")
            || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")))
}
