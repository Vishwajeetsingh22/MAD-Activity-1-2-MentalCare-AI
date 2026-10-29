package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.api.ApiClient
import com.aimentalcare.app.api.BurnoutRequest
import com.aimentalcare.app.data.CheckInEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentAnalyticsBinding
import com.aimentalcare.app.utils.LocalNLPEngine
import com.aimentalcare.app.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Phase 5 — Premium Stress Insights Fragment
 * 7 Dedicated Sections:
 * 1. Current stress status
 * 2. Recent stress history
 * 3. Stress trend (Real responsive canvas chart)
 * 4. Daily check-in completion (e.g. "5 of 7 days")
 * 5. Wellness progress
 * 6. Personalised insights (Strictly derived from real data)
 * 7. Recommended wellness activities (Tailored to current status)
 */
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private var historicalScores = listOf<Int>()
    private var userEmail: String = "user@mentalcare.ai"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        loadAnalyticsData()

        binding.btnRecalculateBurnout.setOnClickListener {
            runBurnoutScan()
        }
    }

    private fun loadAnalyticsData() {
        val appCtx = context?.applicationContext ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val db = MentalCareDatabase.getDatabase(appCtx)
            val allUserCheckIns = db.checkInDao().getAllCheckInsSync(userEmail)

            // CRITICAL BUSINESS RULE: Deduplicate check-ins by unique calendar date
            // Multiple same-day submissions are strictly counted as ONE calendar day (latest submission retained).
            val distinctDayCheckIns = mutableListOf<CheckInEntity>()
            val seenDates = mutableSetOf<String>()
            val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (checkIn in allUserCheckIns) {
                val dateKey = if (checkIn.localDate.isNotEmpty()) {
                    checkIn.localDate
                } else {
                    dateKeyFormat.format(Date(checkIn.timestamp))
                }
                if (!seenDates.contains(dateKey)) {
                    seenDates.add(dateKey)
                    distinctDayCheckIns.add(checkIn)
                }
            }

            // Up to 7 distinct days in chronological order for charting and trajectory
            val recentDistinct = distinctDayCheckIns.take(7)
            val chronologicalCheckIns = recentDistinct.reversed()
            historicalScores = chronologicalCheckIns.map { it.stressScore }

            val dayLabels = chronologicalCheckIns.map { item ->
                try {
                    val key = if (item.localDate.isNotEmpty()) item.localDate else dateKeyFormat.format(Date(item.timestamp))
                    val d = dateKeyFormat.parse(key)
                    SimpleDateFormat("EEE", Locale.getDefault()).format(d ?: Date(item.timestamp))
                } catch (e: Exception) {
                    SimpleDateFormat("EEE", Locale.getDefault()).format(Date(item.timestamp))
                }
            }

            // Calculate actual check-in streak based on distinct dates
            val streakDays = calculateStreak(distinctDayCheckIns.map { it.timestamp })
            val totalCheckInsCount = seenDates.size

            // Weekly rolling 7-day completion: how many unique calendar days logged in the last 7 days
            val weekDateKeys = mutableSetOf<String>()
            val weekCal = Calendar.getInstance()
            for (i in 0 until 7) {
                weekDateKeys.add(dateKeyFormat.format(weekCal.time))
                weekCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            val uniqueDaysThisWeek = seenDates.count { weekDateKeys.contains(it) }

            // Monthly progress: unique days logged in current calendar month
            val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
            val uniqueDaysThisMonth = seenDates.count { it.startsWith(currentMonthPrefix) }

            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext

                // ========================================================
                // 1. SECTION: CURRENT STRESS STATUS
                // ========================================================
                val latestCheckIn = distinctDayCheckIns.firstOrNull()
                if (latestCheckIn != null) {
                    val timeFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                    b.txtCurrentStressTime.text = "Logged: ${timeFormat.format(Date(latestCheckIn.timestamp))}"
                    b.txtCurrentStressStatus.text = "${latestCheckIn.stressLevel} Stress"
                    b.txtCurrentStressScore.text = "${latestCheckIn.stressScore}% Stress Index"

                    // Status color badge
                    val (textColorRes, bgRes) = when (latestCheckIn.stressLevel.uppercase()) {
                        "LOW" -> R.color.primary_green to R.color.stress_low_bg
                        "MODERATE" -> R.color.color_moderate to R.color.stress_moderate_bg
                        else -> R.color.color_high to R.color.stress_high_bg
                    }
                    b.txtCurrentStressScore.setTextColor(ContextCompat.getColor(appCtx, textColorRes))

                    if (latestCheckIn.userText.isNotBlank()) {
                        b.txtCurrentMoodSummary.text = "\"${latestCheckIn.userText}\""
                    } else {
                        b.txtCurrentMoodSummary.text = "${latestCheckIn.statusDescription} • Indicators: ${latestCheckIn.indicatorsCsv}"
                    }
                } else {
                    b.txtCurrentStressTime.text = "No records yet"
                    b.txtCurrentStressStatus.text = "No Check-ins Yet"
                    b.txtCurrentStressScore.text = "-- %"
                    b.txtCurrentStressScore.setTextColor(ContextCompat.getColor(appCtx, R.color.secondary_text))
                    b.txtCurrentMoodSummary.text = "Complete your first daily check-in to establish your current wellness baseline."
                }

                // ========================================================
                // 3. SECTION: STRESS TREND (REAL CANVAS CHART)
                // ========================================================
                b.stressChartView.setScores(historicalScores, dayLabels)

                if (historicalScores.isEmpty()) {
                    b.txt7DayAvg.text = "7-Day Avg: --"
                    b.txtBurnoutProgression.text = "Complete a few wellness check-ins to see your personal trends."
                } else if (historicalScores.size == 1) {
                    val singleScore = historicalScores[0]
                    b.txt7DayAvg.text = "7-Day Avg: $singleScore%"
                    b.txtBurnoutProgression.text = "1 check-in recorded: ${dayLabels.firstOrNull() ?: "Today"} ($singleScore%). Keep logging daily to reveal trends."
                } else {
                    val avg = historicalScores.average().toInt()
                    b.txt7DayAvg.text = getString(R.string.avg_7_day_format, avg)

                    val sb = StringBuilder()
                    chronologicalCheckIns.forEachIndexed { idx, item ->
                        val label = if (idx < dayLabels.size) dayLabels[idx] else "Day ${idx + 1}"
                        sb.append("$label (${item.stressScore}%)")
                        if (idx < chronologicalCheckIns.size - 1) sb.append(" → ")
                    }
                    b.txtBurnoutProgression.text = sb.toString()
                }

                // ========================================================
                // 4. SECTION: DAILY CHECK-IN COMPLETION
                // ========================================================
                b.txtCheckInProgressFraction.text = "$uniqueDaysThisWeek of 7 days"
                b.progressCheckInWeekly.progress = uniqueDaysThisWeek.coerceIn(0, 7)
                b.txtStreakDays.text = if (streakDays == 1) "🔥 1 Day Streak" else "🔥 $streakDays Days Streak"
                b.txtWeeklyConsistency.text = "This Month: $uniqueDaysThisMonth days • Total Logged: $totalCheckInsCount"

                // ========================================================
                // 6. SECTION: PERSONALISED INSIGHTS
                // Strictly derived from actual stored data — NEVER fabricated
                // ========================================================
                if (historicalScores.size < 2) {
                    b.txtPersonalizedInsight.text = "Complete a few wellness check-ins to see your personal trends."
                    b.layoutPatternDetails.visibility = View.GONE
                } else {
                    val first = historicalScores.first()
                    val last = historicalScores.last()
                    val diff = last - first
                    val avg = historicalScores.average().toInt()

                    val trendNarrative = when {
                        diff >= 10 -> "Your recent check-ins show an upward trend in stress over the last few days (+${diff}%). Scheduling regular 5-minute pauses and breathing intervals will help relieve tension."
                        diff <= -10 -> "Your recent check-ins show an improving trend with decreasing stress levels (${diff}%). Your wellness habits and routines are paying off!"
                        else -> "Your recent check-ins indicate a stable, steady emotional wellness pattern. Keep up your balanced daily routine."
                    }

                    val elevatedWarning = if (avg >= 70) {
                        "\n\nNote: Your average stress is currently elevated ($avg%). Be gentle with yourself and consider reaching out to your support circle."
                    } else ""

                    b.txtPersonalizedInsight.text = "$trendNarrative$elevatedWarning"
                    b.layoutPatternDetails.visibility = View.VISIBLE
                    b.txtSleepPatternInsight.text = "• 7-Day Average Stress: $avg%"
                    b.txtEnergyPatternInsight.text = "• Trend Trajectory: ${if (diff > 0) "+$diff%" else "$diff%"} from start"
                }

                // ========================================================
                // 5. SECTION: WELLNESS PROGRESS & BURNOUT RISK
                // ========================================================
                b.txtTotalCheckIns.text = totalCheckInsCount.toString()

                // Observe private journals count
                db.checkInDao().getJournalCount(userEmail).observe(viewLifecycleOwner) { jCount ->
                    _binding?.txtTotalJournals?.text = (jCount ?: 0).toString()
                }

                if (historicalScores.size < 3) {
                    b.txtBurnoutBadge.text = "MONITORING"
                    b.txtBurnoutBadge.setTextColor(ContextCompat.getColor(appCtx, R.color.primary_green))
                    b.txtBurnoutMessage.text = getString(R.string.more_checkins_needed)
                } else {
                    runBurnoutScan()
                }

                // ========================================================
                // 7. SECTION: RECOMMENDED WELLNESS ACTIVITIES
                // ========================================================
                populateRecommendedActivities(appCtx, latestCheckIn?.stressLevel)

                // ========================================================
                // 2. SECTION: RECENT STRESS HISTORY
                // ========================================================
                b.layoutHistoryList.removeAllViews()
                if (recentDistinct.isEmpty()) {
                    b.txtEmptyHistory.visibility = View.VISIBLE
                } else {
                    b.txtEmptyHistory.visibility = View.GONE
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

                    for (item in recentDistinct) {
                        val card = LinearLayout(appCtx).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(28, 20, 28, 20)
                            background = ContextCompat.getDrawable(appCtx, R.drawable.bg_medical_card)
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 14)
                            }
                            layoutParams = params
                        }

                        val headerLayout = RelativeLayout(appCtx).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val dateView = TextView(appCtx).apply {
                            text = dateFormat.format(Date(item.timestamp))
                            setTextColor(ContextCompat.getColor(appCtx, R.color.secondary_text))
                            textSize = 12f
                        }

                        val badgeView = TextView(appCtx).apply {
                            text = "${item.stressLevel} (${item.stressScore}%)"
                            textSize = 11f
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setPadding(16, 6, 16, 6)

                            val (badgeTextRes, badgeBgRes) = when (item.stressLevel.uppercase()) {
                                "LOW" -> R.color.primary_green to R.color.stress_low_bg
                                "MODERATE" -> R.color.color_moderate to R.color.stress_moderate_bg
                                else -> R.color.color_high to R.color.stress_high_bg
                            }
                            setTextColor(ContextCompat.getColor(appCtx, badgeTextRes))
                            background = ContextCompat.getDrawable(appCtx, R.drawable.bg_medical_card)

                            val badgeParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_END)
                            }
                            layoutParams = badgeParams
                        }

                        headerLayout.addView(dateView)
                        headerLayout.addView(badgeView)
                        card.addView(headerLayout)

                        if (item.userText.isNotBlank()) {
                            val textView = TextView(appCtx).apply {
                                text = "\"${item.userText}\""
                                setTextColor(ContextCompat.getColor(appCtx, R.color.main_text))
                                textSize = 13f
                                setPadding(0, 10, 0, 6)
                            }
                            card.addView(textView)
                        }

                        val footerText = TextView(appCtx).apply {
                            text = "${item.statusDescription} • Indicators: ${item.indicatorsCsv}"
                            setTextColor(ContextCompat.getColor(appCtx, R.color.secondary_text))
                            textSize = 11f
                        }
                        card.addView(footerText)

                        b.layoutHistoryList.addView(card)
                    }
                }
            }
        }
    }

    private fun populateRecommendedActivities(appCtx: android.content.Context, stressLevel: String?) {
        val b = _binding ?: return
        b.layoutRecommendedActivities.removeAllViews()

        val activities = when (stressLevel?.uppercase()) {
            "HIGH" -> listOf(
                Triple("🧘 Box Breathing (4-4-4-4)", "4-minute paced breathing cycle to reduce acute nervous tension.", "Start Box Breathing"),
                Triple("🛑 5-4-3-2-1 Sensory Grounding", "Notice 5 sights, 4 sensations, 3 sounds, 2 scents, and 1 taste.", "Open Grounding"),
                Triple("📞 Connect with Trusted Support", "Talk through feelings with your configured support contact.", "Contact Support")
            )
            "MODERATE" -> listOf(
                Triple("🌬️ 4-7-8 Deep Relaxation Breath", "Inhale 4s, hold 7s, exhale 8s to trigger parasympathetic calm.", "Start 4-7-8 Breath"),
                Triple("📝 Mindful Journal Reflection", "Write down lingering worries to unburden your working memory.", "Open Journal"),
                Triple("🚶 15-Minute Movement Break", "Step away from screens and stretch your neck and shoulders.", "Log Movement")
            )
            "LOW" -> listOf(
                Triple("✨ 3 Gratitude Reflections", "Reinforce your positive baseline by noting 3 highlights from today.", "Open Journal"),
                Triple("🎯 Low-Stress Daily Planning", "Prioritize your top 3 non-negotiable tasks for the day.", "Plan Day"),
                Triple("🌙 Restorative Sleep Wind-Down", "Dim blue light screens 45 minutes before bedtime.", "View Wind-Down")
            )
            else -> listOf(
                Triple("🧠 Complete Today's Check-In", "Log your mood and mental indicators in under 1 minute.", "Check In Now"),
                Triple("💬 AI Wellness Assistant Chat", "Talk through any feelings or questions with your AI companion.", "Chat with AI"),
                Triple("🌬️ 4-7-8 Breathing Exercise", "A quick 3-minute relaxation pause for your nervous system.", "Start Breathing")
            )
        }

        for (act in activities) {
            val itemLayout = LinearLayout(appCtx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 18, 24, 18)
                background = ContextCompat.getDrawable(appCtx, R.drawable.bg_medical_card)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 10)
                }
                layoutParams = params
            }

            val titleView = TextView(appCtx).apply {
                text = act.first
                setTextColor(ContextCompat.getColor(appCtx, R.color.main_text))
                textSize = 13.5f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            itemLayout.addView(titleView)

            val descView = TextView(appCtx).apply {
                text = act.second
                setTextColor(ContextCompat.getColor(appCtx, R.color.secondary_text))
                textSize = 11.5f
                setPadding(0, 4, 0, 0)
            }
            itemLayout.addView(descView)

            b.layoutRecommendedActivities.addView(itemLayout)
        }
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateSet = timestamps.map { sdf.format(Date(it)).toInt() }.toSortedSet(Comparator.reverseOrder())

        val cal = Calendar.getInstance()
        val todayInt = sdf.format(cal.time).toInt()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayInt = sdf.format(cal.time).toInt()

        // Check if user checked in today or yesterday to maintain streak
        val startsWithToday = dateSet.contains(todayInt)
        val startsWithYesterday = dateSet.contains(yesterdayInt)

        if (!startsWithToday && !startsWithYesterday) return 0

        cal.time = Date()
        if (!startsWithToday) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        var streak = 0
        while (true) {
            val checkInt = sdf.format(cal.time).toInt()
            if (dateSet.contains(checkInt)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun runBurnoutScan() {
        val appCtx = context?.applicationContext ?: return
        if (historicalScores.size < 3) {
            _binding?.txtBurnoutBadge?.text = "MONITORING"
            _binding?.txtBurnoutMessage?.text = getString(R.string.more_checkins_needed)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var risk = "LOW"
            var isBurnout = false
            var msg = "Monitoring daily wellness pattern."

            try {
                val res = ApiClient.service.analyzeBurnout(BurnoutRequest(historicalScores))
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    risk = body.burnoutRisk
                    isBurnout = body.isBurnoutPattern
                    msg = body.trendMessage
                } else {
                    val local = LocalNLPEngine.analyzeBurnout(historicalScores)
                    risk = local.burnoutRisk
                    isBurnout = local.isBurnoutPattern
                    msg = local.trendMessage
                }
            } catch (e: Exception) {
                val local = LocalNLPEngine.analyzeBurnout(historicalScores)
                risk = local.burnoutRisk
                isBurnout = local.isBurnoutPattern
                msg = local.trendMessage
            }

            if (isBurnout) {
                NotificationHelper.sendAlertNotification(
                    appCtx,
                    getString(R.string.burnout_notification_title),
                    getString(R.string.burnout_notification_message)
                )
            }

            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext
                b.txtBurnoutBadge.text = getString(R.string.risk_format, risk)
                val badgeColor = when (risk.uppercase()) {
                    "HIGH" -> R.color.color_high
                    "MODERATE" -> R.color.color_moderate
                    else -> R.color.primary_green
                }
                b.txtBurnoutBadge.setTextColor(ContextCompat.getColor(appCtx, badgeColor))
                b.txtBurnoutMessage.text = msg
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
