package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseType: String,
    val durationSeconds: Int,
    val caloriesBurned: Int,
    val distanceKm: Double = 0.0,
    val reps: Int = 0,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val sourceDevice: String = "FitTrack App"
)
