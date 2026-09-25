package com.example

import android.app.Application
import com.example.data.local.FitTrackDatabase
import com.example.data.repository.FitTrackRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FitTrackApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { FitTrackDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        FitTrackRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
