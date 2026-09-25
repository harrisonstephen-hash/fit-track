package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeartRateZone

@Composable
fun HeartRateCard(
    currentBpm: Int,
    restingBpm: Int,
    sourceDeviceName: String,
    modifier: Modifier = Modifier
) {
    val zone = HeartRateZone.fromBpm(currentBpm)

    // Pulsing heartbeat animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_heart")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_heart_rate_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF5252).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(pulseScale)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Heart Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Continuous wearable telemetry",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Zone Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = zone.color.copy(alpha = 0.18f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = zone.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = zone.color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Metric Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$currentBpm",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 38.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Resting: $restingBpm BPM",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Daily Peak: ${(currentBpm + 45).coerceAtMost(185)} BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Heart Rate Zones Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            ) {
                // Resting
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            if (zone == HeartRateZone.RESTING) HeartRateZone.RESTING.color else HeartRateZone.RESTING.color.copy(alpha = 0.3f),
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(2.dp))
                // Fat Burn
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(8.dp)
                        .background(
                            if (zone == HeartRateZone.FAT_BURN) HeartRateZone.FAT_BURN.color else HeartRateZone.FAT_BURN.color.copy(alpha = 0.3f)
                        )
                )
                Spacer(modifier = Modifier.width(2.dp))
                // Cardio
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            if (zone == HeartRateZone.CARDIO) HeartRateZone.CARDIO.color else HeartRateZone.CARDIO.color.copy(alpha = 0.3f)
                        )
                )
                Spacer(modifier = Modifier.width(2.dp))
                // Peak
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(8.dp)
                        .background(
                            if (zone == HeartRateZone.PEAK) HeartRateZone.PEAK.color else HeartRateZone.PEAK.color.copy(alpha = 0.3f),
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        )
                )
            }

            // Source Attribution Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Watch,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (sourceDeviceName.isNotBlank() && sourceDeviceName != "None")
                        "Synced via $sourceDeviceName"
                    else
                        "Connect a wearable device to sync optical HR",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
