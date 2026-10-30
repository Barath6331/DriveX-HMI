package com.barath.drivexhmi

import com.barath.drivexhmi.data.SimulatedVehicleDataSource
import com.barath.drivexhmi.data.VehicleRepositoryImpl
import com.barath.drivexhmi.model.DataFreshness
import com.barath.drivexhmi.model.SpeedSensorStatus
import com.barath.drivexhmi.model.VehicleScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleRepositoryTest {

    @Test
    fun `validateSpeed correctly classifies normal, negative, and extreme speeds`() {
        val testDispatcher = StandardTestDispatcher()
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )

        try {
            // Valid speed
            val validStatus = repository.validateSpeed(25f)
            assertTrue(validStatus is SpeedSensorStatus.Valid)
            assertEquals(25f, (validStatus as SpeedSensorStatus.Valid).speedKmh)

            // Edge case 1: Negative speed
            val negativeStatus = repository.validateSpeed(-15f)
            assertTrue(negativeStatus is SpeedSensorStatus.Invalid)
            assertTrue((negativeStatus as SpeedSensorStatus.Invalid).reason.contains("Negative speed"))

            // Edge case 2: NaN speed
            val nanStatus = repository.validateSpeed(Float.NaN)
            assertTrue(nanStatus is SpeedSensorStatus.Invalid)
            assertTrue((nanStatus as SpeedSensorStatus.Invalid).reason.contains("NaN"))

            // Edge case 3: Extreme sensor spike (> 200 km/h)
            val overLimitStatus = repository.validateSpeed(250f)
            assertTrue(overLimitStatus is SpeedSensorStatus.Invalid)
            assertTrue((overLimitStatus as SpeedSensorStatus.Invalid).reason.contains("Over-limit"))
        } finally {
            repository.close()
            dataSource.close()
        }
    }

    @Test
    fun `selectScenario updates vehicle state across mandatory scenarios`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )

        try {
            runCurrent()

            // 1. Parked: 0 km/h | Motor OFF | Kill switch Inactive
            repository.selectScenario(VehicleScenario.PARKED)
            runCurrent()
            var state = repository.vehicleState.value
            assertEquals(VehicleScenario.PARKED, state.scenario)
            assertTrue(state.speedStatus is SpeedSensorStatus.Valid)
            assertEquals(0f, (state.speedStatus as SpeedSensorStatus.Valid).speedKmh)
            assertFalse(state.isMotorOn)
            assertFalse(state.isKillSwitchActive)

            // 2. Ready: 0 km/h | Motor ON | Kill switch Inactive
            repository.selectScenario(VehicleScenario.READY)
            runCurrent()
            state = repository.vehicleState.value
            assertEquals(VehicleScenario.READY, state.scenario)
            assertEquals(0f, (state.speedStatus as SpeedSensorStatus.Valid).speedKmh)
            assertTrue(state.isMotorOn)
            assertFalse(state.isKillSwitchActive)

            // 3. Riding: 25 km/h | Motor ON | Kill switch Inactive
            repository.selectScenario(VehicleScenario.RIDING)
            runCurrent()
            state = repository.vehicleState.value
            assertEquals(VehicleScenario.RIDING, state.scenario)
            assertEquals(25f, (state.speedStatus as SpeedSensorStatus.Valid).speedKmh)
            assertTrue(state.isMotorOn)
            assertFalse(state.isKillSwitchActive)

            // 4. Accelerating: 50 km/h | Motor ON | Kill switch Inactive
            repository.selectScenario(VehicleScenario.ACCELERATING)
            runCurrent()
            state = repository.vehicleState.value
            assertEquals(VehicleScenario.ACCELERATING, state.scenario)
            assertEquals(50f, (state.speedStatus as SpeedSensorStatus.Valid).speedKmh)
            assertTrue(state.isMotorOn)
            assertFalse(state.isKillSwitchActive)

            // 5. Motor disabled: 0 km/h | Motor OFF | Kill switch Active
            repository.selectScenario(VehicleScenario.MOTOR_DISABLED)
            runCurrent()
            state = repository.vehicleState.value
            assertEquals(VehicleScenario.MOTOR_DISABLED, state.scenario)
            assertEquals(0f, (state.speedStatus as SpeedSensorStatus.Valid).speedKmh)
            assertFalse(state.isMotorOn)
            assertTrue(state.isKillSwitchActive)
            assertTrue(state.hasSafetyAlert)
        } finally {
            repository.close()
            dataSource.close()
        }
    }

    @Test
    fun `edge case - stale CAN signal triggers watchdog and marks state as stale`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher,
            staleTimeoutMs = 2000L,
            watchdogTickMs = 500L
        )

        try {
            runCurrent()
            // Simulate stale data event
            repository.selectScenario(VehicleScenario.STALE_DATA)
            runCurrent()

            val state = repository.vehicleState.value
            assertTrue("Expected state freshness to be Stale", state.freshness is DataFreshness.Stale)
            assertTrue("Expected hasSafetyAlert to be true during stale data", state.hasSafetyAlert)
        } finally {
            repository.close()
            dataSource.close()
        }
    }
}
