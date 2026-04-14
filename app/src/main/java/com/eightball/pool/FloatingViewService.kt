package com.eightball.pool

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout

class FloatingViewService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = 100 }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(ImageView(this@FloatingViewService).apply {
                setImageResource(android.R.drawable.ic_menu_more)
                layoutParams = LinearLayout.LayoutParams(80, 80)
            })
        }

        var ix = 0; var iy = 0; var itx = 0f; var ity = 0f
        layout.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    ix = params.x; iy = params.y; itx = e.rawX; ity = e.rawY; true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = ix + (e.rawX - itx).toInt()
                    params.y = iy + (e.rawY - ity).toInt()
                    windowManager?.updateViewLayout(layout, params); true
                }
                MotionEvent.ACTION_UP -> {
                    if (Math.abs(e.rawX - itx) < 5 && Math.abs(e.rawY - ity) < 5) {
                        try {
                            Runtime.getRuntime().exec(arrayOf("sh",
                                "/storage/emulated/0/patoteam/rish",
                                "/storage/emulated/0/patoteam/pato0.sh"))
                        } catch (ex: Exception) {
                            Log.e("FVS", "Error: ${ex.message}")
                        }
                    }
                    true
                }
                else -> false
            }
        }
        floatingView = layout
        windowManager?.addView(layout, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager?.removeView(it) }
    }
}
