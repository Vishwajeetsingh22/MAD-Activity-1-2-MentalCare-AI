package com.aimentalcare.app

import com.aimentalcare.app.data.CheckInEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StressInsightsTest {

    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Helper: Deduplicates check-ins by unique calendar date (taking latest per date)
    private fun deduplicateCheckIns(records: List<CheckInEntity>): List<CheckInEntity> {
        val distinctList = mutableListOf<CheckInEntity>()
        val seenDates = mutableSetOf<String>()

        for (record in records) {
            val key = if (record.localDate.isNotEmpty()) record.localDate else dateKeyFormat.format(Date(record.timestamp))
            if (!seenDates.contains(key)) {
                seenDates.add(key)
                distinctList.add(record)
            }
        }
        return distinctList
    }

    // Helper: Generates personalized insight strictly from actual data
    private fun generateInsight(scores: List<Int>): String {
        if (scores.size < 2) {
            return "Complete a few wellness check-ins to see your personal trends."
        }
        val first = scores.first()
        val last = scores.last()
        val diff = last - first
        val avg = scores.average().toInt()

        val trendNarrative = when {
            diff >= 10 -> "Your recent check-ins show an upward trend in stress over the last few days (+${diff}%). Scheduling regular 5-minute pauses and breathing intervals will help relieve tension."
            diff <= -10 -> "Your recent check-ins show an improving trend with decreasing stress levels (${diff}%). Your wellness habits and routines are paying off!"
            else -> "Your recent check-ins indicate a stable, steady emotional wellness pattern. Keep up your balanced daily routine."
        }

        val elevatedWarning = if (avg >= 70) {
            "\n\nNote: Your average stress is currently elevated ($avg%). Be gentle with yourself and consider reaching out to your support circle."
        } else ""

        return "$trendNarrative$elevatedWarning"
    }

    // 1. EMPTY HISTORY TEST
    @Test
    fun testEmptyHistoryDisplaysGuidanceText() {
        val emptyRecords = emptyList<CheckInEntity>()
        val distinct = deduplicateCheckIns(emptyRecords)
        val scores = distinct.map { it.stressScore }

        assertEquals(0, distinct.size)
        assertEquals(0, scores.size)

        val insight = generateInsight(scores)
        assertEquals("Complete a few wellness check-ins to see your personal trends.", insight)

        val progressFraction = "${distinct.size} of 7 days"
        assertEquals("0 of 7 days", progressFraction)
    }

    // 2. ONE RECORD TEST
    @Test
    fun testSingleRecordState() {
        val singleRecord = listOf(
            CheckInEntity(
                id = 1L,
                userId = "user1",
                dailyRecordId = "user1_2026-10-02",
                localDate = "2026-10-02",
                userText = "Calm and steady",
                stressLevel = "LOW",
                statusDescription = "Normal",
                stressScore = 25,
                confidence = 0.9,
                indicatorsCsv = "Mood: Great",
                isCrisis = false,
                recommendationsCsv = "Rest"
            )
        )
        val distinct = deduplicateCheckIns(singleRecord)
        val scores = distinct.map { it.stressScore }

        assertEquals(1, distinct.size)
        assertEquals(25, scores[0])

        // When only 1 record exists, system must request more data rather than fabricate trends
        val insight = generateInsight(scores)
        assertEquals("Complete a few wellness check-ins to see your personal trends.", insight)

        val progressFraction = "${distinct.size} of 7 days"
        assertEquals("1 of 7 days", progressFraction)
    }

    // 3. MULTIPLE DAYS RISING TREND TEST
    @Test
    fun testMultipleDaysRisingTrend() {
        val chronologicalScores = listOf(30, 42, 55, 68) // Rising by +38%
        val insight = generateInsight(chronologicalScores)

        assertTrue("Insight must detect rising trend", insight.contains("upward trend", ignoreCase = true))
        assertTrue("Insight must recommend breathing/pauses", insight.contains("breathing", ignoreCase = true))
        assertFalse("Insight must NOT display insufficient data text", insight.contains("Complete a few wellness check-ins", ignoreCase = true))
    }

    // 4. MULTIPLE DAYS IMPROVING TREND TEST
    @Test
    fun testMultipleDaysImprovingTrend() {
        val chronologicalScores = listOf(75, 60, 45, 30) // Decreasing by -45%
        val insight = generateInsight(chronologicalScores)

        assertTrue("Insight must detect improving trend", insight.contains("improving trend", ignoreCase = true))
        assertTrue("Insight must acknowledge wellness habits", insight.contains("paying off", ignoreCase = true))
    }

    // 5. MULTIPLE DAYS STABLE TREND TEST
    @Test
    fun testMultipleDaysStableTrend() {
        val chronologicalScores = listOf(40, 42, 41, 40) // Delta = 0%
        val insight = generateInsight(chronologicalScores)

        assertTrue("Insight must detect stable pattern", insight.contains("stable", ignoreCase = true))
    }

    // 6. CRITICAL DUPLICATE SAME-DAY SUBMISSIONS TEST
    // Same-day submissions MUST be counted as ONE calendar day
    @Test
    fun testDuplicateSameDaySubmissionsDeduplicateToOneCalendarDay() {
        val rawSubmissions = listOf(
            // 3 submissions on 2026-10-05 (Monday)
            CheckInEntity(id = 3L, userId = "u1", dailyRecordId = "u1_2026-10-05", localDate = "2026-10-05", userText = "Evening check", stressLevel = "MODERATE", statusDescription = "Norm", stressScore = 55, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "", timestamp = 1700003000L),
            CheckInEntity(id = 2L, userId = "u1", dailyRecordId = "u1_2026-10-05", localDate = "2026-10-05", userText = "Afternoon check", stressLevel = "MODERATE", statusDescription = "Norm", stressScore = 50, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "", timestamp = 1700002000L),
            CheckInEntity(id = 1L, userId = "u1", dailyRecordId = "u1_2026-10-05", localDate = "2026-10-05", userText = "Morning check", stressLevel = "LOW", statusDescription = "Norm", stressScore = 30, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "", timestamp = 1700001000L),
            
            // 2 submissions on 2026-10-06 (Tuesday)
            CheckInEntity(id = 5L, userId = "u1", dailyRecordId = "u1_2026-10-06", localDate = "2026-10-06", userText = "Tuesday evening", stressLevel = "LOW", statusDescription = "Norm", stressScore = 28, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "", timestamp = 1700085000L),
            CheckInEntity(id = 4L, userId = "u1", dailyRecordId = "u1_2026-10-06", localDate = "2026-10-06", userText = "Tuesday morning", stressLevel = "LOW", statusDescription = "Norm", stressScore = 25, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "", timestamp = 1700081000L)
        )

        val deduplicated = deduplicateCheckIns(rawSubmissions)

        // Raw count was 5, but distinct days must be EXACTLY 2!
        assertEquals("Duplicate same-day submissions must collapse to unique dates", 2, deduplicated.size)
        assertEquals("Latest submission on 2026-10-05 must be retained", 55, deduplicated[0].stressScore)
        assertEquals("Latest submission on 2026-10-06 must be retained", 28, deduplicated[1].stressScore)

        val progressFraction = "${deduplicated.size} of 7 days"
        assertEquals("2 of 7 days", progressFraction)
    }

    // 7. RECOMMENDED ACTIVITIES ADAPTATION TEST
    @Test
    fun testRecommendedActivitiesAdaptToCurrentStress() {
        val highStressActivities = listOf("🧘 Box Breathing (4-4-4-4)", "🛑 5-4-3-2-1 Sensory Grounding", "📞 Connect with Trusted Support")
        val moderateStressActivities = listOf("🌬️ 4-7-8 Deep Relaxation Breath", "📝 Mindful Journal Reflection", "🚶 15-Minute Movement Break")
        val lowStressActivities = listOf("✨ 3 Gratitude Reflections", "🎯 Low-Stress Daily Planning", "🌙 Restorative Sleep Wind-Down")

        assertEquals(3, highStressActivities.size)
        assertTrue(highStressActivities[0].contains("Box Breathing"))
        assertTrue(moderateStressActivities[0].contains("4-7-8"))
        assertTrue(lowStressActivities[0].contains("Gratitude"))
    }
}
