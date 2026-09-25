package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.FitTrackApp
import com.example.ui.theme.FitTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackTheme {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    // permissions handled gracefully
                }

                LaunchedEffect(Unit) {
                    val permissionsNeeded = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACTIVITY_RECOGNITION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (permissionsNeeded.isNotEmpty()) {
                        launcher.launch(permissionsNeeded.toTypedArray())
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    FitTrackApp()
                }
            }
        }
    }
}
