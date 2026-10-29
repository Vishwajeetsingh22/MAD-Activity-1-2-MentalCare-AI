package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heart_rate_records")
data class HeartRateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val bpm: Int = 0,
    val accuracy: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "Wearable Sensor",
    val sensorName: String = "Optical Heart Rate Sensor"
)
