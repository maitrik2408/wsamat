package com.example.wsamat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.wsamat.MainActivity
import com.example.wsamat.R
import com.example.wsamat.config.AppConfig
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.utils.ShakeDetector

class ShakeService : Service() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var shakeDetector: ShakeDetector? = null
    private lateinit var userPrefs: UserPreferences

    private var lastShakeTimestamp: Long = 0L
    private var lastPowerPressTimestamp: Long = 0L
    private val powerPressTimestamps = mutableListOf<Long>()
    private var lastSosTriggerTime: Long = 0L

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingShakeRunnable: Runnable? = null

    private val screenPowerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_SCREEN_OFF || action == Intent.ACTION_SCREEN_ON) {
                handlePowerButtonPress()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        userPrefs = UserPreferences(this)
        createNotificationChannel()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shakeDetector = ShakeDetector {
            handleShakeEvent()
        }

        accelerometer?.let {
            sensorManager.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_UI)
        }

        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(screenPowerReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleShakeEvent() {
        val now = System.currentTimeMillis()
        lastShakeTimestamp = now

        if (now - lastPowerPressTimestamp < 2500L) {
            cancelPendingShake()
            triggerSos("Shake + Power Button")
        } else {
            cancelPendingShake()
            val runnable = Runnable {
                triggerSos("Phone Shake Gesture")
            }
            pendingShakeRunnable = runnable
            mainHandler.postDelayed(runnable, 800L)
        }
    }

    private fun handlePowerButtonPress() {
        val now = System.currentTimeMillis()
        lastPowerPressTimestamp = now

        if (now - lastShakeTimestamp < 2500L || pendingShakeRunnable != null) {
            cancelPendingShake()
            triggerSos("Shake + Power Button")
            return
        }

        powerPressTimestamps.add(now)
        powerPressTimestamps.removeAll { now - it > 2000L }

        if (powerPressTimestamps.size >= 3) {
            powerPressTimestamps.clear()
            cancelPendingShake()
            triggerSos("Power Button Triple Press")
        }
    }

    private fun cancelPendingShake() {
        pendingShakeRunnable?.let {
            mainHandler.removeCallbacks(it)
            pendingShakeRunnable = null
        }
    }

    private fun triggerSos(source: String) {
        val now = System.currentTimeMillis()
        if (now - lastSosTriggerTime < 8000L) return
        lastSosTriggerTime = now

        val sosIntent = Intent().setClassName(this, "com.example.wsamat.SosCountdownActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("TRIGGER_SOURCE", source)
        }
        try {
            startActivity(sosIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        cancelPendingShake()
        try {
            unregisterReceiver(screenPowerReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        shakeDetector?.let { sensorManager.unregisterListener(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val shouldAlert = now - userPrefs.lastBackgroundNotificationAlertTimeMillis > AppConfig.NOTIFICATION_ALERT_THROTTLE_INTERVAL_MS
        if (shouldAlert) {
            userPrefs.lastBackgroundNotificationAlertTimeMillis = now
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WSAMat Protection Active")
            .setContentText("Shake + Power Button detection active for emergency SOS.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(!shouldAlert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Background Gesture Protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains quiet background gesture protection"
            setSound(null, null)
            enableVibration(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "wsamat_shake_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, ShakeService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                context.stopService(Intent(context, ShakeService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
