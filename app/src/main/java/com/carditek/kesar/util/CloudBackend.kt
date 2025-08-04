package com.carditek.kesar.util

import android.util.Log
import com.carditek.kesar.Account
import com.carditek.kesar.Backend
import com.carditek.kesar.Chunk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.CookieManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudBackend @Inject constructor(private val account: Account) : Backend {
    class HttpException(val code: Int, message: String) : Exception(message)

    private val http = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(CookieManager()))
        .followRedirects(true)
        .build()

    override suspend fun me(): String? {
        val body = send(builder("/api/me").build()).body
        return withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            body?.string()
        }
    }

    override suspend fun fetch(address: String, stamp: Int): ByteArray? {
        val request = builder(("/api/data/${address}/${stamp}"))
            .addHeader("content-type", CONTENT_TYPE)
            .build()
        val body = send(request).body
        return withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            body?.bytes()
        }
    }

    override suspend fun store(chunk: Chunk) {
        val request = builder(("/api/data/${chunk.address}/${chunk.stamp}"))
            .addHeader("x-carditek-leads", "${chunk.leads}")
            .addHeader("x-carditek-frequency", "${chunk.frequency}")
            .addHeader("x-carditek-patient-name", chunk.patientName)
            .addHeader("x-carditek-patient-phone", chunk.patientPhone)
            .post(chunk.data.toRequestBody(CONTENT_TYPE.toMediaType()))
            .build()
        send(request)
    }

    override suspend fun note(address: String, stamp: Int, text: String) {
        val request = builder("/api/note/${address}/${stamp}")
            .post(text.toRequestBody())
            .build()
        send(request)
    }

    private suspend fun send(request: Request): Response {
        return suspendCancellableCoroutine { continuation ->
            val call = http.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                    Log.e(TAG, "${request.method} ${request.url} failed", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful)
                        continuation.resume(response)
                    else {
                        val message = "HTTP ${response.code} on ${request.method} ${request.url}"
                        continuation.resumeWithException(HttpException(response.code, message))
                        Log.w(TAG, message)
                    }
                }
            })

            continuation.invokeOnCancellation {
                try {
                    call.cancel()
                } catch (ex: Throwable) {
                }
            }
        }
    }

    private fun builder(path: String): Request.Builder {
        val builder = Request.Builder().url(BASE + path)
        builder.addHeader("Authorization", "Bearer " + account.token)
        return builder
    }

    companion object {
        private const val CONTENT_TYPE = "application/octet-stream"
        private const val BASE = "https://ecg.carditek.com"
        private const val TAG = "Cloud"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object BackendModule {
    @Provides
    @Singleton
    fun provideBackend(account: Account): Backend {
        return CloudBackend(account)
    }
}
