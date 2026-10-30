package com.kirtiam.wsamat.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_RUN, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_FIRST_RUN, value).apply()

    var isRegistered: Boolean
        get() = prefs.getBoolean(KEY_IS_REGISTERED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_REGISTERED, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userPasswordHash: String
        get() = prefs.getString(KEY_USER_PASSWORD_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_PASSWORD_HASH, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var appIconMode: String
        get() = prefs.getString(KEY_APP_ICON_MODE, "DEFAULT") ?: "DEFAULT"
        set(value) = prefs.edit().putString(KEY_APP_ICON_MODE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "User") ?: "User"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String
        get() = prefs.getString(KEY_USER_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var emergencyNote: String
        get() = prefs.getString(
            KEY_EMERGENCY_NOTE,
            "EMERGENCY! I need urgent help. Please check my current live location:"
        ) ?: "EMERGENCY! I need urgent help. Please check my current live location:"
        set(value) = prefs.edit().putString(KEY_EMERGENCY_NOTE, value).apply()

    var isShakeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHAKE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SHAKE_ENABLED, value).apply()

    var fakeCallerName: String
        get() = prefs.getString(KEY_FAKE_CALLER_NAME, "Mom") ?: "Mom"
        set(value) = prefs.edit().putString(KEY_FAKE_CALLER_NAME, value).apply()

    var fakeCallerNumber: String
        get() = prefs.getString(KEY_FAKE_CALLER_NUMBER, "+1 555-0199") ?: "+1 555-0199"
        set(value) = prefs.edit().putString(KEY_FAKE_CALLER_NUMBER, value).apply()

    var isSafetyCheckInActive: Boolean
        get() = prefs.getBoolean(KEY_CHECKIN_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_CHECKIN_ACTIVE, value).apply()

    var checkInTargetTimeMillis: Long
        get() = prefs.getLong(KEY_CHECKIN_TARGET_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_CHECKIN_TARGET_TIME, value).apply()

    var isLockScreenSosEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_SCREEN_SOS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_SCREEN_SOS_ENABLED, value).apply()

    var lockScreenSosMode: String
        get() = prefs.getString(KEY_LOCK_SCREEN_SOS_MODE, "INSTANT") ?: "INSTANT"
        set(value) = prefs.edit().putString(KEY_LOCK_SCREEN_SOS_MODE, value).apply()

    var lockScreenButtonText: String
        get() = prefs.getString(KEY_LOCK_SCREEN_BUTTON_TEXT, "🚨 DIRECT SOS MESSAGE") ?: "🚨 DIRECT SOS MESSAGE"
        set(value) = prefs.edit().putString(KEY_LOCK_SCREEN_BUTTON_TEXT, value).apply()

    var lastBackgroundNotificationAlertTimeMillis: Long
        get() = prefs.getLong(KEY_LAST_BG_NOTIF_ALERT_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_BG_NOTIF_ALERT_TIME, value).apply()

    companion object {
        private const val PREF_NAME = "wsamat_user_prefs"
        private const val KEY_IS_FIRST_RUN = "is_first_run"
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD_HASH = "user_password_hash"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_ICON_MODE = "app_icon_mode"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_EMERGENCY_NOTE = "emergency_note"
        private const val KEY_SHAKE_ENABLED = "shake_enabled"
        private const val KEY_FAKE_CALLER_NAME = "fake_caller_name"
        private const val KEY_FAKE_CALLER_NUMBER = "fake_caller_number"
        private const val KEY_CHECKIN_ACTIVE = "checkin_active"
        private const val KEY_CHECKIN_TARGET_TIME = "checkin_target_time"
        private const val KEY_LOCK_SCREEN_SOS_ENABLED = "lock_screen_sos_enabled"
        private const val KEY_LOCK_SCREEN_SOS_MODE = "lock_screen_sos_mode"
        private const val KEY_LOCK_SCREEN_BUTTON_TEXT = "lock_screen_button_text"
        private const val KEY_LAST_BG_NOTIF_ALERT_TIME = "last_bg_notif_alert_time"
    }
}
