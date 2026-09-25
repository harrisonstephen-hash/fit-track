package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.sp
import com.example.data.model.WearableBrand
import com.example.ui.theme.CalorieColor
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TimeColor
import com.example.ui.viewmodel.FitTrackViewModel

@Composable
fun ProfileScreen(
    viewModel: FitTrackViewModel,
    onNavigateToWearables: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val devices by viewModel.wearableDevices.collectAsState()
    val liveHr by viewModel.liveHeartRate.collectAsState()
    val isSyncing by viewModel.isSyncingWearables.collectAsState()

    var name by remember { mutableStateOf(profile.name) }
    var stepGoal by remember { mutableFloatStateOf(profile.dailyStepGoal.toFloat()) }
    var workoutGoalMins by remember { mutableFloatStateOf(profile.dailyWorkoutGoalMinutes.toFloat()) }
    var calorieGoal by remember { mutableFloatStateOf(profile.dailyCalorieGoal.toFloat()) }
    var weightText by remember { mutableStateOf(profile.weightKg.toString()) }
    var heightText by remember { mutableStateOf(profile.heightCm.toString()) }
    var reminderTime by remember { mutableStateOf(profile.preferredReminderTime) }
    var unitSystem by remember { mutableStateOf(profile.unitSystem) }

    LaunchedEffect(profile) {
        name = profile.name
        stepGoal = profile.dailyStepGoal.toFloat()
        workoutGoalMins = profile.dailyWorkoutGoalMinutes.toFloat()
        calorieGoal = profile.dailyCalorieGoal.toFloat()
        weightText = profile.weightKg.toString()
        heightText = profile.heightCm.toString()
        reminderTime = profile.preferredReminderTime
        unitSystem = profile.unitSystem
    }

    val weightKg = weightText.toDoubleOrNull() ?: 68.0
    val heightM = (heightText.toDoubleOrNull() ?: 175.0) / 100.0
    val bmi = if (heightM > 0) Math.round((weightKg / (heightM * heightM)) * 10.0) / 10.0 else 22.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "User Profile & Goals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize your daily targets, personal stats, and preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile Avatar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_user_card"),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldPrimary,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "BMI $bmi • Healthy Weight",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Preferred Workout: $reminderTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Connected Wearables & Health Sync Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_wearables_card"),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF007CC3).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Watch,
                                        contentDescription = null,
                                        tint = Color(0xFF007CC3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Wearable Health Devices",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Fitbit • Apple Watch • Garmin • Wear OS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToWearables,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Manage", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    // Paired Devices Mini Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        devices.take(3).forEach { device ->
                            val brand = WearableBrand.fromString(device.brand)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (device.isConnected) brand.primaryColor.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (device.isConnected) EmeraldPrimary else Color.Gray,
                                            modifier = Modifier.size(6.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = brand.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (device.isConnected) brand.primaryColor else Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "${device.batteryPercent}% 🔋",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Synced Vitals Row (Optical HR & Resting HR)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Live HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$liveHr BPM", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Resting HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${profile.restingHeartRate} BPM", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.syncAllWearables() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isSyncing
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Edit Personal Stats Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Personal Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_weight_input")
                        )

                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_height_input")
                        )
                    }

                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text("Preferred Workout Time") },
                        placeholder = { Text("e.g. 07:30 AM") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_reminder_time_input")
                    )

                    Text(
                        text = "Units System",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = unitSystem.contains("Metric", ignoreCase = true),
                            onClick = { unitSystem = "Metric (km, kg)" },
                            label = { Text("Metric (km, kg)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                        FilterChip(
                            selected = unitSystem.contains("Imperial", ignoreCase = true),
                            onClick = { unitSystem = "Imperial (mi, lbs)" },
                            label = { Text("Imperial (mi, lbs)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }
        }

        // Daily Fitness Targets Customization Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_targets_card"),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Daily Fitness Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Step Goal Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Steps Target",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "%,d steps".format(stepGoal.toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                        Slider(
                            value = stepGoal,
                            onValueChange = { stepGoal = it },
                            valueRange = 4000f..25000f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldPrimary,
                                activeTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("profile_step_goal_slider")
                        )
                    }

                    // Workout Time Goal Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Workout Time",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${workoutGoalMins.toInt()} mins",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TimeColor
                            )
                        }
                        Slider(
                            value = workoutGoalMins,
                            onValueChange = { workoutGoalMins = it },
                            valueRange = 15f..120f,
                            steps = 20,
                            colors = SliderDefaults.colors(
                                thumbColor = TimeColor,
                                activeTrackColor = TimeColor
                            ),
                            modifier = Modifier.testTag("profile_workout_goal_slider")
                        )
                    }

                    // Calorie Burn Goal Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Calorie Burn Target",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${calorieGoal.toInt()} kcal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CalorieColor
                            )
                        }
                        Slider(
                            value = calorieGoal,
                            onValueChange = { calorieGoal = it },
                            valueRange = 200f..1500f,
                            steps = 25,
                            colors = SliderDefaults.colors(
                                thumbColor = CalorieColor,
                                activeTrackColor = CalorieColor
                            ),
                            modifier = Modifier.testTag("profile_calorie_goal_slider")
                        )
                    }
                }
            }
        }

        // Save Profile Preferences Button
        item {
            Button(
                onClick = {
                    viewModel.updateProfile(
                        name = name,
                        stepGoal = stepGoal.toInt(),
                        workoutGoalMins = workoutGoalMins.toInt(),
                        calorieGoal = calorieGoal.toInt(),
                        weightKg = weightKg,
                        heightCm = heightM * 100.0,
                        reminderTime = reminderTime,
                        unitSystem = unitSystem
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
