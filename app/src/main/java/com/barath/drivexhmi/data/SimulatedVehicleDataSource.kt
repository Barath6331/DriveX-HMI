package com.barath.drivexhmi.data

import com.barath.drivexhmi.model.RideMode
import com.barath.drivexhmi.model.VehicleData
import com.barath.drivexhmi.model.VehicleScenario
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable

/**
 * Simulated vehicle data provider that emits vehicle telemetry approximately
 * once per second using Kotlin Coroutines and Flows.
 *
 * Implements the standard sequence:
 * Parked -> Ready -> Riding -> Accelerating -> Motor Disabled
 * as well as edge-case injection scenarios (Negative speed, Stale CAN signal).
 */
class SimulatedVehicleDataSource(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val emissionIntervalMs: Long = 1000L,
    externalScope: CoroutineScope? = null
) : VehicleDataSource, Closeable {

    private val internalJob = SupervisorJob()
    private val scope = externalScope ?: CoroutineScope(dispatcher + internalJob)

    private val _vehicleDataStream = MutableSharedFlow<VehicleData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val vehicleDataStream: Flow<VehicleData> = _vehicleDataStream.asSharedFlow()

    private val _isAutoCycleActive = MutableStateFlow(true)
    override val isAutoCycleActive: StateFlow<Boolean> = _isAutoCycleActive.asStateFlow()

    private val _currentScenario = MutableStateFlow(VehicleScenario.PARKED)
    override val currentScenario: StateFlow<VehicleScenario> = _currentScenario.asStateFlow()

    private val standardCycle = listOf(
        VehicleScenario.PARKED,
        VehicleScenario.READY,
        VehicleScenario.RIDING,
        VehicleScenario.ACCELERATING,
        VehicleScenario.MOTOR_DISABLED
    )

    private var currentCycleIndex = 0
    private var simulationJob: Job? = null

    init {
        // Emit initial parked state
        emitScenarioData(VehicleScenario.PARKED)
        startSimulationLoop()
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (isActive) {
                delay(emissionIntervalMs)
                if (_isAutoCycleActive.value) {
                    currentCycleIndex = (currentCycleIndex + 1) % standardCycle.size
                    val next = standardCycle[currentCycleIndex]
                    _currentScenario.value = next
                    emitScenarioData(next)
                }
            }
        }
    }

    override fun setAutoCycling(enabled: Boolean) {
        _isAutoCycleActive.value = enabled
    }

    override fun selectScenario(scenario: VehicleScenario, keepAutoCycling: Boolean) {
        if (!keepAutoCycling) {
            _isAutoCycleActive.value = false
        }
        val idx = standardCycle.indexOf(scenario)
        if (idx != -1) {
            currentCycleIndex = idx
        }
        _currentScenario.value = scenario

        if (scenario == VehicleScenario.STALE_DATA) {
            // In STALE_DATA scenario, we deliberately withhold new emissions
            // to allow the receiver's heartbeat watchdog timer to detect stale data.
            return
        }

        emitScenarioData(scenario)
    }

    override fun nextScenario() {
        currentCycleIndex = (currentCycleIndex + 1) % standardCycle.size
        val next = standardCycle[currentCycleIndex]
        _currentScenario.value = next
        emitScenarioData(next)
    }

    private fun emitScenarioData(scenario: VehicleScenario) {
        val data = buildVehicleDataForScenario(scenario)
        _vehicleDataStream.tryEmit(data)
    }

    override fun close() {
        simulationJob?.cancel()
        internalJob.cancel()
    }

    private fun buildVehicleDataForScenario(scenario: VehicleScenario): VehicleData {
        val now = System.currentTimeMillis()
        return when (scenario) {
            VehicleScenario.PARKED -> VehicleData(
                speedKmh = 0f,
                isMotorOn = false,
                isKillSwitchActive = false,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.PARK,
                batteryPercent = 94,
                rangeKm = 122,
                odometerKm = 1324.6,
                isSideStandDown = true,
                isHighBeamOn = false,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.READY -> VehicleData(
                speedKmh = 0f,
                isMotorOn = true,
                isKillSwitchActive = false,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.RIDE,
                batteryPercent = 94,
                rangeKm = 122,
                odometerKm = 1324.6,
                isSideStandDown = false,
                isHighBeamOn = false,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.RIDING -> VehicleData(
                speedKmh = 25f,
                isMotorOn = true,
                isKillSwitchActive = false,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.ECO,
                batteryPercent = 93,
                rangeKm = 120,
                odometerKm = 1325.2,
                isSideStandDown = false,
                isHighBeamOn = true,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.ACCELERATING -> VehicleData(
                speedKmh = 50f,
                isMotorOn = true,
                isKillSwitchActive = false,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.SONIC,
                batteryPercent = 91,
                rangeKm = 115,
                odometerKm = 1326.8,
                isSideStandDown = false,
                isHighBeamOn = true,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.MOTOR_DISABLED -> VehicleData(
                speedKmh = 0f,
                isMotorOn = false,
                isKillSwitchActive = true,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.EMERGENCY,
                batteryPercent = 91,
                rangeKm = 115,
                odometerKm = 1326.8,
                isSideStandDown = false,
                isHighBeamOn = false,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.INVALID_SPEED -> VehicleData(
                speedKmh = -15f, // Negative invalid telemetry
                isMotorOn = true,
                isKillSwitchActive = false,
                timestampMillis = now,
                scenario = scenario,
                rideMode = RideMode.RIDE,
                batteryPercent = 91,
                rangeKm = 115,
                odometerKm = 1326.8,
                isSideStandDown = false,
                isHighBeamOn = false,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
            VehicleScenario.STALE_DATA -> VehicleData(
                speedKmh = 0f,
                isMotorOn = false,
                isKillSwitchActive = false,
                timestampMillis = now - 5000L, // Backdated timestamp simulating silence
                scenario = scenario,
                rideMode = RideMode.PARK,
                batteryPercent = 91,
                rangeKm = 115,
                odometerKm = 1326.8,
                isSideStandDown = true,
                isHighBeamOn = false,
                isLeftTurnActive = false,
                isRightTurnActive = false
            )
        }
    }
}
