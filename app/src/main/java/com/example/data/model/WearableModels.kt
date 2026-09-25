package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class WearableBrand(
    val displayName: String,
    val primaryColor: Color,
    val subtitle: String
) {
    GARMIN("Garmin", Color(0xFF007CC3), "Garmin Connect™ & Health API"),
    FITBIT("Fitbit", Color(0xFF00B0B9), "Fitbit Web API & Google Fit"),
    APPLE_WATCH("Apple Watch", Color(0xFFFA2D48), "Apple HealthKit & Companion Bridge"),
    WEAR_OS("Wear OS", Color(0xFF4285F4), "Health Connect & Galaxy Watch"),
    WHOOP("WHOOP", Color(0xFFE05638), "WHOOP Strap 4.0 Telemetry");

    companion object {
        fun fromString(value: String): WearableBrand {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GARMIN
        }
    }
}

enum class HeartRateZone(
    val label: String,
    val minBpm: Int,
    val maxBpm: Int,
    val color: Color
) {
    RESTING("Resting", 40, 69, Color(0xFF4CAF50)),
    FAT_BURN("Fat Burn", 70, 119, Color(0xFFFFB300)),
    CARDIO("Cardio", 120, 155, Color(0xFFFF7043)),
    PEAK("Peak", 156, 220, Color(0xFFE91E63));

    companion object {
        fun fromBpm(bpm: Int): HeartRateZone {
            return when {
                bpm < 70 -> RESTING
                bpm < 120 -> FAT_BURN
                bpm < 156 -> CARDIO
                else -> PEAK
            }
        }
    }
}

data class DiscoveredWearable(
    val id: String,
    val name: String,
    val brand: WearableBrand,
    val model: String,
    val rssiDbm: Int = -55,
    val macAddress: String
)

data class WearableTelemetryData(
    val steps: Int,
    val distanceKm: Double,
    val activeCalories: Int,
    val currentHeartRate: Int,
    val restingHeartRate: Int,
    val workouts: List<WearableWorkoutPayload>
)

data class WearableWorkoutPayload(
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val distanceKm: Double,
    val notes: String
)
