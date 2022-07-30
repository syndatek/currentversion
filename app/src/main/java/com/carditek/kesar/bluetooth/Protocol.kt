package com.carditek.kesar.bluetooth

object Protocol {
    fun unpack(buffer: ByteArray, offset: Int): Int {
        var result = buffer[offset].toInt() and 0xFF
        result = result or ((buffer[offset + 1].toInt() and 0xff) shl 8)
        result = result or ((buffer[offset + 2].toInt() and 0xff) shl 16)
        result = result or ((buffer[offset + 3].toInt() and 0xff) shl 24)
        return result
    }

    fun serial(packet: ByteArray): Int {
        return (packet[0].toInt() and 0xFF) or ((packet[1].toInt() and 0xff) shl 8)
    }
}
