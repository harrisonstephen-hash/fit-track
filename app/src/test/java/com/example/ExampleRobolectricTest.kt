package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.UserProfileEntity
import com.example.data.model.BreakfastCatalog
import com.example.data.model.WorkoutTypeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FitTrack", appName)
    }

    @Test
    fun `user profile default goals`() {
        val profile = UserProfileEntity(
            name = "Alex Morgan",
            dailyStepGoal = 10000,
            dailyWorkoutGoalMinutes = 45
        )
        assertEquals(10000, profile.dailyStepGoal)
        assertEquals(45, profile.dailyWorkoutGoalMinutes)
    }

    @Test
    fun `workout types catalog contains running and walking`() {
        val running = WorkoutTypeCatalog.getType("Running")
        assertNotNull(running)
        assertTrue(running.caloriesPerMinute > 5.0)
        assertTrue(running.tracksDistance)
    }

    @Test
    fun `healthy breakfast catalog contains nutritious recommendations`() {
        val breakfasts = BreakfastCatalog.healthyBreakfasts
        assertTrue(breakfasts.isNotEmpty())
        assertTrue(breakfasts.any { it.proteinGrams >= 15 })
    }

    @Test
    fun `wearable brands include Garmin, Fitbit, Apple Watch, and Wear OS`() {
        val brands = com.example.data.model.WearableBrand.entries
        assertTrue(brands.any { it.name == "GARMIN" })
        assertTrue(brands.any { it.name == "FITBIT" })
        assertTrue(brands.any { it.name == "APPLE_WATCH" })
        assertTrue(brands.any { it.name == "WEAR_OS" })
    }

    @Test
    fun `heart rate zone categorization works correctly`() {
        val resting = com.example.data.model.HeartRateZone.fromBpm(58)
        assertEquals(com.example.data.model.HeartRateZone.RESTING, resting)

        val fatBurn = com.example.data.model.HeartRateZone.fromBpm(95)
        assertEquals(com.example.data.model.HeartRateZone.FAT_BURN, fatBurn)

        val cardio = com.example.data.model.HeartRateZone.fromBpm(140)
        assertEquals(com.example.data.model.HeartRateZone.CARDIO, cardio)

        val peak = com.example.data.model.HeartRateZone.fromBpm(168)
        assertEquals(com.example.data.model.HeartRateZone.PEAK, peak)
    }

    @Test
    fun `wearable device entity creation and sync defaults`() {
        val device = com.example.data.local.entities.WearableDeviceEntity(
            id = "garmin_fenix_7",
            name = "Garmin Fēnix 7",
            brand = "GARMIN",
            model = "Fēnix 7",
            isConnected = true,
            batteryPercent = 88,
            currentHeartRate = 72,
            restingHeartRate = 58
        )
        assertTrue(device.isConnected)
        assertTrue(device.syncSteps)
        assertTrue(device.syncHeartRate)
        assertTrue(device.syncWorkouts)
        assertEquals(88, device.batteryPercent)
        assertEquals(72, device.currentHeartRate)
    }
}
