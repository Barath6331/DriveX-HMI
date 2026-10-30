package com.barath.drivexhmi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barath.drivexhmi.ui.theme.BorderSubtle
import com.barath.drivexhmi.ui.theme.CardBackground
import com.barath.drivexhmi.ui.theme.EmeraldGreen
import com.barath.drivexhmi.ui.theme.EmeraldGreenGlow
import com.barath.drivexhmi.ui.theme.TextMuted
import com.barath.drivexhmi.ui.theme.TextPrimary
import com.barath.drivexhmi.ui.theme.TextSecondary

/**
 * Automotive module clearly presenting Motor ON / Motor OFF state
 * using distinct visual color cues, status halos, and typography.
 */
@Composable
fun MotorStatusBadge(
    isMotorOn: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isMotorOn) EmeraldGreen else BorderSubtle,
        animationSpec = tween(durationMillis = 350),
        label = "MotorBorderColor"
    )

    val haloColor by animateColorAsState(
        targetValue = if (isMotorOn) EmeraldGreenGlow else Color.Transparent,
        animationSpec = tween(durationMillis = 350),
        label = "MotorHaloColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isMotorOn) EmeraldGreen else TextSecondary,
        animationSpec = tween(durationMillis = 350),
        label = "MotorTextColor"
    )

    Box(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Illuminated status ring
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(haloColor)
                    .border(2.dp, if (isMotorOn) EmeraldGreen else TextMuted, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isMotorOn) EmeraldGreen else TextMuted)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "MOTOR STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isMotorOn) "MOTOR ON" else "MOTOR OFF",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isMotorOn) "Inverter Engaged" else "Inverter Standby",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMotorOn) TextSecondary else TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

