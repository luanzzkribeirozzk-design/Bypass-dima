package com.eightball.pool

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout

class FloatingViewService : Service() {

    companion object {
        private const val TAG = "FloatingViewService"
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FloatingViewService created")
        setupFloatingView()
    }

    private fun setupFloatingView() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 100

            // Create a simple floating button
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL

            val imageView = ImageView(this)
            imageView.setImageResource(android.R.drawable.ic_menu_more)
            imageView.layoutParams = LinearLayout.LayoutParams(80, 80)
            layout.addView(imageView)

            floatingView = layout

            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            layout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(layout, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY
                        if (Math.abs(dx) < 5 && Math.abs(dy) < 5) {
                            onFloatingButtonClick()
                        }
                        true
                    }
                    else -> false
                }
            }

            windowManager?.addView(layout, params)
            Log.d(TAG, "Floating view added")

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up floating view: ${e.message}", e)
        }
    }

    private fun onFloatingButtonClick() {
        Log.d(TAG, "Floating button clicked")
        try {
            val rishPath = "/storage/emulated/0/patoteam/rish"
            val scriptPath = "/storage/emulated/0/patoteam/pato0.sh"
            val process = Runtime.getRuntime().exec(arrayOf("sh", rishPath, scriptPath))
            val exitCode = process.waitFor()
            Log.d(TAG, "pato0.sh executed with exit code: $exitCode")
        } catch (e: Exception) {
            Log.e(TAG, "Error executing script: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            windowManager?.removeView(it)
        }
        Log.d(TAG, "FloatingViewService destroyed")
    }
}
