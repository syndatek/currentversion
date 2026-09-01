package com.carditek.kesar

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DatabaseTest {
    @get:Rule
    var hilt = HiltAndroidRule(this)

    @get:Rule
    val instant = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Inject
    lateinit var chunks: ChunkDao

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Before
    fun init() {
        hilt.inject()
        chunks.clear()
    }

    @Test
    fun testChunks() = runTest {
        assertNotNull(chunks)
        assertEquals(chunks.getAll().size, 0)
    }

    @Test
    fun testInsertion() = runTest {
        chunks.insert(zero)
        val chunk = chunks.get(zero.address, zero.stamp)
        assertNotNull(chunk)
    }

    @Test
    fun testDeletion() = runTest {
        chunks.insert(zero)
        assertEquals(1, chunks.count())
        chunks.delete(zero.address, zero.stamp)
        assertEquals(0, chunks.count())
    }

    companion object {
        private val zero = Chunk(
            address = "00:00:00:00:00:00",
            stamp = 1800000000,
            email = "sugopal@carditek.com",
            frequency = 1000,
            leads = 8,
            logged = 0,
            patientName = "Sugandhi Gopal",
            patientPhone = "+1 (650) 555-5555",
            data = ByteArray(360000)
        )
    }
}
