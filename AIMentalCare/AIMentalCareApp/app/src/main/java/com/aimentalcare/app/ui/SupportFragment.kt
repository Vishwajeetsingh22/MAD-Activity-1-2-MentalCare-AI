package com.aimentalcare.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aimentalcare.app.R
import com.aimentalcare.app.databinding.FragmentSupportBinding

class SupportFragment : Fragment() {

    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    private var timer: CountDownTimer? = null
    private var isTimerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCallHotline.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))
            startActivity(intent)
        }

        val prefs = requireContext().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        val sosNum = prefs.getString("sos_emergency_number", "+91 98888 00000") ?: "+91 98888 00000"
        binding.btnCallSosNumber.text = "🚨 Call Emergency SOS ($sosNum)"

        binding.btnCallSosNumber.setOnClickListener {
            val activeSos = prefs.getString("sos_emergency_number", "+91 98888 00000") ?: "+91 98888 00000"
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Emergency Call")
                .setMessage("Are you sure you want to call your emergency contact ($activeSos)?")
                .setPositiveButton("Call") { _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$activeSos"))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Unable to start the call.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnManageContactsFromSupport.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(EmergencyContactsFragment())
        }

        binding.btnStartBreathing.setOnClickListener {
            if (isTimerRunning) {
                stopBreathingTimer()
            } else {
                startBreathingTimer()
            }
        }
    }

    private fun startBreathingTimer() {
        isTimerRunning = true
        _binding?.btnStartBreathing?.text = getString(R.string.stop_exercise)

        val phases = listOf(
            getString(R.string.inhale_phase),
            getString(R.string.hold_breath_phase),
            getString(R.string.exhale_phase),
            getString(R.string.hold_empty_phase)
        )
        
        timer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val b = _binding ?: return
                val secRemaining = (millisUntilFinished / 1000).toInt()
                val min = secRemaining / 60
                val sec = secRemaining % 60
                b.txtBreathingTimer.text = getString(R.string.timer_format, min, sec)

                val phaseIdx = (300 - secRemaining) % 16 / 4
                b.txtBreathingPhase.text = phases[phaseIdx]
            }

            override fun onFinish() {
                val b = _binding ?: return
                b.txtBreathingTimer.text = getString(R.string.timer_format, 0, 0)
                b.txtBreathingPhase.text = getString(R.string.exercise_complete)
                stopBreathingTimer()
            }
        }.start()
    }

    private fun stopBreathingTimer() {
        timer?.cancel()
        isTimerRunning = false
        _binding?.btnStartBreathing?.text = getString(R.string.start_breathing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}
