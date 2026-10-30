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
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.kirtiam.wsamat.LockScreenSosOverlayActivity
import com.kirtiam.wsamat.R
import com.kirtiam.wsamat.config.AppConfig
import com.kirtiam.wsamat.data.UserPreferences

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
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val overlayPendingIntent = PendingIntent.getActivity(
            this,
            102,
            overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val buttonText = userPrefs.lockScreenButtonText.ifEmpty { "🚨 SPECIAL LOCK SCREEN SOS" }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WSAMat Lock Screen Special SOS Active")
            .setContentText("Tap to open special Emergency SOS button on lock screen.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(overlayPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_sos, buttonText, overlayPendingIntent)

        // Best-effort Android Notification Bubble Metadata
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val bubbleData = NotificationCompat.BubbleMetadata.Builder(
                    overlayPendingIntent,
                    IconCompat.createWithResource(this, R.mipmap.ic_launcher)
                )
                    .setDesiredHeight(600)
                    .setAutoExpandBubble(false)
                    .setSuppressNotification(false)
                    .build()
                builder.setBubbleMetadata(bubbleData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lock Screen SOS Protection",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains quiet instant special SOS overlay and bubble on lock screen"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = AppConfig.LOCK_SCREEN_SOS_CHANNEL_ID
        const val NOTIFICATION_ID = AppConfig.LOCK_SCREEN_SOS_NOTIFICATION_ID

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
