package com.barath.drivexhmi.data

import com.barath.drivexhmi.model.DataFreshness
import com.barath.drivexhmi.model.ProcessedVehicleState
import com.barath.drivexhmi.model.SpeedSensorStatus
import com.barath.drivexhmi.model.VehicleData
import com.barath.drivexhmi.model.VehicleScenario
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.Closeable

/**
 * Interface defining domain data access and safety processing for vehicle telemetry.
 */
interface VehicleRepository : Closeable {
    val vehicleState: StateFlow<ProcessedVehicleState>
    val isAutoCycleActive: StateFlow<Boolean>
    val currentScenario: StateFlow<VehicleScenario>

    fun setAutoCycling(enabled: Boolean)
    fun selectScenario(scenario: VehicleScenario)
    fun nextScenario()
}

/**
 * Default implementation of [VehicleRepository] that observes the data source,
 * validates incoming telemetry, enforces safety thresholds, and maintains a
 * heartbeat watchdog to detect stale CAN frames.
 */
class VehicleRepositoryImpl(
    private val dataSource: VehicleDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val staleTimeoutMs: Long = 2500L,
    private val watchdogTickMs: Long = 500L,
    externalScope: CoroutineScope? = null
) : VehicleRepository {

    private val internalJob = SupervisorJob()
    private val scope = externalScope ?: CoroutineScope(dispatcher + internalJob)

    private var lastReceivedTimestamp: Long = System.currentTimeMillis()
    private var lastRawData: VehicleData? = null

    private val _vehicleState = MutableStateFlow(
        createInitialState()
    )
    override val vehicleState: StateFlow<ProcessedVehicleState> = _vehicleState.asStateFlow()

    override val isAutoCycleActive: StateFlow<Boolean> = dataSource.isAutoCycleActive
    override val currentScenario: StateFlow<VehicleScenario> = dataSource.currentScenario

    private var dataCollectionJob: Job? = null
    private var watchdogJob: Job? = null

    init {
        // Collect telemetry from data source
        dataCollectionJob = scope.launch {
            dataSource.vehicleDataStream.collect { rawData ->
                lastReceivedTimestamp = System.currentTimeMillis()
                lastRawData = rawData
                _vehicleState.value = processVehicleData(rawData, DataFreshness.Fresh)
            }
        }

        // Heartbeat Watchdog timer to detect stale/missing CAN telemetry
        watchdogJob = scope.launch {
            while (isActive) {
                delay(watchdogTickMs)
                val now = System.currentTimeMillis()
                val elapsed = now - lastReceivedTimestamp
                if (elapsed > staleTimeoutMs) {
                    val current = _vehicleState.value
                    if (current.freshness !is DataFreshness.Stale) {
                        _vehicleState.value = current.copy(
                            freshness = DataFreshness.Stale(elapsed)
                        )
                    }
                }
            }
        }
    }

    override fun setAutoCycling(enabled: Boolean) {
        dataSource.setAutoCycling(enabled)
    }

    override fun selectScenario(scenario: VehicleScenario) {
        if (scenario == VehicleScenario.STALE_DATA) {
            // Emulate data loss by backdating heartbeat
            lastReceivedTimestamp = System.currentTimeMillis() - (staleTimeoutMs + 1000L)
            dataSource.selectScenario(scenario)
            val current = _vehicleState.value
            _vehicleState.value = current.copy(
                freshness = DataFreshness.Stale(staleTimeoutMs + 1000L),
                scenario = VehicleScenario.STALE_DATA
            )
        } else {
            dataSource.selectScenario(scenario)
        }
    }

    override fun nextScenario() {
        dataSource.nextScenario()
    }

    override fun close() {
        dataCollectionJob?.cancel()
        watchdogJob?.cancel()
        internalJob.cancel()
        (dataSource as? Closeable)?.close()
    }

    private fun processVehicleData(
        data: VehicleData,
        freshness: DataFreshness
    ): ProcessedVehicleState {
        val speedStatus = validateSpeed(data.speedKmh)
        return ProcessedVehicleState(
            speedStatus = speedStatus,
            isMotorOn = data.isMotorOn,
            isKillSwitchActive = data.isKillSwitchActive,
            freshness = freshness,
            scenario = data.scenario,
            rideMode = data.rideMode,
            batteryPercent = data.batteryPercent,
            rangeKm = data.rangeKm,
            odometerKm = data.odometerKm,
            isSideStandDown = data.isSideStandDown,
            isHighBeamOn = data.isHighBeamOn,
            isLeftTurnActive = data.isLeftTurnActive,
            isRightTurnActive = data.isRightTurnActive,
            lastUpdatedMillis = data.timestampMillis
        )
    }

    /**
     * Validates raw speed telemetry against physical vehicle limits.
     * Edge case handling for negative speeds, NaN, or extreme sensor glitches.
     */
    internal fun validateSpeed(speed: Float): SpeedSensorStatus {
        return when {
            speed.isNaN() -> SpeedSensorStatus.Invalid(
                rawValue = speed,
                reason = "Sensor returned NaN (Invalid Signal)"
            )
            speed < 0f -> SpeedSensorStatus.Invalid(
                rawValue = speed,
                reason = "Negative speed sensor error (${speed.toInt()} km/h)"
            )
            speed > 200f -> SpeedSensorStatus.Invalid(
                rawValue = speed,
                reason = "Over-limit sensor spike (${speed.toInt()} km/h > 200)"
            )
            else -> SpeedSensorStatus.Valid(speed)
        }
    }

    private fun createInitialState(): ProcessedVehicleState {
        return ProcessedVehicleState(
            speedStatus = SpeedSensorStatus.Valid(0f),
            isMotorOn = false,
            isKillSwitchActive = false,
            freshness = DataFreshness.Fresh,
            scenario = VehicleScenario.PARKED,
            rideMode = com.barath.drivexhmi.model.RideMode.PARK,
            batteryPercent = 94,
            rangeKm = 122,
            odometerKm = 1324.6,
            isSideStandDown = true,
            isHighBeamOn = false,
            isLeftTurnActive = false,
            isRightTurnActive = false,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }
}
