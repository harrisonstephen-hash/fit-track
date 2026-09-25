package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wearable_devices")
data class WearableDeviceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String, // GARMIN, FITBIT, APPLE_WATCH, WEAR_OS, WHOOP
    val model: String,
    val isConnected: Boolean = true,
    val isPaired: Boolean = true,
    val batteryPercent: Int = 85,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val autoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15,
    val syncSteps: Boolean = true,
    val syncDistance: Boolean = true,
    val syncHeartRate: Boolean = true,
    val syncWorkouts: Boolean = true,
    val currentHeartRate: Int = 72,
    val restingHeartRate: Int = 58,
    val syncedStepsToday: Int = 0,
    val syncedDistanceKmToday: Double = 0.0,
    val syncedCaloriesToday: Int = 0,
    val firmwareVersion: String = "v14.20",
    val macAddress: String = "C4:D3:56:88:9A:12"
)
