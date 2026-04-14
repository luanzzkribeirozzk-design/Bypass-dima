package com.eightball.pool

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class FloatingWebViewService : Service() {

    private var windowManager: WindowManager? = null
    private var webView: WebView? = null

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate() }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupWebView(intent?.getStringExtra("url") ?: "about:blank")
        return START_STICKY
    }

    private fun setupWebView(url: String) {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            webView = WebView(this).apply {
                settings.apply {
                    javaScriptEnabled = true; domStorageEnabled = true
                    loadWithOverviewMode = true; useWideViewPort = true
                    setSupportZoom(true); builtInZoomControls = true
                    displayZoomControls = false; cacheMode = WebSettings.LOAD_DEFAULT
                }
                webViewClient = WebViewClient()
                loadUrl(url)
            }
            windowManager?.addView(webView, params)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.let { windowManager?.removeView(it); it.destroy() }
    }
}
