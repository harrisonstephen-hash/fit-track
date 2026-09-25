package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExerciseCategory {
    CARDIO, STRENGTH, FLEXIBILITY, RECOVERY
}

data class WorkoutTypeInfo(
    val name: String,
    val category: ExerciseCategory,
    val icon: ImageVector,
    val caloriesPerMinute: Double,
    val tracksDistance: Boolean,
    val tracksReps: Boolean
)

object WorkoutTypeCatalog {
    val allTypes: List<WorkoutTypeInfo> = listOf(
        WorkoutTypeInfo("Walking", ExerciseCategory.CARDIO, Icons.Default.DirectionsWalk, 4.2, true, false),
        WorkoutTypeInfo("Running", ExerciseCategory.CARDIO, Icons.Default.DirectionsRun, 11.5, true, false),
        WorkoutTypeInfo("Cycling", ExerciseCategory.CARDIO, Icons.Default.DirectionsBike, 8.5, true, false),
        WorkoutTypeInfo("Jogging", ExerciseCategory.CARDIO, Icons.Default.DirectionsRun, 9.0, true, false),
        WorkoutTypeInfo("Push-ups", ExerciseCategory.STRENGTH, Icons.Default.FitnessCenter, 7.0, false, true),
        WorkoutTypeInfo("Squats", ExerciseCategory.STRENGTH, Icons.Default.FitnessCenter, 7.5, false, true),
        WorkoutTypeInfo("HIIT Workout", ExerciseCategory.CARDIO, Icons.Default.Timer, 12.0, false, false),
        WorkoutTypeInfo("Gym / Weights", ExerciseCategory.STRENGTH, Icons.Default.FitnessCenter, 6.5, false, true),
        WorkoutTypeInfo("Yoga & Stretch", ExerciseCategory.FLEXIBILITY, Icons.Default.SelfImprovement, 3.8, false, false),
        WorkoutTypeInfo("Aerobics", ExerciseCategory.CARDIO, Icons.Default.SportsGymnastics, 7.8, false, false)
    )

    fun getType(name: String): WorkoutTypeInfo {
        return allTypes.find { it.name.equals(name, ignoreCase = true) }
            ?: WorkoutTypeInfo(name, ExerciseCategory.CARDIO, Icons.Default.FitnessCenter, 6.0, false, false)
    }
}
