package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.WorkoutEntity
import com.example.data.model.WorkoutTypeCatalog
import com.example.ui.components.AddOrEditWorkoutDialog
import com.example.ui.components.DailyMotivationalCard
import com.example.ui.components.HeartRateCard
import com.example.ui.components.MultiMetricRingsCard
import com.example.ui.components.WearableStatusBar
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.TimeColor
import com.example.ui.viewmodel.FitTrackViewModel

@Composable
fun DashboardScreen(
    viewModel: FitTrackViewModel,
    onNavigateToWorkout: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToWearables: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todaySteps by viewModel.todayStepRecord.collectAsState()
    val todayStats by viewModel.todayWorkoutStats.collectAsState()
    val todayWorkouts by viewModel.todayWorkouts.collectAsState()
    val sensorStatus by viewModel.sensorStatus.collectAsState()
    val connectedWearables by viewModel.connectedWearables.collectAsState()
    val isSyncingWearables by viewModel.isSyncingWearables.collectAsState()
    val syncStatusText by viewModel.syncStatusText.collectAsState()
    val liveHeartRate by viewModel.liveHeartRate.collectAsState()
    val restingHeartRate by viewModel.restingHeartRate.collectAsState()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${userProfile.name} 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ready to crush your goals today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = "FitTrack",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Wearable Integration Status Bar
        item {
            WearableStatusBar(
                connectedDevices = connectedWearables,
                isSyncing = isSyncingWearables,
                syncStatusText = syncStatusText,
                onSyncClick = { viewModel.syncAllWearables() },
                onManageClick = onNavigateToWearables
            )
        }

        // Daily Motivational Tip
        item {
            DailyMotivationalCard()
        }

        // Daily Concentric Rings (Steps, Workout, Calories)
        item {
            MultiMetricRingsCard(
                steps = todaySteps.steps,
                stepGoal = userProfile.dailyStepGoal,
                activeMinutes = todayStats.first + todaySteps.activeMinutes,
                workoutGoalMinutes = userProfile.dailyWorkoutGoalMinutes,
                caloriesBurned = todaySteps.caloriesBurned + todayStats.second,
                calorieGoal = userProfile.dailyCalorieGoal
            )
        }

        // Live Optical Heart Rate Card from Wearables
        item {
            HeartRateCard(
                currentBpm = liveHeartRate,
                restingBpm = restingHeartRate,
                sourceDeviceName = connectedWearables.firstOrNull()?.name ?: userProfile.connectedWearable
            )
        }

        // Live Step Counter & Sensor Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_step_counter_card"),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Step Counter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (sensorStatus.isListening || sensorStatus.isSimulating) EmeraldPrimary else Color.Gray,
                                        modifier = Modifier.size(7.dp)
                                    ) {}
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (sensorStatus.isSimulating) "Walking Simulator Active" else sensorStatus.sensorType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${todaySteps.distanceKm} km",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TimeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Step Simulator / Adder Buttons for Emulator & Testing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.addManualSteps(250) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_250_steps_button")
                        ) {
                            Text("+250 Steps", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.addManualSteps(1000) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_1000_steps_button")
                        ) {
                            Text("+1,000 Steps", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleWalkingSimulation() },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("toggle_walk_simulation_button")
                        ) {
                            Text(
                                text = if (sensorStatus.isSimulating) "Stop Walk" else "Simulate Walk",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (sensorStatus.isSimulating) RoseError else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Workout Timer",
                    subtitle = "Start Stopwatch",
                    icon = Icons.Default.PlayArrow,
                    color = EmeraldPrimary,
                    testTag = "dashboard_start_workout_action",
                    onClick = onNavigateToWorkout
                )

                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Log Exercise",
                    subtitle = "Manual entry",
                    icon = Icons.Default.Add,
                    color = TimeColor,
                    testTag = "dashboard_log_exercise_action",
                    onClick = { showAddWorkoutDialog = true }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Nutrition",
                    subtitle = "Healthy Breakfast",
                    icon = Icons.Default.Restaurant,
                    color = CalorieColor,
                    testTag = "dashboard_nutrition_action",
                    onClick = onNavigateToNutrition
                )

                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "History",
                    subtitle = "7-day analytics",
                    icon = Icons.Default.DirectionsWalk,
                    color = EmeraldPrimary,
                    testTag = "dashboard_activity_action",
                    onClick = onNavigateToActivity
                )
            }
        }

        item {
            ActionCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Wearable Devices",
                subtitle = "Sync Fitbit, Apple Watch, Garmin & Wear OS",
                icon = Icons.Default.Watch,
                color = Color(0xFF007CC3),
                testTag = "dashboard_wearables_action",
                onClick = onNavigateToWearables
            )
        }

        // Today's Workouts Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${todayWorkouts.size} logged",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (todayWorkouts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No workouts logged yet today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddWorkoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Log First Workout", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(todayWorkouts, key = { it.id }) { workout ->
                WorkoutItemRow(
                    workout = workout,
                    onDelete = { viewModel.deleteWorkout(workout) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddWorkoutDialog) {
        AddOrEditWorkoutDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onSave = { type, durationSec, cal, dist, reps, notes ->
                viewModel.addWorkout(type, durationSec, cal, dist, reps, notes)
                showAddWorkoutDialog = false
            }
        )
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WorkoutItemRow(
    workout: WorkoutEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeInfo = WorkoutTypeCatalog.getType(workout.exerciseType)
    val mins = workout.durationSeconds / 60
    val secs = workout.durationSeconds % 60
    val timeStr = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeInfo.icon,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = workout.exerciseType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$timeStr • ${workout.caloriesBurned} kcal" +
                                (if (workout.distanceKm > 0) " • ${workout.distanceKm} km" else "") +
                                (if (workout.reps > 0) " • ${workout.reps} reps" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (workout.sourceDevice.isNotBlank() && workout.sourceDevice != "FitTrack App") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF007CC3).copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = Color(0xFF007CC3),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Synced: ${workout.sourceDevice}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF007CC3)
                                )
                            }
                        }
                    }
                    if (workout.notes.isNotBlank()) {
                        Text(
                            text = workout.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_workout_${workout.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
