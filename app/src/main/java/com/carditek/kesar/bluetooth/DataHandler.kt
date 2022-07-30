package com.carditek.kesar.bluetooth

import android.os.SystemClock
import android.util.Log
import com.carditek.kesar.Cache
import com.carditek.kesar.cloud.Uploader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DataHandler(
    private val uploader: Uploader,
    private val cache: Cache,
    state: State
) {
    private val packets = state.stats.packets
    private val bytes = state.stats.bytes

    // We save the base upon initialization (new/re- connection); all subsequent calculations
    // use the monotonically increasing "elapsed" system clock (milliseconds since boot).
    private val timebase = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    // Session (connection) variables: serial numbers increment by 1, wrapping around in 16 bits.
    private var next: Int = 0    // next expected serial number

    // Fields describing the next timestamped buffer (chunk) to be sent to the cloud backend.
    private var buffer: ByteArray = ByteArray(BUFFER_SIZE)
    private var serial: Int = 0  // serial number at start of buffer
    private var timestamp: Int = 0

    var recording: Boolean = false
        set(value) {
            field = value
            Log.i(TAG, "recording: $field")
        }

    fun handle(packet: ByteArray) {
        ++packets.total
        bytes.total += packet.size

        if (packet.size != PACKET_SIZE) {
            ++packets.short
            Log.i(TAG, "Received buffer size: ${packet.size}")
            return
        }

        val actual = Protocol.serial(packet)
        if (actual != next) {
            Log.w(TAG, "Got $actual, expected $serial")
            ++packets.skips
            next = actual
        }

        // Serial numbers should increase by 1 each time, wrapping around in 16 bits.
        next = (next + 1) and 0xffff

        // An arbitrary amount of time could have elapsed before we got this packet.  Therefore,
        // it is necessary to use wall-clock time for each packet.  We can expect the clock call
        // to take no more than 1us, so this is acceptable.
        val now = timebase + SystemClock.elapsedRealtime()
        val seconds = now / 1000
        val stamp = (seconds - (seconds % BUFFER_SECONDS)).toInt()

        // Are we clearly into the next chunk timestamp?  If yes, reinitialize as needed.  Note
        // that this path is also taken when we start this handler with a new patch connection.
        if (seconds > timestamp + BUFFER_SECONDS + BUFFER_SECONDS / 3) {
            Log.w(TAG, "Reinitializing from $timestamp to $stamp.")
            val packets = BUFFER_PACKETS * (seconds - stamp).toInt() / BUFFER_SECONDS

            if (timestamp > timebase) flush()
            serial = (actual - packets) and 0xffff
            timestamp = stamp
        }

        // How many packets have been accumulated in the current buffer?  Enough for a full buffer?
        var difference = (actual - serial) and 0xffff
        if (difference >= BUFFER_PACKETS) {
            // We've accumulated a full buffer's worth of data.  Flush it, and start the next.
            flush()
            timestamp += BUFFER_SECONDS
            difference = 0
            serial = actual
        }

        // Where within the buffer should we write the packet just received from the patch?
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

    // Will be launching a coroutine, so capture variables of interest as parameters.
    private fun store(buffer: ByteArray, stamp: Int) {
        GlobalScope.launch {
            uploader.upload(stamp, LEADS, FREQUENCY, buffer)
        }
    }

    companion object {
        // TODO(vjn): make more flexible.
        //
        // Currently, we support only 8 leads, 1000 Hz.  That works out to 24 bytes per sample.  10
        // samples in a packet.  The first four bytes of the packet are a serial number.  There are
        // 100 packets/second.  The buffer holds 15 seconds worth of data.
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

        private const val TAG = "data"
    }
}
