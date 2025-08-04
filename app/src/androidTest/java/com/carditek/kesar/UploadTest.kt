package com.carditek.kesar

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.carditek.kesar.cloud.Uploader
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject
//import com.google.common.util.concurrent.ListenableFuture // ✅ OK
//import kotlinx.coroutines.guava.await
//import com.carditek.kesar.common.CommonUtils  // if there's a file/object named `CommonUtils.kt`


@HiltAndroidTest
class UploadTest {
    @get:Rule
    var hilt = HiltAndroidRule(this)

    @Inject
    lateinit var uploader: Uploader

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    private lateinit var context: Context

    @Inject
    lateinit var chunks: ChunkDao

    @Before
    fun setUp() {
        hilt.inject()
        chunks.clear()

        context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(hiltWorkerFactory)
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    private fun declareConstraintsMet(uuid: UUID) {
        WorkManagerTestInitHelper.getTestDriver(context)?.setAllConstraintsMet(uuid)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun testUpload() = runBlocking {
        val uuid = uploader.upload(1800000000, 8, 1000, ByteArray(3 * 8 * 1000 * 15))
        declareConstraintsMet(uuid)

        // Some notes.  Once the operation is enqueued, the wait is just for queuing, not for
        // task completion!  Similarly, the wait for the listenable future for work information
        // is for retrieving the current information, not for when the task is complete.
        //
        // Hence the following busy loop.  Note that retrieving the work information is not a
        // lightweight operation, so it mustn't be done too often!
        lateinit var info: WorkInfo
        for (i in 0 until 50) {
            info = WorkManager.getInstance(context).getWorkInfoById(uuid).get()
            //val info = WorkManager.getInstance(context).getWorkInfoById(uuid).await()

            if (info.state.isFinished) break
            Thread.sleep(100)
        }

        assertEquals(WorkInfo.State.SUCCEEDED, info.state)
        assertEquals("01:02:03:04:05:06/1800000000/8/1000", info.outputData.getString("result"))
    }
}
