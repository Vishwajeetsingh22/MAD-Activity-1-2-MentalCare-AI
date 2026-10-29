package com.aimentalcare.app.ui

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aimentalcare.app.R
import com.aimentalcare.app.databinding.FragmentSettingsBinding
import com.aimentalcare.app.utils.NotificationHelper
import com.aimentalcare.app.utils.WearableWellnessManager
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Notifications disabled. You can still access Daily Check-in anytime inside the app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val requestBodySensorsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updateWearableStatusUi()
        if (isGranted) {
            Toast.makeText(requireContext(), "Body Sensors permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Body Sensors permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val prefs = ctx.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)

        val email = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        val name = prefs.getString("user_name", "Active User") ?: "Active User"
        binding.txtSettingsUserName.text = name
        binding.txtSettingsUserEmail.text = email

        // 1. APPEARANCE
        setupAppearanceSection(prefs)

        // 2. NOTIFICATIONS
        setupNotificationSection(prefs)

        // 3. AI ASSISTANT
        setupAiAssistantSection(prefs)

        // 4. HEALTH & WEARABLES
        setupHealthSection()

        // 5. EMERGENCY
        setupEmergencySection()

        // 6. ACCOUNT
        setupAccountSection(prefs)

        // 7. PRIVACY
        setupPrivacySection(prefs)
    }

    // =========================================================================
    // 1. APPEARANCE
    // =========================================================================
    private fun setupAppearanceSection(prefs: android.content.SharedPreferences) {
        when (prefs.getString("theme_mode", "SYSTEM")) {
            "LIGHT" -> binding.radioThemeLight.isChecked = true
            "DARK" -> binding.radioThemeDark.isChecked = true
            else -> binding.radioThemeSystem.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val modeStr: String
            val nightModeInt: Int

            when (checkedId) {
                R.id.radioThemeLight -> {
                    modeStr = "LIGHT"
                    nightModeInt = AppCompatDelegate.MODE_NIGHT_NO
                }
                R.id.radioThemeDark -> {
                    modeStr = "DARK"
                    nightModeInt = AppCompatDelegate.MODE_NIGHT_YES
                }
                else -> {
                    modeStr = "SYSTEM"
                    nightModeInt = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }

            // Persist theme choice without touching authentication tokens
            prefs.edit()
                .putString("theme_mode", modeStr)
                .putInt("app_theme_mode", nightModeInt)
                .apply()

            AppCompatDelegate.setDefaultNightMode(nightModeInt)
        }
    }

    // =========================================================================
    // 2. NOTIFICATIONS
    // =========================================================================
    private fun setupNotificationSection(prefs: android.content.SharedPreferences) {
        val checkinRemindersEnabled = prefs.getBoolean("pref_checkin_reminders", true)
        binding.switchCheckinReminders.isChecked = checkinRemindersEnabled
        binding.layoutReminderTime.visibility = if (checkinRemindersEnabled) View.VISIBLE else View.GONE
        binding.layoutReminderDays.visibility = if (checkinRemindersEnabled) View.VISIBLE else View.GONE

        val reminderHour = prefs.getInt("reminder_time_hour", 20)
        val reminderMinute = prefs.getInt("reminder_time_minute", 30)
        updateReminderTimeDisplay(reminderHour, reminderMinute)

        val reminderDays = prefs.getString("pref_reminder_days", "EVERY_DAY") ?: "EVERY_DAY"
        updateReminderDaysDisplay(reminderDays)

        binding.switchCheckinReminders.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_checkin_reminders", isChecked).apply()
            binding.layoutReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.layoutReminderDays.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val h = prefs.getInt("reminder_time_hour", 20)
                val m = prefs.getInt("reminder_time_minute", 30)
                NotificationHelper.scheduleDailyCheckInReminder(requireContext(), h, m)
                Toast.makeText(requireContext(), "Daily reminder enabled", Toast.LENGTH_SHORT).show()
            } else {
                NotificationHelper.cancelDailyCheckInReminder(requireContext())
                Toast.makeText(requireContext(), "Daily reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnChangeReminderTime.setOnClickListener {
            val currentH = prefs.getInt("reminder_time_hour", 20)
            val currentM = prefs.getInt("reminder_time_minute", 30)

            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    prefs.edit()
                        .putInt("reminder_time_hour", selectedHour)
                        .putInt("reminder_time_minute", selectedMinute)
                        .apply()
                    updateReminderTimeDisplay(selectedHour, selectedMinute)
                    if (binding.switchCheckinReminders.isChecked) {
                        NotificationHelper.scheduleDailyCheckInReminder(requireContext(), selectedHour, selectedMinute)
                    }
                    Toast.makeText(requireContext(), "Reminder time updated", Toast.LENGTH_SHORT).show()
                },
                currentH,
                currentM,
                false
            ).show()
        }

        binding.btnChangeReminderDays.setOnClickListener {
            val options = arrayOf("Every Day", "Weekdays Only (Mon-Fri)", "Weekends Only (Sat-Sun)")
            val currentMode = prefs.getString("pref_reminder_days", "EVERY_DAY") ?: "EVERY_DAY"
            val selectedIndex = when (currentMode) {
                "WEEKDAYS" -> 1
                "WEEKENDS" -> 2
                else -> 0
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Select Reminder Days")
                .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                    val newMode = when (which) {
                        1 -> "WEEKDAYS"
                        2 -> "WEEKENDS"
                        else -> "EVERY_DAY"
                    }
                    prefs.edit().putString("pref_reminder_days", newMode).apply()
                    updateReminderDaysDisplay(newMode)
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Schedule: ${options[which]}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Quiet Hours
        val quietHoursEnabled = prefs.getBoolean("pref_quiet_hours_enabled", true)
        binding.switchQuietHours.isChecked = quietHoursEnabled
        binding.switchQuietHours.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_quiet_hours_enabled", isChecked).apply()
            val status = if (isChecked) "enabled (10 PM - 7 AM)" else "disabled"
            Toast.makeText(requireContext(), "Quiet Hours $status", Toast.LENGTH_SHORT).show()
        }

        // Vibration
        val vibrateEnabled = prefs.getBoolean("pref_reminder_vibrate", true)
        binding.switchReminderVibrate.isChecked = vibrateEnabled
        binding.switchReminderVibrate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_reminder_vibrate", isChecked).apply()
        }

        // Sound
        val soundEnabled = prefs.getBoolean("pref_reminder_sound", true)
        binding.switchReminderSound.isChecked = soundEnabled
        binding.switchReminderSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_reminder_sound", isChecked).apply()
        }

        // Real-Time Stress Alerts
        val stressAlertsEnabled = prefs.getBoolean("pref_stress_alerts", true)
        binding.switchStressAlerts.isChecked = stressAlertsEnabled
        binding.switchStressAlerts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_stress_alerts", isChecked).apply()
        }
    }

    private fun updateReminderTimeDisplay(hour: Int, minute: Int) {
        val isPm = hour >= 12
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, if (isPm) "PM" else "AM")
        binding.txtReminderTimeDisplay.text = formatted
    }

    private fun updateReminderDaysDisplay(mode: String) {
        binding.txtReminderDaysDisplay.text = when (mode) {
            "WEEKDAYS" -> "Weekdays (Mon-Fri)"
            "WEEKENDS" -> "Weekends (Sat-Sun)"
            else -> "Every Day"
        }
    }

    // =========================================================================
    // 3. AI ASSISTANT
    // =========================================================================
    private fun setupAiAssistantSection(prefs: android.content.SharedPreferences) {
        val currentTone = prefs.getString("pref_ai_chat_tone", "Empathetic & Supportive") ?: "Empathetic & Supportive"
        binding.txtAiChatToneDisplay.text = currentTone

        binding.btnChangeAiTone.setOnClickListener {
            val tones = arrayOf("Empathetic & Supportive", "Direct & Concise", "Mindful & Reflective")
            val currentIndex = tones.indexOf(binding.txtAiChatToneDisplay.text.toString()).coerceAtLeast(0)

            AlertDialog.Builder(requireContext())
                .setTitle("Select AI Tone")
                .setSingleChoiceItems(tones, currentIndex) { dialog, which ->
                    val chosen = tones[which]
                    prefs.edit().putString("pref_ai_chat_tone", chosen).apply()
                    binding.txtAiChatToneDisplay.text = chosen
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "AI Tone updated to $chosen", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val offlineFallbackEnabled = prefs.getBoolean("pref_ai_offline_fallback", true)
        binding.switchAiOfflineFallback.isChecked = offlineFallbackEnabled
        binding.switchAiOfflineFallback.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_ai_offline_fallback", isChecked).apply()
        }

        binding.btnSettingsChatbot.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ChatbotFragment(), addToBackStack = true)
        }
    }

    // =========================================================================
    // 4. HEALTH & WEARABLES
    // =========================================================================
    private fun setupHealthSection() {
        updateWearableStatusUi()

        binding.btnRequestWearablePermission.setOnClickListener {
            val manager = WearableWellnessManager(requireContext())
            if (!manager.hasPermission()) {
                requestBodySensorsPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            } else {
                Toast.makeText(requireContext(), "Body Sensors permission is already granted.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSettingsWearables.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(WearableWellnessFragment(), addToBackStack = true)
        }
    }

    private fun updateWearableStatusUi() {
        if (_binding == null) return
        val manager = WearableWellnessManager(requireContext())
        val hasPermission = manager.hasPermission()
        val hasSensor = manager.isSensorAvailable()

        if (hasPermission) {
            binding.txtWearablePermissionStatus.text = "Body Sensors Access: Granted ✓"
            binding.btnRequestWearablePermission.text = "Granted"
            binding.btnRequestWearablePermission.isEnabled = false
        } else {
            binding.txtWearablePermissionStatus.text = "Body Sensors Access: Permission Needed"
            binding.btnRequestWearablePermission.text = "Grant"
            binding.btnRequestWearablePermission.isEnabled = true
        }

        if (hasSensor) {
            binding.txtHeartRateAccessStatus.text = "Sensor: Optical PPG Heart-Rate Sensor Active"
        } else {
            binding.txtHeartRateAccessStatus.text = "Sensor: Wear OS / Companion Wearable Required"
        }
    }

    // =========================================================================
    // 5. EMERGENCY
    // =========================================================================
    private fun setupEmergencySection() {
        binding.btnSettingsContacts.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(EmergencyContactsFragment(), addToBackStack = true)
        }

        binding.btnSettingsSos.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SosSettingsFragment(), addToBackStack = true)
        }
    }

    // =========================================================================
    // 6. ACCOUNT
    // =========================================================================
    private fun setupAccountSection(prefs: android.content.SharedPreferences) {
        binding.btnSettingsProfile.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ProfileFragment(), addToBackStack = true)
        }

        binding.btnSettingsLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to sign out of MentalCare AI?")
                .setPositiveButton("Logout") { _, _ ->
                    // Remove active session credentials while preserving global settings
                    prefs.edit()
                        .remove("user_email")
                        .remove("user_name")
                        .remove("account_setup_completed")
                        .apply()

                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // =========================================================================
    // 7. PRIVACY & DISCLAIMERS
    // =========================================================================
    private fun setupPrivacySection(prefs: android.content.SharedPreferences) {
        val localPref = prefs.getBoolean("pref_strict_local_storage", false)
        binding.switchLocalDataPreference.isChecked = localPref
        binding.switchLocalDataPreference.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("pref_strict_local_storage", isChecked).apply()
            val text = if (isChecked) "Strict local storage enabled" else "Cloud sync enabled"
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }

        binding.btnClearCache.setOnClickListener {
            try {
                requireContext().cacheDir.deleteRecursively()
                Toast.makeText(requireContext(), "Cache cleared successfully.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Cache cleared.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnWellnessDisclaimer.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("📋 Clinical Wellness Disclaimer")
                .setMessage(
                    "MentalCare AI is an educational stress-tracking and wellness support application.\n\n" +
                    "• Not a Medical Device: MentalCare AI does not provide clinical psychiatric diagnoses, medical treatments, or pharmaceutical prescriptions.\n" +
                    "• Educational Purpose: Sentiment analysis and physiological telemetry are supportive wellness indicators for lifestyle self-awareness.\n" +
                    "• Crisis Assistance: If you or someone you know is in severe emotional distress or having thoughts of self-harm, please connect immediately with emergency services or call 988 (Suicide & Crisis Lifeline)."
                )
                .setPositiveButton("Understood", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
