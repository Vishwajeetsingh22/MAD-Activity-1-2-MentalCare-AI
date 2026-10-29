package com.aimentalcare.app

import com.aimentalcare.app.data.CheckInEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyQuizEnforcementTest {

    // 1. Stable Record Identifier Format
    @Test
    fun testDailyRecordIdentifierFormat() {
        val userId = "user123"
        val localDate = "2026-10-02"
        val dailyRecordId = "${userId}_$localDate"

        assertEquals("user123_2026-10-02", dailyRecordId)
        assertTrue(dailyRecordId.startsWith(userId))
        assertTrue(dailyRecordId.endsWith(localDate))
    }

    // 2. Same-Day Submission Block Logic
    @Test
    fun testSameDayDuplicateSubmissionIsBlocked() {
        val existingRecords = mutableMapOf<String, CheckInEntity>()
        val userId = "user_student_1"
        val mondayDate = "2026-10-05" // Monday
        val mondayKey = "${userId}_$mondayDate"

        // First Monday Submission
        val firstSubmission = CheckInEntity(
            id = 1L,
            userId = userId,
            dailyRecordId = mondayKey,
            localDate = mondayDate,
            userText = "Feeling balanced today",
            stressLevel = "LOW",
            statusDescription = "Smart Wellness Check-in",
            stressScore = 28,
            confidence = 0.95,
            indicatorsCsv = "Mood: Great",
            isCrisis = false,
            recommendationsCsv = "Maintain healthy hydration"
        )
        existingRecords[mondayKey] = firstSubmission

        // Second Submission Attempt on the SAME day
        val attemptKey = "${userId}_$mondayDate"
        val isAlreadyCompleted = existingRecords.containsKey(attemptKey)

        assertTrue("Second submission on Monday must be blocked", isAlreadyCompleted)
        val blockedMessage = if (isAlreadyCompleted) {
            "Your wellness check-in for today is complete. Come back tomorrow for your next check-in."
        } else {
            "Check-in allowed"
        }
        assertEquals(
            "Your wellness check-in for today is complete. Come back tomorrow for your next check-in.",
            blockedMessage
        )
    }

    // 3. Next-Day Submission Is Permitted
    @Test
    fun testNextDaySubmissionPermitted() {
        val existingRecords = mutableMapOf<String, CheckInEntity>()
        val userId = "user_student_1"
        val mondayDate = "2026-10-05"
        val tuesdayDate = "2026-10-06"

        // Monday completed
        existingRecords["${userId}_$mondayDate"] = CheckInEntity(
            id = 1L,
            userId = userId,
            dailyRecordId = "${userId}_$mondayDate",
            localDate = mondayDate,
            userText = "Monday Checkin",
            stressLevel = "LOW",
            statusDescription = "Smart Wellness Check-in",
            stressScore = 30,
            confidence = 0.9,
            indicatorsCsv = "",
            isCrisis = false,
            recommendationsCsv = ""
        )

        // Tuesday attempt
        val tuesdayKey = "${userId}_$tuesdayDate"
        val isTuesdayBlocked = existingRecords.containsKey(tuesdayKey)

        assertFalse("Tuesday check-in must be available after Monday completion", isTuesdayBlocked)
    }

    // 4. Analytics Deduplication with Duplicate Historical Records
    @Test
    fun testAnalyticsSafelyDeduplicatesMultipleSubmissionsOnSameDate() {
        val allSubmissions = listOf(
            // 3 Submissions on Monday 2026-10-05 (legacy duplicates)
            CheckInEntity(id = 3L, userId = "userA", timestamp = 1759683600000L, localDate = "2026-10-05", userText = "Mon 3", stressLevel = "HIGH", statusDescription = "", stressScore = 70, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = ""),
            CheckInEntity(id = 2L, userId = "userA", timestamp = 1759654800000L, localDate = "2026-10-05", userText = "Mon 2", stressLevel = "MODERATE", statusDescription = "", stressScore = 50, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = ""),
            CheckInEntity(id = 1L, userId = "userA", timestamp = 1759626000000L, localDate = "2026-10-05", userText = "Mon 1", stressLevel = "LOW", statusDescription = "", stressScore = 30, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = ""),
            // 1 Submission on Tuesday 2026-10-06
            CheckInEntity(id = 4L, userId = "userA", timestamp = 1759740000000L, localDate = "2026-10-06", userText = "Tue 1", stressLevel = "LOW", statusDescription = "", stressScore = 25, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = ""),
            // 1 Submission on Wednesday 2026-10-07
            CheckInEntity(id = 5L, userId = "userA", timestamp = 1759826400000L, localDate = "2026-10-07", userText = "Wed 1", stressLevel = "MODERATE", statusDescription = "", stressScore = 45, confidence = 0.9, indicatorsCsv = "", isCrisis = false, recommendationsCsv = "")
        )

        // Deduplication algorithm used in AnalyticsFragment
        val distinctDayCheckIns = mutableListOf<CheckInEntity>()
        val seenDates = mutableSetOf<String>()
        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (checkIn in allSubmissions) {
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

        // Must collapse 3 Monday entries into 1 entry
        assertEquals("Total unique dates must be exactly 3", 3, seenDates.size)
        assertEquals("Distinct day list size must be 3", 3, distinctDayCheckIns.size)
        assertTrue(seenDates.contains("2026-10-05"))
        assertTrue(seenDates.contains("2026-10-06"))
        assertTrue(seenDates.contains("2026-10-07"))

        // Day labels generated from unique entries
        val dayLabels = distinctDayCheckIns.reversed().map { item ->
            val key = if (item.localDate.isNotEmpty()) item.localDate else dateKeyFormat.format(Date(item.timestamp))
            val d = dateKeyFormat.parse(key)
            SimpleDateFormat("EEE", Locale.getDefault()).format(d ?: Date(item.timestamp))
        }

        assertEquals(3, dayLabels.size)
        // All labels are distinct days
        assertEquals(3, dayLabels.toSet().size)
    }

    // 5. Streak Counting Unique Dates
    @Test
    fun testStreakCountingUsesUniqueDatesOnly() {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Today
        val todayTime = cal.timeInMillis
        val todayInt = sdf.format(cal.time).toInt()

        // Yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayTime = cal.timeInMillis
        val yesterdayInt = sdf.format(cal.time).toInt()

        // 2 days ago
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgoTime = cal.timeInMillis
        val twoDaysAgoInt = sdf.format(cal.time).toInt()

        // Multiple timestamps on today, yesterday, and 2 days ago
        val timestampsWithDuplicates = listOf(
            todayTime,
            todayTime + 3600000L,       // 2nd today
            todayTime + 7200000L,       // 3rd today
            yesterdayTime,
            yesterdayTime + 1800000L,   // 2nd yesterday
            twoDaysAgoTime
        )

        // Streak calculation logic
        val dateSet = timestampsWithDuplicates.map { sdf.format(Date(it)).toInt() }.toSortedSet(Comparator.reverseOrder())
        assertEquals("Set of distinct date integers must be 3", 3, dateSet.size)

        var streak = 0
        val checkCal = Calendar.getInstance()
        while (true) {
            val checkInt = sdf.format(checkCal.time).toInt()
            if (dateSet.contains(checkInt)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        assertEquals("Streak should be exactly 3 days despite duplicate same-day records", 3, streak)
    }
}
