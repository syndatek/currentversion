package com.carditek.kesar

import androidx.lifecycle.LiveData

interface Device {
    val address: LiveData<String>
    val recording: LiveData<Boolean>

    fun setAddress(address: String) {}
    fun setRecording(recording: Boolean) {}
}
