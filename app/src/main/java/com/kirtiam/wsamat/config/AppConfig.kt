package com.kirtiam.wsamat.config

import android.Manifest
import android.os.Build

object AppConfig {
    const val APP_NAME = "WSAMat Personal Safety Hub"

    // SOS Timers & Durations
    const val SOS_COUNTDOWN_SECONDS = 7
    const val SOS_COUNTDOWN_MILLIS = 7000L
    const val AMBIENT_AUDIO_RECORD_SECONDS = 20

    // Emergency Hotlines
    const val HOTLINE_NATIONAL_EMERGENCY = "112"
    const val HOTLINE_WOMEN_HELPLINE = "1091"
    const val HOTLINE_POLICE = "100"
    const val HOTLINE_AMBULANCE = "102"

    // Notification Channels & IDs
    const val LOCK_SCREEN_SOS_CHANNEL_ID = "wsamat_lockscreen_sos_channel"
    const val LOCK_SCREEN_SOS_NOTIFICATION_ID = 1003
    const val SAFETY_CHECKIN_CHANNEL_ID = "wsamat_safety_checkin_channel"
    const val SAFETY_CHECKIN_NOTIFICATION_ID = 1002

    // Notification Spam Prevention Constants
    const val NOTIFICATION_ALERT_THROTTLE_INTERVAL_MS = 7200000L // 2 hours

    // Intent Actions & Extras
    const val ACTION_TRIGGER_LOCK_SOS = "com.kirtiam.wsamat.ACTION_TRIGGER_LOCK_SOS"
    const val EXTRA_TRIGGER_SOURCE = "TRIGGER_SOURCE"
    const val EXTRA_CALLER_NAME = "CALLER_NAME"
    const val EXTRA_CALLER_NUMBER = "CALLER_NUMBER"

    // Essential Application Permissions (Including CALL_PHONE)
    val ESSENTIAL_PERMISSIONS = mutableListOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()
}
