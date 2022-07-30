package com.carditek.kesar.module

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.carditek.kesar.Device
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class BluetoothDevice(context: Context): Device {
    private val _address = MutableLiveData<String>()
    private val _recording = MutableLiveData<Boolean>()
    override val address: LiveData<String> = _address
    override val recording: LiveData<Boolean> = _recording

    override fun setRecording(recording: Boolean) {
        preferences.edit(commit = true) { putBoolean(RECORDING, recording) }
        _recording.value = recording
    }

    override fun setAddress(address: String) {
        preferences.edit(commit = true) { putString(ADDRESS, address) }
        _address.value = address
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        setAddress(preferences.getString(ADDRESS, "")!!)
        setRecording(preferences.getBoolean(RECORDING, false))
    }

    companion object {
        private const val ADDRESS = "device"
        private const val RECORDING = "recording"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {
    @Provides
    @Singleton
    fun provideDevice(@ApplicationContext context: Context): Device {
        return BluetoothDevice(context)
    }
}
