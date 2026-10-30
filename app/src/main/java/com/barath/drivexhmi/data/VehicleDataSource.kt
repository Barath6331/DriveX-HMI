package com.barath.drivexhmi.data

import com.barath.drivexhmi.model.VehicleData
import com.barath.drivexhmi.model.VehicleScenario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for vehicle telemetry data streaming.
 * Decouples the UI and domain logic from the concrete simulated or CAN data provider.
 */
interface VehicleDataSource {
    /**
     * Continuous reactive stream of vehicle telemetry updates.
     */
    val vehicleDataStream: Flow<VehicleData>

    /**
     * Flow indicating whether auto-cycling across scenarios is currently active.
     */
    val isAutoCycleActive: StateFlow<Boolean>

    /**
     * Current scenario being simulated.
     */
    val currentScenario: StateFlow<VehicleScenario>

    /**
     * Enables or disables automatic scenario stepping.
     */
    fun setAutoCycling(enabled: Boolean)

    /**
     * Manually forces a specific scenario, pausing auto-cycle if requested.
     */
    fun selectScenario(scenario: VehicleScenario, keepAutoCycling: Boolean = false)

    /**
     * Advances to the next sequential scenario immediately.
     */
    fun nextScenario()
}

