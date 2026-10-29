package com.aimentalcare.app

import com.aimentalcare.app.data.HeartRateEntity
import com.aimentalcare.app.utils.WearableTelemetryEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 8 Unit Tests — Heart Rate and Wearable Experience
 * 
 * Verifies:
 * 1. Zero data fabrication policy (no fake pulse numbers when sensor is absent/empty)
 * 2. Proper state evaluation across permission denied, no sensor, ready, active, and stale data
 * 3. Stale data detection threshold (>= 2 hours old)
 * 4. Physiological telemetry distinction from AI stress classification
 * 5. Rejection of uninitialized / zero BPM sensor readings
 */
class HeartRateAndWearableTest {

    private val testCurrentTime = 1775133600000L // Fixed reference timestamp

    // =========================================================================
    // 1. PERMISSION DENIED STATE
    // =========================================================================

    @Test
    fun testPermissionDeniedState() {
        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = false,
            hasHardwareSensor = true,
            latestReading = null,
            currentTimeMillis = testCurrentTime
        )

        assertFalse("Permission must be reported as denied", state.hasPermission)
        assertEquals("Permission Needed", state.statusBadge)
        assertEquals("Body Sensor Access", state.headline)
        assertEquals("Body sensors permission is required to read live heart rate.", state.subtitle)
        assertEquals("Grant Permission", state.actionText)
        assertNull("Must not fabricate any heart rate reading when permission is denied", state.latestReading)
        assertFalse(state.isStale)
    }

    // =========================================================================
    // 2. NO HARDWARE SENSOR STATE (HANDHELD DEVICE WITHOUT WEARABLE CONNECTED)
    // =========================================================================

    @Test
    fun testNoHardwareSensorState() {
        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = false,
            latestReading = null,
            currentTimeMillis = testCurrentTime
        )

        assertTrue(state.hasPermission)
        assertFalse("Hardware sensor must be absent", state.hasHardwareSensor)
        assertEquals("Not Connected", state.statusBadge)
        assertEquals("Connect your wearable", state.headline)
        assertEquals("Connect a supported health source to view available heart-rate data.", state.subtitle)
        assertEquals("Connect Device", state.actionText)
        assertNull("Must not fabricate heart rate reading when hardware sensor is missing", state.latestReading)
        assertFalse(state.isStale)
    }

    // =========================================================================
    // 3. SENSOR READY BUT NO READING YET
    // =========================================================================

    @Test
    fun testSensorReadyNoReadingState() {
        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = true,
            latestReading = null,
            currentTimeMillis = testCurrentTime
        )

        assertTrue(state.hasPermission)
        assertTrue(state.hasHardwareSensor)
        assertEquals("Ready to Read", state.statusBadge)
        assertEquals("Sensor Ready", state.headline)
        assertEquals("Hardware sensor detected. Tap to capture your current pulse.", state.subtitle)
        assertEquals("Read Pulse", state.actionText)
        assertNull(state.latestReading)
        assertFalse(state.isStale)
    }

    // =========================================================================
    // 4. FRESH REAL READING (GENUINE HARDWARE SENSOR CAPTURE)
    // =========================================================================

    @Test
    fun testFreshRealReadingState() {
        val tenMinutesAgo = testCurrentTime - (10 * 60 * 1000)
        val realReading = HeartRateEntity(
            id = 1,
            userId = "student@mentalcare.ai",
            bpm = 72,
            accuracy = 3,
            timestamp = tenMinutesAgo,
            source = "Hardware Wearable Sensor",
            sensorName = "Optical PPG Heart Rate Sensor"
        )

        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = true,
            latestReading = realReading,
            currentTimeMillis = testCurrentTime
        )

        assertTrue(state.hasPermission)
        assertEquals("72 BPM", state.headline)
        assertEquals("Connected", state.statusBadge)
        assertEquals("Source: Optical PPG Heart Rate Sensor", state.subtitle)
        assertEquals("View Details", state.actionText)
        assertFalse("Reading taken 10 minutes ago must not be flagged as stale", state.isStale)
        assertEquals(72, state.latestReading?.bpm)
    }

    // =========================================================================
    // 5. STALE READING DETECTION (>= 2 HOURS OLD)
    // =========================================================================

    @Test
    fun testStaleReadingState() {
        val threeHoursAgo = testCurrentTime - (3 * 60 * 60 * 1000)
        val staleReading = HeartRateEntity(
            id = 2,
            userId = "student@mentalcare.ai",
            bpm = 78,
            accuracy = 2,
            timestamp = threeHoursAgo,
            source = "Hardware Wearable Sensor",
            sensorName = "Wear OS Sensor"
        )

        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = true,
            latestReading = staleReading,
            currentTimeMillis = testCurrentTime
        )

        assertTrue(state.hasPermission)
        assertEquals("78 BPM", state.headline)
        assertEquals("Stale Data", state.statusBadge)
        assertTrue("Reading older than 2 hours must be marked as stale", state.isStale)
        assertEquals("Sync Fresh Reading", state.actionText)
    }

    // =========================================================================
    // 6. ZERO OR INVALID READING REJECTION (NEVER SHOW FAKE 0 BPM)
    // =========================================================================

    @Test
    fun testZeroOrNegativeBpmIgnored() {
        val zeroReading = HeartRateEntity(
            id = 3,
            userId = "student@mentalcare.ai",
            bpm = 0, // Uninitialized / erroneous sensor artifact
            accuracy = 0,
            timestamp = testCurrentTime,
            source = "Hardware Wearable Sensor",
            sensorName = "Noise"
        )

        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = false,
            latestReading = zeroReading,
            currentTimeMillis = testCurrentTime
        )

        // Should fall back to Connect your wearable instead of showing "0 BPM"
        assertEquals("Not Connected", state.statusBadge)
        assertEquals("Connect your wearable", state.headline)
        assertNull("0 BPM readings must not be treated as valid pulse measurements", state.latestReading)
    }

    // =========================================================================
    // 7. PHYSIOLOGICAL TELEMETRY VS AI STRESS SEPARATION
    // =========================================================================

    @Test
    fun testPhysiologicalHeartRateSeparationFromAiStress() {
        // High pulse rate (e.g. 105 BPM after exercise) is physiological data,
        // and must NOT automatically classify or overwrite psychological AI stress levels.
        val exerciseReading = HeartRateEntity(
            id = 4,
            userId = "student@mentalcare.ai",
            bpm = 105,
            accuracy = 3,
            timestamp = testCurrentTime - (5 * 60 * 1000),
            source = "Connected health device",
            sensorName = "Wearable Pulse Sensor"
        )

        val state = WearableTelemetryEvaluator.evaluateDisplayState(
            hasPermission = true,
            hasHardwareSensor = true,
            latestReading = exerciseReading,
            currentTimeMillis = testCurrentTime
        )

        assertEquals("105 BPM", state.headline)
        // Verify state is strictly biometric telemetry without pseudo-medical diagnosis
        assertFalse(state.headline.contains("High Stress"))
        assertFalse(state.headline.contains("Tachycardia"))
        assertTrue("Subtitle identifies genuine physical sensor source", state.subtitle.contains("Wearable Pulse Sensor"))
    }
}
