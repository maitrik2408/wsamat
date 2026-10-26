package com.example.wsamat.views

import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ThreeDSafetyShieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {

    private val camera = Camera()
    private val matrix3D = Matrix()

    private var rotationX = 15f
    private var rotationY = 25f
    private var autoRotateAngle = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var sensorManager: SensorManager? = null
    private var gyroSensor: Sensor? = null

    private val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val shieldPath = Path()
    private var isReducedMotion = false

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 10f
        borderPaint.color = Color.parseColor("#FFD54F")

        emblemPaint.style = Paint.Style.FILL
        emblemPaint.color = Color.WHITE

        checkReducedMotionPreference()
    }

    private fun checkReducedMotionPreference() {
        try {
            val durationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            isReducedMotion = durationScale == 0f
        } catch (e: Exception) {
            isReducedMotion = false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gyroSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDetachedFromWindow() {
        sensorManager?.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        val size = Math.min(w, h) * 0.4f

        if (size <= 0) return

        glowPaint.shader = RadialGradient(
            cx, cy, size * 1.3f,
            intArrayOf(Color.parseColor("#80C2185B"), Color.parseColor("#20D32F2F"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )

        shieldPaint.shader = LinearGradient(
            cx - size, cy - size, cx + size, cy + size,
            Color.parseColor("#E91E63"), Color.parseColor("#880E4F"),
            Shader.TileMode.CLAMP
        )

        shieldPath.reset()
        shieldPath.moveTo(cx, cy - size)
        shieldPath.cubicTo(cx + size, cy - size, cx + size, cy + size * 0.3f, cx, cy + size)
        shieldPath.cubicTo(cx - size, cy + size * 0.3f, cx - size, cy - size, cx, cy - size)
        shieldPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val size = Math.min(width, height) * 0.4f

        if (size <= 0) return

        if (!isReducedMotion) {
            autoRotateAngle = (autoRotateAngle + 1.2f) % 360f
        }

        canvas.save()

        camera.save()
        camera.rotateX(rotationX)
        camera.rotateY(rotationY + if (isReducedMotion) 0f else (Math.sin(Math.toRadians(autoRotateAngle.toDouble())) * 12.0).toFloat())
        camera.rotateZ(0f)
        camera.getMatrix(matrix3D)
        camera.restore()

        matrix3D.preTranslate(-cx, -cy)
        matrix3D.postTranslate(cx, cy)
        canvas.concat(matrix3D)

        canvas.drawCircle(cx, cy, size * 1.3f, glowPaint)
        canvas.drawPath(shieldPath, shieldPaint)
        canvas.drawPath(shieldPath, borderPaint)

        val crossWidth = size * 0.25f
        val crossLength = size * 0.6f

        canvas.drawRect(cx - crossWidth / 2, cy - crossLength / 2, cx + crossWidth / 2, cy + crossLength / 2, emblemPaint)
        canvas.drawRect(cx - crossLength / 2, cy - crossWidth / 2, cx + crossLength / 2, cy + crossWidth / 2, emblemPaint)

        canvas.restore()

        if (!isReducedMotion) {
            postInvalidateOnAnimation()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                rotationY += dx * 0.4f
                rotationX -= dy * 0.4f
                rotationX = rotationX.coerceIn(-45f, 45f)
                rotationY = rotationY.coerceIn(-60f, 60f)
                lastTouchX = event.x
                lastTouchY = event.y
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (isReducedMotion) return

        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            rotationY += event.values[1] * 2f
            rotationX -= event.values[0] * 2f
            rotationX = rotationX.coerceIn(-45f, 45f)
            rotationY = rotationY.coerceIn(-60f, 60f)
            invalidate()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            rotationY = event.values[0] * 3f
            rotationX = event.values[1] * 3f
            invalidate()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
