package com.carditek.kesar

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import javax.inject.Inject

@AndroidEntryPoint
abstract class WebViewFragment : Fragment() {
    protected abstract fun url(): String

    protected abstract fun webView(): WebView

    @Inject
    lateinit var account: Account

    @Inject
    lateinit var backend: Backend

    @Inject
    lateinit var cache: Cache

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()
        val context = this.requireContext()
        webView().run {
            // WebView hardening (production baseline).
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            @Suppress("DEPRECATION")
            settings.savePassword = false
            settings.setSupportMultipleWindows(false)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

            webChromeClient = ChromeClient()
            webViewClient = WebClient(backend, cache)
            // JS bridge increases attack surface; keep it minimal and avoid release logging.
            addJavascriptInterface(Interface(context), "Android")
        }
    }

    override fun onResume() {
        super.onResume()
        webView().loadUrl(url())
    }

    override fun onPause() {
        super.onPause()
        webView().loadUrl("about:blank")
    }

    private class Interface(private val context: Context) {
        @JavascriptInterface
        fun toast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun log(message: String) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, message)
            }
        }
    }

    private class ChromeClient : WebChromeClient() {
        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "(js) " + message.message())
            }
            return true
        }
    }

    private class WebClient(
        private val backend: Backend, private val cache: Cache
    ) : WebViewClient() {
        private fun isAllowed(url: android.net.Uri): Boolean {
            // Allow only your production domain + local blank page.
            val s = url.toString()
            if (s == "about:blank") return true
            return url.scheme == "https" && url.host == "ecg.carditek.com"
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url ?: return false
            return if (isAllowed(uri)) {
                false
            } else {
                true
            }
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Loading: $url")
            }
            super.onLoadResource(view, url)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            request?.url?.let {
                if (!isAllowed(it)) {
                    return WebResourceResponse(
                        "text/plain", "UTF-8", 403, "Blocked", null,
                        ByteArrayInputStream(ByteArray(0))
                    )
                }
                if (it.path == "/app/data" || (it.path?.startsWith("/api/data/") == true)) {
                    val address: String?
                    val timestamp: Int?
                    val frequency: Int? = it.getQueryParameter("frequency")?.toInt()

                    if (it.path == "/app/data") {
                        address = it.getQueryParameter("mac-address")
                        timestamp = it.getQueryParameter("timestamp")?.toInt()

                    } else {
                        address = it.pathSegments[2]
                        timestamp = it.pathSegments[3]?.toInt()
                    }

                    if (address != null && timestamp != null && frequency != null) {
                        val buffer = cache.get(address, timestamp, frequency)
                        if (buffer != null) {
                            val response = WebResourceResponse(
                                "application/octet-stream",
                                null,
                                ByteArrayInputStream(buffer)
                            )

                            response.responseHeaders = if (it.path == "/app/data") {
                                mapOf(
                                    "frequency" to frequency.toString(),
                                    "leads" to "8"
                                )
                            } else {
                                mapOf(
                                    "X-Carditek-Frequency" to frequency.toString(),
                                    "X-Carditek-Leads" to "8"
                                )
                            }

                            return response
                        }
                    }

                    return WebResourceResponse(
                        "text/plain", "UTF-8", 404, "Not found", null,
                        ByteArrayInputStream(ByteArray(0))
                    )
                }
            }

            if (request?.url?.toString() == "https://ecg.carditek.com/api/me") {
                val result = runBlocking { backend.me() }
                return WebResourceResponse(
                    "text/plain", "UTF-8",
                    result?.byteInputStream(Charset.defaultCharset())
                )
            }
            return super.shouldInterceptRequest(view, request)
        }
    }

    companion object {
        private const val TAG = "webview"
    }
}
