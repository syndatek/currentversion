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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(factory)
            .setMinimumLoggingLevel(INFO)
            .build()
}







