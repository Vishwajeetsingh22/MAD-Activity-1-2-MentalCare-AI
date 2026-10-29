package com.aimentalcare.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.aimentalcare.app.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
            val userEmail = prefs.getString("user_email", null)
            val setupCompleted = prefs.getBoolean("account_setup_completed", false) || prefs.getBoolean("onboarding_completed", false)

            if (userEmail.isNullOrEmpty()) {
                // Not authenticated → Login
                startActivity(Intent(this, LoginActivity::class.java))
            } else if (!setupCompleted) {
                // Authenticated and setup incomplete → Account Setup
                val intent = Intent(this, OnboardingActivity::class.java).apply {
                    putExtra("IS_FIRST_TIME_REGISTRATION", true)
                }
                startActivity(intent)
            } else {
                // Authenticated and setup complete → Main Dashboard
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 1500)
    }
}
