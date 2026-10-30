package com.barath.drivexhmi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barath.drivexhmi.model.VehicleScenario
import com.barath.drivexhmi.ui.theme.AlertRed
import com.barath.drivexhmi.ui.theme.CardBackground
import com.barath.drivexhmi.ui.theme.ElectricCyan
import com.barath.drivexhmi.ui.theme.EmeraldGreen
import com.barath.drivexhmi.ui.theme.TextMuted
import com.barath.drivexhmi.ui.theme.TextPrimary
import com.barath.drivexhmi.ui.theme.TextSecondary
import com.barath.drivexhmi.ui.theme.WarningAmber

/**
 * Interactive simulation control dock allowing candidates and reviewers to:
 * 1. Observe the automatic 1-second cycling across all 5 mandatory scenarios.
 * 2. Pause / Resume auto-cycling.
 * 3. Step or jump directly into any scenario.
 * 4. Inject edge conditions (Negative speed sensor fault, Stale CAN signal loss).
 */
@Composable
fun SimulationControlPanel(
    currentScenario: VehicleScenario,
    isAutoCycling: Boolean,
    onToggleAutoCycle: () -> Unit,
    onSelectScenario: (VehicleScenario) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground.copy(alpha = 0.95f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, Color(0xFF263347), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top compact summary strip with expand toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto-cycle indicator & Play/Pause toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleAutoCycle() }
                    .background(Color(0xFF0F1522))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAutoCycling) EmeraldGreen else WarningAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAutoCycling) "AUTO-CYCLE: ON (1s)" else "AUTO-CYCLE: PAUSED",
                    color = if (isAutoCycling) EmeraldGreen else WarningAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAutoCycling) "⏸" else "▶",
                    color = TextPrimary,
                    fontSize = 11.sp
                )
            }

            // Current Scenario Pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVE SCENARIO: ",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentScenario.title,
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${currentScenario.description})",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            // Expand / Collapse quick control buttons
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .background(Color(0xFF1E2838))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Hide Controls ▾" else "Scenario Switcher ▴",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Expanded scenario & edge case selector drawer
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val scenarios = listOf(
                        VehicleScenario.PARKED,
                        VehicleScenario.READY,
                        VehicleScenario.RIDING,
                        VehicleScenario.ACCELERATING,
                        VehicleScenario.MOTOR_DISABLED
                    )

                    scenarios.forEach { scenario ->
                        val isSelected = currentScenario == scenario
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectScenario(scenario) }
                                .background(
                                    if (isSelected) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF131A26)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else Color(0xFF222F42),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = scenario.title,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Edge case injector buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edge Case 1: Negative Speed
                    val isInvalidSpeed = currentScenario == VehicleScenario.INVALID_SPEED
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectScenario(VehicleScenario.INVALID_SPEED) }
                            .background(
                                if (isInvalidSpeed) AlertRed.copy(alpha = 0.25f) else Color(0xFF1A1318)
                            )
                            .border(
                                1.dp,
                                if (isInvalidSpeed) AlertRed else Color(0xFF42222E),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ Inject Edge: Negative Speed (-15 km/h)",
                            color = if (isInvalidSpeed) AlertRed else Color(0xFFFF8A9E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Edge Case 2: Stale CAN Signal
                    val isStale = currentScenario == VehicleScenario.STALE_DATA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectScenario(VehicleScenario.STALE_DATA) }
                            .background(
                                if (isStale) WarningAmber.copy(alpha = 0.25f) else Color(0xFF1A1813)
                            )
                            .border(
                                1.dp,
                                if (isStale) WarningAmber else Color(0xFF423822),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏱ Inject Edge: Stale CAN Signal (Timeout)",
                            color = if (isStale) WarningAmber else Color(0xFFFFD180),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

