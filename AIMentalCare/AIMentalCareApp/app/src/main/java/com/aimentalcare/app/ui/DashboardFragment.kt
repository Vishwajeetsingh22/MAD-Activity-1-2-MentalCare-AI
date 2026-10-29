package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.data.LifestyleEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentDashboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    companion object {
        var sessionPromptDismissed = false
    }

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var userEmail: String = "user@mentalcare.ai"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", null)
            ?: prefs.getString("registered_full_name", null)
            ?: "Friend"
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        // Dynamic time-specific greeting with actual registered name
        updateDynamicGreeting(userName)

        setupMoodButtons(db)
        setupNavigationButtons()
        observeDatabaseData(db)
    }

    private fun updateDynamicGreeting(userName: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        // 5:00 AM – 11:59 AM: Good morning, {name} 👋 ("Start your day with a quick wellness check-in.")
        // 12:00 PM – 4:59 PM: Good afternoon, {name} 👋 ("Take a moment to check in with yourself.")
        // 5:00 PM – 8:59 PM: Good evening, {name} 👋 ("Reflect on your day and take some time to unwind.")
        // 9:00 PM – 4:59 AM: Good night, {name} 🌙 ("Wind down and give yourself some time to rest.")
        val (greeting, subtitle) = when {
            totalMinutes in 300..719 -> {
                "Good morning, $userName 👋" to "Start your day with a quick wellness check-in."
            }
            totalMinutes in 720..1019 -> {
                "Good afternoon, $userName 👋" to "Take a moment to check in with yourself."
            }
            totalMinutes in 1020..1259 -> {
                "Good evening, $userName 👋" to "Reflect on your day and take some time to unwind."
            }
            else -> {
                "Good night, $userName 🌙" to "Wind down and give yourself some time to rest."
            }
        }

        binding.txtGreeting.text = greeting
        binding.txtGreetingSubtitle.text = subtitle
    }

    private fun setupMoodButtons(db: MentalCareDatabase) {
        val moods = mapOf(
            binding.btnMoodGreat to ("😊" to "Great"),
            binding.btnMoodGood to ("🙂" to "Good"),
            binding.btnMoodOkay to ("😐" to "Okay"),
            binding.btnMoodStressed to ("😟" to "Stressed"),
            binding.btnMoodLow to ("😔" to "Low")
        )

        moods.forEach { (button, pair) ->
            button.setOnClickListener {
                val (emoji, label) = pair
                lifecycleScope.launch(Dispatchers.IO) {
                    db.checkInDao().insertLifestyle(
                        LifestyleEntity(
                            sleepHrs = 7.0f,
                            workHrs = 7.0f,
                            screenHrs = 3.5f,
                            exerciseMins = 15,
                            energyLevel = label,
                            moodEmoji = "$emoji $label",
                            stressScore = if (label == "Stressed" || label == "Low") 70 else 35
                        )
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Mood recorded: $emoji $label", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupNavigationButtons() {
        // Daily Check-in Card Action
        binding.btnStartCheckIn.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(DailyCheckInFragment())
        }

        // Insights & Trends
        binding.btnViewInsights.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_analytics)
        }

        // In-App Check-in Prompt
        binding.btnDismissPrompt.setOnClickListener {
            sessionPromptDismissed = true
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("dismissed_prompt_date", todayStr).apply()
            binding.cardInAppCheckInPrompt.visibility = View.GONE
        }
        binding.btnPromptNotNow.setOnClickListener {
            sessionPromptDismissed = true
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("dismissed_prompt_date", todayStr).apply()
            binding.cardInAppCheckInPrompt.visibility = View.GONE
        }
        binding.btnPromptStartCheckIn.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(DailyCheckInFragment())
        }

        // Reminder Status Settings
        binding.cardReminderStatus.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SettingsFragment())
        }
        binding.btnAdjustReminder.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SettingsFragment())
        }
    }

    private fun observeDatabaseData(db: MentalCareDatabase) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyRecordId = "${userEmail}_$todayStr"
        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        val isDismissedToday = prefs.getString("dismissed_prompt_date", null) == todayStr

        // 1. Observe Today's Daily Check-in Status
        db.checkInDao().getTodayDailyCheckIn(userEmail, todayStr, dailyRecordId).observe(viewLifecycleOwner) { todayCheckIn ->
            val b = _binding ?: return@observe
            if (todayCheckIn != null) {
                b.txtCheckInIcon.text = "✓"
                b.txtCheckInTitle.text = "✓ Today's check-in completed"
                b.txtCheckInSubtitle.text = "Come back tomorrow for your next wellness check-in."
                b.btnStartCheckIn.text = "View Results"
                b.cardInAppCheckInPrompt.visibility = View.GONE
            } else {
                b.txtCheckInIcon.text = "🧠"
                b.txtCheckInTitle.text = "How are you feeling today?"
                b.txtCheckInSubtitle.text = "Take a moment to understand how you're feeling."
                b.btnStartCheckIn.text = "Start Check-in"
                b.cardInAppCheckInPrompt.visibility = if (sessionPromptDismissed || isDismissedToday) View.GONE else View.VISIBLE
            }
        }

        // 2. Observe Latest Check-in for Summary & Trend
        db.checkInDao().getLatestCheckIn().observe(viewLifecycleOwner) { checkIn ->
            val b = _binding ?: return@observe
            if (checkIn != null) {
                b.txtSummaryStress.text = checkIn.stressLevel
                b.txtSummaryStressScore.text = "${checkIn.stressScore}%"
                val colorHex = when (checkIn.stressLevel) {
                    "LOW" -> "#10B981"
                    "MODERATE" -> "#F59E0B"
                    else -> "#EF4444"
                }
                b.txtSummaryStress.setTextColor(colorHex.toColorInt())

                b.txtDashboardStressScore.text = "Stress: ${checkIn.stressLevel} (${checkIn.stressScore}%)"
                b.txtDashboardStressBadge.text = checkIn.stressLevel
                b.txtDashboardStressBadge.setTextColor(colorHex.toColorInt())

                val trend = if (checkIn.stressScore < 45) "Improving" else if (checkIn.stressScore < 70) "Stable" else "Increasing"
                b.txtDashboardTrend.text = "Trend: $trend"
            } else {
                b.txtSummaryStress.text = "--"
                b.txtSummaryStressScore.text = "No data"
                b.txtDashboardStressScore.text = "Stress: Not logged yet"
                b.txtDashboardStressBadge.text = "NORMAL"
                b.txtDashboardTrend.text = "Trend: Baseline"
            }
        }

        // 3. Observe All Check-Ins for Trend Empty State
        db.checkInDao().getAllCheckIns().observe(viewLifecycleOwner) { allCheckIns ->
            val b = _binding ?: return@observe
            if (allCheckIns.isNullOrEmpty()) {
                b.layoutTrendEmptyState.visibility = View.VISIBLE
                b.layoutTrendContent.visibility = View.GONE
            } else {
                b.layoutTrendEmptyState.visibility = View.GONE
                b.layoutTrendContent.visibility = View.VISIBLE
            }
        }

        // 4. Observe Latest Lifestyle for Sleep in Wellness Summary
        db.checkInDao().getLatestLifestyle().observe(viewLifecycleOwner) { lifestyle ->
            val b = _binding ?: return@observe
            if (lifestyle != null) {
                b.txtSummarySleep.text = "${lifestyle.sleepHrs.toInt()}h"
                b.txtSummarySleepLabel.text = "${lifestyle.energyLevel ?: "Rest"} stamina"
            } else {
                b.txtSummarySleep.text = "--"
                b.txtSummarySleepLabel.text = "No sleep log"
            }
        }

        // 5. Observe Today's Plan for Progress in Wellness Summary
        db.checkInDao().getDailyPlanByDate(userEmail, todayStr).observe(viewLifecycleOwner) { plan ->
            val b = _binding ?: return@observe
            val completed = plan?.completedCount() ?: 0
            val percent = (completed * 100) / 5
            b.txtSummaryProgress.text = "$percent%"
            b.txtSummaryPlanTasks.text = "$completed/5 tasks"
        }

        // 6. Reminder Time Display
        val isReminderEnabled = prefs.getBoolean("pref_checkin_reminders", true)
        val hour = prefs.getInt("reminder_time_hour", 9)
        val minute = prefs.getInt("reminder_time_minute", 0)
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val displayMinute = String.format(Locale.getDefault(), "%02d", minute)
        val timeFormatted = "$displayHour:$displayMinute $amPm"

        if (isReminderEnabled) {
            binding.txtNextReminderNotice.text = "Daily Reminder: Scheduled for $timeFormatted"
        } else {
            binding.txtNextReminderNotice.text = "Daily Reminders: Disabled (Tap to configure)"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
