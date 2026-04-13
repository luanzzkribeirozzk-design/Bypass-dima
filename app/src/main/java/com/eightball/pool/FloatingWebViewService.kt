package com.eightball.pool
import android.app.Service; import android.content.Intent; import android.graphics.PixelFormat
import android.os.IBinder; import android.util.Log; import android.view.Gravity
import android.view.WindowManager; import android.webkit.WebSettings
import android.webkit.WebView; import android.webkit.WebViewClient
class FloatingWebViewService : Service() {
    private var wm: WindowManager? = null; private var wv: WebView? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupWebView(intent?.getStringExtra("url") ?: "about:blank"); return START_STICKY
    }
    private fun setupWebView(url: String) {
        try {
            wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                if (android.os.Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            wv = WebView(this).apply {
                settings.apply { javaScriptEnabled=true; domStorageEnabled=true; loadWithOverviewMode=true; useWideViewPort=true; setSupportZoom(true); builtInZoomControls=true; displayZoomControls=false; cacheMode=WebSettings.LOAD_DEFAULT }
                webViewClient = WebViewClient(); loadUrl(url)
            }
            wm?.addView(wv, params)
        } catch (e: Exception) { Log.e("FWV", "${e.message}") }
    }
    override fun onDestroy() { super.onDestroy(); wv?.let { wm?.removeView(it); it.destroy() } }
}
