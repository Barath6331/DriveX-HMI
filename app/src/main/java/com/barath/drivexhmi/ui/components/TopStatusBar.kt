package com.barath.drivexhmi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Top automotive status bar presenting telemetry meta-data:
 * device pairing, current time, vehicle brand mark, and high-visibility battery SOC.
 */
@Composable
fun TopStatusBar(
    batteryPercent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Device & Network Status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ DriveX One",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(TextMuted, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BLE Paired",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Center: Date & Time
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "10:48 AM",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• FRI 24 OCT",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Right: Battery SOC Pill & LTE
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF222F42), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                // Battery outline
                Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, EmeraldGreen, RoundedCornerShape(2.dp))
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (batteryPercent / 100f).coerceIn(0f, 1f))
                            .background(EmeraldGreen)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "$batteryPercent%",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "LTE ıll",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

