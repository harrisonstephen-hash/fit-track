package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.WorkoutEntity
import com.example.data.model.WorkoutTypeCatalog
import com.example.ui.theme.EmeraldPrimary

@Composable
fun AddOrEditWorkoutDialog(
    initialWorkout: WorkoutEntity? = null,
    onDismiss: () -> Unit,
    onSave: (exerciseType: String, durationSec: Int, calories: Int, distanceKm: Double, reps: Int, notes: String) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(initialWorkout?.exerciseType ?: "Running")
    }
    var durationMinutesText by remember {
        mutableStateOf(initialWorkout?.let { (it.durationSeconds / 60).toString() } ?: "30")
    }
    var caloriesText by remember {
        mutableStateOf(initialWorkout?.caloriesBurned?.toString() ?: "")
    }
    var distanceText by remember {
        mutableStateOf(initialWorkout?.let { if (it.distanceKm > 0) it.distanceKm.toString() else "" } ?: "")
    }
    var repsText by remember {
        mutableStateOf(initialWorkout?.let { if (it.reps > 0) it.reps.toString() else "" } ?: "")
    }
    var notesText by remember {
        mutableStateOf(initialWorkout?.notes ?: "")
    }

    val typeInfo = WorkoutTypeCatalog.getType(selectedType)

    // Auto-calculate calories if empty
    val autoCalories = remember(selectedType, durationMinutesText) {
        val mins = durationMinutesText.toIntOrNull() ?: 0
        Math.round(mins * typeInfo.caloriesPerMinute).toInt()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialWorkout == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialWorkout == null) "Log Exercise" else "Edit Workout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Exercise Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WorkoutTypeCatalog.allTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type.name,
                            onClick = { selectedType = type.name },
                            label = { Text(type.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = durationMinutesText,
                    onValueChange = { durationMinutesText = it },
                    label = { Text("Duration (Minutes)*") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_duration_input")
                )

                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories (kcal) - Auto: ~$autoCalories") },
                    placeholder = { Text("$autoCalories") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_calories_input")
                )

                if (typeInfo.tracksDistance) {
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { distanceText = it },
                        label = { Text("Distance (km, optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_distance_input")
                    )
                }

                if (typeInfo.tracksReps) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text("Total Reps (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_reps_input")
                    )
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Workout Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationMins = durationMinutesText.toIntOrNull() ?: 1
                    val durationSec = durationMins * 60
                    val cal = caloriesText.toIntOrNull() ?: autoCalories
                    val distance = distanceText.toDoubleOrNull() ?: 0.0
                    val reps = repsText.toIntOrNull() ?: 0
                    onSave(selectedType, durationSec, cal, distance, reps, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_workout_confirm_button")
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, hour: Int, minute: Int, days: String) -> Unit
) {
    var title by remember { mutableStateOf("Daily Workout") }
    var hour by remember { mutableIntStateOf(7) }
    var minute by remember { mutableIntStateOf(30) }
    var isAm by remember { mutableStateOf(true) }

    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed", "Thu", "Fri")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = EmeraldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Exercise Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g. Morning Jog, Core Blast") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input")
                )

                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = if (hour == 0) "12" else hour.toString(),
                        onValueChange = {
                            val h = it.toIntOrNull()
                            if (h != null && h in 1..12) hour = if (h == 12) 0 else h
                        },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = "%02d".format(minute),
                        onValueChange = {
                            val m = it.toIntOrNull()
                            if (m != null && m in 0..59) minute = m
                        },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        FilterChip(
                            selected = isAm,
                            onClick = { isAm = true },
                            label = { Text("AM") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = !isAm,
                            onClick = { isAm = false },
                            label = { Text("PM") }
                        )
                    }
                }

                Text(
                    text = "Repeat Days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allDays.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(day) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHour = if (isAm) {
                        if (hour == 12) 0 else hour
                    } else {
                        if (hour == 12) 12 else hour + 12
                    }
                    val daysStr = if (selectedDays.size == 7) "Daily" else selectedDays.joinToString(",")
                    onSave(title, finalHour, minute, daysStr)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_reminder_confirm_button")
            ) {
                Text("Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
