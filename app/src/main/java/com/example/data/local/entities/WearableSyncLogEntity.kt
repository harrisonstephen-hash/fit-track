package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wearable_sync_logs")
data class WearableSyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceId: String,
    val deviceName: String,
    val brand: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepsSynced: Int,
    val distanceKmSynced: Double,
    val heartRateBpm: Int,
    val workoutsCount: Int,
    val statusMessage: String
)
