//package com.carditek.kesar
//
//import androidx.lifecycle.LiveData
//
//interface Device {
//    val address: LiveData<String>
//    val recording: LiveData<Boolean>
//    var firstTimestamp: Int? = null//added by 15/aug 2025
//
//    fun setAddress(address: String) {}
//    fun setRecording(recording: Boolean) {}
//
//    // ✅ Add this:
//    fun disconnect() {}}


package com.carditek.kesar

import androidx.lifecycle.LiveData

interface Device {
    val address: LiveData<String>
    val recording: LiveData<Boolean>
    var firstTimestamp: Int?

    fun setAddress(address: String) {}
    fun setRecording(recording: Boolean) {}
    fun disconnect() {}
}
