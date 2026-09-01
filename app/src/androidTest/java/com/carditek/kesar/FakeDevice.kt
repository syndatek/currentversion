package com.carditek.kesar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.carditek.kesar.module.AccountModule
import com.carditek.kesar.module.DeviceModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakeDevice : Device {
    private val _address = MutableLiveData<String>("01:02:03:04:05:06")
    private val _recording = MutableLiveData<Boolean>(false)

    override val address: LiveData<String> = _address
    override val recording: LiveData<Boolean> = _recording
    override var firstTimestamp: Int? = null
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DeviceModule::class]
)
object FakeDeviceModule {
    @Provides
    @Singleton
    fun provideDevice(): Device = FakeDevice()
}
