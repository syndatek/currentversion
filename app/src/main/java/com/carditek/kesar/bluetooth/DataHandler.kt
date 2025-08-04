package com.carditek.kesar.bluetooth
import android.os.SystemClock
import android.util.Log
import com.carditek.kesar.Cache
import com.carditek.kesar.cloud.Uploader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class DataHandler @Inject constructor(
    private val uploader: Uploader,
    private val cache: Cache,
    private val state: State
)  {
    // LiveData for battery percentage
    private val _batteryPercentage = MutableLiveData<Int>().apply{value=100}
    val batteryPercentage: LiveData<Int> get() = _batteryPercentage

    private val packets = state.stats.packets
    private val bytes = state.stats.bytes

    // Initialize the timebase, next, buffer, etc.
    private val timebase = System.currentTimeMillis() - SystemClock.elapsedRealtime()
    private var next: Int = 0
    private var buffer: ByteArray = ByteArray(BUFFER_SIZE)
    private var serial: Int = 0
    private var timestamp: Int = 0
    private var extractedBytes: ByteArray? = null
    var recording: Boolean = false
        set(value) {
            field = value
            Log.i(TAG, "recording: $field")
        }

    fun handle(packet: ByteArray) {
        // Update stats, handle packets, etc.
        ++packets.total
        bytes.total += packet.size

        if (packet.size != PACKET_SIZE) {
            ++packets.short
            Log.i(TAG, "Received buffer size: ${packet.size}")
            return
        }

        // Process the serial numbers, reinitializing buffer if necessary
        val actual = Protocol.serial(packet)
        if (actual != next) {
            Log.w(TAG, "Got $actual, expected $serial")
            ++packets.skips
            next = actual
        }

        next = (next + 1) and 0xffff

        val now = timebase + SystemClock.elapsedRealtime()
        val seconds = now / 1000
        val stamp = (seconds - (seconds % BUFFER_SECONDS)).toInt()

        if (seconds > timestamp + BUFFER_SECONDS + BUFFER_SECONDS / 3) {
            Log.w(TAG, "Reinitializing from $timestamp to $stamp.")
            val packets = BUFFER_PACKETS * (seconds - stamp).toInt() / BUFFER_SECONDS

            if (timestamp > timebase) flush()
            serial = (actual - packets) and 0xffff
            timestamp = stamp
        }

        var difference = (actual - serial) and 0xffff
        if (difference >= BUFFER_PACKETS) {
            flush()
            timestamp += BUFFER_SECONDS
            difference = 0
            serial = actual
        }

        val offset = difference * PAYLOAD_BYTES
        packet.copyInto(buffer, offset, 4, 4 + PAYLOAD_BYTES)
        if ((difference + 1) % PACKETS_PER_SECOND == 0) {
            cache.put(
                timestamp + difference / PACKETS_PER_SECOND,
                buffer.sliceArray(
                    offset + PAYLOAD_BYTES - PAYLOAD_BYTES_PER_SECOND
                            until offset + PAYLOAD_BYTES
                )
            )
        }

        // Extract the first 4 bytes of the packet and store them
        extractedBytes = packet.copyOfRange(4, 8)

        // Calculate battery percentage based on the extracted bytes
        extractedBytes?.let {
            val adcValue = 0x190F // Placeholder for actual ADC value

            val maxAdcValue = 0x190F  // Corresponding to 100% (4.12V)
            val minAdcValue = 0x0B00  // Corresponding to 10% (3.50V)
            //_batteryPercentage.postValue(255)

            val batteryPercentage = when {
                adcValue >= maxAdcValue -> 100
                adcValue <= minAdcValue -> 10
                else -> ((adcValue - minAdcValue).toFloat() / (maxAdcValue - minAdcValue) * 90 + 10).toInt()
            }
            // Debug: Print the calculated battery percentage and ADC value to the console
            Log.d(TAG, "Calculated battery percentage: $batteryPercentage, ADC value: $adcValue")


            // Post the calculated battery percentage to LiveData
           // batteryPercentage=100
            _batteryPercentage.postValue(batteryPercentage)
        }
    }

    fun close() {
        flush()
    }

    private fun flush() {
        if (recording) {
            store(buffer, timestamp)
            buffer = ByteArray(BUFFER_SIZE)
        }
    }

    private fun store(buffer: ByteArray, stamp: Int) {
        GlobalScope.launch {
            uploader.upload(stamp, LEADS, FREQUENCY, buffer)

        }
    }

    companion object {
        private const val LEADS = 8
        private const val FREQUENCY = 1000
        private const val SAMPLE_BYTES = 3 * LEADS

        private const val PACKET_SIZE = 244
        private const val PAYLOAD_BYTES = PACKET_SIZE - 4
        private const val PAYLOAD_SAMPLES = PAYLOAD_BYTES / SAMPLE_BYTES

        private const val PACKETS_PER_SECOND = FREQUENCY / PAYLOAD_SAMPLES
        private const val PAYLOAD_BYTES_PER_SECOND = PACKETS_PER_SECOND * PAYLOAD_BYTES

        private const val BUFFER_SECONDS = 15
        private const val BUFFER_SIZE = PAYLOAD_BYTES * 100 * BUFFER_SECONDS
        private const val BUFFER_PACKETS = BUFFER_SIZE / PAYLOAD_BYTES

        private const val TAG = "DataHandler"


    }
}

