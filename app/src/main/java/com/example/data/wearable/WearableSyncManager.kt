package com.example.data.wearable

import android.content.Context
import com.example.data.local.WearableDao
import com.example.data.local.entities.DailyStepEntity
import com.example.data.local.entities.WearableDeviceEntity
import com.example.data.local.entities.WearableSyncLogEntity
import com.example.data.local.entities.WorkoutEntity
import com.example.data.model.DiscoveredWearable
import com.example.data.model.WearableBrand
import com.example.data.repository.FitTrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class SyncResult(
    val deviceName: String,
    val brand: String,
    val stepsSynced: Int,
    val distanceKm: Double,
    val heartRateBpm: Int,
    val workoutsSynced: Int,
    val message: String
)

class WearableSyncManager(
    private val context: Context,
    private val repository: FitTrackRepository,
    private val wearableDao: WearableDao,
    private val scope: CoroutineScope
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusText = MutableStateFlow("All devices up to date")
    val syncStatusText: StateFlow<String> = _syncStatusText.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredWearable>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredWearable>> = _discoveredDevices.asStateFlow()

    private val _liveHeartRate = MutableStateFlow(72)
    val liveHeartRate: StateFlow<Int> = _liveHeartRate.asStateFlow()

    private val _restingHeartRate = MutableStateFlow(58)
    val restingHeartRate: StateFlow<Int> = _restingHeartRate.asStateFlow()

    private var autoSyncJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        startTelemetrySimulation()
        startPeriodicAutoSync()
    }

    private fun startTelemetrySimulation() {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(3500)
                val connected = wearableDao.getConnectedDevices().firstOrNull() ?: emptyList()
                if (connected.isNotEmpty()) {
                    val baseHr = _restingHeartRate.value + 12
                    // Realistic heart rate drift between 68 and 82 bpm during daily rest/walking
                    val variance = Random.nextInt(-4, 6)
                    val newHr = (baseHr + variance).coerceIn(52, 140)
                    _liveHeartRate.value = newHr
                }
            }
        }
    }

    private fun startPeriodicAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(90_000) // Every 90 seconds, run an auto-sync check
                val connected = wearableDao.getConnectedDevices().firstOrNull() ?: emptyList()
                val autoSyncDevices = connected.filter { it.autoSyncEnabled }
                if (autoSyncDevices.isNotEmpty()) {
                    performAutoSync(autoSyncDevices)
                }
            }
        }
    }

    suspend fun syncAllConnected(): List<SyncResult> = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        _syncStatusText.value = "Connecting to wearable devices..."
        delay(600)

        val connected = wearableDao.getConnectedDevices().firstOrNull() ?: emptyList()
        val results = mutableListOf<SyncResult>()

        if (connected.isEmpty()) {
            _syncStatusText.value = "No wearable devices connected"
            _isSyncing.value = false
            return@withContext emptyList()
        }

        for (device in connected) {
            _syncStatusText.value = "Syncing ${device.name}..."
            delay(700)
            val result = syncSingleDeviceInternal(device)
            results.add(result)
        }

        _syncStatusText.value = "Synced ${results.size} device(s) successfully"
        _isSyncing.value = false
        results
    }

    suspend fun syncDevice(deviceId: String): SyncResult? = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        _syncStatusText.value = "Connecting to device..."
        delay(500)

        val device = wearableDao.getDeviceById(deviceId)
        if (device == null || !device.isConnected) {
            _isSyncing.value = false
            _syncStatusText.value = "Device is not connected"
            return@withContext null
        }

        _syncStatusText.value = "Synchronizing data with ${device.name}..."
        delay(800)
        val result = syncSingleDeviceInternal(device)
        _syncStatusText.value = "Sync complete: ${result.message}"
        _isSyncing.value = false
        result
    }

    private suspend fun syncSingleDeviceInternal(device: WearableDeviceEntity): SyncResult {
        val now = System.currentTimeMillis()
        val todayStr = repository.getTodayDateString()

        // 1. Calculate realistic synced steps & distance from device
        val brand = WearableBrand.fromString(device.brand)
        val baseSteps = when (brand) {
            WearableBrand.GARMIN -> Random.nextInt(750, 1850)
            WearableBrand.FITBIT -> Random.nextInt(600, 1400)
            WearableBrand.APPLE_WATCH -> Random.nextInt(800, 1600)
            WearableBrand.WEAR_OS -> Random.nextInt(500, 1200)
            WearableBrand.WHOOP -> Random.nextInt(400, 950)
        }

        val stepIncrement = if (device.syncSteps) baseSteps else 0
        val distIncrement = Math.round((stepIncrement * 0.00078) * 100.0) / 100.0

        // Synchronize steps into database
        if (stepIncrement > 0) {
            val userProfile = repository.getUserProfileDirect()
            val goal = userProfile?.dailyStepGoal ?: 10000
            repository.incrementSteps(stepIncrement, goal)
        }

        // 2. Heart rate sync
        val hr = when (brand) {
            WearableBrand.GARMIN -> Random.nextInt(68, 76)
            WearableBrand.FITBIT -> Random.nextInt(70, 78)
            WearableBrand.APPLE_WATCH -> Random.nextInt(66, 74)
            else -> Random.nextInt(69, 77)
        }
        val restingHr = when (brand) {
            WearableBrand.GARMIN -> 55
            WearableBrand.FITBIT -> 58
            WearableBrand.APPLE_WATCH -> 56
            else -> 60
        }
        _liveHeartRate.value = hr
        _restingHeartRate.value = restingHr

        // Update profile vitals
        val profile = repository.getUserProfileDirect()
        if (profile != null) {
            repository.saveUserProfile(
                profile.copy(
                    restingHeartRate = restingHr,
                    currentHeartRate = hr,
                    connectedWearable = device.name
                )
            )
        }

        // 3. Optional workout import from wearable
        var workoutsImported = 0
        var workoutSummary = ""
        if (device.syncWorkouts && Random.nextFloat() > 0.45f) {
            val importedWorkout = generateSampleWorkoutForDevice(device, todayStr)
            repository.insertWorkout(importedWorkout)
            workoutsImported = 1
            workoutSummary = " + imported ${importedWorkout.exerciseType}"
        }

        // 4. Update device state in database
        val updatedDevice = device.copy(
            lastSyncTimestamp = now,
            currentHeartRate = hr,
            restingHeartRate = restingHr,
            syncedStepsToday = device.syncedStepsToday + stepIncrement,
            syncedDistanceKmToday = Math.round((device.syncedDistanceKmToday + distIncrement) * 10.0) / 10.0,
            batteryPercent = (device.batteryPercent - Random.nextInt(0, 2)).coerceAtLeast(12)
        )
        wearableDao.updateDevice(updatedDevice)

        // 5. Insert Sync Log
        val statusMsg = "Synced +$stepIncrement steps, +${distIncrement}km, HR: $hr bpm$workoutSummary"
        wearableDao.insertSyncLog(
            WearableSyncLogEntity(
                deviceId = device.id,
                deviceName = device.name,
                brand = device.brand,
                timestamp = now,
                stepsSynced = stepIncrement,
                distanceKmSynced = distIncrement,
                heartRateBpm = hr,
                workoutsCount = workoutsImported,
                statusMessage = statusMsg
            )
        )

        return SyncResult(
            deviceName = device.name,
            brand = device.brand,
            stepsSynced = stepIncrement,
            distanceKm = distIncrement,
            heartRateBpm = hr,
            workoutsSynced = workoutsImported,
            message = statusMsg
        )
    }

    private fun generateSampleWorkoutForDevice(device: WearableDeviceEntity, todayStr: String): WorkoutEntity {
        val brand = WearableBrand.fromString(device.brand)
        val (name, mins, cal, dist) = when (brand) {
            WearableBrand.GARMIN -> listOf(
                Tuple4("Garmin GPS Outdoor Run", 32, 290, 4.6),
                Tuple4("Garmin Trail Hike", 45, 310, 3.8),
                Tuple4("Garmin Threshold Cycling", 35, 340, 11.2)
            ).random()
            WearableBrand.FITBIT -> listOf(
                Tuple4("Fitbit Outdoor Walk", 30, 160, 2.7),
                Tuple4("Fitbit Aerobics Session", 28, 220, 0.0),
                Tuple4("Fitbit Cardio Blast", 25, 240, 0.0)
            ).random()
            WearableBrand.APPLE_WATCH -> listOf(
                Tuple4("Apple Watch HIIT", 30, 280, 0.0),
                Tuple4("Apple Watch Outdoor Cycle", 40, 360, 13.5),
                Tuple4("Apple Watch Functional Strength", 35, 210, 0.0)
            ).random()
            else -> listOf(
                Tuple4("Wear OS Fitness Track", 25, 200, 2.5),
                Tuple4("Wear OS Strength Circuit", 30, 215, 0.0)
            ).random()
        }

        return WorkoutEntity(
            exerciseType = name,
            durationSeconds = mins * 60,
            caloriesBurned = cal,
            distanceKm = dist,
            reps = 0,
            date = todayStr,
            notes = "Auto-synced from ${device.name} via ${device.brand} Health API.",
            sourceDevice = device.name
        )
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private suspend fun performAutoSync(devices: List<WearableDeviceEntity>) {
        for (device in devices) {
            syncSingleDeviceInternal(device)
        }
    }

    // Device Discovery & Pairing
    fun startScanning() {
        _isScanning.value = true
        _discoveredDevices.value = emptyList()
        scope.launch(Dispatchers.Default) {
            delay(800)
            val sampleDiscovered = listOf(
                DiscoveredWearable(
                    id = "garmin_forerunner_965",
                    name = "Garmin Forerunner® 965",
                    brand = WearableBrand.GARMIN,
                    model = "Forerunner 965 (Black/Carbon)",
                    rssiDbm = -48,
                    macAddress = "E4:55:6C:12:33:AA"
                ),
                DiscoveredWearable(
                    id = "fitbit_versa_4",
                    name = "Fitbit Versa 4",
                    brand = WearableBrand.FITBIT,
                    model = "Versa 4 Fitness Smartwatch",
                    rssiDbm = -54,
                    macAddress = "D2:77:88:99:A1:BC"
                ),
                DiscoveredWearable(
                    id = "google_pixel_watch_2",
                    name = "Google Pixel Watch 2",
                    brand = WearableBrand.WEAR_OS,
                    model = "Pixel Watch 2 (Matte Black)",
                    rssiDbm = -62,
                    macAddress = "B8:11:44:66:88:FF"
                ),
                DiscoveredWearable(
                    id = "whoop_strap_4",
                    name = "WHOOP 4.0",
                    brand = WearableBrand.WHOOP,
                    model = "WHOOP Strap 4.0 (Onyx)",
                    rssiDbm = -58,
                    macAddress = "A1:F2:D3:E4:C5:B6"
                )
            )
            _discoveredDevices.value = sampleDiscovered
            delay(1500)
            _isScanning.value = false
        }
    }

    fun stopScanning() {
        _isScanning.value = false
    }

    suspend fun pairAndConnectDiscovered(discovered: DiscoveredWearable): WearableDeviceEntity = withContext(Dispatchers.IO) {
        val newEntity = WearableDeviceEntity(
            id = discovered.id,
            name = discovered.name,
            brand = discovered.brand.name,
            model = discovered.model,
            isConnected = true,
            isPaired = true,
            batteryPercent = 95,
            lastSyncTimestamp = System.currentTimeMillis(),
            autoSyncEnabled = true,
            syncIntervalMinutes = 15,
            syncSteps = true,
            syncDistance = true,
            syncHeartRate = true,
            syncWorkouts = true,
            currentHeartRate = 72,
            restingHeartRate = 58,
            firmwareVersion = "v1.0.4",
            macAddress = discovered.macAddress
        )
        wearableDao.upsertDevice(newEntity)
        // Perform initial sync
        syncSingleDeviceInternal(newEntity)
        newEntity
    }

    suspend fun toggleConnection(deviceId: String, connect: Boolean) = withContext(Dispatchers.IO) {
        wearableDao.updateConnectionStatus(deviceId, connect)
    }

    suspend fun toggleAutoSync(deviceId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        wearableDao.updateAutoSync(deviceId, enabled)
    }

    suspend fun updateDevice(device: WearableDeviceEntity) = withContext(Dispatchers.IO) {
        wearableDao.updateDevice(device)
    }

    suspend fun unpairDevice(device: WearableDeviceEntity) = withContext(Dispatchers.IO) {
        wearableDao.deleteDevice(device)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        wearableDao.clearSyncLogs()
    }
}
