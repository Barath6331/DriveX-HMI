package com.barath.drivexhmi.model

/**
 * Encapsulates speed telemetry validation states to ensure edge-case safety.
 */
sealed interface SpeedSensorStatus {
    /**
     * Speed reading is within normal operating limits (0..200 km/h).
     */
    data class Valid(val speedKmh: Float) : SpeedSensorStatus

    /**
     * Speed reading is anomalous (e.g. negative speed or extreme spike),
     * preventing crashes or erratic needle movements.
     */
    data class Invalid(
        val rawValue: Float,
        val reason: String
    ) : SpeedSensorStatus
}

