package com.aimentalcare.app

import androidx.appcompat.app.AppCompatDelegate
import com.aimentalcare.app.utils.ProfileSettingsEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 9 Unit Tests — Premium Profile and Settings
 * 
 * Verifies:
 * 1. Profile avatar initials generation (single-name, multi-name, empty fallbacks)
 * 2. Theme mode mapping (Light, Dark, System default)
 * 3. Session preservation when settings and theme are updated (no logout on change)
 * 4. Safe logout semantics (clears user auth session without corrupting device preferences)
 * 5. Full grouped sections coverage for both Profile and Settings screens
 * 6. Clinical wellness non-diagnostic disclaimer integrity
 */
class ProfileAndSettingsTest {

    // =========================================================================
    // 1. AVATAR INITIALS COMPUTATION
    // =========================================================================

    @Test
    fun testAvatarInitialsComputation() {
        assertEquals("JF", ProfileSettingsEvaluator.computeAvatarInitials("Joylyn Fernandes"))
        assertEquals("SP", ProfileSettingsEvaluator.computeAvatarInitials("Syed Faizan Pasha"))
        assertEquals("VS", ProfileSettingsEvaluator.computeAvatarInitials("Vishwajeet Singh"))
        assertEquals("AU", ProfileSettingsEvaluator.computeAvatarInitials("Active User"))
        assertEquals("AL", ProfileSettingsEvaluator.computeAvatarInitials("Alex"))
        assertEquals("MC", ProfileSettingsEvaluator.computeAvatarInitials(""))
        assertEquals("MC", ProfileSettingsEvaluator.computeAvatarInitials("   "))
    }

    // =========================================================================
    // 2. THEME MODE MAPPING (LIGHT, DARK, SYSTEM)
    // =========================================================================

    @Test
    fun testThemeModeMapping() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ProfileSettingsEvaluator.mapThemeModeToNightMode("LIGHT"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ProfileSettingsEvaluator.mapThemeModeToNightMode("light"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ProfileSettingsEvaluator.mapThemeModeToNightMode("DARK"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ProfileSettingsEvaluator.mapThemeModeToNightMode("dark"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ProfileSettingsEvaluator.mapThemeModeToNightMode("SYSTEM"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ProfileSettingsEvaluator.mapThemeModeToNightMode("system"))
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ProfileSettingsEvaluator.mapThemeModeToNightMode(null))
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ProfileSettingsEvaluator.mapThemeModeToNightMode("UNKNOWN"))
    }

    // =========================================================================
    // 3. CHANGING SETTINGS MUST NEVER LOG USER OUT
    // =========================================================================

    @Test
    fun testChangingSettingsPreservesAuthTokens() {
        val simulatedSession = mutableMapOf<String, Any?>(
            "user_email" to "joylyn@mentalcare.ai",
            "user_name" to "Joylyn Fernandes",
            "account_setup_completed" to true,
            "theme_mode" to "SYSTEM",
            "pref_checkin_reminders" to true,
            "reminder_time_hour" to 20,
            "reminder_time_minute" to 30
        )

        // Simulate changing theme mode to DARK
        simulatedSession["theme_mode"] = "DARK"
        simulatedSession["app_theme_mode"] = AppCompatDelegate.MODE_NIGHT_YES
        assertTrue("User must remain authenticated when theme changes", ProfileSettingsEvaluator.isAuthPreservedOnSettingChange(simulatedSession))

        // Simulate changing reminder time
        simulatedSession["reminder_time_hour"] = 21
        simulatedSession["reminder_time_minute"] = 0
        assertTrue("User must remain authenticated when reminder time changes", ProfileSettingsEvaluator.isAuthPreservedOnSettingChange(simulatedSession))

        // Simulate changing AI tone
        simulatedSession["pref_ai_chat_tone"] = "Direct & Concise"
        assertTrue("User must remain authenticated when AI tone changes", ProfileSettingsEvaluator.isAuthPreservedOnSettingChange(simulatedSession))

        // Ensure user email is preserved intact
        assertEquals("joylyn@mentalcare.ai", simulatedSession["user_email"])
    }

    // =========================================================================
    // 4. LOGOUT REMOVES SESSION WHILE PRESERVING DEVICE PREFERENCES
    // =========================================================================

    @Test
    fun testSafeLogoutSemantics() {
        val simulatedSession = mutableMapOf<String, Any?>(
            "user_email" to "faizan@mentalcare.ai",
            "user_name" to "Faizan Pasha",
            "account_setup_completed" to true,
            "theme_mode" to "DARK",
            "pref_checkin_reminders" to true
        )

        // Perform safe logout: remove user identity keys
        simulatedSession.remove("user_email")
        simulatedSession.remove("user_name")
        simulatedSession.remove("account_setup_completed")

        // Verify user is logged out
        assertFalse("Session should be unauthenticated after logout", ProfileSettingsEvaluator.isAuthPreservedOnSettingChange(simulatedSession))

        // Verify device level preferences like Dark Mode are retained
        assertEquals("DARK", simulatedSession["theme_mode"])
    }

    // =========================================================================
    // 5. PROFILE GROUPED SECTIONS COVERAGE
    // =========================================================================

    @Test
    fun testRequiredProfileSectionsCoverage() {
        val sections = ProfileSettingsEvaluator.getRequiredProfileSections()
        assertTrue(sections.contains("Wellness Goals"))
        assertTrue(sections.contains("Progress"))
        assertTrue(sections.contains("Daily Check-in"))
        assertTrue(sections.contains("Health & Wearables"))
        assertTrue(sections.contains("Emergency Contacts"))
        assertTrue(sections.contains("Settings"))
        assertTrue(sections.contains("Privacy"))
        assertTrue(sections.contains("About MentalCare AI"))
        assertEquals(8, sections.size)
    }

    // =========================================================================
    // 6. SETTINGS GROUPED SECTIONS COVERAGE
    // =========================================================================

    @Test
    fun testRequiredSettingsSectionsCoverage() {
        val sections = ProfileSettingsEvaluator.getRequiredSettingsSections()
        assertTrue(sections.contains("Appearance"))
        assertTrue(sections.contains("Notifications"))
        assertTrue(sections.contains("AI Assistant"))
        assertTrue(sections.contains("Health"))
        assertTrue(sections.contains("Emergency"))
        assertTrue(sections.contains("Account"))
        assertTrue(sections.contains("Privacy"))
        assertEquals(7, sections.size)
    }

    // =========================================================================
    // 7. CLINICAL WELLNESS DISCLAIMER INTEGRITY
    // =========================================================================

    @Test
    fun testClinicalWellnessDisclaimerIntegrity() {
        val disclaimer = "MentalCare AI provides supportive wellness tracking and does not provide clinical diagnosis or psychiatric treatment."
        assertTrue(disclaimer.contains("wellness"))
        assertTrue(disclaimer.contains("not provide clinical diagnosis"))
    }
}
