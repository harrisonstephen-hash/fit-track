package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String, // e.g., "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
    val isEnabled: Boolean = true
)
