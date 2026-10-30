package com.kirtiam.wsamat.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.kirtiam.wsamat.data.UserPreferences

object AppIconManager {

    enum class IconMode(val key: String, val aliasClassName: String, val labelName: String) {
        DEFAULT("DEFAULT", "com.kirtiam.wsamat.MainActivityDefault", "WSAMat Safety"),
        CALCULATOR("CALCULATOR", "com.kirtiam.wsamat.MainActivityCalculator", "Calculator"),
        COMPASS("COMPASS", "com.kirtiam.wsamat.MainActivityCompass", "Compass"),
        CLOCK("CLOCK", "com.kirtiam.wsamat.MainActivityClock", "Clock")
    }

    fun applyIconMode(context: Context, targetMode: IconMode) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        val userPrefs = UserPreferences(context)
        userPrefs.appIconMode = targetMode.key

        // Enable targetMode FIRST before disabling other aliases
        try {
            val targetComponent = ComponentName(packageName, targetMode.aliasClassName)
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Throwable) {
            Log.e("AppIconManager", "Failed to enable target component ${targetMode.aliasClassName}", e)
        }

        // Disable other aliases AFTER enabling targetMode
        for (mode in IconMode.values()) {
            if (mode == targetMode) continue
            val componentName = ComponentName(packageName, mode.aliasClassName)
            try {
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Throwable) {
                Log.e("AppIconManager", "Failed to disable component ${mode.aliasClassName}", e)
            }
        }
    }

    fun getSavedIconMode(context: Context): IconMode {
        val userPrefs = UserPreferences(context)
        val key = userPrefs.appIconMode
        return IconMode.values().firstOrNull { it.key == key } ?: IconMode.DEFAULT
    }
}
