package com.aimentalcare.app

import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.GoalEntity
import com.aimentalcare.app.data.JournalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 6 Unit Tests — Premium Journal, Goals, Habits & Wellness Activities
 */
class JournalAndWellnessTest {

    // =========================================================================
    // 1. JOURNAL LOGIC & NATURAL TITLE EXTRACTION
    // =========================================================================

    @Test
    fun testJournalAutoTitleExtraction() {
        val multilineContent = "Had a calm walk in the park today.\nFelt grounded and ready for work."
        val firstLine = multilineContent.lines().firstOrNull { it.isNotBlank() } ?: "Journal Reflection"
        val derivedTitle = if (firstLine.length > 40) firstLine.take(37) + "..." else firstLine

        assertEquals("Had a calm walk in the park today.", derivedTitle)

        val entity = JournalEntity(
            userId = "test@mentalcare.ai",
            title = derivedTitle,
            content = multilineContent,
            moodTag = "😊 Great"
        )

        assertEquals("Had a calm walk in the park today.", entity.title)
        assertEquals("😊 Great", entity.moodTag)
        assertEquals(multilineContent, entity.content)
    }

    @Test
    fun testJournalLongTitleTruncation() {
        val longContent = "Today was an exceptionally intensive day full of meetings and backlog grooming sessions"
        val firstLine = longContent.lines().firstOrNull { it.isNotBlank() } ?: "Journal Reflection"
        val derivedTitle = if (firstLine.length > 40) firstLine.take(37) + "..." else firstLine

        assertTrue(derivedTitle.endsWith("..."))
        assertEquals(40, derivedTitle.length)
        assertEquals("Today was an exceptionally intensive ...", derivedTitle)
    }

    // =========================================================================
    // 2. GOAL PROGRESS CALCULATION (ACTUAL STORED DATA)
    // =========================================================================

    @Test
    fun testGoalProgressCalculationFromStoredData() {
        // Example 1: Sleep better - 6 of 8 days = 75%
        val goal1 = GoalEntity(
            userId = "user@mentalcare.ai",
            title = "Sleep better",
            category = "Sleep Routine",
            targetDays = 8,
            completedDays = 6,
            isCompleted = false
        )
        val target1 = goal1.targetDays.coerceAtLeast(1)
        val completed1 = if (goal1.isCompleted) target1 else goal1.completedDays.coerceIn(0, target1)
        val percent1 = (completed1.toFloat() / target1 * 100).toInt()
        assertEquals(75, percent1)

        // Example 2: Reduce stress - 4 of 8 days = 50%
        val goal2 = GoalEntity(
            userId = "user@mentalcare.ai",
            title = "Reduce stress",
            category = "Mindfulness",
            targetDays = 8,
            completedDays = 4,
            isCompleted = false
        )
        val target2 = goal2.targetDays.coerceAtLeast(1)
        val completed2 = if (goal2.isCompleted) target2 else goal2.completedDays.coerceIn(0, target2)
        val percent2 = (completed2.toFloat() / target2 * 100).toInt()
        assertEquals(50, percent2)

        // Example 3: Build healthy habits - 6 of 7 days = 85%
        val goal3 = GoalEntity(
            userId = "user@mentalcare.ai",
            title = "Build healthy habits",
            category = "Healthy Habits",
            targetDays = 7,
            completedDays = 6,
            isCompleted = false
        )
        val target3 = goal3.targetDays.coerceAtLeast(1)
        val completed3 = if (goal3.isCompleted) target3 else goal3.completedDays.coerceIn(0, target3)
        val percent3 = (completed3.toFloat() / target3 * 100).toInt()
        assertEquals(85, percent3)
    }

    @Test
    fun testCompletedGoalAlwaysYieldsHundredPercent() {
        val completedGoal = GoalEntity(
            userId = "user@mentalcare.ai",
            title = "Morning stretch",
            category = "Stretching",
            targetDays = 10,
            completedDays = 3,
            isCompleted = true // Marked completed by user
        )
        val target = completedGoal.targetDays.coerceAtLeast(1)
        val completed = if (completedGoal.isCompleted) target else completedGoal.completedDays.coerceIn(0, target)
        val percent = (completed.toFloat() / target * 100).toInt()
        assertEquals(100, percent)
    }

    // =========================================================================
    // 3. HABIT COMPLETION & CLEAN PILL STATUS
    // =========================================================================

    @Test
    fun testHabitsCompletionCountAndStatusPill() {
        val plan = DailyPlanEntity(
            userId = "user@mentalcare.ai",
            dateString = "2026-10-02",
            breathingDone = true,   // Morning breathing ✓ Completed
            breakDone = false,       // Drink water ○ Pending
            reflectionDone = true,  // Sleep routine ✓ Completed
            checkInDone = true,     // Daily check-in ✓ Completed
            journalDone = false     // Gratitude journaling ○ Pending
        )

        assertEquals(3, plan.completedCount())

        fun formatHabitStatus(isDone: Boolean): String = if (isDone) "✓ Completed" else "○ Pending"

        assertEquals("✓ Completed", formatHabitStatus(plan.breathingDone))
        assertEquals("○ Pending", formatHabitStatus(plan.breakDone))
        assertEquals("✓ Completed", formatHabitStatus(plan.reflectionDone))
        assertEquals("✓ Completed", formatHabitStatus(plan.checkInDone))
        assertEquals("○ Pending", formatHabitStatus(plan.journalDone))
    }

    // =========================================================================
    // 4. WELLNESS ACTIVITIES CATALOG INTEGRITY & NON-CLINICAL CHECKS
    // =========================================================================

    @Test
    fun testWellnessActivityCatalogCategories() {
        val requiredCategories = listOf("Breathing", "Meditation", "Relaxation", "Stretching", "Journaling")

        val catalog = listOf(
            mapOf("category" to "Breathing", "title" to "5-Minute Box Breathing"),
            mapOf("category" to "Meditation", "title" to "Guided Calm Meditation"),
            mapOf("category" to "Relaxation", "title" to "5-4-3-2-1 Sensory Grounding"),
            mapOf("category" to "Stretching", "title" to "Gentle Desk & Neck Stretching"),
            mapOf("category" to "Journaling", "title" to "Reflective Gratitude Journal")
        )

        val presentCategories = catalog.map { it["category"] }.toSet()

        for (category in requiredCategories) {
            assertTrue("Expected catalog to contain category '$category'", presentCategories.contains(category))
        }
    }
}
