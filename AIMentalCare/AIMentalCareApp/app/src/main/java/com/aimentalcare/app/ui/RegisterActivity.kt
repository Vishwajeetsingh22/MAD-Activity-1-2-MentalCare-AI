package com.aimentalcare.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.data.UserEntity
import com.aimentalcare.app.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val pass = binding.etRegPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.register_empty_error, Toast.LENGTH_SHORT).show()
            } else {
                val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("user_email", email)
                    .putString("user_name", name)
                    .putBoolean("account_setup_completed", false)
                    .putBoolean("onboarding_completed", false)
                    .apply()

                // Save user record to local database
                val db = MentalCareDatabase.getDatabase(applicationContext)
                lifecycleScope.launch(Dispatchers.IO) {
                    db.checkInDao().insertOrUpdateUser(
                        UserEntity(
                            userId = email,
                            fullName = name,
                            email = email,
                            accountSetupCompleted = false
                        )
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, OnboardingActivity::class.java).apply {
                            putExtra("IS_FIRST_TIME_REGISTRATION", true)
                            putExtra("REGISTERED_NAME", name)
                            putExtra("REGISTERED_EMAIL", email)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        binding.txtLoginLink.setOnClickListener {
            finish()
        }
    }
}
