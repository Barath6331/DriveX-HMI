package com.barath.drivexhmi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import com.barath.drivexhmi.ui.theme.AlertRed
import com.barath.drivexhmi.ui.theme.AlertRedGlow
import com.barath.drivexhmi.ui.theme.BorderSubtle
import com.barath.drivexhmi.ui.theme.CardBackground
import com.barath.drivexhmi.ui.theme.TextMuted
import com.barath.drivexhmi.ui.theme.TextPrimary
import com.barath.drivexhmi.ui.theme.TextSecondary

/**
 * Automotive kill-switch indicator with distinct visuals for Active vs Inactive states.
 * When active, displays high-priority warning cues and pulsing alert halo to warn the rider.
 */
@Composable
fun KillSwitchBadge(
    isKillSwitchActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Pulse animation when kill switch is active (safety interlock)
    val infiniteTransition = rememberInfiniteTransition(label = "KillSwitchPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isKillSwitchActive) Color(0xFF261014) else CardBackground,
        animationSpec = tween(durationMillis = 350),
        label = "KillSwitchBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isKillSwitchActive) AlertRed else BorderSubtle,
        animationSpec = tween(durationMillis = 350),
        label = "KillSwitchBorder"
    )

    val titleColor by animateColorAsState(
        targetValue = if (isKillSwitchActive) AlertRed else TextPrimary,
        animationSpec = tween(durationMillis = 350),
        label = "KillSwitchTitle"
    )

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon halo / indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isKillSwitchActive) AlertRedGlow.copy(alpha = pulseAlpha) else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (isKillSwitchActive) AlertRed else Color(0xFF38455A),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isKillSwitchActive) "✕" else "✓",
                    color = if (isKillSwitchActive) AlertRed else Color(0xFF5E7290),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "KILL SWITCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isKillSwitchActive) AlertRed.copy(alpha = 0.8f) else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isKillSwitchActive) "ACTIVE" else "INACTIVE",
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isKillSwitchActive) "Power Interlock • Motor Cut" else "Circuit Closed • Drive Allowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isKillSwitchActive) AlertRed.copy(alpha = 0.85f) else TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

