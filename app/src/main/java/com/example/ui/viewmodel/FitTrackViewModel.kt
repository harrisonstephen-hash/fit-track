package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FitTrackDatabase
import com.example.data.local.entities.DailyStepEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.local.entities.WearableDeviceEntity
import com.example.data.local.entities.WearableSyncLogEntity
import com.example.data.local.entities.WorkoutEntity
import com.example.data.model.BreakfastCatalog
import com.example.data.model.BreakfastMeal
import com.example.data.model.DiscoveredWearable
import com.example.data.model.MotivationalMessage
import com.example.data.model.MotivationalSystem
import com.example.data.model.WorkoutTypeCatalog
import com.example.data.repository.FitTrackRepository
import com.example.data.sensor.SensorStatus
import com.example.data.sensor.StepSensorManager
import com.example.data.wearable.WearableSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TimerUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedMillis: Long = 0L,
    val selectedExercise: String = "Running",
    val estimatedCalories: Int = 0
)

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitTrackRepository
    private val sensorManager: StepSensorManager
    private val wearableSyncManager: WearableSyncManager

    init {
        val database = FitTrackDatabase.getDatabase(application, viewModelScope)
        repository = FitTrackRepository(database)
        wearableSyncManager = WearableSyncManager(application, repository, database.wearableDao(), viewModelScope)
        sensorManager = StepSensorManager(application, viewModelScope) { stepIncrement ->
            onStepsIncremented(stepIncrement)
        }
        sensorManager.startListening()
    }

    val sensorStatus: StateFlow<SensorStatus> = sensorManager.sensorStatus

    // Wearable Devices Integration
    val wearableDevices: StateFlow<List<WearableDeviceEntity>> = repository.getAllWearableDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedWearables: StateFlow<List<WearableDeviceEntity>> = repository.getConnectedWearableDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSyncLogs: StateFlow<List<WearableSyncLogEntity>> = repository.getRecentSyncLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSyncingWearables: StateFlow<Boolean> = wearableSyncManager.isSyncing
    val syncStatusText: StateFlow<String> = wearableSyncManager.syncStatusText
    val liveHeartRate: StateFlow<Int> = wearableSyncManager.liveHeartRate
    val restingHeartRate: StateFlow<Int> = wearableSyncManager.restingHeartRate
    val discoveredDevices: StateFlow<List<DiscoveredWearable>> = wearableSyncManager.discoveredDevices
    val isScanningDevices: StateFlow<Boolean> = wearableSyncManager.isScanning

    // User Profile
    val userProfile: StateFlow<UserProfileEntity> = repository.getUserProfile()
        .map { it ?: UserProfileEntity() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfileEntity()
        )

    // Today's Step Record
    val todayStepRecord: StateFlow<DailyStepEntity> = repository.getTodayStepRecord()
        .map { record ->
            record ?: DailyStepEntity(
                date = repository.getTodayDateString(),
                steps = 6420,
                stepGoal = 10000,
                distanceKm = 4.8,
                caloriesBurned = 320,
                activeMinutes = 35
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DailyStepEntity(
                date = repository.getTodayDateString(),
                steps = 6420,
                stepGoal = 10000,
                distanceKm = 4.8,
                caloriesBurned = 320,
                activeMinutes = 35
            )
        )

    // Past 7 Days Records
    val weeklyStepRecords: StateFlow<List<DailyStepEntity>> = repository.getLast7DaysSteps()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Workouts
    val todayWorkouts: StateFlow<List<WorkoutEntity>> = repository.getWorkoutsForToday()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.getAllWorkouts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Today Workout Summary (minutes, calories)
    val todayWorkoutStats = todayWorkouts.map { list ->
        val totalSecs = list.sumOf { it.durationSeconds }
        val totalCals = list.sumOf { it.caloriesBurned }
        Pair(totalSecs / 60, totalCals)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    // Reminders
    val reminders: StateFlow<List<ReminderEntity>> = repository.getAllReminders()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Timer State
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // Nutrition State
    private val _loggedMeals = MutableStateFlow<List<BreakfastMeal>>(emptyList())
    val loggedMeals: StateFlow<List<BreakfastMeal>> = _loggedMeals.asStateFlow()

    private val _nutritionFilter = MutableStateFlow("All")
    val nutritionFilter: StateFlow<String> = _nutritionFilter.asStateFlow()

    // Achievement / Toast Event
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Active Celebration Banner
    private val _celebrationMessage = MutableStateFlow<MotivationalMessage?>(null)
    val celebrationMessage: StateFlow<MotivationalMessage?> = _celebrationMessage.asStateFlow()

    fun dismissCelebration() {
        _celebrationMessage.value = null
    }

    // Step Operations
    private fun onStepsIncremented(increment: Int) {
        viewModelScope.launch {
            val goal = userProfile.value.dailyStepGoal
            val currentSteps = todayStepRecord.value.steps
            repository.incrementSteps(increment, goal)

            // Check goal achievement celebration
            if (currentSteps < goal && (currentSteps + increment) >= goal) {
                _celebrationMessage.value = MotivationalSystem.getStepGoalCelebration(currentSteps + increment, goal)
                _toastEvent.emit("🎉 Congratulations! Daily step goal achieved!")
            }
        }
    }

    fun addManualSteps(steps: Int) {
        onStepsIncremented(steps)
    }

    fun toggleWalkingSimulation() {
        sensorManager.toggleWalkingSimulation()
    }

    // Timer Controls
    fun selectWorkoutTypeForTimer(exerciseType: String) {
        val currentMillis = _timerState.value.elapsedMillis
        val calPerMin = WorkoutTypeCatalog.getType(exerciseType).caloriesPerMinute
        val minutes = currentMillis / 60000.0
        val cal = Math.round(minutes * calPerMin).toInt()
        _timerState.value = _timerState.value.copy(
            selectedExercise = exerciseType,
            estimatedCalories = cal
        )
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = false
        )
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - _timerState.value.elapsedMillis
            val calPerMin = WorkoutTypeCatalog.getType(_timerState.value.selectedExercise).caloriesPerMinute
            while (isActive) {
                delay(100)
                val elapsed = System.currentTimeMillis() - startTime
                val minutes = elapsed / 60000.0
                val cal = Math.round(minutes * calPerMin).toInt()
                _timerState.value = _timerState.value.copy(
                    elapsedMillis = elapsed,
                    estimatedCalories = cal
                )
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            elapsedMillis = 0L,
            estimatedCalories = 0
        )
    }

    fun saveTimerAsWorkout(notes: String = "") {
        val elapsedSec = (_timerState.value.elapsedMillis / 1000).toInt()
        if (elapsedSec < 5) {
            viewModelScope.launch { _toastEvent.emit("Workout too short to save (< 5 seconds)") }
            return
        }
        val exercise = _timerState.value.selectedExercise
        val calories = _timerState.value.estimatedCalories.coerceAtLeast(1)
        val info = WorkoutTypeCatalog.getType(exercise)
        val distance = if (info.tracksDistance) {
            Math.round((elapsedSec / 3600.0 * 8.5) * 10.0) / 10.0 // approx distance
        } else 0.0

        addWorkout(
            exerciseType = exercise,
            durationSeconds = elapsedSec,
            calories = calories,
            distanceKm = distance,
            reps = 0,
            notes = notes
        )
        resetTimer()
        viewModelScope.launch {
            _toastEvent.emit("Workout '$exercise' saved successfully! 🔥")
        }
    }

    // Workouts CRUD
    fun addWorkout(
        exerciseType: String,
        durationSeconds: Int,
        calories: Int,
        distanceKm: Double = 0.0,
        reps: Int = 0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val todayStr = repository.getTodayDateString()
            val entity = WorkoutEntity(
                exerciseType = exerciseType,
                durationSeconds = durationSeconds,
                caloriesBurned = calories,
                distanceKm = distanceKm,
                reps = reps,
                date = todayStr,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertWorkout(entity)

            val workoutGoal = userProfile.value.dailyWorkoutGoalMinutes
            val currentWorkoutMins = (todayWorkoutStats.value.first + (durationSeconds / 60))
            if (currentWorkoutMins >= workoutGoal && todayWorkoutStats.value.first < workoutGoal) {
                _celebrationMessage.value = MotivationalSystem.getWorkoutGoalCelebration(currentWorkoutMins)
            }
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            _toastEvent.emit("Workout updated")
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
            _toastEvent.emit("Workout removed")
        }
    }

    // Reminders CRUD
    fun addReminder(title: String, hour: Int, minute: Int, daysOfWeek: String) {
        viewModelScope.launch {
            val entity = ReminderEntity(
                title = title.ifBlank { "Daily Exercise" },
                hour = hour,
                minute = minute,
                daysOfWeek = daysOfWeek,
                isEnabled = true
            )
            repository.insertReminder(entity)
            _toastEvent.emit("Reminder scheduled for %02d:%02d".format(hour, minute))
        }
    }

    fun toggleReminder(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setReminderEnabled(id, isEnabled)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _toastEvent.emit("Reminder deleted")
        }
    }

    fun triggerTestReminderNotification(reminder: ReminderEntity) {
        viewModelScope.launch {
            _celebrationMessage.value = MotivationalMessage(
                title = "⏰ Exercise Reminder: ${reminder.title}",
                message = "Time for your workout session! Lace up your shoes and let's get moving! 💪",
                iconName = "reminder",
                category = "reminder"
            )
            _toastEvent.emit("Reminder alert: ${reminder.title}")
        }
    }

    // Nutrition
    fun setNutritionFilter(filter: String) {
        _nutritionFilter.value = filter
    }

    fun logBreakfastMeal(meal: BreakfastMeal) {
        _loggedMeals.value = _loggedMeals.value + meal
        viewModelScope.launch {
            _toastEvent.emit("Logged ${meal.title} (+${meal.calories} kcal)")
        }
    }

    // User Profile
    fun updateProfile(
        name: String,
        stepGoal: Int,
        workoutGoalMins: Int,
        calorieGoal: Int,
        weightKg: Double,
        heightCm: Double,
        reminderTime: String,
        unitSystem: String
    ) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                dailyStepGoal = stepGoal,
                dailyWorkoutGoalMinutes = workoutGoalMins,
                dailyCalorieGoal = calorieGoal,
                weightKg = weightKg,
                heightCm = heightCm,
                preferredReminderTime = reminderTime,
                unitSystem = unitSystem
            )
            repository.saveUserProfile(updated)
            _toastEvent.emit("Profile preferences updated!")
        }
    }

    // Wearable Sync & Device Controls
    fun syncAllWearables() {
        viewModelScope.launch {
            val results = wearableSyncManager.syncAllConnected()
            if (results.isNotEmpty()) {
                val totalSteps = results.sumOf { it.stepsSynced }
                val totalWorkouts = results.sumOf { it.workoutsSynced }
                val workoutMsg = if (totalWorkouts > 0) ", $totalWorkouts workout(s) imported" else ""
                _toastEvent.emit("Synced ${results.size} wearable(s): +$totalSteps steps$workoutMsg ⚡")
            } else {
                _toastEvent.emit("No connected wearables to sync")
            }
        }
    }

    fun syncWearableDevice(deviceId: String) {
        viewModelScope.launch {
            val res = wearableSyncManager.syncDevice(deviceId)
            if (res != null) {
                _toastEvent.emit("${res.deviceName}: ${res.message} ⚡")
            }
        }
    }

    fun toggleWearableConnection(deviceId: String, connect: Boolean) {
        viewModelScope.launch {
            wearableSyncManager.toggleConnection(deviceId, connect)
            _toastEvent.emit(if (connect) "Wearable connected & ready" else "Wearable disconnected")
        }
    }

    fun toggleWearableAutoSync(deviceId: String, enabled: Boolean) {
        viewModelScope.launch {
            wearableSyncManager.toggleAutoSync(deviceId, enabled)
            _toastEvent.emit(if (enabled) "Auto-sync enabled for device" else "Auto-sync disabled")
        }
    }

    fun updateWearablePreferences(
        device: WearableDeviceEntity,
        syncSteps: Boolean,
        syncDistance: Boolean,
        syncHeartRate: Boolean,
        syncWorkouts: Boolean
    ) {
        viewModelScope.launch {
            val updated = device.copy(
                syncSteps = syncSteps,
                syncDistance = syncDistance,
                syncHeartRate = syncHeartRate,
                syncWorkouts = syncWorkouts
            )
            wearableSyncManager.updateDevice(updated)
            _toastEvent.emit("Sync settings saved for ${device.name}")
        }
    }

    fun startWearableScanning() {
        wearableSyncManager.startScanning()
    }

    fun stopWearableScanning() {
        wearableSyncManager.stopScanning()
    }

    fun pairDiscoveredWearable(discovered: DiscoveredWearable) {
        viewModelScope.launch {
            val paired = wearableSyncManager.pairAndConnectDiscovered(discovered)
            _toastEvent.emit("Paired & connected ${paired.name}! ✨")
        }
    }

    fun unpairWearable(device: WearableDeviceEntity) {
        viewModelScope.launch {
            wearableSyncManager.unpairDevice(device)
            _toastEvent.emit("Unpaired ${device.name}")
        }
    }

    fun clearWearableSyncLogs() {
        viewModelScope.launch {
            wearableSyncManager.clearLogs()
            _toastEvent.emit("Wearable sync history cleared")
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
        timerJob?.cancel()
    }
}
