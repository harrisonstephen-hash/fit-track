package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DailyStepEntity
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TimeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ChartMetric {
    STEPS, ACTIVE_MINUTES, CALORIES
}

@Composable
fun WeeklyHistoryCard(
    weeklyRecords: List<DailyStepEntity>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.STEPS) }
    var selectedRecord by remember { mutableStateOf<DailyStepEntity?>(null) }

    // Ensure we have 7 days representation
    val displayRecords = remember(weeklyRecords) {
        if (weeklyRecords.isEmpty()) {
            val today = LocalDate.now()
            (0..6).reversed().map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                DailyStepEntity(date, 7000 + (daysAgo * 350), 10000, 5.2, 350, 40)
            }
        } else {
            weeklyRecords.sortedBy { it.date }
        }
    }

    val totalSteps = displayRecords.sumOf { it.steps }
    val avgSteps = if (displayRecords.isNotEmpty()) totalSteps / displayRecords.size else 0
    val totalActiveMins = displayRecords.sumOf { it.activeMinutes }
    val totalCalories = displayRecords.sumOf { it.caloriesBurned }
    val bestDay = displayRecords.maxByOrNull { it.steps }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_history_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weekly Activity (Past 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMetric == ChartMetric.STEPS,
                    onClick = { selectedMetric = ChartMetric.STEPS },
                    label = { Text("Steps") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = EmeraldPrimary
                    )
                )
                FilterChip(
                    selected = selectedMetric == ChartMetric.ACTIVE_MINUTES,
                    onClick = { selectedMetric = ChartMetric.ACTIVE_MINUTES },
                    label = { Text("Active Mins") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TimeColor.copy(alpha = 0.25f),
                        selectedLabelColor = TimeColor
                    )
                )
                FilterChip(
                    selected = selectedMetric == ChartMetric.CALORIES,
                    onClick = { selectedMetric = ChartMetric.CALORIES },
                    label = { Text("Calories") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CalorieColor.copy(alpha = 0.25f),
                        selectedLabelColor = CalorieColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-day Bar Chart Canvas
            WeeklyBarChartView(
                records = displayRecords,
                metric = selectedMetric,
                onSelectRecord = { selectedRecord = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day or Weekly Summary
            if (selectedRecord != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selected: ${selectedRecord?.date}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${selectedRecord?.steps} steps • ${selectedRecord?.activeMinutes} mins • ${selectedRecord?.caloriesBurned} kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${Math.round(((selectedRecord?.steps ?: 0) / 10000.0) * 100)}% Goal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Summary Stats Grid (4 Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Steps",
                    value = "%,d".format(totalSteps),
                    color = EmeraldPrimary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Average",
                    value = "%,d".format(avgSteps),
                    color = TimeColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Time",
                    value = "${totalActiveMins / 60}h ${totalActiveMins % 60}m",
                    color = TimeColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Burned",
                    value = "%,d kcal".format(totalCalories),
                    color = CalorieColor
                )
            }

            if (bestDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Peak Performance: %,d steps on ${bestDay.date}".format(bestDay.steps),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyBarChartView(
    records: List<DailyStepEntity>,
    metric: ChartMetric,
    onSelectRecord: (DailyStepEntity) -> Unit
) {
    val maxVal = remember(records, metric) {
        val max = records.maxOfOrNull {
            when (metric) {
                ChartMetric.STEPS -> it.steps
                ChartMetric.ACTIVE_MINUTES -> it.activeMinutes
                ChartMetric.CALORIES -> it.caloriesBurned
            }
        } ?: 10000
        (max * 1.15f).coerceAtLeast(10f)
    }

    val primaryColor = when (metric) {
        ChartMetric.STEPS -> EmeraldPrimary
        ChartMetric.ACTIVE_MINUTES -> TimeColor
        ChartMetric.CALORIES -> CalorieColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        records.takeLast(7).forEach { record ->
            val value = when (metric) {
                ChartMetric.STEPS -> record.steps
                ChartMetric.ACTIVE_MINUTES -> record.activeMinutes
                ChartMetric.CALORIES -> record.caloriesBurned
            }
            val heightFraction = (value / maxVal).coerceIn(0.08f, 1f)
            val dayLabel = try {
                LocalDate.parse(record.date).dayOfWeek.name.take(3)
            } catch (e: Exception) {
                record.date.takeLast(2)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectRecord(record) }
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = when (metric) {
                        ChartMetric.STEPS -> "${value / 1000}k"
                        ChartMetric.ACTIVE_MINUTES -> "${value}m"
                        ChartMetric.CALORIES -> "$value"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // The bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((110 * heightFraction).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(primaryColor, primaryColor.copy(alpha = 0.5f))
                            )
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
