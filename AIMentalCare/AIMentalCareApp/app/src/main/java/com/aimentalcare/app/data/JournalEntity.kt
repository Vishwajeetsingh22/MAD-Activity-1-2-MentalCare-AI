package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true)
    val journalId: Long = 0,
    val userId: String = "default_user",
    val title: String,
    val content: String,
    val moodTag: String = "Neutral",
    val timestamp: Long = System.currentTimeMillis(),
    val aiStressLevel: String? = null,
    val aiAnalyzed: Boolean = false,
    val aiRecommendations: String? = null
)
