package com.aimentalcare.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.aimentalcare.app.databinding.FragmentSosSettingsBinding

class SosSettingsFragment : Fragment() {

    private var _binding: FragmentSosSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSosSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        val currentSos = prefs.getString("sos_emergency_number", "+91 98888 00000") ?: "+91 98888 00000"
        binding.txtCurrentSosNumber.text = currentSos

        binding.btnSaveSosNumber.setOnClickListener {
            val inputNumber = binding.etNewSosNumber.text.toString().trim()
            val cleanDigits = inputNumber.replace(Regex("[^0-9]"), "")

            if (inputNumber.isEmpty() || cleanDigits.length < 7) {
                Toast.makeText(requireContext(), "Invalid phone number", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().putString("sos_emergency_number", inputNumber).apply()
                binding.txtCurrentSosNumber.text = inputNumber
                binding.etNewSosNumber.text.clear()
                Toast.makeText(requireContext(), "SOS number updated successfully.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnTestSosCall.setOnClickListener {
            val targetNumber = prefs.getString("sos_emergency_number", "+91 98888 00000") ?: "+91 98888 00000"
            showEmergencyCallConfirmation(targetNumber)
        }
    }

    private fun showEmergencyCallConfirmation(phoneNumber: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Emergency Call")
            .setMessage("Are you sure you want to call your emergency contact ($phoneNumber)?")
            .setPositiveButton("Call") { _, _ ->
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                try {
                    startActivity(dialIntent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Unable to start the call.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
