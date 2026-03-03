package com.carditek.kesar

import android.app.Application
import android.util.Log.INFO
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Application : Application(), Configuration.Provider {
    @Inject
    lateinit var factory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(factory)
            .setMinimumLoggingLevel(INFO)
            .build()
    }
}




//@HiltAndroidApp
//class MyApplication : Application()
