package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Alex Morgan",
    val dailyStepGoal: Int = 10000,
    val dailyWorkoutGoalMinutes: Int = 45,
    val dailyCalorieGoal: Int = 500,
    val weightKg: Double = 68.0,
    val heightCm: Double = 175.0,
    val preferredReminderTime: String = "07:30 AM",
    val unitSystem: String = "Metric (km, kg)",
    val restingHeartRate: Int = 58,
    val currentHeartRate: Int = 72,
    val connectedWearable: String = "Garmin Fēnix 7X"
)
