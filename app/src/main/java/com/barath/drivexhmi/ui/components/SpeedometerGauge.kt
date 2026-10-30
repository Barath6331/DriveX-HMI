package com.barath.drivexhmi.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barath.drivexhmi.model.RideMode
import com.barath.drivexhmi.ui.theme.AlertRed
import com.barath.drivexhmi.ui.theme.CardBackground
import com.barath.drivexhmi.ui.theme.ElectricCyan
import com.barath.drivexhmi.ui.theme.ElectricTeal
import com.barath.drivexhmi.ui.theme.EmeraldGreen
import com.barath.drivexhmi.ui.theme.SonicOrange
import com.barath.drivexhmi.ui.theme.TextMuted
import com.barath.drivexhmi.ui.theme.TextPrimary
import com.barath.drivexhmi.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance automotive digital speedometer gauge featuring an animated curved
 * radial arc, thruster speed sweep, ride mode pill, and bold digital readout.
 */
@Composable
fun SpeedometerGauge(
    speedDisplay: String,
    gaugeSpeed: Float,
    speedUnit: String,
    rideMode: RideMode,
    rangeKm: Int,
    isMotorDisabled: Boolean,
    modifier: Modifier = Modifier
) {
    val maxGaugeSpeed = 100f
    val clampedSpeed = gaugeSpeed.coerceIn(0f, maxGaugeSpeed)

    // Smooth animation for speedometer needle / arc fill
    val animatedProgress by animateFloatAsState(
        targetValue = clampedSpeed / maxGaugeSpeed,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "SpeedArcProgress"
    )

    // Dynamic accent color based on speed and drive mode
    val activeColor by animateColorAsState(
        targetValue = when {
            isMotorDisabled -> AlertRed
            clampedSpeed >= 40f -> SonicOrange
            clampedSpeed >= 15f -> ElectricCyan
            else -> ElectricTeal
        },
        animationSpec = tween(durationMillis = 400),
        label = "GaugeColor"
    )

    Box(
        modifier = modifier.size(340.dp, 240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Curved dial arc canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = size.width - strokeWidth * 2
            val topLeft = Offset(strokeWidth, strokeWidth * 0.8f)

            val startAngle = 145f
            val sweepAngle = 250f

            // Background track arc
            drawArc(
                color = Color(0xFF141C2B),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress arc
            if (animatedProgress > 0.005f) {
                val currentSweep = sweepAngle * animatedProgress
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to ElectricCyan,
                        0.5f to activeColor,
                        1.0f to activeColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Radial tick marks
            val center = Offset(size.width / 2f, size.height * 0.58f)
            val radius = arcSize / 2f + strokeWidth * 0.7f
            val numTicks = 25
            for (i in 0..numTicks) {
                val fraction = i.toFloat() / numTicks
                val angleDeg = startAngle + sweepAngle * fraction
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val tickLength = if (i % 5 == 0) 12.dp.toPx() else 6.dp.toPx()
                val tickColor = if (fraction <= animatedProgress) activeColor.copy(alpha = 0.9f) else Color(0xFF233047)

                val start = Offset(
                    x = (center.x + (radius - tickLength) * cos(angleRad)).toFloat(),
                    y = (center.y + (radius - tickLength) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = (center.x + radius * cos(angleRad)).toFloat(),
                    y = (center.y + radius * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 5 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Center cluster contents: Mode badge + Range, Speed Readout, Unit
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            // Mode & Range Pill Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(CardBackground.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF28364F), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                // Mode Badge
                Text(
                    text = rideMode.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = when (rideMode) {
                        RideMode.SONIC -> SonicOrange
                        RideMode.EMERGENCY -> AlertRed
                        RideMode.RIDE -> ElectricCyan
                        RideMode.ECO -> EmeraldGreen
                        RideMode.PARK -> TextSecondary
                    },
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(TextMuted, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))

                // Range Estimate
                Text(
                    text = "$rangeKm km Range",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Speed Large Readout
            Text(
                text = speedDisplay,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (speedDisplay.length > 2) 74.sp else 86.sp
                ),
                color = if (isMotorDisabled) AlertRed else TextPrimary,
                fontWeight = FontWeight.Black
            )

            // Speed Unit
            Text(
                text = speedUnit,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.offset(y = (-8).dp)
            )
        }
    }
}

