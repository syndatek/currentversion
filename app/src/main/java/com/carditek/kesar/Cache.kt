



package com.carditek.kesar



import android.content.Context

import android.util.Log

import android.util.LruCache

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.LiveData

import com.carditek.kesar.util.filters.edgecomputing.EdgeComputingProcessor

import javax.inject.Inject





class Cache @Inject constructor(

    private val context: Context,

    private val device: Device,

    private val edgeComputingProcessor: EdgeComputingProcessor

) {



    //==========================================================

    // LiveData exposed to UI

    //==========================================================



    val heartRateLive: LiveData<Int> = edgeComputingProcessor.heartRateLive



    val beatNumberLive = edgeComputingProcessor.beatNumberLive



    val pOnsetLocationLive = edgeComputingProcessor.pOnsetLocationLive



    val pPeakLocationLive = edgeComputingProcessor.pPeakLocationLive



    val qrsOnLocationLive = edgeComputingProcessor.qrsOnLocationLive



    val qrsOffLocationLive = edgeComputingProcessor.qrsOffLocationLive



    val rPeakLocationLive = edgeComputingProcessor.rPeakLocationLive



    val prDurationLive = edgeComputingProcessor.prDurationLive



    val qrsDurationLive = edgeComputingProcessor.qrsDurationLive



    val paDurationLive =edgeComputingProcessor.paDurationLive



    val ahDurationLive =edgeComputingProcessor.ahDurationLive

    val hvDurationLive =edgeComputingProcessor.hvDurationLive

    val hAmplitudeLive =edgeComputingProcessor.hAmplitudeLive



    val lastUpdatedLive = edgeComputingProcessor.lastUpdatedLive

    val bleConnectedLive = MutableLiveData(false)

    /**

     * Enable / Disable Edge Computing

     */

    fun setFilteringEnabled(enabled: Boolean) {

        edgeComputingProcessor.setEnabled(enabled)

        Log.d("CACHE_DEBUG", "Edge Computing Enabled = $enabled")

    }



    fun isFilteringEnabled(): Boolean =

        edgeComputingProcessor.isEnabled()



    /**

     * Return cached ECG (100 Hz)

     */

    fun get(address: String, stamp: Int, frequency: Int): ByteArray? {

        return helper(address, stamp - 2, frequency)

    }



    private fun helper(

        address: String,

        stamp: Int,

        frequency: Int

    ): ByteArray? {



        if (address != device.address.value || frequency != 100) {

            return null

        }



        val buffer = lru.get(stamp) ?: return null



        if (buffer.size != 2400) {

            throw Exception("Expected 2400 bytes, got ${buffer.size}")

        }



        return if (address != "54:6C:0E:83:3E:49") {

            buffer

        } else {

            val begin = (stamp % 15) * 2400

            load().sliceArray(begin until begin + 2400)

        }

    }



    /**

     * Bluetooth ECG Entry Point

     */

    fun put(stamp: Int, buffer: ByteArray) {



        if (buffer.size != 24000) {

            throw Exception("Expected 24000 bytes, got ${buffer.size}")

        }



        //------------------------------------------------------

        // Step 1 : Downsample 1000 Hz -> 100 Hz

        //------------------------------------------------------



        val sampled = ByteArray(2400)



        for (i in 0 until 100) {

            buffer.copyInto(

                sampled,

                i * 24,

                i * 240,

                i * 240 + 24

            )

        }



        //------------------------------------------------------

        // Step 2 : Store for waveform display

        //------------------------------------------------------



        lru.put(stamp, sampled)



        //------------------------------------------------------

        // Step 3 : Send RAW ECG to Edge Computing

        //------------------------------------------------------



        if (edgeComputingProcessor.isEnabled()) {



            edgeComputingProcessor.processRawData(buffer)



            Log.d(

                "CACHE_FLOW",

                "Processed ECG Stamp = $stamp"

            )



        } else {



            Log.d(

                "CACHE_FLOW",

                "Edge Computing Disabled"

            )

        }

    }



    /**

     * Demo Data

     */

    @Synchronized

    fun load(): ByteArray {



        if (fake == null) {

            fake = context.resources

                .openRawResource(R.raw.chunkdata)

                .readBytes()

        }



        return fake!!

    }



    /**

     * Local Cache

     */

    private val lru = LruCache<Int, ByteArray>(300)



    companion object {

        private var fake: ByteArray? = null

    }

}



