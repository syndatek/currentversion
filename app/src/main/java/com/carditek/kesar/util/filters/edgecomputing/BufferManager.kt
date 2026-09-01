

package com.carditek.kesar.util.filters.edgecomputing

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class BufferManager(
    private val listener: BufferListener
) {

    companion object {

        private const val TAG = "BufferManager"

        //==================================================
        // ECG Configuration
        //==================================================

        const val SAMPLE_RATE = 1000                 // Hz

        const val BUFFER_SECONDS = 15

        const val BUFFER_SIZE =
            SAMPLE_RATE * BUFFER_SECONDS             //15000

        const val BUFFER2_HOLD_TIME = 5000L          //5 Seconds
    }

    //==================================================
    // Buffer States
    //==================================================

    enum class State {

        IDLE,

        COLLECTING,

        COPYING,

        BUFFER2_HOLD,

        CLEARING_BUFFER2,

        WAITING
    }

    //==================================================
    // Callback Interface
    //==================================================

    interface BufferListener {

        fun onBufferReady(
            samples: IntArray,
            cycle: Int
        )

        fun onLog(
            message: String
        )
    }

    //==================================================
    // Synchronization
    //==================================================

    /**
     * Single lock for ALL buffer operations.
     *
     * Never synchronize on buffer1/buffer2 directly.
     */
    private val lock = Any()

    //==================================================
    // Buffers
    //==================================================

    private val buffer1 =
        ArrayList<Int>(BUFFER_SIZE)

    private val buffer2 =
        ArrayList<Int>(BUFFER_SIZE)

    //==================================================
    // Android Handler
    //==================================================

    private val handler =
        Handler(Looper.getMainLooper())

    //==================================================
    // Atomic Flags
    //==================================================

    private val collecting =
        AtomicBoolean(false)

    private val buffer2Busy =
        AtomicBoolean(false)

    //==================================================
    // Runtime State
    //==================================================

    @Volatile
    private var state =
        State.IDLE

    //==================================================
    // Statistics
    //==================================================

    private var cycle = 1

    private var totalSamples = 0L

    private var totalPackets = 0L

    //==================================================
    // Timing
    //==================================================

    private var acquisitionStart = 0L

    private var bufferStart = 0L

    private var bufferEnd = 0L

    private var copyStart = 0L

    private var copyEnd = 0L

    private var callbackStart = 0L

    private var callbackEnd = 0L

    private var holdStart = 0L

    private var holdEnd = 0L
    // Initialization

    init {

        acquisitionStart =
            SystemClock.elapsedRealtime()

        bufferStart =
            acquisitionStart

        collecting.set(true)

        state =
            State.COLLECTING

        logHeader()

        log(
            """
Buffer Manager Initialized

Sample Rate      : $SAMPLE_RATE Hz

Buffer Size      : $BUFFER_SIZE Samples

Window Length    : $BUFFER_SECONDS Seconds

Buffer2 Hold     : ${BUFFER2_HOLD_TIME} ms

Initial State    : $state
            """.trimIndent()
        )
    }


    // Public API
    fun start() {

        synchronized(lock) {

            collecting.set(true)

            state =
                State.COLLECTING

            acquisitionStart =
                SystemClock.elapsedRealtime()

            bufferStart =
                acquisitionStart

            log(
                """
Acquisition Started

Cycle = $cycle
                """.trimIndent()
            )
        }
    }

    fun stop() {

        synchronized(lock) {

            collecting.set(false)

            state =
                State.IDLE

            log(
                """
Acquisition Stopped

Total Samples = $totalSamples

Total Packets = $totalPackets
                """.trimIndent()
            )
        }
    }

    // Logger


    private fun logHeader() {

        Log.d(
            TAG,
            "=========================================================="
        )

        Log.d(
            TAG,
            "             ECG DOUBLE BUFFER MANAGER"
        )

        Log.d(
            TAG,
            "=========================================================="
        )
    }

    private fun log(message: String) {

        val time =
            SimpleDateFormat(
                "HH:mm:ss.SSS",
                Locale.getDefault()
            ).format(Date())

        val text =
            """
--------------------------------------------------------
Time         : $time

Thread       : ${Thread.currentThread().name}

State        : $state

Cycle        : $cycle

Buffer1 Size : ${buffer1.size}

Buffer2 Size : ${buffer2.size}

Collecting   : ${collecting.get()}

Buffer2 Busy : ${buffer2Busy.get()}

Packets      : $totalPackets

Samples      : $totalSamples

$message
--------------------------------------------------------
            """.trimIndent()

        Log.d(
            TAG,
            text
        )

        // Forward to EdgeComputingProcessor if required
        listener.onLog(text)
    }

    // Packet Counter


    fun onPacketReceived() {

        totalPackets++

        if (totalPackets % 100 == 0L) {

            log(
                """
Packets Received

Total Packets = $totalPackets
                """.trimIndent()
            )
        }
    }

    // Lead4 Sample Collection


    fun addSample(sample: Int) {

        if (!collecting.get()) {
            return
        }

        synchronized(lock) {

            // Do not accept new samples while copying
            if (state != State.COLLECTING) {
//            if (!collecting.get()) {
                return
            }


            // Store sample


            buffer1.add(sample)

            totalSamples++


            // Every second


            if (buffer1.size % SAMPLE_RATE == 0) {

                val elapsed =
                    SystemClock.elapsedRealtime() - bufferStart

                val remaining =
                    BUFFER_SIZE - buffer1.size

                log(
                    """
[01] BUFFER1 COLLECTING

Collected Samples : ${buffer1.size}/$BUFFER_SIZE

Elapsed Time      : ${elapsed} ms

Remaining Samples : $remaining
                    """.trimIndent()
                )
            }

            // Overflow Protection


            if (buffer1.size > BUFFER_SIZE) {

                log(
                    """
******** BUFFER OVERFLOW ********

Buffer1 Size = ${buffer1.size}

Expected     = $BUFFER_SIZE

State        = $state

Busy         = ${buffer2Busy.get()}
                    """.trimIndent()
                )

                return
            }


            // Buffer Full


            if (buffer1.size >= BUFFER_SIZE) {

                state =
                    State.COPYING

                bufferEnd =
                    SystemClock.elapsedRealtime()

                val duration =
                    bufferEnd - bufferStart

                log(
                    """
==================================================

[02] BUFFER1 FULL

Collection Finished

Samples       : ${buffer1.size}

Duration      : ${duration} ms

Starting Copy To Buffer2

==================================================
                    """.trimIndent()
                )

                copyBuffer1ToBuffer2()
            }
        }
    }

    //==================================================
    // Status
    //==================================================

    fun printStatus() {

        synchronized(lock) {

            val elapsed =
                SystemClock.elapsedRealtime() - acquisitionStart

            log(
                """
CURRENT STATUS

Cycle            : $cycle

State            : $state

Buffer1 Samples  : ${buffer1.size}

Buffer2 Samples  : ${buffer2.size}

Buffer2 Busy     : ${buffer2Busy.get()}

Collecting       : ${collecting.get()}

Total Samples    : $totalSamples

Total Packets    : $totalPackets

Running Time     : ${elapsed} ms
                """.trimIndent()
            )
        }
    }
    // Helper Functions


    fun getBuffer1Size(): Int {

        synchronized(lock) {
            return buffer1.size
        }
    }

    fun getBuffer2Size(): Int {

        synchronized(lock) {
            return buffer2.size
        }
    }

    fun getCycle(): Int {

        synchronized(lock) {
            return cycle
        }
    }

    fun getCurrentState(): State {

        return state
    }

    fun isCollecting(): Boolean {

        return collecting.get()
    }

    fun isBuffer2Busy(): Boolean {

        return buffer2Busy.get()
    }

    // Copy Buffer1 -> Buffer2

    private fun copyBuffer1ToBuffer2() {

        synchronized(lock) {

            // Prevent overlapping copy operations

            if (buffer2Busy.get()) {

                log(
                    """
==================================================

COPY REQUEST REJECTED

Reason : Buffer2 is still BUSY

Cycle  : $cycle

State  : $state

==================================================
                    """.trimIndent()
                )

                return
            }

            //--------------------------------------------------
            // Enter COPY state
            //--------------------------------------------------

            state = State.COPYING

            buffer2Busy.set(true)

            log(
                """
==================================================

[03] COPY STARTED

Cycle           : $cycle

Buffer1 Samples : ${buffer1.size}

Buffer2 Samples : ${buffer2.size}

==================================================
                """.trimIndent()
            )
            // Measure Copy Time


            copyStart = SystemClock.elapsedRealtime()


            // Clear Previous Buffer2

            if (buffer2.isNotEmpty()) {

                log(
                    """
Old Buffer2 Found

Previous Samples = ${buffer2.size}

Clearing Before Copy...
                    """.trimIndent()
                )

                buffer2.clear()
            }

            // Copy Buffer1 -> Buffer2


            buffer2.addAll(buffer1)

            copyEnd = SystemClock.elapsedRealtime()

            log(
                """
==================================================

[04] COPY FINISHED

Copied Samples : ${buffer2.size}

Copy Time      : ${copyEnd - copyStart} ms

==================================================
                """.trimIndent()
            )



            // Clear Buffer1 Immediately

            val oldSize = buffer1.size

            buffer1.clear()

            bufferStart =
                SystemClock.elapsedRealtime()

            log(
                """
==================================================

[05] BUFFER1 CLEARED

Previous Size : $oldSize

Current Size  : ${buffer1.size}

New Acquisition Started

==================================================
                """.trimIndent()
            )

            //--------------------------------------------------
            // Switch to Collection Again
            //--------------------------------------------------

            state = State.COLLECTING

            log(
                """
Buffer1 Ready

Collecting Next 15 Second Window...

Cycle = $cycle
                """.trimIndent()
            )
        }


        val callbackStart = SystemClock.elapsedRealtime()
        listener.onBufferReady(
            buffer2.toIntArray(),
            cycle
        )
        val callbackEnd = SystemClock.elapsedRealtime()

        log(
            """
==================================================

[06] CALLBACK SENT

Cycle = $cycle

Samples = ${buffer2.size}

Callback Time = ${callbackEnd - callbackStart} ms

==================================================
                """.trimIndent()
        )



        startBuffer2Hold()
    }


    private fun startBuffer2Hold() {

        holdStart = SystemClock.elapsedRealtime()
        state = State.COLLECTING

        log(
            """
==================================================

[07] BUFFER2 HOLD STARTED

Holding Previous ECG Window

Hold Time : ${BUFFER2_HOLD_TIME} ms

Buffer1 Continues Collecting...

==================================================
            """.trimIndent()
        )

        handler.postDelayed({

            holdEnd = SystemClock.elapsedRealtime()

            log(
                """
==================================================

[08] BUFFER2 HOLD COMPLETED

Hold Duration : ${holdEnd - holdStart} ms

5 Seconds Expired

==================================================
                """.trimIndent()
            )

            checkBuffer1()

        }, BUFFER2_HOLD_TIME)
    }

    //==================================================
    // Check Buffer1 Before Clearing Buffer2
    //==================================================

    private fun checkBuffer1() {

        synchronized(lock) {

            state = State.WAITING

            val collected = buffer1.size

            log(
                """
==================================================

[08] CHECKING BUFFER1

Current Samples = $collected

Target Samples  = $BUFFER_SIZE

==================================================
                """.trimIndent()
            )

            //--------------------------------------------------
            // Buffer1 has started collecting normally
            //--------------------------------------------------

            if (collected > 0) {

                log(
                    """
Buffer1 Is Collecting Normally

Collected Samples = $collected

Safe To Clear Buffer2
                    """.trimIndent()
                )

                clearBuffer2()

                return
            }



            log(
                """
Buffer1 Still Empty

Waiting Another 1000 ms...
                """.trimIndent()
            )
        }

        handler.postDelayed({

            checkBuffer1()

        }, 1000)
    }

    //==================================================
    // Clear Buffer2
    //==================================================

    private fun clearBuffer2() {

        synchronized(lock) {

            state = State.CLEARING_BUFFER2

            log(
                """
==================================================

[09] CLEAR BUFFER2

Buffer2 Size Before Clear = ${buffer2.size}

==================================================
                """.trimIndent()
            )

            buffer2.clear()

            buffer2Busy.set(false)

            cycle++

            state = State.COLLECTING

            log(
                """
==================================================

[10] NEXT CYCLE STARTED

Cycle              = $cycle

Buffer1 Samples    = ${buffer1.size}

Buffer2 Samples    = ${buffer2.size}

Buffer2 Busy       = ${buffer2Busy.get()}

Collecting         = ${collecting.get()}

Waiting For Next 15000 Samples

==================================================
                """.trimIndent()
            )
        }
    }
    //==================================================
    // Reset Buffer Manager
    //==================================================

    fun reset() {

        synchronized(lock) {

            handler.removeCallbacksAndMessages(null)

            buffer1.clear()
            buffer2.clear()

            buffer2Busy.set(false)
            collecting.set(true)

            cycle = 1
            totalSamples = 0
            totalPackets = 0

            acquisitionStart =
                SystemClock.elapsedRealtime()

            bufferStart =
                acquisitionStart

            bufferEnd = 0L

            copyStart = 0L

            copyEnd = 0L

            callbackStart = 0L

            callbackEnd = 0L

            holdStart = 0L

            holdEnd = 0L

            state = State.COLLECTING

            log(
                """
==================================================

BUFFER MANAGER RESET

Buffer1 Cleared

Buffer2 Cleared

Counters Reset

Ready For New Acquisition

==================================================
                """.trimIndent()
            )
        }
    }

    //==================================================
    // Clear Buffers Only
    //==================================================

    fun clearAllBuffers() {

        synchronized(lock) {

            buffer1.clear()

            buffer2.clear()

            buffer2Busy.set(false)

            log(
                """
==================================================

ALL BUFFERS CLEARED

Buffer1 = ${buffer1.size}

Buffer2 = ${buffer2.size}

==================================================
                """.trimIndent()
            )
        }
    }

    //==================================================
    // Release Resources
    //==================================================

    fun release() {

        synchronized(lock) {

            collecting.set(false)

            handler.removeCallbacksAndMessages(null)

            buffer1.clear()

            buffer2.clear()

            buffer2Busy.set(false)

            state = State.IDLE

            log(
                """
==================================================

BUFFER MANAGER RELEASED

All Timers Cancelled

Buffers Cleared

State = $state

==================================================
                """.trimIndent()
            )
        }
    }

    //==================================================
    // Runtime Information
    //==================================================

    fun printStatistics() {

        synchronized(lock) {

            val runningTime =
                SystemClock.elapsedRealtime() - acquisitionStart

            log(
                """
================ STATISTICS ================

Cycle               : $cycle

Current State       : $state

Running Time        : ${runningTime} ms

Buffer1 Samples     : ${buffer1.size}

Buffer2 Samples     : ${buffer2.size}

Total Samples       : $totalSamples

Total Packets       : $totalPackets

Buffer2 Busy        : ${buffer2Busy.get()}

Collecting          : ${collecting.get()}

============================================
                """.trimIndent()
            )
        }
    }

    //==================================================
    // Public Getters
    //==================================================

    fun getTotalSamples(): Long = totalSamples

    fun getTotalPackets(): Long = totalPackets

    fun getCycleNumber(): Int = cycle

    fun getState(): State = state

    fun getBuffer1Snapshot(): IntArray {

        synchronized(lock) {

            return buffer1.toIntArray()
        }
    }

    fun getBuffer2Snapshot(): IntArray {

        synchronized(lock) {

            return buffer2.toIntArray()
        }
    }

    //==================================================
    // Debug Dump
    //==================================================

    fun dumpBuffers() {

        synchronized(lock) {

            log(
                """
================ BUFFER DUMP ================

Cycle        : $cycle

State        : $state

Buffer1 Size : ${buffer1.size}

Buffer2 Size : ${buffer2.size}

Buffer2 Busy : ${buffer2Busy.get()}

Collecting   : ${collecting.get()}

=============================================
                """.trimIndent()
            )
        }
    }
}
