package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.DailyStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM daily_step_records WHERE date = :date")
    fun getStepRecord(date: String): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_step_records WHERE date = :date")
    suspend fun getStepRecordDirect(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_step_records ORDER BY date DESC LIMIT 7")
    fun getLast7DaysRecords(): Flow<List<DailyStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStepRecord(record: DailyStepEntity)
}
