package com.example.wsamat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wsamat.LockScreenSosOverlayActivity
import com.example.wsamat.R
import com.example.wsamat.data.UserPreferences

class LockScreenSosService : Service() {

    private lateinit var userPrefs: UserPreferences

    override fun onCreate() {
        super.onCreate()
        userPrefs = UserPreferences(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!userPrefs.isLockScreenSosEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

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

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val overlayIntent = Intent(this, LockScreenSosOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val overlayPendingIntent = PendingIntent.getActivity(
            this,
            102,
            overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val buttonText = userPrefs.lockScreenButtonText.ifEmpty { "🚨 SPECIAL LOCK SCREEN SOS" }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WSAMat Lock Screen Special SOS Active")
            .setContentText("Tap to open special Emergency SOS button on lock screen.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(overlayPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_sos, buttonText, overlayPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lock Screen SOS Button",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Maintains instant special SOS button on device lock screen"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "wsamat_lockscreen_sos_channel"
        const val NOTIFICATION_ID = 1003

        fun startService(context: Context) {
            val userPrefs = UserPreferences(context)
            if (userPrefs.isLockScreenSosEnabled) {
                try {
                    val intent = Intent(context, LockScreenSosService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun stopService(context: Context) {
            try {
                context.stopService(Intent(context, LockScreenSosService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateService(context: Context) {
            stopService(context)
            startService(context)
        }
    }
}
