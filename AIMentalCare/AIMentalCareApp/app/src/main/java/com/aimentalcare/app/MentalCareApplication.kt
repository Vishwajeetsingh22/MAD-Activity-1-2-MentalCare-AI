package com.aimentalcare.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MentalCareApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
        val themeString = prefs.getString("theme_mode", "SYSTEM")
        val themeMode = com.aimentalcare.app.utils.ProfileSettingsEvaluator.mapThemeModeToNightMode(themeString)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
}
