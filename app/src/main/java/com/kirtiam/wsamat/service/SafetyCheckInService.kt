package com.kirtiam.wsamat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kirtiam.wsamat.MainActivity
import com.kirtiam.wsamat.R
import com.kirtiam.wsamat.config.AppConfig
import com.kirtiam.wsamat.data.UserPreferences

class SafetyCheckInService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private lateinit var userPrefs: UserPreferences

    override fun onCreate() {
        super.onCreate()
        userPrefs = UserPreferences(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val targetTime = userPrefs.checkInTargetTimeMillis
        val remaining = targetTime - System.currentTimeMillis()

        if (remaining <= 0) {
            triggerCheckInTimeout()
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(remaining),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification(remaining))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startTimer(remaining)

        return START_STICKY
    }

    private fun startTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                userPrefs.isSafetyCheckInActive = false
                triggerCheckInTimeout()
                stopSelf()
            }
        }.start()
    }

    private fun triggerCheckInTimeout() {
        val sosIntent = Intent().setClassName(this, "com.kirtiam.wsamat.SosCountdownActivity").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("TRIGGER_SOURCE", "Missed Safety Check-in")
        }
        startActivity(sosIntent)
    }

    private fun buildNotification(remainingMillis: Long): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = (remainingMillis / 1000) / 60
        val seconds = (remainingMillis / 1000) % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Safety Check-in Active")
            .setContentText("Check-in required in $timeStr. Tap if safe.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(remainingMillis: Long) {
        val notification = buildNotification(remainingMillis)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Safety Check-in Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors safety check-in timer without repeated alert pop-ups."
            setSound(null, null)
            enableVibration(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = AppConfig.SAFETY_CHECKIN_CHANNEL_ID
        const val NOTIFICATION_ID = AppConfig.SAFETY_CHECKIN_NOTIFICATION_ID

        fun startService(context: Context) {
            val intent = Intent(context, SafetyCheckInService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val userPrefs = UserPreferences(context)
            userPrefs.isSafetyCheckInActive = false
            context.stopService(Intent(context, SafetyCheckInService::class.java))
        }
    }
}
