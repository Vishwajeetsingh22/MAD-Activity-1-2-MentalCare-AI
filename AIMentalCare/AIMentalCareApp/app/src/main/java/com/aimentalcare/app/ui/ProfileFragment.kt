package com.aimentalcare.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentProfileBinding
import com.aimentalcare.app.utils.WearableWellnessManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()
        val prefs = ctx.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        val email = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        val name = prefs.getString("user_name", "Joylyn Fernandes") ?: "Joylyn Fernandes"

        binding.txtProfileName.text = name
        binding.txtProfileEmail.text = email
        binding.txtAvatarInitials.text = getInitials(name)

        // Asynchronously check today's check-in status from Room
        checkTodayCheckInStatus(email)

        // Check wearable sensor status
        checkWearableStatus()

        // Edit Profile Dialog
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog(prefs)
        }

        // Group 1: Wellness & Routine
        binding.layoutProfileGoals.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(GoalsFragment(), addToBackStack = true)
        }

        binding.layoutProfileProgress.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AnalyticsFragment(), addToBackStack = true)
        }

        binding.layoutProfileDailyCheckIn.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(DailyCheckInFragment(), addToBackStack = true)
        }

        // Group 2: Health & Wearables
        binding.layoutProfileWearables.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(WearableWellnessFragment(), addToBackStack = true)
        }

        // Group 3: Safety & Emergency
        binding.layoutProfileContacts.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(EmergencyContactsFragment(), addToBackStack = true)
        }

        binding.layoutProfileSos.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SosSettingsFragment(), addToBackStack = true)
        }

        // Group 4: Preferences & About
        binding.layoutProfileSettings.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(SettingsFragment(), addToBackStack = true)
        }

        binding.layoutProfilePrivacy.setOnClickListener {
            showPrivacyDialog()
        }

        binding.layoutProfileAbout.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(AboutFragment(), addToBackStack = true)
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation(prefs)
        }
    }

    private fun getInitials(name: String): String {
        return com.aimentalcare.app.utils.ProfileSettingsEvaluator.computeAvatarInitials(name)
    }

    private fun checkTodayCheckInStatus(email: String) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MentalCareDatabase.getDatabase(requireContext())
                val todayCheckIn = db.checkInDao().getLatestCheckInForDateSync(email, todayStr)
                withContext(Dispatchers.Main) {
                    if (_binding != null) {
                        if (todayCheckIn != null) {
                            binding.txtProfileCheckInStatus.text = "✓ Completed for today (${todayCheckIn.stressLevel})"
                            binding.txtProfileCheckInStatus.setTextColor(
                                androidx.core.content.ContextCompat.getColor(requireContext(), com.aimentalcare.app.R.color.dark_green)
                            )
                        } else {
                            binding.txtProfileCheckInStatus.text = "Pending today's check-in"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkWearableStatus() {
        val wearableManager = WearableWellnessManager(requireContext())
        val hasSensor = wearableManager.isSensorAvailable()
        val hasPermission = wearableManager.hasPermission()

        if (hasSensor && hasPermission) {
            binding.txtProfileWearableStatus.text = "✓ Hardware sensor connected & active"
        } else if (hasSensor) {
            binding.txtProfileWearableStatus.text = "Hardware sensor found • Permission needed"
        } else {
            binding.txtProfileWearableStatus.text = "Optical PPG pulse & smartwatch sensor hub"
        }
    }

    private fun showPrivacyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🔒 Privacy & Data Protection")
            .setMessage(
                "MentalCare AI strictly adheres to data privacy best practices:\n\n" +
                "• On-Device Storage: All personal check-ins and journal reflections are safely encrypted within your local SQLite/Room database.\n" +
                "• Zero Tracking: Your data is never sold, leased, or shared with commercial advertising brokers.\n" +
                "• Transparent Permissions: Wearable biometric sensors and emergency contacts access are only invoked when explicitly granted.\n" +
                "• Anonymized AI: Mental health sentiment inference uses anonymized requests without attaching identity tokens."
            )
            .setPositiveButton("Understood", null)
            .show()
    }

    private fun showEditProfileDialog(prefs: android.content.SharedPreferences) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
        }

        val nameInput = EditText(requireContext()).apply {
            hint = "Full Name"
            setText(binding.txtProfileName.text)
        }

        val emailInput = EditText(requireContext()).apply {
            hint = "Email Address"
            setText(binding.txtProfileEmail.text)
        }

        layout.addView(nameInput)
        layout.addView(emailInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()

                if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                    prefs.edit()
                        .putString("user_name", newName)
                        .putString("user_email", newEmail)
                        .apply()

                    binding.txtProfileName.text = newName
                    binding.txtProfileEmail.text = newEmail
                    binding.txtAvatarInitials.text = getInitials(newName)
                    Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmation(prefs: android.content.SharedPreferences) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to sign out of MentalCare AI?")
            .setPositiveButton("Logout") { _, _ ->
                // Clear user session without corrupting global device preferences
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
