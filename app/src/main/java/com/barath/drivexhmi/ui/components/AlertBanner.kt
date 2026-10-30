package com.barath.drivexhmi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.barath.drivexhmi.ui.theme.AlertRed
import com.barath.drivexhmi.ui.theme.WarningAmber

/**
 * Diagnostic alert banner surfacing edge conditions (sensor error, stale CAN bus data)
 * safely and gracefully to the driver without crashing or freezing the UI.
 */
@Composable
fun AlertBanner(
    isStaleData: Boolean,
    staleMessage: String?,
    sensorErrorMessage: String?,
    modifier: Modifier = Modifier
) {
    val isVisible = isStaleData || sensorErrorMessage != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val isCritical = sensorErrorMessage != null
        val accentColor = if (isCritical) AlertRed else WarningAmber
        val bgColor = if (isCritical) Color(0xFF2C0F14) else Color(0xFF2E220D)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(1.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCritical) "⚠️ SENSOR FAULT" else "⚡ SIGNAL WARNING",
                    color = accentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = sensorErrorMessage ?: staleMessage ?: "Telemetry anomaly detected",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

