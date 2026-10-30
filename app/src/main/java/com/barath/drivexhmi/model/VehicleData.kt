package com.barath.drivexhmi.model

/**
 * Raw telemetry payload emitted from the vehicle data source.
 * Corresponds to simulated CAN/sensor bus frames.
 */
data class VehicleData(
    val speedKmh: Float,
    val isMotorOn: Boolean,
    val isKillSwitchActive: Boolean,
    val timestampMillis: Long = System.currentTimeMillis(),
    val scenario: VehicleScenario = VehicleScenario.PARKED,
    val rideMode: RideMode = RideMode.PARK,
    val batteryPercent: Int = 92,
    val rangeKm: Int = 118,
    val odometerKm: Double = 1324.6,
    val isSideStandDown: Boolean = false,
    val isHighBeamOn: Boolean = false,
    val isLeftTurnActive: Boolean = false,
    val isRightTurnActive: Boolean = false
)

