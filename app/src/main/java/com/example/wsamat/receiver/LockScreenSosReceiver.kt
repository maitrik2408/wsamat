package com.example.wsamat.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.wsamat.R
import com.example.wsamat.SosCountdownActivity
import com.example.wsamat.data.DatabaseHelper
import com.example.wsamat.data.Incident
import com.example.wsamat.data.UserPreferences
import com.example.wsamat.utils.AudioRecorderHelper
import com.example.wsamat.utils.CameraHelper
import com.example.wsamat.utils.LocationHelper
import com.example.wsamat.utils.SmsHelper

class LockScreenSosReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 10000L) {
            return
        }
        lastTriggerTime = now

        val userPrefs = UserPreferences(context)
        val mode = userPrefs.lockScreenSosMode

        if (mode == "COUNTDOWN") {
            val sosIntent = Intent(context, SosCountdownActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("TRIGGER_SOURCE", "Lock Screen Button")
            }
            context.startActivity(sosIntent)
        } else {
            Toast.makeText(context, "Lock Screen Instant SOS Triggered!", Toast.LENGTH_SHORT).show()

            val dbHelper = DatabaseHelper(context)
            val locationHelper = LocationHelper(context)
            val smsHelper = SmsHelper(context)
            val cameraHelper = CameraHelper(context)
            val audioRecorderHelper = AudioRecorderHelper(context)

            val contacts = dbHelper.getAllContacts()

            val audioPath = audioRecorderHelper.startRecording(20)

            cameraHelper.captureSilentPhoto { photoPath ->
                locationHelper.fetchCurrentLocation { loc ->
                    val locationText = loc?.formattedAddress ?: "Location unavailable"
                    val mapUrl = loc?.mapUrl ?: ""
                    val customNote = userPrefs.emergencyNote

                    val isSent = smsHelper.sendEmergencySms(contacts, customNote, mapUrl)
                    val statusStr = if (isSent) "Direct SMS Sent to ${contacts.size} Contacts" else "SMS Dispatched"

                    dbHelper.addIncident(
                        Incident(
                            timestamp = System.currentTimeMillis(),
                            eventType = "Lock Screen Instant SOS",
                            locationText = locationText,
                            mapUrl = mapUrl,
                            status = statusStr,
                            photoPath = photoPath,
                            audioPath = audioPath
                        )
                    )

                    showResultNotification(context, statusStr, mapUrl, photoPath != null, audioPath != null)
                }
            }
        }
    }

    private fun showResultNotification(
        context: Context,
        status: String,
        mapUrl: String,
        hasPhoto: Boolean,
        hasAudio: Boolean
    ) {
        val channelId = "wsamat_lock_sos_result"
        val manager = context.getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Lock Screen SOS Result",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows confirmation when Lock Screen SOS is triggered"
        }
        manager?.createNotificationChannel(channel)

        val mediaText = buildString {
            if (hasPhoto) append(" | 📷 Photo")
            if (hasAudio) append(" | 🎙️ Audio")
        }

        val text = if (mapUrl.isNotEmpty()) "$status$mediaText\nMap: $mapUrl" else "$status$mediaText"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 Emergency SOS Dispatched")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        manager?.notify(2001, notification)
    }

    companion object {
        const val ACTION_TRIGGER_LOCK_SOS = "com.example.wsamat.ACTION_TRIGGER_LOCK_SOS"
        private var lastTriggerTime = 0L
    }
}
