package com.barath.drivexhmi.ui.model

import com.barath.drivexhmi.model.DataFreshness
import com.barath.drivexhmi.model.ProcessedVehicleState
import com.barath.drivexhmi.model.RideMode
import com.barath.drivexhmi.model.SpeedSensorStatus
import com.barath.drivexhmi.model.VehicleScenario

/**
 * Immutable UI State representing the complete presentation model for the HMI dashboard.
 */
data class DashboardUiState(
    val speedDisplay: String = "0",
    val speedGaugeValue: Float = 0f,
    val speedUnit: String = "km/h",
    val isMotorOn: Boolean = false,
    val motorStatusText: String = "MOTOR OFF",
    val isKillSwitchActive: Boolean = false,
    val killSwitchStatusText: String = "KILL SWITCH INACTIVE",
    val isStaleData: Boolean = false,
    val staleWarningMessage: String? = null,
    val sensorErrorMessage: String? = null,
    val currentScenario: VehicleScenario = VehicleScenario.PARKED,
    val isAutoCycleActive: Boolean = true,
    val rideMode: RideMode = RideMode.PARK,
    val batteryPercent: Int = 94,
    val rangeKm: Int = 122,
    val odometerKm: Double = 1324.6,
    val isSideStandDown: Boolean = true,
    val isHighBeamOn: Boolean = false,
    val isLeftTurnActive: Boolean = false,
    val isRightTurnActive: Boolean = false
) {
    companion object {
        fun fromProcessedState(
            state: ProcessedVehicleState,
            isAutoCycleActive: Boolean
        ): DashboardUiState {
            val isStale = state.freshness is DataFreshness.Stale
            val isSpeedInvalid = state.speedStatus is SpeedSensorStatus.Invalid

            val speedText: String
            val gaugeSpeed: Float
            var sensorErrorMsg: String? = null

            when (state.speedStatus) {
                is SpeedSensorStatus.Valid -> {
                    speedText = if (isStale) "--" else state.speedStatus.speedKmh.toInt().toString()
                    gaugeSpeed = if (isStale) 0f else state.speedStatus.speedKmh
                }
                is SpeedSensorStatus.Invalid -> {
                    speedText = "--"
                    gaugeSpeed = 0f
                    sensorErrorMsg = state.speedStatus.reason
                }
            }

            val staleMsg = if (isStale) {
                val elapsed = (state.freshness as DataFreshness.Stale).elapsedSinceLastSignalMs / 1000.0
                "CAN Heartbeat Lost (No signal for %.1fs)".format(elapsed)
            } else null

            return DashboardUiState(
                speedDisplay = speedText,
                speedGaugeValue = gaugeSpeed,
                speedUnit = "km/h",
                isMotorOn = state.isMotorOn,
                motorStatusText = if (state.isMotorOn) "MOTOR ON" else "MOTOR OFF",
                isKillSwitchActive = state.isKillSwitchActive,
                killSwitchStatusText = if (state.isKillSwitchActive) "KILL SWITCH ACTIVE" else "KILL SWITCH INACTIVE",
                isStaleData = isStale,
                staleWarningMessage = staleMsg,
                sensorErrorMessage = sensorErrorMsg,
                currentScenario = state.scenario,
                isAutoCycleActive = isAutoCycleActive,
                rideMode = state.rideMode,
                batteryPercent = state.batteryPercent,
                rangeKm = state.rangeKm,
                odometerKm = state.odometerKm,
                isSideStandDown = state.isSideStandDown,
                isHighBeamOn = state.isHighBeamOn,
                isLeftTurnActive = state.isLeftTurnActive,
                isRightTurnActive = state.isRightTurnActive
            )
        }
    }
}

