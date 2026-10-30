package com.kirtiam.wsamat.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import com.kirtiam.wsamat.data.UserPreferences

object ThemeManager {

    fun applyTheme(userPrefs: UserPreferences) {
        val targetMode = when (userPrefs.themeMode) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }
}
