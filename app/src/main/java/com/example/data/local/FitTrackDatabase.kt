package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.entities.DailyStepEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.local.entities.WearableDeviceEntity
import com.example.data.local.entities.WearableSyncLogEntity
import com.example.data.local.entities.WorkoutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        WorkoutEntity::class,
        DailyStepEntity::class,
        ReminderEntity::class,
        UserProfileEntity::class,
        WearableDeviceEntity::class,
        WearableSyncLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun stepDao(): StepDao
    abstract fun reminderDao(): ReminderDao
    abstract fun profileDao(): ProfileDao
    abstract fun wearableDao(): WearableDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(FitTrackDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class FitTrackDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: FitTrackDatabase) {
                val now = System.currentTimeMillis()

                // 1. Initial User Profile
                db.profileDao().saveUserProfile(
                    UserProfileEntity(
                        id = 1,
                        name = "Alex Morgan",
                        dailyStepGoal = 10000,
                        dailyWorkoutGoalMinutes = 45,
                        dailyCalorieGoal = 500,
                        weightKg = 68.0,
                        heightCm = 175.0,
                        preferredReminderTime = "07:30 AM",
                        unitSystem = "Metric (km, kg)",
                        restingHeartRate = 58,
                        currentHeartRate = 72,
                        connectedWearable = "Garmin Fēnix 7X"
                    )
                )

                // 2. Pre-populate popular Wearable Devices (Garmin, Fitbit, Apple Watch, Wear OS)
                db.wearableDao().upsertDevices(
                    listOf(
                        WearableDeviceEntity(
                            id = "garmin_fenix_7x",
                            name = "Garmin Fēnix 7X Sapphire Solar",
                            brand = "GARMIN",
                            model = "Fēnix 7X (Titanium/DLC)",
                            isConnected = true,
                            isPaired = true,
                            batteryPercent = 86,
                            lastSyncTimestamp = now - (6 * 60 * 1000), // 6 mins ago
                            autoSyncEnabled = true,
                            syncIntervalMinutes = 15,
                            syncSteps = true,
                            syncDistance = true,
                            syncHeartRate = true,
                            syncWorkouts = true,
                            currentHeartRate = 71,
                            restingHeartRate = 56,
                            syncedStepsToday = 4820,
                            syncedDistanceKmToday = 3.8,
                            syncedCaloriesToday = 265,
                            firmwareVersion = "v16.22",
                            macAddress = "D8:A9:8B:11:4F:92"
                        ),
                        WearableDeviceEntity(
                            id = "fitbit_charge_6",
                            name = "Fitbit Charge 6",
                            brand = "FITBIT",
                            model = "Charge 6 (Obsidian/Black)",
                            isConnected = true,
                            isPaired = true,
                            batteryPercent = 92,
                            lastSyncTimestamp = now - (25 * 60 * 1000), // 25 mins ago
                            autoSyncEnabled = true,
                            syncIntervalMinutes = 30,
                            syncSteps = true,
                            syncDistance = true,
                            syncHeartRate = true,
                            syncWorkouts = true,
                            currentHeartRate = 74,
                            restingHeartRate = 59,
                            syncedStepsToday = 1600,
                            syncedDistanceKmToday = 1.2,
                            syncedCaloriesToday = 95,
                            firmwareVersion = "v200.11",
                            macAddress = "C4:7D:EE:22:98:31"
                        ),
                        WearableDeviceEntity(
                            id = "apple_watch_ultra_2",
                            name = "Apple Watch Ultra 2",
                            brand = "APPLE_WATCH",
                            model = "Ultra 2 (49mm Titanium)",
                            isConnected = false,
                            isPaired = true,
                            batteryPercent = 78,
                            lastSyncTimestamp = now - (3 * 3600 * 1000), // 3 hours ago
                            autoSyncEnabled = false,
                            syncIntervalMinutes = 60,
                            syncSteps = true,
                            syncDistance = true,
                            syncHeartRate = true,
                            syncWorkouts = true,
                            currentHeartRate = 68,
                            restingHeartRate = 57,
                            syncedStepsToday = 0,
                            syncedDistanceKmToday = 0.0,
                            syncedCaloriesToday = 0,
                            firmwareVersion = "watchOS 10.4",
                            macAddress = "F0:99:B6:33:88:20"
                        ),
                        WearableDeviceEntity(
                            id = "samsung_galaxy_watch_6",
                            name = "Galaxy Watch 6 Classic",
                            brand = "WEAR_OS",
                            model = "Wear OS by Google / Health Connect",
                            isConnected = false,
                            isPaired = false,
                            batteryPercent = 65,
                            lastSyncTimestamp = 0L,
                            autoSyncEnabled = false,
                            syncIntervalMinutes = 15,
                            syncSteps = true,
                            syncDistance = true,
                            syncHeartRate = true,
                            syncWorkouts = true,
                            currentHeartRate = 70,
                            restingHeartRate = 60,
                            syncedStepsToday = 0,
                            syncedDistanceKmToday = 0.0,
                            syncedCaloriesToday = 0,
                            firmwareVersion = "One UI 5.0 Watch",
                            macAddress = "A0:B1:C2:77:88:99"
                        )
                    )
                )

                // 3. Initial Wearable Sync Logs
                db.wearableDao().insertSyncLog(
                    WearableSyncLogEntity(
                        deviceId = "garmin_fenix_7x",
                        deviceName = "Garmin Fēnix 7X Sapphire Solar",
                        brand = "GARMIN",
                        timestamp = now - (6 * 60 * 1000),
                        stepsSynced = 4820,
                        distanceKmSynced = 3.8,
                        heartRateBpm = 71,
                        workoutsCount = 1,
                        statusMessage = "Synced 4,820 steps, 3.8 km & 1 workout (Morning Jog)"
                    )
                )
                db.wearableDao().insertSyncLog(
                    WearableSyncLogEntity(
                        deviceId = "fitbit_charge_6",
                        deviceName = "Fitbit Charge 6",
                        brand = "FITBIT",
                        timestamp = now - (25 * 60 * 1000),
                        stepsSynced = 1600,
                        distanceKmSynced = 1.2,
                        heartRateBpm = 74,
                        workoutsCount = 0,
                        statusMessage = "Synced 1,600 steps & 74 bpm resting heart rate"
                    )
                )

                // 4. Initial Reminders
                db.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Morning Jog & Stretch",
                        hour = 7,
                        minute = 30,
                        daysOfWeek = "Mon,Tue,Wed,Thu,Fri",
                        isEnabled = true
                    )
                )
                db.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Afternoon HIIT / Gym",
                        hour = 17,
                        minute = 45,
                        daysOfWeek = "Mon,Wed,Fri,Sat",
                        isEnabled = true
                    )
                )
                db.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "Evening Walk & Cooldown",
                        hour = 20,
                        minute = 15,
                        daysOfWeek = "Daily",
                        isEnabled = false
                    )
                )

                // 5. Past 7 days realistic steps & workouts
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE

                val stepHistories = listOf(
                    Triple(6, 8420, 32),   // 6 days ago
                    Triple(5, 10250, 48),  // 5 days ago
                    Triple(4, 9150, 38),   // 4 days ago
                    Triple(3, 11800, 55),  // 3 days ago
                    Triple(2, 7900, 30),   // 2 days ago
                    Triple(1, 10600, 50),  // yesterday
                    Triple(0, 6420, 28)    // today so far
                )

                for ((daysAgo, steps, activeMins) in stepHistories) {
                    val dateStr = today.minusDays(daysAgo.toLong()).format(formatter)
                    val distanceKm = Math.round(steps * 0.000762 * 10.0) / 10.0
                    val cal = Math.round(steps * 0.042 + activeMins * 5.5).toInt()
                    db.stepDao().upsertStepRecord(
                        DailyStepEntity(
                            date = dateStr,
                            steps = steps,
                            stepGoal = 10000,
                            distanceKm = distanceKm,
                            caloriesBurned = cal,
                            activeMinutes = activeMins
                        )
                    )
                }

                // Sample today's workouts with device tags
                val todayStr = today.format(formatter)
                val yesterdayStr = today.minusDays(1).format(formatter)

                db.workoutDao().insertWorkout(
                    WorkoutEntity(
                        exerciseType = "Morning Jog",
                        durationSeconds = 25 * 60,
                        caloriesBurned = 230,
                        distanceKm = 3.4,
                        reps = 0,
                        date = todayStr,
                        notes = "Fresh morning pace along the park trail.",
                        sourceDevice = "Garmin Fēnix 7X"
                    )
                )

                db.workoutDao().insertWorkout(
                    WorkoutEntity(
                        exerciseType = "Push-ups & Core",
                        durationSeconds = 15 * 60,
                        caloriesBurned = 110,
                        distanceKm = 0.0,
                        reps = 60,
                        date = todayStr,
                        notes = "3 sets of 20 pushups + planks.",
                        sourceDevice = "FitTrack App"
                    )
                )

                db.workoutDao().insertWorkout(
                    WorkoutEntity(
                        exerciseType = "Cycling",
                        durationSeconds = 40 * 60,
                        caloriesBurned = 340,
                        distanceKm = 12.8,
                        reps = 0,
                        date = yesterdayStr,
                        notes = "Outdoor bike loop, moderate intensity.",
                        sourceDevice = "Fitbit Charge 6"
                    )
                )
            }
        }
    }
}
