package com.aimentalcare.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aimentalcare.app.data.MentalCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CheckInReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)

        // If boot completed, restore the scheduled daily alarm
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val isEnabled = prefs.getBoolean("pref_checkin_reminders", true)
            if (isEnabled) {
                val hour = prefs.getInt("reminder_time_hour", 20)
                val minute = prefs.getInt("reminder_time_minute", 30)
                NotificationHelper.scheduleDailyCheckInReminder(context, hour, minute)
            }
            return
        }

        val isEnabled = prefs.getBoolean("pref_checkin_reminders", true)
        val reminderDaysMode = prefs.getString("pref_reminder_days", "EVERY_DAY") ?: "EVERY_DAY"
        val quietHoursEnabled = prefs.getBoolean("pref_quiet_hours_enabled", true)
        val quietStart = prefs.getInt("quiet_hours_start", 22) // 10 PM
        val quietEnd = prefs.getInt("quiet_hours_end", 7)      // 7 AM
        val vibrate = prefs.getBoolean("pref_reminder_vibrate", true)
        val sound = prefs.getBoolean("pref_reminder_sound", true)

        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordId = "${userEmail}_$todayStr"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MentalCareDatabase.getDatabase(context)
                val existingCheckIn = db.checkInDao().getTodayDailyCheckInSync(userEmail, todayStr, recordId)
                val isCheckInAlreadyCompleted = existingCheckIn != null

                val shouldTrigger = NotificationHelper.shouldTriggerReminder(
                    isReminderEnabled = isEnabled,
                    isCheckInAlreadyCompleted = isCheckInAlreadyCompleted,
                    reminderDaysMode = reminderDaysMode,
                    dayOfWeek = dayOfWeek,
                    isQuietHoursEnabled = quietHoursEnabled,
                    currentHour = currentHour,
                    quietStartHour = quietStart,
                    quietEndHour = quietEnd
                )

                if (shouldTrigger) {
                    NotificationHelper.sendDailyCheckInNotification(
                        context = context,
                        title = "Daily Wellness Check-in",
                        message = "Your daily wellness check-in is ready 🌿",
                        vibrate = vibrate,
                        sound = sound
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                rescheduleNextDay(context, prefs)
            }
        }
    }

    private fun rescheduleNextDay(context: Context, prefs: android.content.SharedPreferences) {
        val hour = prefs.getInt("reminder_time_hour", 20)
        val minute = prefs.getInt("reminder_time_minute", 30)
        NotificationHelper.scheduleDailyCheckInReminder(context, hour, minute)
    }
}
