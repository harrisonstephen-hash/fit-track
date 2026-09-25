package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE date = :date")
    suspend fun getWorkoutsForDateDirect(date: String): List<WorkoutEntity>

    @Query("SELECT * FROM workout_logs WHERE date IN (:dates) ORDER BY timestamp ASC")
    fun getWorkoutsForDates(dates: List<String>): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)
}
