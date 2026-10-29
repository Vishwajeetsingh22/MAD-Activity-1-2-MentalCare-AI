package com.aimentalcare.app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aimentalcare.app.R
import com.aimentalcare.app.data.HeartRateEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentWearableWellnessBinding
import com.aimentalcare.app.utils.WearableWellnessManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WearableWellnessFragment : Fragment(), WearableWellnessManager.HeartRateCallback {

    private var _binding: FragmentWearableWellnessBinding? = null
    private val binding get() = _binding!!

    private lateinit var wearableManager: WearableWellnessManager
    private var isMonitoring = false
    private var userEmail: String = "user@mentalcare.ai"

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            binding.btnGrantPermission.visibility = View.GONE
            startMonitoring()
        } else {
            binding.badgeConnectionStatus.text = "Permission Denied"
            binding.txtStatusDescription.text = "Body sensors permission was denied. Enable permission in system settings to let MentalCare AI capture heart rate."
            binding.btnGrantPermission.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWearableWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        wearableManager = WearableWellnessManager(ctx)

        val prefs = ctx.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        binding.btnWearableBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        binding.btnGrantPermission.setOnClickListener {
            permissionLauncher.launch(WearableWellnessManager.PERMISSION_BODY_SENSORS)
        }

        binding.btnToggleSensor.setOnClickListener {
            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }

        // Check initial sensor & permission state
        checkInitialState()

        // Observe historical real heart-rate measurements from Room DB
        val db = MentalCareDatabase.getDatabase(ctx)
        db.checkInDao().getRecentHeartRates(userEmail).observe(viewLifecycleOwner) { records ->
            renderHistoricalRecords(records)
        }
    }

    private fun checkInitialState() {
        if (!wearableManager.hasPermission()) {
            binding.badgeConnectionStatus.text = "Permission Needed"
            binding.btnGrantPermission.visibility = View.VISIBLE
            binding.txtStatusDescription.text = "Body sensor permission is required to read live heart rate from a connected watch or body sensor."
        } else if (!wearableManager.isSensorAvailable()) {
            binding.badgeConnectionStatus.text = "No Handheld Sensor"
            binding.btnGrantPermission.visibility = View.GONE
            binding.txtStatusDescription.text = "No direct optical sensor on this phone. Connect a Wear OS smartwatch or Health Connect compatible device to record genuine pulse data."
        } else {
            binding.badgeConnectionStatus.text = "Ready to Read"
            binding.btnGrantPermission.visibility = View.GONE
            binding.txtStatusDescription.text = "Hardware sensor detected. Tap 'Read Real-time Sensor' to capture your current pulse."
        }
    }

    private fun startMonitoring() {
        if (!wearableManager.hasPermission()) {
            permissionLauncher.launch(WearableWellnessManager.PERMISSION_BODY_SENSORS)
            return
        }

        wearableManager.startHeartRateMonitoring(this)
        isMonitoring = true
        binding.btnToggleSensor.text = "Stop Sensor Read"
        binding.btnToggleSensor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.alert_banner_red))
    }

    private fun stopMonitoring() {
        wearableManager.stopHeartRateMonitoring()
        isMonitoring = false
        binding.btnToggleSensor.text = "Read Real-time Sensor"
        binding.btnToggleSensor.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_green))
        binding.badgeConnectionStatus.text = "Disconnected"
    }

    override fun onMeasurementReceived(bpm: Int, accuracy: Int, sensorName: String) {
        activity?.runOnUiThread {
            binding.txtHeartRateValue.text = "$bpm BPM"
            val nowStr = SimpleDateFormat("hh:mm:ss a • MMM dd", Locale.getDefault()).format(Date())
            binding.txtHeartRateTimestamp.text = "Measured: $nowStr"
            binding.txtHeartRateSource.text = "Source: $sensorName (Accuracy: $accuracy)"
            binding.badgeConnectionStatus.text = "Live Active"
        }
    }

    override fun onStatusUpdated(statusCode: Int, statusMessage: String) {
        activity?.runOnUiThread {
            binding.txtStatusDescription.text = statusMessage
            when (statusCode) {
                WearableWellnessManager.STATUS_ACTIVE -> {
                    binding.badgeConnectionStatus.text = "Active"
                    binding.btnGrantPermission.visibility = View.GONE
                }
                WearableWellnessManager.STATUS_PERMISSION_REQUIRED -> {
                    binding.badgeConnectionStatus.text = "Permission Required"
                    binding.btnGrantPermission.visibility = View.VISIBLE
                }
                WearableWellnessManager.STATUS_NO_HARDWARE_SENSOR -> {
                    binding.badgeConnectionStatus.text = "No Device Sensor"
                    binding.btnGrantPermission.visibility = View.GONE
                }
                WearableWellnessManager.STATUS_DISCONNECTED -> {
                    binding.badgeConnectionStatus.text = "Disconnected"
                }
            }
        }
    }

    private fun renderHistoricalRecords(records: List<HeartRateEntity>) {
        val b = _binding ?: return
        if (records.isEmpty()) {
            b.txtEmptyMeasurements.visibility = View.VISIBLE
            b.layoutRecentMeasurements.removeAllViews()
            return
        }

        b.txtEmptyMeasurements.visibility = View.GONE
        b.layoutRecentMeasurements.removeAllViews()

        // Update top card with most recent record if not currently actively monitoring
        if (!isMonitoring && records.isNotEmpty()) {
            val latest = records.first()
            b.txtHeartRateValue.text = "${latest.bpm} BPM"
            val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(latest.timestamp))
            b.txtHeartRateTimestamp.text = "Last logged: $dateStr"
            b.txtHeartRateSource.text = "Source: ${latest.sensorName}"
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        val appCtx = requireContext()

        for (record in records) {
            val row = LinearLayout(appCtx).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 16, 24, 16)
                background = ContextCompat.getDrawable(appCtx, R.drawable.bg_medical_card)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 10)
                }
                layoutParams = params
            }

            val iconText = TextView(appCtx).apply {
                text = "❤️"
                textSize = 20f
                setPadding(0, 0, 16, 0)
            }

            val detailsLayout = LinearLayout(appCtx).apply {
                orientation = LinearLayout.VERTICAL
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = p
            }

            val bpmText = TextView(appCtx).apply {
                text = "${record.bpm} BPM"
                textSize = 15f
                setTextColor(ContextCompat.getColor(appCtx, R.color.text_dark))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val metaText = TextView(appCtx).apply {
                text = "${dateFormat.format(Date(record.timestamp))} • ${record.sensorName}"
                textSize = 11f
                setTextColor(ContextCompat.getColor(appCtx, R.color.text_subtle))
            }

            detailsLayout.addView(bpmText)
            detailsLayout.addView(metaText)

            row.addView(iconText)
            row.addView(detailsLayout)

            b.layoutRecentMeasurements.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        wearableManager.stopHeartRateMonitoring()
        _binding = null
    }
}
