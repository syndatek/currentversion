package com.carditek.kesar.util.filters.edgecomputing

import androidx.lifecycle.MutableLiveData

object HisBundleData {



    @Volatile
    var graphPath: String = ""


    val graphPathLive: MutableLiveData<String> = MutableLiveData()
}
