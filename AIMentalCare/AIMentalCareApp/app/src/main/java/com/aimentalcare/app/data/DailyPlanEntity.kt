package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_plan_entries")
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val planId: Long = 0,
    val userId: String = "default_user",
    val dateString: String, // e.g. "2026-09-29"
    val checkInDone: Boolean = false,
    val breathingDone: Boolean = false,
    val journalDone: Boolean = false,
    val breakDone: Boolean = false,
    val reflectionDone: Boolean = false
) {
    fun completedCount(): Int {
        var count = 0
        if (checkInDone) count++
        if (breathingDone) count++
        if (journalDone) count++
        if (breakDone) count++
        if (reflectionDone) count++
        return count
    }

    fun totalCount(): Int = 5
}
