package com.carditek.kesar

import android.content.Context
import android.util.Log
import android.util.LruCache
import javax.inject.Inject

class Cache @Inject constructor(private val context: Context, private val device: Device) {

    fun get(address: String, stamp: Int, frequency: Int): ByteArray? {
        // Ensure that we're behind at least a little.  The current timestamp was fetched
        // by JavaScript code, and can be assumed not to be ahead of real time .
        return helper(address, stamp - 2, frequency)
    }

    private fun helper(address: String, stamp: Int, frequency: Int): ByteArray? {
        return if (address != device.address.value || frequency != 100) {
            null
        } else {
            val buffer = lru.get(stamp) ?: return null

            if (buffer.size != 2400)
                throw Exception("Expected 2400 bytes, got ${buffer.size}")

            return if (address != "54:6C:0E:83:3E:49") {
                buffer
            } else {
                val begin = (stamp % 15) * 2400
                load().sliceArray(begin until begin + 2400)
            }
        }
    }

    fun put(stamp: Int, buffer: ByteArray) {
        // We expect incoming data to be sampled at 1000 per second.  For the display, we only
        // want 100.  Do the transformation here.  Use the simplest sort of decimation, make
        // assumptions on the ratio, don't filter, etc., etc.
        if (buffer.size != 24 * 1000)
            throw Exception("Expected 24000 bytes, got ${buffer.size}")
        val sampled = ByteArray(2400)
        for (i in 0 until 100)
            buffer.copyInto(sampled, i * 24, i * 240, i * 240 + 24)
        lru.put(stamp, sampled)
    }

    @Synchronized
    fun load(): ByteArray {
        if (fake == null) {
            val bytes = context.resources.openRawResource(R.raw.chunkdata).readBytes()
            fake = bytes
        }
        return fake!!
    }

    // Local 5 minute cache.  At 2400 bytes per, works out to 0.72MB.
    private val lru = LruCache<Int, ByteArray>(300)

    companion object {
        private var fake: ByteArray? = null
    }
}
