package com.aimentalcare.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.GoalEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.ActivityOnboardingBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentStep = 1
    private val totalSteps = 8

    // Collected User Preferences
    private var preferredName: String = "Friend"
    private var ageRange: String = "18 - 24"
    private var occupation: String = "Student"
    private val selectedGoals = mutableListOf<String>()
    private val selectedChallenges = mutableListOf<String>()
    private val selectedHabits = mutableListOf<String>()
    private var sleepPattern: String = "Normal"
    private var dailyCommitmentMinutes: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", null)
        val isFirstTime = intent.getBooleanExtra("IS_FIRST_TIME_REGISTRATION", false)
        val regName = intent.getStringExtra("REGISTERED_NAME") ?: prefs.getString("user_name", "") ?: ""

        if (regName.isNotEmpty()) {
            preferredName = regName
            binding.etOnboardingName.setText(regName)
        }

        // If authenticated user or arriving directly from registration, jump to Step 2 (Account Setup)
        if (isFirstTime || !userEmail.isNullOrEmpty()) {
            currentStep = 2
        }

        setupStepViews()
        setupListeners()
    }

    private fun setupListeners() {
        // Step 1: Welcome Listeners
        binding.btnWelcomeGetStarted.setOnClickListener {
            goToStep(2)
        }

        binding.btnWelcomeLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Top Back Button
        binding.btnStepBack.setOnClickListener {
            val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
            val userEmail = prefs.getString("user_email", null)
            val minStep = if (!userEmail.isNullOrEmpty()) 2 else 1

            if (currentStep > minStep) {
                goToStep(currentStep - 1)
            } else if (!userEmail.isNullOrEmpty()) {
                Toast.makeText(this, "Please complete your account setup to start your wellness journey.", Toast.LENGTH_SHORT).show()
            }
        }

        // Bottom Continue Button
        binding.btnStepNext.setOnClickListener {
            handleStepNext()
        }
    }

    private fun handleStepNext() {
        when (currentStep) {
            2 -> {
                // Save Personalization
                val nameInput = binding.etOnboardingName.text?.toString()?.trim()
                preferredName = if (!nameInput.isNullOrEmpty()) nameInput else "Friend"

                val checkedAgeId = binding.chipGroupAge.checkedChipId
                if (checkedAgeId != View.NO_ID) {
                    ageRange = findViewById<Chip>(checkedAgeId)?.text?.toString() ?: "18 - 24"
                }

                val checkedOccId = binding.chipGroupOccupation.checkedChipId
                if (checkedOccId != View.NO_ID) {
                    occupation = findViewById<Chip>(checkedOccId)?.text?.toString() ?: "Student"
                }

                goToStep(3)
            }
            3 -> {
                // Save Goals
                selectedGoals.clear()
                for (i in 0 until binding.chipGroupGoals.childCount) {
                    val chip = binding.chipGroupGoals.getChildAt(i) as? Chip
                    if (chip != null && chip.isChecked) {
                        selectedGoals.add(chip.text.toString())
                    }
                }
                val customGoal = binding.etCustomGoal.text?.toString()?.trim()
                if (!customGoal.isNullOrEmpty()) {
                    selectedGoals.add(customGoal)
                }
                if (selectedGoals.isEmpty()) {
                    selectedGoals.add("Understand my stress better")
                }
                goToStep(4)
            }
            4 -> {
                // Save Challenges
                selectedChallenges.clear()
                for (i in 0 until binding.chipGroupChallenges.childCount) {
                    val chip = binding.chipGroupChallenges.getChildAt(i) as? Chip
                    if (chip != null && chip.isChecked) {
                        selectedChallenges.add(chip.text.toString())
                    }
                }
                if (selectedChallenges.isEmpty()) {
                    selectedChallenges.add("Academic pressure")
                }
                goToStep(5)
            }
            5 -> {
                // Save Habits
                selectedHabits.clear()
                for (i in 0 until binding.chipGroupHabits.childCount) {
                    val chip = binding.chipGroupHabits.getChildAt(i) as? Chip
                    if (chip != null && chip.isChecked) {
                        selectedHabits.add(chip.text.toString())
                    }
                }
                if (selectedHabits.isEmpty()) {
                    selectedHabits.add("Sleep Routine")
                }
                goToStep(6)
            }
            6 -> {
                // Save Sleep
                val checkedSleepId = binding.chipGroupSleep.checkedChipId
                sleepPattern = if (checkedSleepId != View.NO_ID) {
                    findViewById<Chip>(checkedSleepId)?.text?.toString() ?: "I often wake up feeling tired"
                } else {
                    "I often wake up feeling tired"
                }
                goToStep(7)
            }
            7 -> {
                // Save Commitment
                dailyCommitmentMinutes = when (binding.chipGroupCommitment.checkedChipId) {
                    R.id.chipTime5m -> 5
                    R.id.chipTime15m -> 15
                    R.id.chipTime20mPlus -> 20
                    else -> 10
                }
                generatePersonalizedPlan()
                goToStep(8)
            }
            8 -> {
                // Complete Onboarding & Save
                completeOnboarding()
            }
        }
    }

    private fun generatePersonalizedPlan() {
        val goalsText = if (selectedGoals.isNotEmpty()) selectedGoals.take(2).joinToString(" & ") else "Understand stress"
        binding.txtPlanGoalsSummary.text = "🎯 Focused On: $goalsText ($dailyCommitmentMinutes min/day)"

        val routine = buildString {
            append("• Morning: 5-minute daily mental check-in\n")
            if (selectedChallenges.any { it.contains("pressure", ignoreCase = true) || it.contains("deadline", ignoreCase = true) }) {
                append("• Midday: 25-minute Pomodoro focus block & short stretch\n")
            } else {
                append("• Midday: 5-minute Box Breathing pause\n")
            }
            if (selectedGoals.any { it.contains("journal", ignoreCase = true) || it.contains("stress", ignoreCase = true) }) {
                append("• Afternoon: Reflective journal entry with AI insights\n")
            } else {
                append("• Afternoon: Mindful hydration and posture reset\n")
            }
            if (sleepPattern.contains("tired", ignoreCase = true) || sleepPattern.contains("asleep", ignoreCase = true)) {
                append("• Evening: 10-minute screen-free wind-down & relaxation\n")
            } else {
                append("• Evening: Daily wellness review & reflection\n")
            }
        }
        binding.txtPlanSummary.text = routine
    }

    private fun completeOnboarding() {
        val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        prefs.edit()
            .putString("user_name", preferredName)
            .putString("user_age_range", ageRange)
            .putString("user_occupation", occupation)
            .putString("user_goals_csv", selectedGoals.joinToString(", "))
            .putString("user_challenges_csv", selectedChallenges.joinToString(", "))
            .putString("user_habits_csv", selectedHabits.joinToString(", "))
            .putString("user_sleep_pattern", sleepPattern)
            .putInt("user_daily_commitment", dailyCommitmentMinutes)
            .putBoolean("account_setup_completed", true)
            .putBoolean("onboarding_completed", true)
            .apply()

        // Seed initial goals and today's plan in database
        val db = MentalCareDatabase.getDatabase(applicationContext)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Update User setup status
                db.checkInDao().updateUserSetupStatus(userEmail, true)

                // Seed goals
                for (goal in selectedGoals.take(3)) {
                    db.checkInDao().insertGoal(
                        GoalEntity(
                            userId = userEmail,
                            title = goal,
                            category = if (goal.contains("sleep", ignoreCase = true)) "Sleep" else "Wellness",
                            targetDays = 7,
                            completedDays = 0,
                            isCompleted = false
                        )
                    )
                }

                // Seed today's plan if not present
                val existingPlan = db.checkInDao().getDailyPlanByDateSync(userEmail, todayStr)
                if (existingPlan == null) {
                    db.checkInDao().insertOrUpdateDailyPlan(
                        DailyPlanEntity(
                            userId = userEmail,
                            dateString = todayStr,
                            checkInDone = false,
                            breathingDone = false,
                            journalDone = false,
                            breakDone = false,
                            reflectionDone = false
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OnboardingActivity, "Account setup completed! Welcome to MentalCare AI, $preferredName!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@OnboardingActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OnboardingActivity, "Error saving setup: ${e.localizedMessage}. Please retry.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToStep(step: Int) {
        currentStep = step
        setupStepViews()
    }

    private fun setupStepViews() {
        if (currentStep == 1) {
            binding.layoutStepProgress.visibility = View.GONE
            binding.layoutBottomStepNav.visibility = View.GONE
        } else {
            binding.layoutStepProgress.visibility = View.VISIBLE
            binding.layoutBottomStepNav.visibility = View.VISIBLE
            binding.txtStepCounter.text = "Step $currentStep of $totalSteps"
            binding.progressBarSteps.progress = currentStep
            binding.btnStepNext.text = if (currentStep == totalSteps) "Start My Wellness Journey" else "Continue"
        }

        binding.step1Welcome.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        binding.step2Personalization.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        binding.step3Goals.visibility = if (currentStep == 3) View.VISIBLE else View.GONE
        binding.step4Challenges.visibility = if (currentStep == 4) View.VISIBLE else View.GONE
        binding.step5Habits.visibility = if (currentStep == 5) View.VISIBLE else View.GONE
        binding.step6Sleep.visibility = if (currentStep == 6) View.VISIBLE else View.GONE
        binding.step7Commitment.visibility = if (currentStep == 7) View.VISIBLE else View.GONE
        binding.step8Plan.visibility = if (currentStep == 8) View.VISIBLE else View.GONE
    }
}
