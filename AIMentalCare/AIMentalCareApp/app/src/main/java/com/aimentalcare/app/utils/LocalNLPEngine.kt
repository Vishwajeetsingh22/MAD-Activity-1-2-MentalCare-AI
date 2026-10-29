package com.aimentalcare.app.utils

data class NLPAnalysisResult(
    val stressLevel: String, // LOW, MODERATE, HIGH
    val statusDescription: String,
    val stressScore: Int, // 0 - 100
    val confidence: Double,
    val indicators: List<String>,
    val isCrisis: Boolean,
    val recommendations: List<String>
)

data class BurnoutAnalysisResult(
    val burnoutRisk: String, // LOW, MODERATE, HIGH
    val isBurnoutPattern: Boolean,
    val trendMessage: String,
    val averageScore: Double,
    val recommendations: List<String>
)

object LocalNLPEngine {

    private val CRISIS_KEYWORDS = listOf(
        "give up", "giving up", "suicide", "end my life", "harm myself",
        "can't go on", "breakdown", "hopeless", "extreme crisis", "kill myself"
    )

    private val INDICATOR_MAP = mapOf(
        "Work pressure" to listOf("work", "working", "deadline", "boss", "office", "project", "assignment", "tasks", "workload", "meeting", "study"),
        "Poor sleep" to listOf("sleep", "sleeping", "insomnia", "awake", "restless", "night", "tired"),
        "Mental fatigue" to listOf("exhausted", "tired", "drained", "fatigue", "burnt out", "burnout", "no energy", "brain fog", "concentrate")
    )

    fun analyzeText(text: String, sleepHrs: Float? = null, workHrs: Float? = null): NLPAnalysisResult {
        val lowerText = text.lowercase()
        val isCrisis = CRISIS_KEYWORDS.any { lowerText.contains(it) }

        val indicators = mutableListOf<String>()
        INDICATOR_MAP.forEach { (category, keywords) ->
            if (keywords.any { lowerText.contains(it) }) {
                indicators.add(category)
            }
        }
        if (indicators.isEmpty()) {
            indicators.add("General wellness state")
        }

        var score = 32
        var level = "LOW"
        var desc = "Normal wellness state"
        var confidence = 91.5

        if (isCrisis) {
            score = 92
            level = "HIGH"
            desc = "Elevated stress & crisis indicators"
            confidence = 97.8
        } else if (lowerText.contains("exhausted") || lowerText.contains("can't sleep") || lowerText.contains("overwhelmed") || lowerText.contains("burnt out") || lowerText.contains("continuously")) {
            score = 81
            level = "HIGH"
            desc = "High stress indicators detected"
            confidence = 88.5
        } else if (lowerText.contains("worried") || lowerText.contains("rushed") || lowerText.contains("deadline") || lowerText.contains("concentrate")) {
            score = 58
            level = "MODERATE"
            desc = "Moderate stress level"
            confidence = 84.0
        } else if (lowerText.contains("relaxed") || lowerText.contains("calm") || lowerText.contains("happy") || lowerText.contains("good")) {
            score = 25
            level = "LOW"
            desc = "Normal state"
            confidence = 95.0
        } else {
            score = (35 + indicators.size * 12).coerceAtMost(80)
            if (score > 68) {
                level = "HIGH"
                desc = "High stress level"
            } else if (score > 48) {
                level = "MODERATE"
                desc = "Moderate stress level"
            }
        }

        val recommendations = mutableListOf<String>()
        if (isCrisis) {
            recommendations.add("⚠️ High distress indicators detected. Consider reaching out to your pre-configured trusted contact or a healthcare professional.")
            recommendations.add("Take immediate rest away from work duties.")
        } else {
            if (indicators.contains("Poor sleep") || (sleepHrs != null && sleepHrs < 6.5f)) {
                recommendations.add("😴 Sleep Recovery: Practice a 10-minute bedtime wind-down audio and avoid screen time 1 hour before sleep.")
            }
            if (indicators.contains("Work pressure") || (workHrs != null && workHrs > 8.5f)) {
                recommendations.add("⏸️ Workload Break: Consider taking a short 15-minute break and prioritizing essential rest.")
            }
            if (indicators.contains("Mental fatigue")) {
                recommendations.add("🧘 5-Minute Breathing: Try a 4-4-4-4 box breathing exercise to relax your central nervous system.")
            }
            if (recommendations.isEmpty()) {
                recommendations.add("🌿 Wellness Tip: Stay hydrated, take regular stretch breaks, and maintain your balanced daily routine!")
            }
        }

        return NLPAnalysisResult(
            stressLevel = level,
            statusDescription = desc,
            stressScore = score,
            confidence = confidence,
            indicators = indicators,
            isCrisis = isCrisis,
            recommendations = recommendations
        )
    }

    fun analyzeBurnout(historicalScores: List<Int>): BurnoutAnalysisResult {
        if (historicalScores.size < 3) {
            return BurnoutAnalysisResult(
                burnoutRisk = "LOW",
                isBurnoutPattern = false,
                trendMessage = "Monitoring daily trends. Keep logging check-ins to detect multi-day patterns.",
                averageScore = if (historicalScores.isNotEmpty()) historicalScores.average() else 0.0,
                recommendations = listOf("Log at least 3 daily check-ins to detect multi-day burnout trends.")
            )
        }

        val recent = historicalScores.takeLast(5)
        val avg = recent.average()
        var isIncreasing = true
        for (i in 0 until recent.size - 1) {
            if (recent[i] > recent[i + 1]) {
                isIncreasing = false
                break
            }
        }

        val isBurnout = (isIncreasing && recent.size >= 4 && avg > 58.0) || (avg >= 75.0)

        val risk: String
        val msg: String
        val recs = mutableListOf<String>()

        if (isBurnout) {
            risk = "HIGH"
            msg = "⚠️ Increasing Stress Pattern Detected! Your stress levels have risen consistently over recent check-ins (Avg ${avg.toInt()}%). Consider prioritizing rest and speaking with a trusted person."
            recs.add("Schedule dedicated rest hours away from work or study demands.")
            recs.add("Discuss workload management with a manager or academic supervisor.")
            recs.add("Connect with your pre-configured trusted contact or a wellness counselor.")
        } else if (avg > 48.0) {
            risk = "MODERATE"
            msg = "Moderate stress trajectory (Avg ${avg.toInt()}%). Keep up proactive daily wellness breaks."
            recs.add("Ensure consistent 7-8 hours of restful sleep.")
            recs.add("Take short 15-minute relaxation breaks during busy hours.")
        } else {
            risk = "LOW"
            msg = "Healthy stress trajectory (Avg ${avg.toInt()}%). Your emotional wellness pattern is stable."
            recs.add("Maintain your current healthy routine and sleep schedule!")
        }

        return BurnoutAnalysisResult(
            burnoutRisk = risk,
            isBurnoutPattern = isBurnout,
            trendMessage = msg,
            averageScore = avg,
            recommendations = recs
        )
    }
}
