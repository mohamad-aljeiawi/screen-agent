package com.cping.test_touch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class FaceDetectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BroadcastResults.ACTION_FACE_DETECTED) {
            val x = intent.getIntExtra("x", 0)
            val y = intent.getIntExtra("y", 0)
            val width = intent.getIntExtra("width", 0)
            val height = intent.getIntExtra("height", 0)

            Log.d("FaceDetection", "وجه مكتشف في: x=$x, y=$y, width=$width, height=$height")
        }
    }
}