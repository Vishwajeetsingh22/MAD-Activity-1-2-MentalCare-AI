package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val goalId: Long = 0,
    val userId: String = "default_user",
    val title: String,
    val category: String = "General",
    val targetDays: Int = 7,
    val completedDays: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
