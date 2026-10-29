package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lifestyle_logs")
data class LifestyleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sleepHrs: Float,
    val workHrs: Float,
    val screenHrs: Float,
    val exerciseMins: Int,
    val energyLevel: String, // Low, Medium, High
    val moodEmoji: String, // 😊, 😐, 😔, 😫
    val stressScore: Int
)
