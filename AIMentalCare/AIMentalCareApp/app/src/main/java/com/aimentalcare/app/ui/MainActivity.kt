package com.aimentalcare.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aimentalcare.app.R
import com.aimentalcare.app.databinding.ActivityMainBinding
import com.aimentalcare.app.utils.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTabId: Int = R.id.nav_dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("MentalCarePrefs", MODE_PRIVATE)
        val userEmail = prefs.getString("user_email", null)
        val setupCompleted = prefs.getBoolean("account_setup_completed", false) || prefs.getBoolean("onboarding_completed", false)

        if (userEmail.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else if (!setupCompleted) {
            val intent = Intent(this, OnboardingActivity::class.java).apply {
                putExtra("IS_FIRST_TIME_REGISTRATION", true)
            }
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Android 13+ Notification Runtime Permission Request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        binding.fabAiAssistant.setOnClickListener {
            replaceFragment(ChatbotFragment(), addToBackStack = true)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            binding.fabAiAssistant.visibility = if (current is ChatbotFragment) android.view.View.GONE else android.view.View.VISIBLE
        }

        val navTarget = intent?.getStringExtra(NotificationHelper.EXTRA_NAV_TARGET)
        if (NotificationHelper.TARGET_DAILY_CHECKIN == navTarget) {
            currentTabId = R.id.nav_checkin
            binding.bottomNavigation.selectedItemId = R.id.nav_checkin
            replaceFragment(DailyCheckInFragment(), addToBackStack = true)
        } else if (savedInstanceState == null) {
            replaceFragment(DashboardFragment(), addToBackStack = false)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId != currentTabId) {
                currentTabId = item.itemId
                when (item.itemId) {
                    R.id.nav_dashboard -> replaceFragment(DashboardFragment(), addToBackStack = false)
                    R.id.nav_checkin -> replaceFragment(DailyCheckInFragment(), addToBackStack = true)
                    R.id.nav_analytics -> replaceFragment(AnalyticsFragment(), addToBackStack = true)
                    R.id.nav_lifestyle -> replaceFragment(LifestyleFragment(), addToBackStack = true)
                    R.id.nav_support -> replaceFragment(SupportFragment(), addToBackStack = true)
                }
            }
            true
        }

        binding.btnNavProfile.setOnClickListener {
            replaceFragment(ProfileFragment(), addToBackStack = true)
        }

        binding.btnNavAbout.setOnClickListener {
            replaceFragment(AboutFragment(), addToBackStack = true)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }

    fun navigateToTab(tabId: Int) {
        binding.bottomNavigation.selectedItemId = tabId
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val navTarget = intent?.getStringExtra(NotificationHelper.EXTRA_NAV_TARGET)
        if (NotificationHelper.TARGET_DAILY_CHECKIN == navTarget) {
            currentTabId = R.id.nav_checkin
            binding.bottomNavigation.selectedItemId = R.id.nav_checkin
            replaceFragment(DailyCheckInFragment(), addToBackStack = true)
        }
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        if (isFinishing || isDestroyed) return
        try {
            binding.fabAiAssistant.visibility = if (fragment is ChatbotFragment) android.view.View.GONE else android.view.View.VISIBLE
            val tx = supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
            if (addToBackStack) {
                tx.addToBackStack(null)
            }
            tx.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
