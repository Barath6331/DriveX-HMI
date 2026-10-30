package com.barath.drivexhmi.model

/**
 * Validated, high-integrity domain vehicle state processed by the repository.
 * Contains sanitized sensor values, safety flags, and telemetry freshness.
 */
data class ProcessedVehicleState(
    val speedStatus: SpeedSensorStatus,
    val isMotorOn: Boolean,
    val isKillSwitchActive: Boolean,
    val freshness: DataFreshness,
    val scenario: VehicleScenario,
    val rideMode: RideMode,
    val batteryPercent: Int,
    val rangeKm: Int,
    val odometerKm: Double,
    val isSideStandDown: Boolean,
    val isHighBeamOn: Boolean,
    val isLeftTurnActive: Boolean,
    val isRightTurnActive: Boolean,
    val lastUpdatedMillis: Long
) {
    /**
     * Sanitized speed for gauge animations (clamped to 0 if invalid).
     */
    val safeDisplaySpeedKmh: Float
        get() = when (speedStatus) {
            is SpeedSensorStatus.Valid -> speedStatus.speedKmh
            is SpeedSensorStatus.Invalid -> 0f
        }

    /**
     * True if any critical warning exists (kill switch active, stale data, or sensor glitch).
     */
    val hasSafetyAlert: Boolean
        get() = isKillSwitchActive ||
                freshness is DataFreshness.Stale ||
                speedStatus is SpeedSensorStatus.Invalid
}

