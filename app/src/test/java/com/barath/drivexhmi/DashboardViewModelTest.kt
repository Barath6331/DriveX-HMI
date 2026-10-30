package com.barath.drivexhmi

import com.barath.drivexhmi.data.SimulatedVehicleDataSource
import com.barath.drivexhmi.data.VehicleRepositoryImpl
import com.barath.drivexhmi.model.VehicleScenario
import com.barath.drivexhmi.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel correctly translates vehicle scenarios into reactive UI state`() = runTest(testDispatcher) {
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )
        val viewModel = DashboardViewModel(repository)

        val collectJob = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        try {
            runCurrent()

            // 1. Parked state verification
            viewModel.selectScenario(VehicleScenario.PARKED)
            runCurrent()
            var uiState = viewModel.uiState.value
            assertEquals("0", uiState.speedDisplay)
            assertEquals(0f, uiState.speedGaugeValue)
            assertFalse(uiState.isMotorOn)
            assertEquals("MOTOR OFF", uiState.motorStatusText)
            assertFalse(uiState.isKillSwitchActive)
            assertEquals("KILL SWITCH INACTIVE", uiState.killSwitchStatusText)

            // 2. Riding state verification (25 km/h)
            viewModel.selectScenario(VehicleScenario.RIDING)
            runCurrent()
            uiState = viewModel.uiState.value
            assertEquals("25", uiState.speedDisplay)
            assertEquals(25f, uiState.speedGaugeValue)
            assertTrue(uiState.isMotorOn)
            assertEquals("MOTOR ON", uiState.motorStatusText)
            assertFalse(uiState.isKillSwitchActive)

            // 3. Accelerating state verification (50 km/h)
            viewModel.selectScenario(VehicleScenario.ACCELERATING)
            runCurrent()
            uiState = viewModel.uiState.value
            assertEquals("50", uiState.speedDisplay)
            assertEquals(50f, uiState.speedGaugeValue)
            assertTrue(uiState.isMotorOn)

            // 4. Motor disabled state verification (0 km/h, Kill switch Active)
            viewModel.selectScenario(VehicleScenario.MOTOR_DISABLED)
            runCurrent()
            uiState = viewModel.uiState.value
            assertEquals("0", uiState.speedDisplay)
            assertFalse(uiState.isMotorOn)
            assertTrue(uiState.isKillSwitchActive)
            assertEquals("KILL SWITCH ACTIVE", uiState.killSwitchStatusText)
        } finally {
            collectJob.cancel()
            repository.close()
            dataSource.close()
        }
    }

    @Test
    fun `edge case - negative speed displays gracefully without crash`() = runTest(testDispatcher) {
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )
        val viewModel = DashboardViewModel(repository)

        val collectJob = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        try {
            runCurrent()
            viewModel.injectNegativeSpeedEdgeCase()
            runCurrent()

            val uiState = viewModel.uiState.value
            // Speed readout should safely show fallback dashes "--" and clamp gauge to 0
            assertEquals("--", uiState.speedDisplay)
            assertEquals(0f, uiState.speedGaugeValue)
            assertNotNull("Sensor error message must be populated", uiState.sensorErrorMessage)
            assertTrue(uiState.sensorErrorMessage!!.contains("Negative speed"))
        } finally {
            collectJob.cancel()
            repository.close()
            dataSource.close()
        }
    }

    @Test
    fun `edge case - stale CAN signal displays fallback and warning banner`() = runTest(testDispatcher) {
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )
        val viewModel = DashboardViewModel(repository)

        val collectJob = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        try {
            runCurrent()
            viewModel.injectStaleDataEdgeCase()
            runCurrent()

            val uiState = viewModel.uiState.value
            assertTrue(uiState.isStaleData)
            assertEquals("--", uiState.speedDisplay)
            assertNotNull("Stale warning message must be displayed", uiState.staleWarningMessage)
        } finally {
            collectJob.cancel()
            repository.close()
            dataSource.close()
        }
    }

    @Test
    fun `toggleAutoCycle pauses and resumes automatic scenario updates`() = runTest(testDispatcher) {
        val dataSource = SimulatedVehicleDataSource(dispatcher = testDispatcher)
        val repository = VehicleRepositoryImpl(
            dataSource = dataSource,
            dispatcher = testDispatcher
        )
        val viewModel = DashboardViewModel(repository)

        val collectJob = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        try {
            runCurrent()
            assertTrue(viewModel.uiState.value.isAutoCycleActive)

            viewModel.toggleAutoCycle()
            runCurrent()
            assertFalse(viewModel.uiState.value.isAutoCycleActive)

            viewModel.toggleAutoCycle()
            runCurrent()
            assertTrue(viewModel.uiState.value.isAutoCycleActive)
        } finally {
            collectJob.cancel()
            repository.close()
            dataSource.close()
        }
    }
}
