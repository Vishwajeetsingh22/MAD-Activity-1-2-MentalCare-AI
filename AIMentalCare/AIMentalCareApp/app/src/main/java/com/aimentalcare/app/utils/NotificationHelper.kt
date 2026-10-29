package com.aimentalcare.app.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aimentalcare.app.R
import com.aimentalcare.app.ui.MainActivity
import java.util.Calendar

object NotificationHelper {

    const val EXTRA_NAV_TARGET = "EXTRA_NAV_TARGET"
    const val TARGET_DAILY_CHECKIN = "daily_checkin"

    private const val CHANNEL_ALERTS_ID = "mental_care_alerts"
    private const val CHANNEL_ALERTS_NAME = "MentalCare Real-Time Alerts"

    private const val CHANNEL_REMINDERS_ID = "mental_care_reminders"
    private const val CHANNEL_REMINDERS_NAME = "Daily Wellness Reminders"

    private const val REMINDER_REQUEST_CODE = 2001

    fun sendAlertNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                CHANNEL_ALERTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for elevated stress & burnout warnings"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun sendDailyCheckInNotification(
        context: Context,
        title: String = "Daily Wellness Check-in",
        message: String = "Your daily wellness check-in is ready 🌿",
        vibrate: Boolean = true,
        sound: Boolean = true
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                CHANNEL_REMINDERS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily mindfulness and check-in prompts"
                enableVibration(vibrate)
                if (!sound) {
                    setSound(null, null)
                }
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_NAV_TARGET, TARGET_DAILY_CHECKIN)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 1001, intent, flag)

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\nTake a moment to understand how you're feeling today.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (vibrate) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        if (sound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        manager.notify(1002, builder.build())
    }

    /**
     * Pure business logic evaluation for whether a daily reminder should be triggered.
     * Prevents duplicate notifications when today's check-in is already completed,
     * respects active reminder days (Everyday, Weekdays, Weekends), and respects Quiet Hours.
     */
    fun shouldTriggerReminder(
        isReminderEnabled: Boolean,
        isCheckInAlreadyCompleted: Boolean,
        reminderDaysMode: String = "EVERY_DAY",
        dayOfWeek: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK),
        isQuietHoursEnabled: Boolean = false,
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        quietStartHour: Int = 22,
        quietEndHour: Int = 7
    ): Boolean {
        // 1. Reminders must be enabled
        if (!isReminderEnabled) return false

        // 2. CRITICAL: If quiz is already completed for today, do NOT send duplicate reminders
        if (isCheckInAlreadyCompleted) return false

        // 3. Active reminder days check
        val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        val dayMatches = when (reminderDaysMode.uppercase()) {
            "WEEKDAYS" -> isWeekday
            "WEEKENDS" -> isWeekend
            else -> true // "EVERY_DAY"
        }
        if (!dayMatches) return false

        // 4. Quiet hours check
        if (isQuietHoursEnabled) {
            val inQuietHours = if (quietStartHour > quietEndHour) {
                currentHour >= quietStartHour || currentHour < quietEndHour
            } else {
                currentHour in quietStartHour until quietEndHour
            }
            if (inQuietHours) return false
        }

        return true
    }

    fun scheduleDailyCheckInReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, CheckInReminderReceiver::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, intent, flag)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDailyCheckInReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, CheckInReminderReceiver::class.java)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, REMINDER_REQUEST_CODE, intent, flag)
        alarmManager.cancel(pendingIntent)
    }
}
