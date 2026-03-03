package com.carditek.kesar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class DeviceImpl : Device {
    override val address = MutableLiveData<String>()
    override val recording = MutableLiveData<Boolean>()

    //  This actually stores the timestamp
    override var firstTimestamp: Int? = null

    override fun setAddress(address: String) {
        this.address.postValue(address)
    }

    override fun setRecording(recording: Boolean) {
        this.recording.postValue(recording)
    }

    override fun disconnect() {
        // Add disconnect logic if needed
    }
}
