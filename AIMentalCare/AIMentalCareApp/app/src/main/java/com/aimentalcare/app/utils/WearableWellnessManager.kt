package com.aimentalcare.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import com.aimentalcare.app.data.HeartRateEntity
import com.aimentalcare.app.data.MentalCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearableWellnessManager(private val context: Context) : SensorEventListener {

    interface HeartRateCallback {
        fun onMeasurementReceived(bpm: Int, accuracy: Int, sensorName: String)
        fun onStatusUpdated(statusCode: Int, statusMessage: String)
    }

    companion object {
        const val STATUS_ACTIVE = 1
        const val STATUS_PERMISSION_REQUIRED = 2
        const val STATUS_NO_HARDWARE_SENSOR = 3
        const val STATUS_DISCONNECTED = 4

        const val PERMISSION_BODY_SENSORS = Manifest.permission.BODY_SENSORS
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    private var currentCallback: HeartRateCallback? = null
    private var isListening = false

    fun isSensorAvailable(): Boolean {
        return heartRateSensor != null
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PERMISSION_BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startHeartRateMonitoring(callback: HeartRateCallback) {
        this.currentCallback = callback

        if (!hasPermission()) {
            callback.onStatusUpdated(
                STATUS_PERMISSION_REQUIRED,
                "Permission required: Body Sensors permission needed to read genuine heart-rate data."
            )
            return
        }

        if (heartRateSensor == null) {
            callback.onStatusUpdated(
                STATUS_NO_HARDWARE_SENSOR,
                "No direct heart rate sensor found on this handheld device. Connect a Wear OS smartwatch or Health Connect compatible wearable."
            )
            return
        }

        val success = sensorManager.registerListener(
            this,
            heartRateSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if (success) {
            isListening = true
            callback.onStatusUpdated(
                STATUS_ACTIVE,
                "Connected to ${heartRateSensor.name}. Reading real-time heart rate..."
            )
        } else {
            callback.onStatusUpdated(
                STATUS_DISCONNECTED,
                "Failed to activate ${heartRateSensor.name} sensor listener."
            )
        }
    }

    fun stopHeartRateMonitoring() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            currentCallback?.onStatusUpdated(STATUS_DISCONNECTED, "Sensor disconnected.")
        }
        currentCallback = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_HEART_RATE) return

        val heartRateValue = event.values.firstOrNull()?.toInt() ?: 0
        if (heartRateValue > 0) {
            val sensorName = event.sensor.name ?: "Optical Heart Rate Sensor"
            currentCallback?.onMeasurementReceived(heartRateValue, event.accuracy, sensorName)

            // Persist valid real reading to local Room database
            val prefs = context.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
            val userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = MentalCareDatabase.getDatabase(context)
                    db.checkInDao().insertHeartRate(
                        HeartRateEntity(
                            userId = userEmail,
                            bpm = heartRateValue,
                            accuracy = event.accuracy,
                            timestamp = System.currentTimeMillis(),
                            source = "Hardware Wearable Sensor",
                            sensorName = sensorName
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Optional calibration listener
    }
}

data class WearableDisplayState(
    val hasPermission: Boolean,
    val hasHardwareSensor: Boolean,
    val latestReading: HeartRateEntity?,
    val isStale: Boolean,
    val statusBadge: String,
    val headline: String,
    val subtitle: String,
    val actionText: String
)

object WearableTelemetryEvaluator {
    fun evaluateDisplayState(
        hasPermission: Boolean,
        hasHardwareSensor: Boolean,
        latestReading: HeartRateEntity?,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): WearableDisplayState {
        if (!hasPermission) {
            return WearableDisplayState(
                hasPermission = false,
                hasHardwareSensor = hasHardwareSensor,
                latestReading = null,
                isStale = false,
                statusBadge = "Permission Needed",
                headline = "Body Sensor Access",
                subtitle = "Body sensors permission is required to read live heart rate.",
                actionText = "Grant Permission"
            )
        }

        if (latestReading != null && latestReading.bpm > 0) {
            val ageHours = (currentTimeMillis - latestReading.timestamp) / (1000 * 60 * 60)
            val isStale = ageHours >= 2
            return WearableDisplayState(
                hasPermission = true,
                hasHardwareSensor = hasHardwareSensor,
                latestReading = latestReading,
                isStale = isStale,
                statusBadge = if (isStale) "Stale Data" else "Connected",
                headline = "${latestReading.bpm} BPM",
                subtitle = "Source: ${latestReading.sensorName}",
                actionText = if (isStale) "Sync Fresh Reading" else "View Details"
            )
        }

        if (!hasHardwareSensor) {
            return WearableDisplayState(
                hasPermission = true,
                hasHardwareSensor = false,
                latestReading = null,
                isStale = false,
                statusBadge = "Not Connected",
                headline = "Connect your wearable",
                subtitle = "Connect a supported health source to view available heart-rate data.",
                actionText = "Connect Device"
            )
        }

        return WearableDisplayState(
            hasPermission = true,
            hasHardwareSensor = true,
            latestReading = null,
            isStale = false,
            statusBadge = "Ready to Read",
            headline = "Sensor Ready",
            subtitle = "Hardware sensor detected. Tap to capture your current pulse.",
            actionText = "Read Pulse"
        )
    }
}
