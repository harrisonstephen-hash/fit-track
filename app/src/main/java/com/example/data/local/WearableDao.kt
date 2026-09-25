package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.WearableDeviceEntity
import com.example.data.local.entities.WearableSyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WearableDao {
    @Query("SELECT * FROM wearable_devices ORDER BY isConnected DESC, name ASC")
    fun getAllDevices(): Flow<List<WearableDeviceEntity>>

    @Query("SELECT * FROM wearable_devices WHERE isConnected = 1")
    fun getConnectedDevices(): Flow<List<WearableDeviceEntity>>

    @Query("SELECT * FROM wearable_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): WearableDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: WearableDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<WearableDeviceEntity>)

    @Update
    suspend fun updateDevice(device: WearableDeviceEntity)

    @Delete
    suspend fun deleteDevice(device: WearableDeviceEntity)

    @Query("UPDATE wearable_devices SET isConnected = :connected WHERE id = :id")
    suspend fun updateConnectionStatus(id: String, connected: Boolean)

    @Query("UPDATE wearable_devices SET autoSyncEnabled = :enabled WHERE id = :id")
    suspend fun updateAutoSync(id: String, enabled: Boolean)

    @Query("SELECT * FROM wearable_sync_logs ORDER BY timestamp DESC LIMIT 30")
    fun getRecentSyncLogs(): Flow<List<WearableSyncLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(log: WearableSyncLogEntity): Long

    @Query("DELETE FROM wearable_sync_logs")
    suspend fun clearSyncLogs()
}
