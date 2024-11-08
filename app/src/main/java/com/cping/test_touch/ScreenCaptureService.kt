package com.cping.test_touch

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions


class ScreenCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val handler = Handler(Looper.getMainLooper())
    private var imageReader: ImageReader? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "screen_capture_channel"
    private val TAG = "ScreenCaptureService"

    private var overlayView: FaceOverlayView? = null

    private val viewModel by lazy { FaceDetectionViewModel.getInstance() }

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            handler.post {
                Log.d(TAG, "MediaProjection stopped")
                stopCapture()
            }
        }
    }

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()

        FaceDetection.getClient(options)
    }

    private val screenCaptureRunnable = object : Runnable {
        override fun run() {
            captureScreen()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
        initializeOverlay()
    }

    private fun initializeOverlay() {
        overlayView = FaceOverlayView(this)
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView?.attach(windowManager)
    }

    private fun createNotificationChannel() {
        val name = "Screen Capture"
        val descriptionText = "Screen capture service notification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d(TAG, "Notification channel created")
    }

    private fun createNotification(): Notification {
        Log.d(TAG, "Creating notification")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("تحليل الشاشة")
            .setContentText("جاري البحث عن الوجوه...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        // نبدأ الخدمة في المقدمة فوراً
        startForeground(NOTIFICATION_ID, createNotification())

        intent?.let {
            val resultCode = it.getIntExtra("resultCode", Activity.RESULT_CANCELED)
            val data: Intent? = it.getParcelableExtra("data")

            if (data != null) {
                startCapture(resultCode, data)
            } else {
                Log.e(TAG, "No data received")
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        Log.d(TAG, "Starting capture")
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        try {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

            val projectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)?.apply {
                registerCallback(mediaProjectionCallback, handler)
                virtualDisplay = createVirtualDisplay(
                    "ScreenCapture",
                    width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null
                )
            }

            if (mediaProjection == null) {
                Log.e(TAG, "MediaProjection is null")
                stopSelf()
                return
            }

            handler.post(screenCaptureRunnable)
            showToast("بدأ تحليل الشاشة")
            updateNotification("جاري تحليل الشاشة...")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting capture", e)
            showToast("حدث خطأ في بدء التحليل")
            stopSelf()
        }
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("تحليل الشاشة")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureScreen() {
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null during capture")
            return
        }

        val image = imageReader?.acquireLatestImage()
        image?.use { img ->
            try {
                val bitmap = imageToBitmap(img)
                detectFaces(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing screen", e)
            }
        }
    }

    private fun detectFaces(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                val faceRects = faces.map { face ->
                    val bounds = face.boundingBox
                    Rect(bounds.left, bounds.top, bounds.right, bounds.bottom)
                }

                // تحديث Overlay
                handler.post {
                    overlayView?.updateFaces(faceRects)
                }

                val faceDataList = faces.map { face ->
                    val bounds = face.boundingBox
                    FaceDetectionData(
                        bounds.left,
                        bounds.top,
                        bounds.width(),
                        bounds.height()
                    )
                }

                viewModel.onFaceDetected(faceDataList)

                if (faces.isNotEmpty()) {
                    updateNotification("تم العثور على ${faces.size} وجه")
                    Log.d(TAG, "Found ${faces.size} faces")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error detecting faces", e)
            }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun stopCapture() {
        Log.d(TAG, "Stopping capture")
        try {
            handler.removeCallbacks(screenCaptureRunnable)
            virtualDisplay?.release()
            mediaProjection?.let {
                it.unregisterCallback(mediaProjectionCallback)
                it.stop()
            }
            mediaProjection = null
            imageReader?.close()
            overlayView?.detach()
            overlayView = null
            showToast("تم إيقاف التحليل")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capture", e)
        }
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        stopCapture()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null
}