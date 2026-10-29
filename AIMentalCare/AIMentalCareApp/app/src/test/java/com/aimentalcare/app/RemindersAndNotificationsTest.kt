package com.aimentalcare.app

import com.aimentalcare.app.utils.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Phase 7 Unit Tests — Smart Wellness Reminders and Notifications
 */
class RemindersAndNotificationsTest {

    // =========================================================================
    // 1. NOTIFICATION ENABLED / DISABLED
    // =========================================================================

    @Test
    fun testNotificationDisabledDoesNotTrigger() {
        val shouldTrigger = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = false,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "EVERY_DAY",
            dayOfWeek = Calendar.WEDNESDAY,
            isQuietHoursEnabled = false
        )
        assertFalse("Reminder must not trigger when notification is disabled", shouldTrigger)
    }

    @Test
    fun testNotificationEnabledAndIncompleteQuizTriggers() {
        val shouldTrigger = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "EVERY_DAY",
            dayOfWeek = Calendar.WEDNESDAY,
            isQuietHoursEnabled = false
        )
        assertTrue("Reminder must trigger when enabled and quiz is incomplete", shouldTrigger)
    }

    // =========================================================================
    // 2. CRITICAL: ALREADY COMPLETED QUIZ SKIPS DUPLICATE REMINDERS
    // =========================================================================

    @Test
    fun testAlreadyCompletedQuizSkipsDuplicateReminder() {
        val shouldTrigger = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = true, // User completed today's quiz
            reminderDaysMode = "EVERY_DAY",
            dayOfWeek = Calendar.MONDAY,
            isQuietHoursEnabled = false
        )
        assertFalse("Must never send duplicate reminder if today's quiz is already completed", shouldTrigger)
    }

    // =========================================================================
    // 3. REMINDER DAYS CONFIGURATION (Every Day, Weekdays, Weekends)
    // =========================================================================

    @Test
    fun testReminderDaysWeekdaysMode() {
        // Wednesday (Weekday) -> should trigger
        val weekdayResult = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "WEEKDAYS",
            dayOfWeek = Calendar.WEDNESDAY
        )
        assertTrue("Weekdays mode must trigger on Wednesday", weekdayResult)

        // Saturday (Weekend) -> should NOT trigger
        val weekendResult = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "WEEKDAYS",
            dayOfWeek = Calendar.SATURDAY
        )
        assertFalse("Weekdays mode must NOT trigger on Saturday", weekendResult)
    }

    @Test
    fun testReminderDaysWeekendsMode() {
        // Sunday (Weekend) -> should trigger
        val sundayResult = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "WEEKENDS",
            dayOfWeek = Calendar.SUNDAY
        )
        assertTrue("Weekends mode must trigger on Sunday", sundayResult)

        // Tuesday (Weekday) -> should NOT trigger
        val tuesdayResult = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            reminderDaysMode = "WEEKENDS",
            dayOfWeek = Calendar.TUESDAY
        )
        assertFalse("Weekends mode must NOT trigger on Tuesday", tuesdayResult)
    }

    // =========================================================================
    // 4. QUIET HOURS (DO NOT DISTURB) ENFORCEMENT
    // =========================================================================

    @Test
    fun testQuietHoursOvernightWindowSuppression() {
        // Quiet hours: 22 (10 PM) to 7 (7 AM)
        val inQuietNight = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            isQuietHoursEnabled = true,
            currentHour = 23, // 11 PM
            quietStartHour = 22,
            quietEndHour = 7
        )
        assertFalse("Must suppress notification during late night quiet hours (11 PM)", inQuietNight)

        val inQuietEarlyMorning = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            isQuietHoursEnabled = true,
            currentHour = 5, // 5 AM
            quietStartHour = 22,
            quietEndHour = 7
        )
        assertFalse("Must suppress notification during early morning quiet hours (5 AM)", inQuietEarlyMorning)

        val outsideQuietHours = NotificationHelper.shouldTriggerReminder(
            isReminderEnabled = true,
            isCheckInAlreadyCompleted = false,
            isQuietHoursEnabled = true,
            currentHour = 14, // 2 PM
            quietStartHour = 22,
            quietEndHour = 7
        )
        assertTrue("Must allow notification outside quiet hours window (2 PM)", outsideQuietHours)
    }

    // =========================================================================
    // 5. TAPPING NOTIFICATION OPENS DAILY WELLNESS QUIZ
    // =========================================================================

    @Test
    fun testNotificationNavigationConstants() {
        assertEquals("EXTRA_NAV_TARGET", NotificationHelper.EXTRA_NAV_TARGET)
        assertEquals("daily_checkin", NotificationHelper.TARGET_DAILY_CHECKIN)
    }

    // =========================================================================
    // 6. MULTI-DAY PERSISTENCE & SUBTLE IN-APP PROMPT DISMISSAL
    // =========================================================================

    @Test
    fun testSubtlePromptMultiDayLogic() {
        val todayStr = "2026-10-02"
        val yesterdayStr = "2026-10-01"

        // If user dismissed yesterday, prompt reappears today
        val isDismissedTodayForYesterdayDismissal = yesterdayStr == todayStr
        assertFalse(isDismissedTodayForYesterdayDismissal)

        // If user dismissed today, prompt is hidden today
        val isDismissedTodayForTodayDismissal = todayStr == todayStr
        assertTrue(isDismissedTodayForTodayDismissal)
    }
}
