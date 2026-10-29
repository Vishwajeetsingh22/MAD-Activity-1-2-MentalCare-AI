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
import com.aimentalcare.app.api.ApiClient
import com.aimentalcare.app.api.TextAnalyzeRequest
import com.aimentalcare.app.data.CheckInEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentCheckinBinding
import com.aimentalcare.app.utils.LocalNLPEngine
import com.aimentalcare.app.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckInFragment : Fragment() {

    private var _binding: FragmentCheckinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAnalyze.setOnClickListener {
            val text = binding.etCheckInText.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), R.string.checkin_empty_error, Toast.LENGTH_SHORT).show()
            } else {
                performAnalysis(text)
            }
        }

        binding.btnCrisisHelp.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_support)
        }

        binding.btnSaveResult.setOnClickListener {
            Toast.makeText(requireContext(), "Check-in assessment saved to local history", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_analytics)
        }
    }

    private fun performAnalysis(userText: String) {
        val appCtx = context?.applicationContext ?: return

        // Set UI to analyzing state
        binding.btnAnalyze.isEnabled = false
        binding.btnAnalyze.text = getString(R.string.ai_loading_state)
        binding.txtAnalyzingState.text = getString(R.string.ai_loading_state)
        binding.layoutAnalyzingState.visibility = View.VISIBLE
        binding.cardResultContainer.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            var level = "LOW"
            var desc = "Normal"
            var score = 25
            var conf = 90.0
            var indicators = listOf<String>()
            var isCrisis = false
            var recommendations = listOf<String>()
            var statusToast: String? = null

            try {
                val res = ApiClient.service.analyzeText(TextAnalyzeRequest(text = userText))
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    level = body.stressLevel
                    desc = body.statusDescription
                    score = body.stressScore
                    conf = body.confidence
                    indicators = body.indicators
                    isCrisis = body.isCrisis
                    recommendations = body.recommendations
                    statusToast = getString(R.string.ai_success_msg)
                } else {
                    val local = LocalNLPEngine.analyzeText(userText)
                    level = local.stressLevel
                    desc = local.statusDescription
                    score = local.stressScore
                    conf = local.confidence
                    indicators = local.indicators
                    isCrisis = local.isCrisis
                    recommendations = local.recommendations
                    statusToast = getString(R.string.ai_backend_error)
                }
            } catch (e: Exception) {
                val local = LocalNLPEngine.analyzeText(userText)
                level = local.stressLevel
                desc = local.statusDescription
                score = local.stressScore
                conf = local.confidence
                indicators = local.indicators
                isCrisis = local.isCrisis
                recommendations = local.recommendations
                statusToast = getString(R.string.ai_network_error)
            }

            val prefs = appCtx.getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
            val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val recordId = "${userEmail}_$todayStr"

            val db = MentalCareDatabase.getDatabase(appCtx)
            val existingToday = db.checkInDao().getTodayDailyCheckInSync(userEmail, todayStr, recordId)
            if (existingToday == null) {
                db.checkInDao().insertCheckIn(
                    CheckInEntity(
                        userId = userEmail,
                        dailyRecordId = recordId,
                        localDate = todayStr,
                        timezone = java.util.TimeZone.getDefault().id,
                        userText = userText,
                        stressLevel = level,
                        statusDescription = desc,
                        stressScore = score,
                        confidence = conf,
                        indicatorsCsv = indicators.joinToString(", "),
                        isCrisis = isCrisis,
                        recommendationsCsv = recommendations.joinToString(" | ")
                    )
                )
            } else {
                statusToast = "Your wellness check-in for today is complete. Come back tomorrow for your next check-in."
            }

            if (score >= 75 || isCrisis) {
                NotificationHelper.sendAlertNotification(
                    appCtx,
                    getString(R.string.high_stress_notification_title),
                    getString(R.string.high_stress_notification_message_format, level, score)
                )
            }

            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext
                b.btnAnalyze.isEnabled = true
                b.btnAnalyze.text = getString(R.string.perform_ai_analysis)
                b.layoutAnalyzingState.visibility = View.GONE
                statusToast?.let { Toast.makeText(appCtx, it, Toast.LENGTH_SHORT).show() }
                displayResult(b, level, desc, score, conf, indicators, isCrisis, recommendations)
            }
        }
    }

    private fun displayResult(
        b: FragmentCheckinBinding,
        level: String,
        desc: String,
        score: Int,
        conf: Double,
        indicators: List<String>,
        isCrisis: Boolean,
        recommendations: List<String>
    ) {
        b.cardResultContainer.visibility = View.VISIBLE
        val confPercent = if (conf <= 1.0 && conf > 0.0) (conf * 100).toInt() else conf.toInt()
        b.txtResultConfidence.text = getString(R.string.confidence_format, confPercent)
        b.txtResultLevel.text = getString(R.string.result_level_format, level, score)
        b.txtResultDescription.text = desc

        val badgeColor = when (level) {
            "LOW" -> "#10B981"
            "MODERATE" -> "#F59E0B"
            "HIGH" -> "#EF4444"
            else -> "#991B1B"
        }
        b.txtResultLevel.setTextColor(badgeColor.toColorInt())

        val indText = StringBuilder()
        indicators.forEach { indText.append("• ").append(it).append("\n") }
        b.txtResultIndicators.text = indText.toString().trim()

        val recText = StringBuilder()
        recommendations.forEach { recText.append("• ").append(it).append("\n") }
        b.txtResultRecommendations.text = recText.toString().trim()

        if (isCrisis) {
            b.bannerCrisisAlert.visibility = View.VISIBLE
        } else {
            b.bannerCrisisAlert.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
