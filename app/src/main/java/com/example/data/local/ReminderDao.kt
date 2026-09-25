package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM exercise_reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM exercise_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE exercise_reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setReminderEnabled(id: Long, isEnabled: Boolean)
}
