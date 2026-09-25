package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddOrEditWorkoutDialog
import com.example.ui.components.WeeklyHistoryCard
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.TimeColor
import com.example.ui.viewmodel.FitTrackViewModel

@Composable
fun ActivityScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val todaySteps by viewModel.todayStepRecord.collectAsState()
    val weeklyRecords by viewModel.weeklyStepRecords.collectAsState()
    val allWorkouts by viewModel.allWorkouts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val sensorStatus by viewModel.sensorStatus.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("activity_fab_add_workout")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Workout")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Activity & History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your daily steps and exercise progression",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Step Counter Main Feature Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_detail_card"),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Pedometer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Sensor Live Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (sensorStatus.isListening || sensorStatus.isSimulating)
                                    EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = if (sensorStatus.isListening || sensorStatus.isSimulating) EmeraldPrimary else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (sensorStatus.isSimulating) "Walking Simulator" else "Hardware Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sensorStatus.isListening || sensorStatus.isSimulating) EmeraldPrimary else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Step Numbers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "%,d".format(todaySteps.steps),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 38.sp
                                    ),
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "Goal: %,d steps (%d%% completed)".format(
                                        userProfile.dailyStepGoal,
                                        ((todaySteps.steps.toFloat() / userProfile.dailyStepGoal.coerceAtLeast(1)) * 100).toInt()
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Distance and Calories Sub-stats
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${todaySteps.distanceKm} km",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TimeColor
                                )
                                Text(
                                    text = "${todaySteps.caloriesBurned} kcal burned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CalorieColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Step testing simulation controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.addManualSteps(250) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+250")
                            }
                            FilledTonalButton(
                                onClick = { viewModel.addManualSteps(1000) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1k")
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleWalkingSimulation() },
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    text = if (sensorStatus.isSimulating) "Stop Sim" else "Simulate Walk",
                                    color = if (sensorStatus.isSimulating) RoseError else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 7-day Weekly History Bar Chart
            item {
                WeeklyHistoryCard(weeklyRecords = weeklyRecords)
            }

            // Full Exercise History Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Exercise Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${allWorkouts.size} total entries",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allWorkouts.isEmpty()) {
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
                            Text(
                                text = "No workouts recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(allWorkouts, key = { it.id }) { workout ->
                    WorkoutItemRow(
                        workout = workout,
                        onDelete = { viewModel.deleteWorkout(workout) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddOrEditWorkoutDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, durationSec, cal, dist, reps, notes ->
                viewModel.addWorkout(type, durationSec, cal, dist, reps, notes)
                showAddDialog = false
            }
        )
    }
}
