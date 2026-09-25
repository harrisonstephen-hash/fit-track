package com.example.data.repository

import com.example.data.local.FitTrackDatabase
import com.example.data.local.entities.DailyStepEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.local.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FitTrackRepository(
    private val database: FitTrackDatabase
) {
    private val workoutDao = database.workoutDao()
    private val stepDao = database.stepDao()
    private val reminderDao = database.reminderDao()
    private val profileDao = database.profileDao()

    fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // Workouts
    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    fun getWorkoutsForToday(): Flow<List<WorkoutEntity>> {
        return workoutDao.getWorkoutsForDate(getTodayDateString())
    }

    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntity>> {
        return workoutDao.getWorkoutsForDate(date)
    }

    suspend fun insertWorkout(workout: WorkoutEntity): Long {
        val id = workoutDao.insertWorkout(workout)
        // Also update today's active minutes in step record
        updateTodayActiveMinutes(workout.durationSeconds / 60, workout.caloriesBurned)
        return id
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun deleteWorkoutById(id: Long) {
        workoutDao.deleteWorkoutById(id)
    }

    // Steps
    fun getTodayStepRecord(): Flow<DailyStepEntity?> {
        return stepDao.getStepRecord(getTodayDateString())
    }

    fun getLast7DaysSteps(): Flow<List<DailyStepEntity>> {
        return stepDao.getLast7DaysRecords()
    }

    suspend fun incrementSteps(stepsToAdd: Int, stepGoal: Int) {
        val todayStr = getTodayDateString()
        val existing = stepDao.getStepRecordDirect(todayStr)
        val currentSteps = (existing?.steps ?: 0) + stepsToAdd
        val currentGoal = existing?.stepGoal ?: stepGoal
        val distanceKm = Math.round(currentSteps * 0.000762 * 10.0) / 10.0
        val activeMins = existing?.activeMinutes ?: (currentSteps / 110)
        val stepCalories = Math.round(currentSteps * 0.042).toInt()
        val totalCalories = (existing?.caloriesBurned ?: 0) + Math.round(stepsToAdd * 0.042).toInt()

        val updated = DailyStepEntity(
            date = todayStr,
            steps = currentSteps,
            stepGoal = currentGoal,
            distanceKm = distanceKm,
            caloriesBurned = totalCalories.coerceAtLeast(stepCalories),
            activeMinutes = activeMins
        )
        stepDao.upsertStepRecord(updated)
    }

    private suspend fun updateTodayActiveMinutes(additionalMinutes: Int, additionalCalories: Int) {
        val todayStr = getTodayDateString()
        val existing = stepDao.getStepRecordDirect(todayStr)
        if (existing != null) {
            val updated = existing.copy(
                activeMinutes = existing.activeMinutes + additionalMinutes,
                caloriesBurned = existing.caloriesBurned + additionalCalories
            )
            stepDao.upsertStepRecord(updated)
        }
    }

    // Reminders
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun setReminderEnabled(id: Long, isEnabled: Boolean) {
        reminderDao.setReminderEnabled(id, isEnabled)
    }

    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity?> = profileDao.getUserProfile()

    suspend fun getUserProfileDirect(): UserProfileEntity? {
        return profileDao.getUserProfileDirect()
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        profileDao.saveUserProfile(profile)
        // Also update stepGoal on today's record if present
        val todayStr = getTodayDateString()
        val existing = stepDao.getStepRecordDirect(todayStr)
        if (existing != null && existing.stepGoal != profile.dailyStepGoal) {
            stepDao.upsertStepRecord(existing.copy(stepGoal = profile.dailyStepGoal))
        }
    }

    // Wearable Devices
    fun getAllWearableDevices() = database.wearableDao().getAllDevices()

    fun getConnectedWearableDevices() = database.wearableDao().getConnectedDevices()

    fun getRecentSyncLogs() = database.wearableDao().getRecentSyncLogs()

    suspend fun getWearableById(id: String) = database.wearableDao().getDeviceById(id)

    suspend fun upsertWearable(device: com.example.data.local.entities.WearableDeviceEntity) =
        database.wearableDao().upsertDevice(device)

    suspend fun updateWearable(device: com.example.data.local.entities.WearableDeviceEntity) =
        database.wearableDao().updateDevice(device)

    suspend fun deleteWearable(device: com.example.data.local.entities.WearableDeviceEntity) =
        database.wearableDao().deleteDevice(device)

    suspend fun clearSyncLogs() = database.wearableDao().clearSyncLogs()
}
