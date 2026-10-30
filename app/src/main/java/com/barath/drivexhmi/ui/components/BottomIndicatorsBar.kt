package com.barath.drivexhmi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barath.drivexhmi.ui.theme.CardBackground
import com.barath.drivexhmi.ui.theme.ElectricCyan
import com.barath.drivexhmi.ui.theme.EmeraldGreen
import com.barath.drivexhmi.ui.theme.TextMuted
import com.barath.drivexhmi.ui.theme.TextPrimary
import com.barath.drivexhmi.ui.theme.TextSecondary
import com.barath.drivexhmi.ui.theme.WarningAmber

/**
 * Bottom cluster indicators bar showing side stand status, turn signals, high beams,
 * and persistent vehicle odometer telemetry.
 */
@Composable
fun BottomIndicatorsBar(
    odometerKm: Double,
    isSideStandDown: Boolean,
    isHighBeamOn: Boolean,
    isLeftTurnActive: Boolean,
    isRightTurnActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(CardBackground.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF222F42), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Turn Indicator
        Text(
            text = "◀",
            color = if (isLeftTurnActive) EmeraldGreen else Color(0xFF253347),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(16.dp))

        // High Beam Indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isHighBeamOn) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent,
                    CircleShape
                )
                .border(
                    1.dp,
                    if (isHighBeamOn) ElectricCyan else Color(0xFF2E3D52),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⪪",
                color = if (isHighBeamOn) ElectricCyan else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Side Stand Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    if (isSideStandDown) WarningAmber.copy(alpha = 0.2f) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isSideStandDown) "⟂ STAND DOWN" else "⟂ STAND UP",
                color = if (isSideStandDown) WarningAmber else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Odometer Readout
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ODO",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%.1f km".format(odometerKm),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Turn Indicator
        Text(
            text = "▶",
            color = if (isRightTurnActive) EmeraldGreen else Color(0xFF253347),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

