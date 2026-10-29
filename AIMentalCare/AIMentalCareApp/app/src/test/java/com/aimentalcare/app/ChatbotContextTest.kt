package com.aimentalcare.app

import com.aimentalcare.app.utils.LocalChatEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatbotContextTest {

    private val convId1 = "test_conv_user1_session_1"
    private val convId2 = "test_conv_user2_session_2"

    @Before
    fun setup() {
        LocalChatEngine.clearSession(convId1)
        LocalChatEngine.clearSession(convId2)
    }

    // 1. Root Cause Fix: User says "Yes ask me questions" after stress disclosure
    // Previously, the bot repeated the canned fallback greeting. Now it must ask guided questions!
    @Test
    fun testRootCauseNoRepetitionOnAffirmation() {
        // Turn 1: User discloses stress
        val turn1 = LocalChatEngine.respond("I am feeling very stressed lately", convId1, "MODERATE")
        assertFalse("Turn 1 should not be crisis", turn1.isCrisis)
        assertTrue("Turn 1 should acknowledge stress and offer support", 
            turn1.reply.contains("stress", ignoreCase = true) || turn1.reply.contains("wellness", ignoreCase = true))

        // Turn 2: User responds "Yes ask me questions"
        val turn2 = LocalChatEngine.respond("Yes ask me questions", convId1, "MODERATE")
        
        // Assert: It must NOT repeat the exact same response as Turn 1
        assertNotEquals("Turn 2 must NOT repeat Turn 1 canned response", turn1.reply, turn2.reply)
        // Assert: It should ask clarifying guided questions
        assertTrue("Turn 2 must ask clarifying questions", 
            turn2.reply.contains("pressure", ignoreCase = true) || 
            turn2.reply.contains("source", ignoreCase = true) || 
            turn2.reply.contains("weighing", ignoreCase = true) || 
            turn2.reply.contains("mind", ignoreCase = true) ||
            turn2.reply.contains("question", ignoreCase = true))
    }

    // 2. Entity Extraction and Context Recall
    @Test
    fun testEntityExtractionAndContextRecall() {
        // Turn 1: Mention exams
        LocalChatEngine.respond("I have upcoming mathematics exams and I feel overwhelmed", convId1, "HIGH")

        // Turn 2: Context recall query
        val turn2 = LocalChatEngine.respond("What did I tell you about my exams?", convId1, "HIGH")

        // Assert: Bot recalls the exam mention
        assertTrue("Bot must recall exams in context", turn2.reply.contains("exam", ignoreCase = true))
        assertFalse("Recall is not crisis", turn2.isCrisis)
    }

    // 3. Quick Prompt: Guided Breathing Relaxation
    @Test
    fun testQuickPromptHelpMeRelax() {
        val result = LocalChatEngine.respond("Can you guide me through a calming breathing exercise to help me relax?", convId1, "LOW")
        assertTrue("Should provide breathing or relaxation technique", 
            result.reply.contains("breath", ignoreCase = true) || result.reply.contains("4-7-8", ignoreCase = true) || result.reply.contains("calm", ignoreCase = true))
        assertFalse(result.isCrisis)
    }

    // 4. Quick Prompt: Tell me about my recent check-ins
    @Test
    fun testQuickPromptRecentCheckIns() {
        val result = LocalChatEngine.respond("Tell me about my recent check-ins and how my stress has been tracking.", convId1, "MODERATE")
        assertTrue("Should include stress level context", 
            result.reply.contains("check-in", ignoreCase = true) || result.reply.contains("MODERATE", ignoreCase = true))
        assertFalse(result.isCrisis)
    }

    // 5. Crisis Trigger & Safety Protocol
    @Test
    fun testCrisisDetectionAndEmergencySupport() {
        val result = LocalChatEngine.respond("I want to end my life, everything is hopeless", convId1, "HIGH")
        assertTrue("Crisis flag must be true", result.isCrisis)
        assertTrue("Must include crisis lifeline 988 or 14416 or 112", 
            result.reply.contains("988") || result.reply.contains("14416") || result.reply.contains("112"))
    }

    // 6. Session Isolation between Users / Conversations
    @Test
    fun testSessionIsolation() {
        // User 1 talks about exams
        LocalChatEngine.respond("I have biology exams", convId1, "LOW")

        // User 2 in a different conversation asks recall
        val user2Result = LocalChatEngine.respond("What did I tell you about my exams?", convId2, "LOW")

        // User 2 should NOT inherit User 1's exams entity
        assertFalse("User 2 must not see User 1's biology exam entity", user2Result.reply.contains("biology", ignoreCase = true))
    }
}
