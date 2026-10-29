package com.aimentalcare.app.utils

/**
 * MentalCare AI — Authentication State & Flow Evaluator
 *
 * Implements validation rules, state routing evaluation, and account linking
 * parity checks for the secure multi-stage authentication system.
 */
object AuthenticationEvaluator {

    enum class FlowState {
        UNAUTHENTICATED,
        NEEDS_EMAIL_VERIFICATION,
        NEEDS_PHONE_VERIFICATION,
        NEEDS_ACCOUNT_SETUP,
        AUTHENTICATED_AND_READY
    }

    /**
     * Determines user destination screen based on true verification flags.
     */
    fun evaluateFlowState(
        isAuthenticated: Boolean,
        isEmailVerified: Boolean,
        isPhoneVerified: Boolean,
        accountSetupCompleted: Boolean
    ): FlowState {
        if (!isAuthenticated) return FlowState.UNAUTHENTICATED
        if (!isEmailVerified) return FlowState.NEEDS_EMAIL_VERIFICATION
        if (!isPhoneVerified) return FlowState.NEEDS_PHONE_VERIFICATION
        if (!accountSetupCompleted) return FlowState.NEEDS_ACCOUNT_SETUP
        return FlowState.AUTHENTICATED_AND_READY
    }

    /**
     * Validates email address format
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$".toRegex()
        return regex.matches(email.trim())
    }

    /**
     * Validates international mobile number (10 to 15 digits)
     */
    fun isValidPhoneNumber(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return false
        val cleanDigits = phone.replace("[^0-9]".toRegex(), "")
        return cleanDigits.length in 10..15
    }

    /**
     * Validates SMS OTP (strictly 6 numeric digits)
     */
    fun isValidOtp(otp: String?): Boolean {
        if (otp == null) return false
        return otp.trim().length == 6 && otp.all { it.isDigit() }
    }

    /**
     * Account linking safety verification:
     * Ensures linking a phone credential preserves the active user's UID and email,
     * preventing disjoint or duplicate user accounts.
     */
    fun verifyAccountLinkingIntegrity(
        originalUid: String,
        originalEmail: String,
        linkedPhone: String,
        updatedSession: Map<String, Any?>
    ): Boolean {
        val sessionUid = updatedSession["uid"] as? String
        val sessionEmail = updatedSession["email"] as? String
        val sessionPhone = updatedSession["phoneNumber"] as? String
        val isPhoneVerified = updatedSession["isPhoneVerified"] as? Boolean ?: false

        return sessionUid == originalUid &&
               sessionEmail == originalEmail &&
               sessionPhone == linkedPhone &&
               isPhoneVerified
    }

    /**
     * Maps Firebase error codes to user-friendly messages
     */
    fun mapFirebaseError(errorCode: String): String {
        return when (errorCode.lowercase()) {
            "email-already-in-use" -> "That email is already registered. Try logging in."
            "invalid-email" -> "Please enter a valid email address."
            "weak-password" -> "Password must be at least 6 characters long."
            "invalid-phone-number" -> "Please enter a valid mobile number with country code."
            "invalid-verification-code" -> "The verification code is incorrect. Please try again."
            "session-expired" -> "The verification code has expired. Please request a new one."
            "too-many-requests" -> "Too many attempts. Please wait a moment and try again."
            "network-request-failed" -> "We couldn't connect right now. Check your internet connection."
            "credential-already-in-use" -> "This phone number is already linked to another account."
            else -> "Authentication failed. Please verify your details."
        }
    }
}
