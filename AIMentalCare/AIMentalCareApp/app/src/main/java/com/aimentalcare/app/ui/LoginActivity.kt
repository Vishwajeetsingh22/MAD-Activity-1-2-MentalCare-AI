package com.aimentalcare.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aimentalcare.app.R
import com.aimentalcare.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.login_empty_error, Toast.LENGTH_SHORT).show()
            } else {
                val userName = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
                prefs.edit().putString("user_email", email).putString("user_name", userName).apply()

                val setupCompleted = prefs.getBoolean("account_setup_completed", false) || prefs.getBoolean("onboarding_completed", false)
                Toast.makeText(this, getString(R.string.welcome_back_format, userName), Toast.LENGTH_SHORT).show()
                if (setupCompleted) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    val intent = Intent(this, OnboardingActivity::class.java).apply {
                        putExtra("IS_FIRST_TIME_REGISTRATION", true)
                    }
                    startActivity(intent)
                }
                finish()
            }
        }

        binding.txtRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
