package com.cping.test_touch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.WindowManager

class FaceOverlayView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
        alpha = 180
    }

    private var faces: List<Rect> = listOf()
    private var windowManager: WindowManager? = null
    private var isAttached = false

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    )

    fun attach(windowManager: WindowManager) {
        this.windowManager = windowManager
        if (!isAttached) {
            try {
                windowManager.addView(this, layoutParams)
                isAttached = true
                Log.d("FaceOverlay", "View attached")
            } catch (e: Exception) {
                Log.e("FaceOverlay", "Error attaching view", e)
            }
        }
    }

    fun detach() {
        if (isAttached) {
            try {
                windowManager?.removeView(this)
                isAttached = false
                Log.d("FaceOverlay", "View detached")
            } catch (e: Exception) {
                Log.e("FaceOverlay", "Error detaching view", e)
            }
        }
    }

    fun updateFaces(newFaces: List<Rect>) {
        faces = newFaces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        faces.forEach { rect ->
            canvas.drawRect(rect, paint)
        }
    }
}