package com.aimentalcare.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.R
import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.data.WellnessActivityEntity
import com.aimentalcare.app.databinding.DialogActivityTimerBinding
import com.aimentalcare.app.databinding.FragmentActivitiesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ActivitiesAdapter
    private var activeTimer: CountDownTimer? = null
    private var userEmail: String = "user@mentalcare.ai"

    private val activityCatalog = listOf(
        ActivityItem(
            id = "act_breathing",
            icon = "🫁",
            title = "5-Minute Box Breathing",
            category = "Breathing",
            durationMinutes = 5,
            description = "Inhale 4s, hold 4s, exhale 4s, hold 4s. Lowers physical stress and regulates autonomic response.",
            guidance = "Inhale slowly (4s)... Hold gently (4s)... Exhale smoothly (4s)... Hold empty (4s)..."
        ),
        ActivityItem(
            id = "act_meditation",
            icon = "🧘",
            title = "Guided Calm Meditation",
            category = "Meditation",
            durationMinutes = 10,
            description = "Gentle breath awareness and body mindfulness to quiet mental chatter and center focus.",
            guidance = "Close your eyes softly. Notice the natural rhythm of your breath entering and leaving your chest."
        ),
        ActivityItem(
            id = "act_relaxation",
            icon = "🌿",
            title = "5-4-3-2-1 Sensory Grounding",
            category = "Relaxation",
            durationMinutes = 5,
            description = "Re-anchor racing thoughts by noticing 5 things you see, 4 you feel, 3 hear, 2 smell, 1 taste.",
            guidance = "Observe your immediate surroundings without judgment. What sensations are present right now?"
        ),
        ActivityItem(
            id = "act_stretching",
            icon = "🤸",
            title = "Gentle Desk & Neck Stretching",
            category = "Stretching",
            durationMinutes = 5,
            description = "Slow neck rolls, shoulder drops, and spine extensions to release tension accumulated from sitting.",
            guidance = "Roll your shoulders back gently. Tilt your head to the left, hold 15s, then slowly to the right."
        ),
        ActivityItem(
            id = "act_journaling",
            icon = "✍️",
            title = "Reflective Gratitude Journal",
            category = "Journaling",
            durationMinutes = 5,
            description = "Write down 3 moments, thoughts, or interactions that brought you relief or peace today.",
            guidance = "Notice the small positive moments that often get overshadowed by daily pressure."
        ),
        ActivityItem(
            id = "act_sleep",
            icon = "🌙",
            title = "Evening Screen Wind-down",
            category = "Sleep Routine",
            durationMinutes = 10,
            description = "Dim phone brightness, put away social media, and relax facial muscles before bedtime.",
            guidance = "Allow your mind to decelerate. Give yourself permission to disconnect until tomorrow."
        ),
        ActivityItem(
            id = "act_habits",
            icon = "💧",
            title = "Hydration & Posture Reset",
            category = "Healthy Habits",
            durationMinutes = 3,
            description = "Drink a full glass of water, stand up, roll back your shoulders, and look 20 feet away.",
            guidance = "Sip slowly. Feel the physical refreshment and reset your seated posture."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        binding.btnActivitiesBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        adapter = ActivitiesAdapter { item ->
            startActivityTimer(item)
        }

        binding.recyclerActivities.layoutManager = LinearLayoutManager(context)
        binding.recyclerActivities.adapter = adapter
        adapter.submitList(activityCatalog)
    }

    private fun startActivityTimer(item: ActivityItem) {
        val dialogBinding = DialogActivityTimerBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.txtTimerActivityTitle.text = "${item.icon} ${item.title}"
        dialogBinding.txtTimerActivityCategory.text = "${item.category} • ${item.durationMinutes} Minutes"
        dialogBinding.txtTimerGuidance.text = item.guidance

        val totalSeconds = item.durationMinutes * 60
        dialogBinding.progressActivityTimer.max = totalSeconds
        dialogBinding.progressActivityTimer.progress = totalSeconds

        fun formatTime(sec: Int): String {
            val m = sec / 60
            val s = sec % 60
            return String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }

        dialogBinding.txtCountdownTimer.text = formatTime(totalSeconds)

        activeTimer?.cancel()
        activeTimer = object : CountDownTimer((totalSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secRemaining = (millisUntilFinished / 1000).toInt()
                dialogBinding.txtCountdownTimer.text = formatTime(secRemaining)
                dialogBinding.progressActivityTimer.progress = secRemaining
            }

            override fun onFinish() {
                dialogBinding.txtCountdownTimer.text = "00:00"
                dialogBinding.progressActivityTimer.progress = 0
                completeActivity(item, dialog)
            }
        }.start()

        dialogBinding.btnCancelTimer.setOnClickListener {
            activeTimer?.cancel()
            dialog.dismiss()
        }

        dialogBinding.btnCompleteTimer.setOnClickListener {
            activeTimer?.cancel()
            completeActivity(item, dialog)
        }

        dialog.show()
    }

    private fun completeActivity(item: ActivityItem, dialog: AlertDialog) {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            // Record activity
            db.checkInDao().insertWellnessActivity(
                WellnessActivityEntity(
                    userId = userEmail,
                    title = item.title,
                    category = item.category,
                    durationMinutes = item.durationMinutes
                )
            )

            // If breathing was completed, update Today's Plan
            if (item.category.equals("Breathing", ignoreCase = true)) {
                val plan = db.checkInDao().getDailyPlanByDateSync(userEmail, todayStr) ?: DailyPlanEntity(
                    userId = userEmail,
                    dateString = todayStr
                )
                db.checkInDao().insertOrUpdateDailyPlan(plan.copy(breathingDone = true))
            }

            withContext(Dispatchers.Main) {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Activity completed! 🎉", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeTimer?.cancel()
        _binding = null
    }
}
