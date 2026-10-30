package com.barath.drivexhmi.model

/**
 * Represents the distinct vehicle operational scenarios required by the HMI specification,
 * including standard operating states and simulated edge conditions.
 */
enum class VehicleScenario(
    val title: String,
    val description: String
) {
    PARKED(
        title = "Parked",
        description = "0 km/h | Motor OFF | Kill switch Inactive"
    ),
    READY(
        title = "Ready",
        description = "0 km/h | Motor ON | Kill switch Inactive"
    ),
    RIDING(
        title = "Riding",
        description = "25 km/h | Motor ON | Kill switch Inactive"
    ),
    ACCELERATING(
        title = "Accelerating",
        description = "50 km/h | Motor ON | Kill switch Inactive"
    ),
    MOTOR_DISABLED(
        title = "Motor Disabled",
        description = "0 km/h | Motor OFF | Kill switch Active"
    ),
    INVALID_SPEED(
        title = "Edge: Negative Speed",
        description = "-15 km/h | Faulty sensor telemetry"
    ),
    STALE_DATA(
        title = "Edge: Stale CAN Signal",
        description = "Data pipeline timeout / Heartbeat lost"
    )
}

