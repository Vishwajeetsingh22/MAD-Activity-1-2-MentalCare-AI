package com.aimentalcare.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TrustedContacts")
data class TrustedContactEntity(
    @PrimaryKey(autoGenerate = true)
    val contactId: Long = 0,
    val userId: String = "user_default",
    val contactName: String,
    val contactPhone: String,
    val relationship: String
)
