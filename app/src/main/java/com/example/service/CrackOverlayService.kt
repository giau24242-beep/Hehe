package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.manager.PrankManager
import com.example.model.CrackImpact
import com.example.model.RgbGlitchLine
import com.example.util.CrackGenerator

class CrackOverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "broken_screen_prank_channel"
    }

    private var windowManager: WindowManager? = null
    private var overlayRootView: FrameLayout? = null
    private var crackDrawView: OverlayCrackDrawView? = null

    private var lastBackPressTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PrankManager.initialize(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                PrankManager.setOverlayRunning(true)
                showOverlay()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bể Màn Hình Troll",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo điều khiển lớp phủ vỡ màn hình"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, CrackOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            this,
            1,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bể Màn Hình Đang Bật")
            .setContentText("Chạm vào màn hình để làm vỡ. Nhấn Back 2 lần để thoát.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(appPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Tắt Troll Ngay",
                stopPendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showOverlay() {
        if (overlayRootView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Layout params: focusable so it can intercept BACK key presses!
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        overlayRootView = object : FrameLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        PrankManager.getVibrationManager(context).triggerDoubleBackHaptic()
                        stopSelf()
                        return true
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(
                            context,
                            "Nhấn nút Back thêm 1 lần nữa để tắt màn hình bể",
                            Toast.LENGTH_SHORT
                        ).show()
                        return true
                    }
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        // Crack Drawing View
        crackDrawView = OverlayCrackDrawView(this) { x, y ->
            handleScreenTouch(x, y)
        }
        overlayRootView?.addView(
            crackDrawView,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        )

        // Subtle Emergency Exit Pill at top center
        val exitBadge = TextView(this).apply {
            text = "Bấm Back 2 lần hoặc chạm vào đây để thoát ✕"
            setTextColor(Color.WHITE)
            textSize = 11f
            setBackgroundColor(Color.argb(130, 20, 24, 33))
            setPadding(30, 16, 30, 16)
            gravity = Gravity.CENTER
            setOnClickListener {
                stopSelf()
            }
        }
        val badgeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = 50
        }
        overlayRootView?.addView(exitBadge, badgeParams)

        try {
            windowManager?.addView(overlayRootView, params)
            overlayRootView?.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun handleScreenTouch(x: Float, y: Float) {
        val width = overlayRootView?.width?.toFloat() ?: 1080f
        val height = overlayRootView?.height?.toFloat() ?: 2400f
        val config = PrankManager.settings.value

        // Sound
        if (config.soundEnabled) {
            PrankManager.getSoundManager(this).playCrackSound(config.soundType, config.soundVolume)
        }

        // Strong Vibration
        if (config.vibrationEnabled) {
            PrankManager.getVibrationManager(this).triggerVibration(config.vibrationIntensity)
        }

        crackDrawView?.addImpact(x, y, width, height)
    }

    override fun onDestroy() {
        super.onDestroy()
        PrankManager.setOverlayRunning(false)
        if (overlayRootView != null && windowManager != null) {
            try {
                windowManager?.removeView(overlayRootView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayRootView = null
        }
    }
}

/**
 * Custom Hardware-accelerated drawing view for the System Overlay window.
 */
@SuppressLint("ViewConstructor")
class OverlayCrackDrawView(
    context: Context,
    private val onScreenTouched: (x: Float, y: Float) -> Unit
) : View(context) {

    private val impacts = mutableListOf<CrackImpact>()
    private var globalGlitches = listOf<RgbGlitchLine>()
    private var totalTaps = 0
    private var isFlickering = false

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.argb(160, 5, 10, 20)
    }

    private val iceTintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.argb(200, 186, 230, 253)
    }

    private val specularWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
    }

    private val inkCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val inkHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shardStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(220, 255, 255, 255)
        strokeWidth = 1.2f
    }

    private val glitchPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            onScreenTouched(event.rawX, event.rawY)
            return true
        }
        return super.onTouchEvent(event)
    }

    fun addImpact(x: Float, y: Float, width: Float, height: Float) {
        totalTaps++
        val config = PrankManager.settings.value

        val nearbyCount = impacts.count {
            val dx = it.touchX - x
            val dy = it.touchY - y
            (dx * dx + dy * dy) < 180f * 180f
        }

        val impact = CrackGenerator.createImpact(
            touchX = x,
            touchY = y,
            screenWidth = width,
            screenHeight = height,
            tapCount = totalTaps,
            existingNearbyCount = nearbyCount,
            settings = config
        )

        impacts.add(impact)

        if (config.rgbGlitchEnabled) {
            globalGlitches = CrackGenerator.generateGlobalGlitchLines(width, height, totalTaps)
        }

        isFlickering = config.flickerGlitchEnabled && (totalTaps >= 6 && totalTaps % 3 == 0)

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw each impact
        for (impact in impacts) {
            // A. AMOLED / LCD Ink Bleed Blobs
            for (blob in impact.inkBlobs) {
                if (blob.contourPoints.size >= 3) {
                    val p = Path().apply {
                        moveTo(blob.contourPoints[0].x, blob.contourPoints[0].y)
                        for (i in 1 until blob.contourPoints.size) {
                            lineTo(blob.contourPoints[i].x, blob.contourPoints[i].y)
                        }
                        close()
                    }

                    // Halo gradient
                    inkHaloPaint.shader = RadialGradient(
                        blob.center.x,
                        blob.center.y,
                        blob.radius * 1.35f,
                        intArrayOf(Color.BLACK, Color.argb(160, 74, 14, 78), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.7f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(blob.center.x, blob.center.y, blob.radius * 1.35f, inkHaloPaint)

                    // Black ink blob core
                    canvas.drawPath(p, inkCorePaint)
                }
            }

            // B. Glass Shards
            for (shard in impact.glassShards) {
                if (shard.vertices.size >= 3) {
                    val p = Path().apply {
                        moveTo(shard.vertices[0].x, shard.vertices[0].y)
                        for (v in 1 until shard.vertices.size) {
                            lineTo(shard.vertices[v].x, shard.vertices[v].y)
                        }
                        close()
                    }
                    shardFillPaint.color = Color.argb(
                        (shard.fillColor.alpha * 255).toInt(),
                        (shard.fillColor.red * 255).toInt(),
                        (shard.fillColor.green * 255).toInt(),
                        (shard.fillColor.blue * 255).toInt()
                    )
                    canvas.drawPath(p, shardFillPaint)
                    canvas.drawPath(p, shardStrokePaint)
                }
            }

            // C. Radial Rays (3-Layer Stroke)
            for (ray in impact.radialRays) {
                drawRayLines(canvas, ray.points, ray.strokeWidth)
                for (branch in ray.branchPaths) {
                    drawRayLines(canvas, branch, ray.strokeWidth * 0.75f)
                }
            }

            // D. Spiderweb Rings
            for (ring in impact.webRings) {
                drawRayLines(canvas, ring.points, ring.strokeWidth)
            }

            // E. Glitch lines for this impact
            for (glitch in impact.glitchLines) {
                drawGlitchLine(canvas, glitch, w, h)
            }

            // Central impact point
            specularWhitePaint.style = Paint.Style.FILL
            canvas.drawCircle(impact.touchX, impact.touchY, 8f + impact.severityLevel * 2f, shadowPaint)
            canvas.drawCircle(impact.touchX, impact.touchY, 4f, specularWhitePaint)
            specularWhitePaint.style = Paint.Style.STROKE
        }

        // 2. Global Glitch Lines
        for (glitch in globalGlitches) {
            drawGlitchLine(canvas, glitch, w, h)
        }

        // 3. Screen flickering artifact
        if (isFlickering) {
            canvas.drawColor(Color.argb(60, 0, 0, 0))
        }
    }

    private fun drawRayLines(canvas: Canvas, points: List<androidx.compose.ui.geometry.Offset>, strokeWidth: Float) {
        if (points.size < 2) return

        val shadowPath = Path().apply {
            moveTo(points[0].x + 1.2f, points[0].y + 1.2f)
            for (i in 1 until points.size) {
                lineTo(points[i].x + 1.2f, points[i].y + 1.2f)
            }
        }

        val mainPath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        // 1. Shadow
        shadowPaint.strokeWidth = strokeWidth * 2.2f
        canvas.drawPath(shadowPath, shadowPaint)

        // 2. Ice Blue Reflection
        iceTintPaint.strokeWidth = strokeWidth * 1.4f
        canvas.drawPath(mainPath, iceTintPaint)

        // 3. White highlight
        specularWhitePaint.strokeWidth = strokeWidth * 0.75f
        canvas.drawPath(mainPath, specularWhitePaint)
    }

    private fun drawGlitchLine(canvas: Canvas, glitch: RgbGlitchLine, w: Float, h: Float) {
        glitchPaint.color = Color.argb(
            (glitch.alpha * 255).toInt(),
            (glitch.color.red * 255).toInt(),
            (glitch.color.green * 255).toInt(),
            (glitch.color.blue * 255).toInt()
        )
        glitchPaint.strokeWidth = glitch.strokeWidth

        if (glitch.isVertical) {
            val startY = h * glitch.offsetRatio
            val endY = startY + h * glitch.lengthRatio
            canvas.drawLine(glitch.position, startY, glitch.position, endY, glitchPaint)
        } else {
            val startX = w * glitch.offsetRatio
            val endX = startX + w * glitch.lengthRatio
            canvas.drawLine(startX, glitch.position, endX, glitch.position, glitchPaint)
        }
    }
}
