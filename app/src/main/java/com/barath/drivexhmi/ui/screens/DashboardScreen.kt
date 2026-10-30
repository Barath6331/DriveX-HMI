package com.barath.drivexhmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barath.drivexhmi.model.RideMode
import com.barath.drivexhmi.ui.components.AlertBanner
import com.barath.drivexhmi.ui.components.BottomIndicatorsBar
import com.barath.drivexhmi.ui.components.KillSwitchBadge
import com.barath.drivexhmi.ui.components.MotorStatusBadge
import com.barath.drivexhmi.ui.components.SimulationControlPanel
import com.barath.drivexhmi.ui.components.SpeedometerGauge
import com.barath.drivexhmi.ui.components.TopStatusBar
import com.barath.drivexhmi.ui.model.DashboardUiState
import com.barath.drivexhmi.ui.theme.CarbonDark
import com.barath.drivexhmi.ui.theme.SurfaceDark
import com.barath.drivexhmi.ui.viewmodel.DashboardViewModel

/**
 * Root Automotive Landscape Ride Dashboard Composable.
 * Presents speedometer, motor status, kill-switch status, and indicators reactively.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onToggleAutoCycle = { viewModel.toggleAutoCycle() },
        onSelectScenario = { scenario -> viewModel.selectScenario(scenario) },
        modifier = modifier
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onToggleAutoCycle: () -> Unit,
    onSelectScenario: (com.barath.drivexhmi.model.VehicleScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to CarbonDark,
                    0.6f to SurfaceDark,
                    1.0f to CarbonDark
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Telemetry Header
            Column(modifier = Modifier.fillMaxWidth()) {
                TopStatusBar(batteryPercent = uiState.batteryPercent)

                // Edge case alert banner (visible only if error or stale data occurs)
                AlertBanner(
                    isStaleData = uiState.isStaleData,
                    staleMessage = uiState.staleWarningMessage,
                    sensorErrorMessage = uiState.sensorErrorMessage,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )
            }

            // 2. Main Cluster Display: Left Badges | Center Speedometer | Right Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Wing: Motor Status
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MotorStatusBadge(
                        isMotorOn = uiState.isMotorOn
                    )
                }

                // Centerpiece: Speedometer Arc & Digital Readout
                Box(
                    modifier = Modifier.weight(1.5f),
                    contentAlignment = Alignment.Center
                ) {
                    SpeedometerGauge(
                        speedDisplay = uiState.speedDisplay,
                        gaugeSpeed = uiState.speedGaugeValue,
                        speedUnit = uiState.speedUnit,
                        rideMode = uiState.rideMode,
                        rangeKm = uiState.rangeKm,
                        isMotorDisabled = uiState.isKillSwitchActive || uiState.rideMode == RideMode.EMERGENCY
                    )
                }

                // Right Wing: Kill Switch Status
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    KillSwitchBadge(
                        isKillSwitchActive = uiState.isKillSwitchActive
                    )
                }
            }

            // 3. Bottom Indicators Bar (Odometer, Side Stand, Lights, Turns)
            BottomIndicatorsBar(
                odometerKm = uiState.odometerKm,
                isSideStandDown = uiState.isSideStandDown,
                isHighBeamOn = uiState.isHighBeamOn,
                isLeftTurnActive = uiState.isLeftTurnActive,
                isRightTurnActive = uiState.isRightTurnActive,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 4. Interactive Simulation Control Dock (Scenario Switcher & Auto-Cycle)
            SimulationControlPanel(
                currentScenario = uiState.currentScenario,
                isAutoCycling = uiState.isAutoCycleActive,
                onToggleAutoCycle = onToggleAutoCycle,
                onSelectScenario = onSelectScenario
            )
        }
    }
}

