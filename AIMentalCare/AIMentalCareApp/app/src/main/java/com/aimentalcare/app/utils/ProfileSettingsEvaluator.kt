package com.aimentalcare.app.utils

import androidx.appcompat.app.AppCompatDelegate

object ProfileSettingsEvaluator {

    fun computeAvatarInitials(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "MC"
        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts.first().first().uppercase()}${parts.last().first().uppercase()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "MC"
        }
    }

    fun mapThemeModeToNightMode(themeMode: String?): Int {
        return when (themeMode?.uppercase()) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            "SYSTEM" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    fun getRequiredProfileSections(): List<String> {
        return listOf(
            "Wellness Goals",
            "Progress",
            "Daily Check-in",
            "Health & Wearables",
            "Emergency Contacts",
            "Settings",
            "Privacy",
            "About MentalCare AI"
        )
    }

    fun getRequiredSettingsSections(): List<String> {
        return listOf(
            "Appearance",
            "Notifications",
            "AI Assistant",
            "Health",
            "Emergency",
            "Account",
            "Privacy"
        )
    }

    fun isAuthPreservedOnSettingChange(sessionMap: Map<String, Any?>): Boolean {
        val email = sessionMap["user_email"] as? String
        return !email.isNullOrEmpty()
    }
}
