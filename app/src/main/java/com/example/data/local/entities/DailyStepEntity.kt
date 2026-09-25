package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_step_records")
data class DailyStepEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val steps: Int,
    val stepGoal: Int,
    val distanceKm: Double,
    val caloriesBurned: Int,
    val activeMinutes: Int
)
