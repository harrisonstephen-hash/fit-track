package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutTypeCatalog
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.TimeColor
import com.example.ui.viewmodel.TimerUiState

@Composable
fun WorkoutTimerCard(
    timerState: TimerUiState,
    onSelectExercise: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onSaveWorkout: (notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var workoutNotes by remember { mutableStateOf("") }
    var showSaveSection by remember { mutableStateOf(false) }

    val elapsedMillis = timerState.elapsedMillis
    val totalSeconds = (elapsedMillis / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val millisRemainder = (elapsedMillis % 1000) / 100

    val timeFormatted = if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d.%01d".format(minutes, seconds, millisRemainder)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("workout_timer_card"),
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
                text = "Live Workout Stopwatch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Exercise Selection Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkoutTypeCatalog.allTypes.forEach { typeInfo ->
                    FilterChip(
                        selected = timerState.selectedExercise.equals(typeInfo.name, ignoreCase = true),
                        onClick = { onSelectExercise(typeInfo.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = typeInfo.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(typeInfo.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = EmeraldPrimary,
                            selectedLeadingIconColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Digital Display Box
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 44.sp,
                            letterSpacing = 2.sp
                        ),
                        color = if (timerState.isRunning) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CalorieColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CalorieColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "~${timerState.estimatedCalories} kcal burned (${timerState.selectedExercise})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stopwatch Controls: Start, Pause, Resume, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = {
                        onReset()
                        showSaveSection = false
                    },
                    enabled = elapsedMillis > 0,
                    modifier = Modifier.testTag("timer_reset_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                // Main Play/Pause Button
                if (!timerState.isRunning && !timerState.isPaused) {
                    // Not started
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("timer_start_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Workout", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else if (timerState.isRunning) {
                    // Running -> Pause
                    Button(
                        onClick = {
                            onPause()
                            showSaveSection = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TimeColor),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("timer_pause_button")
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Paused -> Resume
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("timer_resume_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Save Workout Prompt if paused or stopped with elapsed time
            AnimatedVisibility(visible = (timerState.isPaused || (!timerState.isRunning && elapsedMillis > 5000))) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = workoutNotes,
                        onValueChange = { workoutNotes = it },
                        label = { Text("Workout Notes (optional)") },
                        placeholder = { Text("e.g. Felt energetic, 5k trail route") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timer_notes_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onSaveWorkout(workoutNotes)
                            workoutNotes = ""
                            showSaveSection = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("timer_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Log Workout", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
