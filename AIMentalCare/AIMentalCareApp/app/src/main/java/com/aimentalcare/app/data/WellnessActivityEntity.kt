package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_activities")
data class WellnessActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val activityId: Long = 0,
    val userId: String = "default_user",
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)
