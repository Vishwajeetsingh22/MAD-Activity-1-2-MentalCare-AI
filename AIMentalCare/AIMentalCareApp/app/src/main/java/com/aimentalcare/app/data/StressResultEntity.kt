package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StressResults")
data class StressResultEntity(
    @PrimaryKey(autoGenerate = true)
    val resultId: Long = 0,
    val userId: String = "user_default",
    val timestamp: Long = System.currentTimeMillis(),
    val stressLevel: String, // LOW, MODERATE, HIGH
    val stressScorePercent: Int, // 0 - 100
    val confidenceScore: Double,
    val indicatorsCsv: String,
    val recommendation: String,
    val isBurnoutPattern: Boolean = false
)
