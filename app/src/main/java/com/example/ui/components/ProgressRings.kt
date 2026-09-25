package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TimeColor

@Composable
fun MultiMetricRingsCard(
    steps: Int,
    stepGoal: Int,
    activeMinutes: Int,
    workoutGoalMinutes: Int,
    caloriesBurned: Int,
    calorieGoal: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_progress_rings_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Activity Target",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Concentric Rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                ConcentricFitnessRings(
                    stepsProgress = (steps.toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                    workoutProgress = (activeMinutes.toFloat() / workoutGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f),
                    calorieProgress = (caloriesBurned.toFloat() / calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
                    size = 190.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Steps",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "%,d".format(steps),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/ %,d steps".format(stepGoal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Row with 3 Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricLegendItem(
                    title = "Steps",
                    value = "$steps",
                    target = "$stepGoal",
                    unit = "steps",
                    color = EmeraldPrimary,
                    icon = Icons.Default.DirectionsWalk
                )
                MetricLegendItem(
                    title = "Workout",
                    value = "$activeMinutes",
                    target = "$workoutGoalMinutes",
                    unit = "mins",
                    color = TimeColor,
                    icon = Icons.Default.Timer
                )
                MetricLegendItem(
                    title = "Calories",
                    value = "$caloriesBurned",
                    target = "$calorieGoal",
                    unit = "kcal",
                    color = CalorieColor,
                    icon = Icons.Default.LocalFireDepartment
                )
            }
        }
    }
}

@Composable
fun ConcentricFitnessRings(
    stepsProgress: Float,
    workoutProgress: Float,
    calorieProgress: Float,
    size: Dp
) {
    val animatedSteps = remember { Animatable(0f) }
    val animatedWorkout = remember { Animatable(0f) }
    val animatedCalories = remember { Animatable(0f) }

    LaunchedEffect(stepsProgress) {
        animatedSteps.animateTo(stepsProgress, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(workoutProgress) {
        animatedWorkout.animateTo(workoutProgress, tween(900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(calorieProgress) {
        animatedCalories.animateTo(calorieProgress, tween(900, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = 11.dp.toPx()
        val spacing = 15.dp.toPx()
        val center = Offset(size.toPx() / 2, size.toPx() / 2)

        // Outer Ring: Steps (Emerald)
        val outerRadius = (size.toPx() - strokeWidth) / 2
        drawCircle(
            color = EmeraldPrimary.copy(alpha = 0.15f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(EmeraldPrimary.copy(0.7f), EmeraldPrimary)),
            startAngle = -90f,
            sweepAngle = animatedSteps.value * 360f,
            useCenter = false,
            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Middle Ring: Workout Time (Cyan)
        val middleRadius = outerRadius - spacing
        drawCircle(
            color = TimeColor.copy(alpha = 0.15f),
            radius = middleRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(TimeColor.copy(0.7f), TimeColor)),
            startAngle = -90f,
            sweepAngle = animatedWorkout.value * 360f,
            useCenter = false,
            topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
            size = Size(middleRadius * 2, middleRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Inner Ring: Calories (Orange)
        val innerRadius = middleRadius - spacing
        drawCircle(
            color = CalorieColor.copy(alpha = 0.15f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(CalorieColor.copy(0.7f), CalorieColor)),
            startAngle = -90f,
            sweepAngle = animatedCalories.value * 360f,
            useCenter = false,
            topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
            size = Size(innerRadius * 2, innerRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MetricLegendItem(
    title: String,
    value: String,
    target: String,
    unit: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Goal: $target",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
