package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.api.ApiClient
import com.aimentalcare.app.api.DailyQuizSubmitRequest
import com.aimentalcare.app.api.TextAnalyzeRequest
import com.aimentalcare.app.data.CheckInEntity
import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.JournalEntity
import com.aimentalcare.app.data.LifestyleEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentDailyCheckinBinding
import com.aimentalcare.app.utils.LocalNLPEngine
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DailyCheckInFragment : Fragment() {

    private var _binding: FragmentDailyCheckinBinding? = null
    private val binding get() = _binding!!

    data class ScaleQuestion(
        val category: String,
        val title: String,
        val subtitle: String,
        val options: List<String>,
        val isNegativeImpact: Boolean // true: higher rating = higher stress
    )

    private val scaleQuestions = listOf(
        // Q2: Stress level
        ScaleQuestion(
            category = "STRESS LEVEL",
            title = "What is your current stress level?",
            subtitle = "Rate how pressured, anxious, or overwhelmed you feel",
            options = listOf(
                "🌿  1 - Very Calm / At ease",
                "🍃  2 - Mild / Manageable",
                "😐  3 - Moderate / Noticeable",
                "😟  4 - High / Pressured",
                "🚨  5 - Severe / Overwhelmed"
            ),
            isNegativeImpact = true
        ),
        // Q3: Sleep quality
        ScaleQuestion(
            category = "SLEEP RECOVERY",
            title = "How was your sleep quality last night?",
            subtitle = "Restfulness and restorative quality of your rest",
            options = listOf(
                "🌙  1 - Poor / Restless (< 5 hrs)",
                "🙁  2 - Broken sleep (5-6 hrs)",
                "😐  3 - Moderate / Fair (6-7 hrs)",
                "🙂  4 - Good / Rested (7-8 hrs)",
                "🛌  5 - Deep & Restorative (8+ hrs)"
            ),
            isNegativeImpact = false
        ),
        // Q4: Energy level
        ScaleQuestion(
            category = "ENERGY & STAMINA",
            title = "How is your energy and stamina today?",
            subtitle = "Rate your physical and mental alertness",
            options = listOf(
                "😴  1 - Low / Exhausted",
                "🙁  2 - Sluggish / Low focus",
                "😐  3 - Steady / Moderate",
                "🙂  4 - Good / Active",
                "⚡  5 - High / Vibrant"
            ),
            isNegativeImpact = false
        ),
        // Q5: Mental focus / overthinking
        ScaleQuestion(
            category = "MENTAL FOCUS",
            title = "How often have you noticed overthinking?",
            subtitle = "Repetitive worries, mental loops, or racing thoughts",
            options = listOf(
                "🎯  1 - Clear mind / Focused",
                "🙂  2 - Occasional distraction",
                "😐  3 - Moderate mental chatter",
                "😟  4 - Frequent overthinking",
                "🌪️  5 - Paralyzing mental loops"
            ),
            isNegativeImpact = true
        )
    )

    // Current guided state (5 questions: 0: Mood, 1: Stress, 2: Sleep, 3: Energy, 4: Focus)
    private var currentStep = 0
    private val totalSteps = 5

    private var selectedMoodIndex = 2 // default to 😐 Okay (index 2)
    private val scaleRatings = IntArray(4) { 3 } // ratings for Q2-Q5 (default 3)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyCheckinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCheckInBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        binding.btnPrevQuestion.setOnClickListener {
            if (currentStep > 0) {
                currentStep--
                renderStep()
            }
        }

        binding.btnNextQuestion.setOnClickListener {
            if (currentStep < totalSteps - 1) {
                currentStep++
                renderStep()
            } else {
                calculateAndSaveResult()
            }
        }

        binding.btnCheckInDone.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        binding.btnViewTrends.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_analytics)
        }

        setupMoodListeners()
        setupRatingScaleListeners()
        checkTodayStatus()
    }

    private fun checkTodayStatus() {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)
        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyRecordId = "${userEmail}_$todayStr"

        lifecycleScope.launch(Dispatchers.IO) {
            val existing = db.checkInDao().getTodayDailyCheckInSync(userEmail, todayStr, dailyRecordId)
            withContext(Dispatchers.Main) {
                if (existing != null) {
                    showAlreadyCompletedView(existing, todayStr)
                } else {
                    showQuestionView()
                }
            }
        }
    }

    private fun showQuestionView() {
        binding.scrollQuestionContainer.visibility = View.VISIBLE
        binding.layoutQuestionProgress.visibility = View.VISIBLE
        binding.layoutQuestionButtons.visibility = View.VISIBLE
        binding.layoutCheckInResult.visibility = View.GONE
        renderStep()
    }

    private fun showAlreadyCompletedView(checkIn: CheckInEntity, dateStr: String) {
        binding.scrollQuestionContainer.visibility = View.GONE
        binding.layoutQuestionProgress.visibility = View.GONE
        binding.layoutQuestionButtons.visibility = View.GONE
        binding.layoutCheckInResult.visibility = View.VISIBLE

        binding.txtCheckInResultTitle.text = "✓ Today's check-in completed"
        binding.txtCheckInResultSubtitle.text = "Your wellness check-in for today is complete.\nCome back tomorrow for your next check-in."

        val dateDisplay = try {
            val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(d ?: Date(checkIn.timestamp))
        } catch (e: Exception) {
            SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date(checkIn.timestamp))
        }
        binding.txtCheckInCompletedDate.text = "Completed: $dateDisplay"

        binding.txtResultStressLevel.text = "${checkIn.stressLevel} STRESS"
        val colorHex = when (checkIn.stressLevel) {
            "LOW" -> "#10B981"
            "MODERATE" -> "#F59E0B"
            else -> "#EF4444"
        }
        binding.txtResultStressLevel.setTextColor(colorHex.toColorInt())
        binding.txtResultScore.text = "Estimated Stress Score: ${checkIn.stressScore}/100"
        binding.txtResultRecommendation.text = "Recommendation: ${checkIn.recommendationsCsv}"
    }

    private fun setupMoodListeners() {
        val moodButtons = listOf(
            binding.btnMood1,
            binding.btnMood2,
            binding.btnMood3,
            binding.btnMood4,
            binding.btnMood5
        )

        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedMoodIndex = index
                highlightMoodButton(index)
            }
        }
    }

    private fun highlightMoodButton(selectedIndex: Int) {
        val moodButtons = listOf(
            binding.btnMood1,
            binding.btnMood2,
            binding.btnMood3,
            binding.btnMood4,
            binding.btnMood5
        )

        moodButtons.forEachIndexed { index, button ->
            val isSelected = index == selectedIndex
            styleSelectionButton(button, isSelected)
        }
    }

    private fun setupRatingScaleListeners() {
        val ratingButtons = listOf(
            binding.btnRating1,
            binding.btnRating2,
            binding.btnRating3,
            binding.btnRating4,
            binding.btnRating5
        )

        ratingButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val qIndex = currentStep - 1
                if (qIndex in 0..3) {
                    scaleRatings[qIndex] = index + 1
                    highlightRatingButton(index + 1)
                }
            }
        }
    }

    private fun highlightRatingButton(ratingValue: Int) {
        val ratingButtons = listOf(
            binding.btnRating1,
            binding.btnRating2,
            binding.btnRating3,
            binding.btnRating4,
            binding.btnRating5
        )

        ratingButtons.forEachIndexed { index, button ->
            val isSelected = (index + 1) == ratingValue
            styleSelectionButton(button, isSelected)
        }
    }

    private fun styleSelectionButton(button: MaterialButton, isSelected: Boolean) {
        val context = button.context
        if (isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.mint))
            button.setTextColor(ContextCompat.getColor(context, R.color.dark_green))
            button.strokeColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.primary_green)
            )
            button.strokeWidth = 4
        } else {
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
            button.setTextColor(ContextCompat.getColor(context, R.color.main_text))
            button.strokeColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.card_border)
            )
            button.strokeWidth = 2
        }
    }

    private fun renderStep() {
        binding.txtQuestionCounter.text = "Question ${currentStep + 1} of $totalSteps"
        binding.progressBarQuestions.progress = currentStep + 1
        binding.btnPrevQuestion.isEnabled = currentStep > 0
        binding.btnNextQuestion.text = if (currentStep == totalSteps - 1) "Complete Check-in ✨" else "Continue"

        when (currentStep) {
            0 -> {
                // Step 0 (Question 1 of 5): Mood selection
                binding.txtQuestionCategory.text = "CURRENT MOOD"
                binding.txtQuestionTitle.text = "How are you feeling today?"
                binding.txtQuestionSubtitle.text = "Select the option that best reflects your emotional state"
                binding.layoutMoodChips.visibility = View.VISIBLE
                binding.layoutRatingScale.visibility = View.GONE
                binding.layoutReflection.visibility = View.GONE
                highlightMoodButton(selectedMoodIndex)
            }
            in 1..3 -> {
                // Steps 1 to 3 (Questions 2 to 4 of 5): Scale questions
                val qIndex = currentStep - 1
                val q = scaleQuestions[qIndex]
                binding.txtQuestionCategory.text = q.category
                binding.txtQuestionTitle.text = q.title
                binding.txtQuestionSubtitle.text = q.subtitle
                binding.layoutMoodChips.visibility = View.GONE
                binding.layoutRatingScale.visibility = View.VISIBLE
                binding.layoutReflection.visibility = View.GONE

                binding.btnRating1.text = q.options[0]
                binding.btnRating2.text = q.options[1]
                binding.btnRating3.text = q.options[2]
                binding.btnRating4.text = q.options[3]
                binding.btnRating5.text = q.options[4]

                highlightRatingButton(scaleRatings[qIndex])
            }
            4 -> {
                // Step 4 (Question 5 of 5): Mental focus + optional reflection
                val q = scaleQuestions[3]
                binding.txtQuestionCategory.text = q.category
                binding.txtQuestionTitle.text = q.title
                binding.txtQuestionSubtitle.text = q.subtitle
                binding.layoutMoodChips.visibility = View.GONE
                binding.layoutRatingScale.visibility = View.VISIBLE
                binding.layoutReflection.visibility = View.VISIBLE

                binding.btnRating1.text = q.options[0]
                binding.btnRating2.text = q.options[1]
                binding.btnRating3.text = q.options[2]
                binding.btnRating4.text = q.options[3]
                binding.btnRating5.text = q.options[4]

                highlightRatingButton(scaleRatings[3])
            }
        }
    }

    private fun calculateAndSaveResult() {
        val moodLabels = listOf("Great", "Good", "Okay", "Low", "Difficult")
        val moodEmojis = listOf("😊", "🙂", "😐", "😟", "😣")
        val moodStressScores = listOf(15, 28, 48, 72, 85)

        val selectedMoodTag = moodLabels[selectedMoodIndex]
        val selectedEmoji = moodEmojis[selectedMoodIndex]
        val moodStress = moodStressScores[selectedMoodIndex]

        // Calculate scale questions score
        var scaleStressTotal = 0
        var maxScaleTotal = 0
        scaleQuestions.forEachIndexed { i, q ->
            val score = scaleRatings[i]
            val contrib = if (q.isNegativeImpact) score else (6 - score)
            scaleStressTotal += contrib
            maxScaleTotal += 5
        }
        val scalePercent = ((scaleStressTotal.toFloat() / maxScaleTotal) * 100).toInt()

        // Base questionnaire stress score (40% mood + 60% scale)
        var estimatedStressScore = ((moodStress * 0.40) + (scalePercent * 0.60)).toInt().coerceIn(10, 95)

        val reflectionText = binding.editReflectionText.text.toString().trim()
        val saveToJournal = binding.cbSaveToJournal.isChecked

        val sleepScore = scaleRatings[1] // Q3
        val sleepHours = when (sleepScore) {
            1 -> 4.5f
            2 -> 5.5f
            3 -> 6.5f
            4 -> 7.5f
            else -> 8.5f
        }

        val energyScore = scaleRatings[2] // Q4
        val energyText = when (energyScore) {
            1 -> "Low"
            2 -> "Low"
            3 -> "Moderate"
            4 -> "Good"
            else -> "High"
        }

        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)
        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timezoneStr = TimeZone.getDefault().id
        val recordId = "${userEmail}_$todayStr"

        binding.btnNextQuestion.isEnabled = false
        binding.btnNextQuestion.text = "Analyzing with AI..."

        lifecycleScope.launch(Dispatchers.IO) {
            // CRITICAL BUSINESS RULE: Atomic check to prevent duplicate submission on the same calendar day
            val existingToday = db.checkInDao().getTodayDailyCheckInSync(userEmail, todayStr, recordId)
            if (existingToday != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Your wellness check-in for today is complete. Come back tomorrow for your next check-in.",
                        Toast.LENGTH_LONG
                    ).show()
                    showAlreadyCompletedView(existingToday, todayStr)
                }
                return@launch
            }

            var nlpIndicators: List<String> = emptyList()
            var isCrisis = false

            // AI NLP analysis if reflection text is provided
            if (reflectionText.isNotBlank()) {
                try {
                    val apiResponse = ApiClient.service.analyzeText(
                        TextAnalyzeRequest(
                            text = reflectionText,
                            sleepHrs = sleepHours,
                            workHrs = 7.0f
                        )
                    )
                    if (apiResponse.isSuccessful && apiResponse.body() != null) {
                        val body = apiResponse.body()!!
                        val nlpScore = body.stressScore
                        nlpIndicators = body.indicators
                        isCrisis = body.isCrisis
                        estimatedStressScore = ((estimatedStressScore * 0.60) + (nlpScore * 0.40)).toInt().coerceIn(10, 95)
                    } else {
                        val localResult = LocalNLPEngine.analyzeText(reflectionText, sleepHours, 7.0f)
                        estimatedStressScore = ((estimatedStressScore * 0.60) + (localResult.stressScore * 0.40)).toInt().coerceIn(10, 95)
                        nlpIndicators = localResult.indicators
                        isCrisis = localResult.isCrisis
                    }
                } catch (e: Exception) {
                    val localResult = LocalNLPEngine.analyzeText(reflectionText, sleepHours, 7.0f)
                    estimatedStressScore = ((estimatedStressScore * 0.60) + (localResult.stressScore * 0.40)).toInt().coerceIn(10, 95)
                    nlpIndicators = localResult.indicators
                    isCrisis = localResult.isCrisis
                }
            }

            val finalStressLevel = when {
                estimatedStressScore < 40 -> "LOW"
                estimatedStressScore < 70 -> "MODERATE"
                else -> "HIGH"
            }

            val rec = when (finalStressLevel) {
                "LOW" -> "Your wellness indicators are balanced today. Continue maintaining regular hydration and healthy sleep routines."
                "MODERATE" -> "Moderate stress pressure detected. A 5-minute Box Breathing pause and breaking current tasks into small blocks will help restore focus."
                else -> "Elevated stress detected. Prioritize stepping away from screens, talk with a trusted friend, and allow yourself restorative rest."
            }

            val indicatorsText = if (nlpIndicators.isNotEmpty()) {
                "Themes: " + nlpIndicators.joinToString(", ")
            } else {
                "Mood: $selectedEmoji $selectedMoodTag, Sleep: ${sleepHours}h, Energy: $energyText"
            }

            // Save CheckInEntity to Room DB with unique dailyRecordId and localDate
            db.checkInDao().insertCheckIn(
                CheckInEntity(
                    userId = userEmail,
                    dailyRecordId = recordId,
                    localDate = todayStr,
                    timezone = timezoneStr,
                    userText = if (reflectionText.isNotBlank()) reflectionText else "Daily check-in: Mood $selectedEmoji $selectedMoodTag, Energy: $energyText, Sleep: ${sleepHours}h",
                    stressScore = estimatedStressScore,
                    stressLevel = finalStressLevel,
                    statusDescription = "Smart Wellness Check-in",
                    confidence = 0.92,
                    indicatorsCsv = indicatorsText,
                    recommendationsCsv = rec,
                    isCrisis = isCrisis || (estimatedStressScore >= 85)
                )
            )

            // Save LifestyleEntity
            db.checkInDao().insertLifestyle(
                LifestyleEntity(
                    sleepHrs = sleepHours,
                    workHrs = 7.0f,
                    screenHrs = 4.0f,
                    exerciseMins = 20,
                    energyLevel = energyText,
                    moodEmoji = selectedEmoji,
                    stressScore = estimatedStressScore
                )
            )

            // Update Today's Daily Plan checkInDone flag
            val currentPlan = db.checkInDao().getDailyPlanByDateSync(userEmail, todayStr) ?: DailyPlanEntity(
                userId = userEmail,
                dateString = todayStr
            )
            db.checkInDao().insertOrUpdateDailyPlan(currentPlan.copy(checkInDone = true))

            // Save Journal Entry if requested
            var journalSaved = false
            if (saveToJournal && reflectionText.isNotBlank()) {
                db.checkInDao().insertJournal(
                    JournalEntity(
                        userId = userEmail,
                        title = "Daily Reflection ($todayStr)",
                        content = reflectionText,
                        moodTag = selectedMoodTag,
                        timestamp = System.currentTimeMillis(),
                        aiStressLevel = finalStressLevel,
                        aiAnalyzed = true,
                        aiRecommendations = rec
                    )
                )
                journalSaved = true
            }

            // Async sync with backend
            try {
                val answersList = listOf(selectedMoodIndex + 1) + scaleRatings.toList()
                ApiClient.service.submitDailyQuiz(
                    DailyQuizSubmitRequest(
                        userId = userEmail,
                        localDate = todayStr,
                        timezone = timezoneStr,
                        answers = answersList,
                        stressScore = estimatedStressScore,
                        stressLevel = finalStressLevel,
                        recommendation = rec
                    )
                )
            } catch (e: Exception) {
                // Offline fallback
            }

            withContext(Dispatchers.Main) {
                binding.scrollQuestionContainer.visibility = View.GONE
                binding.layoutQuestionProgress.visibility = View.GONE
                binding.layoutQuestionButtons.visibility = View.GONE
                binding.layoutCheckInResult.visibility = View.VISIBLE

                binding.txtCheckInResultTitle.text = "✓ Check-in completed"
                binding.txtCheckInResultSubtitle.text = "Your wellness check-in has been\nsaved for today."
                val dateDisplay = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
                binding.txtCheckInCompletedDate.text = "Completed: $dateDisplay"

                binding.txtResultStressLevel.text = "$finalStressLevel STRESS"
                val colorHex = when (finalStressLevel) {
                    "LOW" -> "#10B981"
                    "MODERATE" -> "#F59E0B"
                    else -> "#EF4444"
                }
                binding.txtResultStressLevel.setTextColor(colorHex.toColorInt())
                binding.txtResultScore.text = "Estimated Stress Score: $estimatedStressScore/100"
                binding.txtResultRecommendation.text = "Recommendation: $rec"

                if (journalSaved) {
                    binding.txtResultJournalSaved.visibility = View.VISIBLE
                } else {
                    binding.txtResultJournalSaved.visibility = View.GONE
                }

                if (nlpIndicators.isNotEmpty()) {
                    binding.txtResultNlpIndicators.visibility = View.VISIBLE
                    binding.txtResultNlpIndicators.text = "AI Detected Themes: ${nlpIndicators.joinToString(", ")}"
                } else {
                    binding.txtResultNlpIndicators.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
