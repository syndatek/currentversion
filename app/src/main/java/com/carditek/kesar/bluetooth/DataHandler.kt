package com.carditek.kesar.bluetooth
import android.os.SystemClock
import android.util.Log
import com.carditek.kesar.Cache
import com.carditek.kesar.cloud.Uploader
import com.carditek.kesar.util.filters.edgecomputing.EdgeComputingProcessor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


class DataHandler @Inject constructor(
    private val uploader: Uploader,
    private val cache: Cache,
    private val edgeComputingProcessor: EdgeComputingProcessor,
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
            // Extract 1 second of RAW data (24000 bytes @ 1000 Hz)
            val rawData = buffer.sliceArray(
                offset + PAYLOAD_BYTES - PAYLOAD_BYTES_PER_SECOND
                        until offset + PAYLOAD_BYTES
            )
            
            // Path 1: EDGE COMPUTING
            // Process: Decimate → Filter → HR → SNR → Sat → LiveData (UI)
            edgeComputingProcessor.processRawData(rawData)
            
            // Path 2: CACHE
            // Process: Decimate → Store → (if recording) Upload RAW to Cloud
            // Note: No filtering in cache path - RAW data is uploaded
            cache.put(
                timestamp + difference / PACKETS_PER_SECOND,
                rawData
            )
        }

        // Extract the first 4 bytes of the packet and store them
        extractedBytes = packet.copyOfRange(4, 8)

        // Calculate battery percentage based on the extracted bytes
//        extractedBytes?.let {
//            val adcValue = 0x190F // Placeholder for actual ADC value
//
//            val maxAdcValue = 0x190F  // Corresponding to 100% (4.12V)
//            val minAdcValue = 0x0B00  // Corresponding to 10% (3.50V)
        extractedBytes?.let { bytes ->
            // Convert 4 bytes to ADC value using little-endian format
            val adcValue = (bytes[0].toInt() and 0xFF) or
                    ((bytes[1].toInt() and 0xFF) shl 8) or
                    ((bytes[2].toInt() and 0xFF) shl 16) or
                    ((bytes[3].toInt() and 0xFF) shl 24)

            // Log the received bytes for debugging
            val hexString = bytes.joinToString(" ") { "%02X".format(it) }
            Log.d(TAG, "Received battery bytes: [$hexString] -> ADC: 0x${adcValue.toString(16).uppercase()}")

            // Actual ADC values from device measurements
            val maxAdcValue = 0x190F  // 100% (4.12V)
            val highAdcValue = 0x0E0B // 75% (3.85V)
            val midAdcValue = 0x0DB7  // 50% (3.74V)
            val minAdcValue = 0x0D07  // 10% (3.50V)

            //_batteryPercentage.postValue(255)

            val batteryPercentage = when {
                adcValue >= maxAdcValue -> 100
//                adcValue <= minAdcValue -> 10
//                else -> ((adcValue - minAdcValue).toFloat() / (maxAdcValue - minAdcValue) * 90 + 10).toInt()
                adcValue >= highAdcValue -> {
                    // Linear interpolation between 100% and 75%
                    val ratio = (adcValue - highAdcValue).toFloat() / (maxAdcValue - highAdcValue)
                    75 + (ratio * 25).toInt()
                }
                adcValue >= midAdcValue -> {
                    // Linear interpolation between 75% and 50%
                    val ratio = (adcValue - midAdcValue).toFloat() / (highAdcValue - midAdcValue)
                    50 + (ratio * 25).toInt()
                }
                adcValue >= minAdcValue -> {
                    // Linear interpolation between 50% and 10%
                    val ratio = (adcValue - minAdcValue).toFloat() / (midAdcValue - minAdcValue)
                    10 + (ratio * 40).toInt()
                }
                else -> 10

            }
            // Calculate voltage from ADC value using actual measurements
            val batteryVoltage = when {
                adcValue >= maxAdcValue -> 4.12f
                adcValue >= highAdcValue -> {
                    // Linear interpolation between 4.12V and 3.85V
                    3.85f + (4.12f - 3.85f) * (adcValue - highAdcValue).toFloat() / (maxAdcValue - highAdcValue)
                }
                adcValue >= midAdcValue -> {
                    // Linear interpolation between 3.85V and 3.74V
                    3.74f + (3.85f - 3.74f) * (adcValue - midAdcValue).toFloat() / (highAdcValue - midAdcValue)
                }
                adcValue >= minAdcValue -> {
                    // Linear interpolation between 3.74V and 3.50V
                    3.50f + (3.74f - 3.50f) * (adcValue - minAdcValue).toFloat() / (midAdcValue - minAdcValue)
                }
                else -> 3.50f
            }
//            // Debug: Print the calculated battery percentage and ADC value to the console
//            Log.d(TAG, "Calculated battery percentage: $batteryPercentage, ADC value: $adcValue")

            // Debug: Print the calculated battery percentage and voltage
            Log.d(TAG, "Battery: $batteryPercentage%, ${String.format("%.2f", batteryVoltage)}V, ADC: 0x${adcValue.toString(16).uppercase()}")

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

