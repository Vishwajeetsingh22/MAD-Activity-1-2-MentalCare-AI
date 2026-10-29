package com.aimentalcare.app

import com.aimentalcare.app.utils.AuthenticationEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase: Secure Authentication Flow Unit Tests (Academic Review Parity)
 *
 * Verifies:
 * 1. Multi-stage state routing (Unauthenticated -> Email -> Phone -> Setup -> Ready)
 * 2. Strict Account Setup gating (Dashboard inaccessible until accountSetupCompleted == true)
 * 3. Prevention of duplicate user accounts via Firebase account linking integrity
 * 4. No redundant login after registration + verification
 * 5. Input validation (Email format, phone format, 6-digit OTP format)
 * 6. User-friendly Firebase error message mapping
 */
class AuthenticationFlowTest {

    // =========================================================================
    // 1. STATE ROUTING EVALUATION
    // =========================================================================

    @Test
    fun testUnauthenticatedStateRoutesToLogin() {
        val state = AuthenticationEvaluator.evaluateFlowState(
            isAuthenticated = false,
            isEmailVerified = false,
            isPhoneVerified = false,
            accountSetupCompleted = false
        )
        assertEquals(AuthenticationEvaluator.FlowState.UNAUTHENTICATED, state)
    }

    @Test
    fun testRegisteredUserNeedsEmailVerification() {
        val state = AuthenticationEvaluator.evaluateFlowState(
            isAuthenticated = true,
            isEmailVerified = false,
            isPhoneVerified = false,
            accountSetupCompleted = false
        )
        assertEquals(AuthenticationEvaluator.FlowState.NEEDS_EMAIL_VERIFICATION, state)
    }

    @Test
    fun testEmailVerifiedUserNeedsPhoneVerification() {
        val state = AuthenticationEvaluator.evaluateFlowState(
            isAuthenticated = true,
            isEmailVerified = true,
            isPhoneVerified = false,
            accountSetupCompleted = false
        )
        assertEquals(AuthenticationEvaluator.FlowState.NEEDS_PHONE_VERIFICATION, state)
    }

    @Test
    fun testPhoneVerifiedUserMustCompleteAccountSetup() {
        val state = AuthenticationEvaluator.evaluateFlowState(
            isAuthenticated = true,
            isEmailVerified = true,
            isPhoneVerified = true,
            accountSetupCompleted = false
        )
        assertEquals(AuthenticationEvaluator.FlowState.NEEDS_ACCOUNT_SETUP, state)
    }

    @Test
    fun testCompletedUserRoutesDirectlyToDashboard() {
        val state = AuthenticationEvaluator.evaluateFlowState(
            isAuthenticated = true,
            isEmailVerified = true,
            isPhoneVerified = true,
            accountSetupCompleted = true
        )
        assertEquals(AuthenticationEvaluator.FlowState.AUTHENTICATED_AND_READY, state)
    }

    // =========================================================================
    // 2. DUPLICATE ACCOUNT PREVENTION VIA ACCOUNT LINKING
    // =========================================================================

    @Test
    fun testAccountLinkingPreservesOriginalUserIdentity() {
        val originalUid = "mc_uid_10928374"
        val originalEmail = "joylyn@mentalcare.ai"
        val phoneToLink = "+91 98765 43210"

        // Simulated session after Firebase linkWithCredential(phoneAuthCredential)
        val linkedSession = mapOf<String, Any?>(
            "uid" to originalUid,
            "email" to originalEmail,
            "phoneNumber" to phoneToLink,
            "isEmailVerified" to true,
            "isPhoneVerified" to true,
            "accountSetupCompleted" to false
        )

        val isLinkedCleanly = AuthenticationEvaluator.verifyAccountLinkingIntegrity(
            originalUid = originalUid,
            originalEmail = originalEmail,
            linkedPhone = phoneToLink,
            updatedSession = linkedSession
        )

        assertTrue("Linking must retain original UID and avoid orphan account creation", isLinkedCleanly)
    }

    @Test
    fun testAccountLinkingFailsIfUidChanged() {
        val originalUid = "mc_uid_10928374"
        val originalEmail = "joylyn@mentalcare.ai"
        val phoneToLink = "+91 98765 43210"

        // Defective session where a new orphan UID was created
        val defectiveSession = mapOf<String, Any?>(
            "uid" to "mc_orphan_uid_9999",
            "email" to originalEmail,
            "phoneNumber" to phoneToLink,
            "isEmailVerified" to true,
            "isPhoneVerified" to true,
            "accountSetupCompleted" to false
        )

        val isLinkedCleanly = AuthenticationEvaluator.verifyAccountLinkingIntegrity(
            originalUid = originalUid,
            originalEmail = originalEmail,
            linkedPhone = phoneToLink,
            updatedSession = defectiveSession
        )

        assertFalse("Account linking must fail if UID is replaced with duplicate", isLinkedCleanly)
    }

    // =========================================================================
    // 3. INPUT VALIDATION (EMAIL, PHONE, OTP)
    // =========================================================================

    @Test
    fun testEmailValidation() {
        assertTrue(AuthenticationEvaluator.isValidEmail("user@mentalcare.ai"))
        assertTrue(AuthenticationEvaluator.isValidEmail("joylyn.fernandes@jainuniversity.ac.in"))
        assertFalse(AuthenticationEvaluator.isValidEmail("invalid-email"))
        assertFalse(AuthenticationEvaluator.isValidEmail("@nodomain.com"))
        assertFalse(AuthenticationEvaluator.isValidEmail(""))
        assertFalse(AuthenticationEvaluator.isValidEmail(null))
    }

    @Test
    fun testPhoneNumberValidation() {
        assertTrue(AuthenticationEvaluator.isValidPhoneNumber("+91 98765 43210"))
        assertTrue(AuthenticationEvaluator.isValidPhoneNumber("9876543210"))
        assertTrue(AuthenticationEvaluator.isValidPhoneNumber("+1 (555) 019-2834"))
        assertFalse(AuthenticationEvaluator.isValidPhoneNumber("12345"))
        assertFalse(AuthenticationEvaluator.isValidPhoneNumber(""))
        assertFalse(AuthenticationEvaluator.isValidPhoneNumber(null))
    }

    @Test
    fun testOtpValidation() {
        assertTrue(AuthenticationEvaluator.isValidOtp("123456"))
        assertTrue(AuthenticationEvaluator.isValidOtp("000000"))
        assertFalse(AuthenticationEvaluator.isValidOtp("12345"))   // 5 digits
        assertFalse(AuthenticationEvaluator.isValidOtp("1234567")) // 7 digits
        assertFalse(AuthenticationEvaluator.isValidOtp("12345A"))  // contains letter
        assertFalse(AuthenticationEvaluator.isValidOtp(""))
        assertFalse(AuthenticationEvaluator.isValidOtp(null))
    }

    // =========================================================================
    // 4. FIREBASE ERROR CODE NORMALIZATION
    // =========================================================================

    @Test
    fun testFirebaseErrorNormalization() {
        assertEquals(
            "That email is already registered. Try logging in.",
            AuthenticationEvaluator.mapFirebaseError("email-already-in-use")
        )
        assertEquals(
            "The verification code is incorrect. Please try again.",
            AuthenticationEvaluator.mapFirebaseError("invalid-verification-code")
        )
        assertEquals(
            "Too many attempts. Please wait a moment and try again.",
            AuthenticationEvaluator.mapFirebaseError("too-many-requests")
        )
        assertEquals(
            "This phone number is already linked to another account.",
            AuthenticationEvaluator.mapFirebaseError("credential-already-in-use")
        )
    }
}
