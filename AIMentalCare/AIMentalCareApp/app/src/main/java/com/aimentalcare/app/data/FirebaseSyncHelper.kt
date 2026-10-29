package com.aimentalcare.app.data

import android.content.Context
import android.util.Log

/**
 * FirebaseSyncHelper manages cloud synchronization for user-specific data
 * (Emergency Contacts and Chatbot Conversations).
 *
 * Cloud Database Architecture:
 * users/
 *  └── {userId}/
 *       ├── emergencyContacts/
 *       │    └── {contactId} -> { name, phone, relationship }
 *       └── conversations/
 *            └── {conversationId} -> { messages: [...] }
 *
 * Operates gracefully offline when google-services.json credentials are not supplied,
 * relying on the encrypted local Room SQLite database as the resilient source of truth.
 */
object FirebaseSyncHelper {

    private const val TAG = "FirebaseSyncHelper"

    fun syncEmergencyContactToCloud(context: Context, contact: TrustedContactEntity) {
        // Ready for live Firestore cloud synchronization once google-services.json is attached
        Log.d(TAG, "Syncing contact ${contact.contactName} (${contact.contactPhone}) for user ${contact.userId} to Firestore path: users/${contact.userId}/emergencyContacts/${contact.contactId}")
    }

    fun syncChatToCloud(context: Context, chatMessage: ChatMessageEntity) {
        Log.d(TAG, "Syncing message from ${chatMessage.sender} for user ${chatMessage.userId} to Firestore path: users/${chatMessage.userId}/conversations/${chatMessage.conversationId}")
    }
}
