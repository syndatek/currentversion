package com.carditek.kesar.bluetooth

import androidx.lifecycle.MutableLiveData

class State {
    val address = MutableLiveData<String>("")
    val state = MutableLiveData<String>(DISCONNECTED)
    val stats = Stats()

    data class Stats(
        val packets: PacketStats = PacketStats(),
        val bytes: ByteStats = ByteStats(),
        val connections: ConnectionStats = ConnectionStats(),
        val cloud: CloudStats = CloudStats()
    )

    data class ByteStats(
        var total: Int = 0
    )

    data class PacketStats(
        var total: Int = 0,
        var short: Int = 0,
        var skips: Int = 0,
        var error: Int = 0
    )

    data class ConnectionStats(
        var disconnects: Int = 0
    )

    data class CloudStats(
        var pending: Int = 0,
        var upload: Int = 0,
    )

    companion object {
        const val CONNECTING = "connecting"
        const val CONNECTED = "connected"
        const val DISCONNECTING = "disconnecting"
        const val DISCONNECTED = "disconnected"
    }
}
