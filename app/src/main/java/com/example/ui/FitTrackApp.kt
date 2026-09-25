package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CelebrationDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.WearablesScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.FitTrackViewModel
import kotlinx.coroutines.flow.collectLatest

enum class FitTrackDestination(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Default.Dashboard, "nav_item_home"),
    ACTIVITY("Activity", Icons.Default.DirectionsWalk, "nav_item_activity"),
    WORKOUT("Workout", Icons.Default.FitnessCenter, "nav_item_workout"),
    WEARABLES("Devices", Icons.Default.Watch, "nav_item_wearables"),
    NUTRITION("Nutrition", Icons.Default.Restaurant, "nav_item_nutrition"),
    REMINDERS("Reminders", Icons.Default.Alarm, "nav_item_reminders"),
    PROFILE("Profile", Icons.Default.Person, "nav_item_profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitTrackApp(
    viewModel: FitTrackViewModel = viewModel()
) {
    var currentDestination by remember { mutableStateOf(FitTrackDestination.HOME) }
    val celebrationMessage by viewModel.celebrationMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Tablet / Desktop Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    FitTrackDestination.values().forEach { dest ->
                        NavigationRailItem(
                            selected = currentDestination == dest,
                            onClick = { currentDestination = dest },
                            icon = { Icon(imageVector = dest.icon, contentDescription = dest.title) },
                            label = { Text(dest.title) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag(dest.tag)
                        )
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "FitTrack • ${currentDestination.title}",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            ScreenContent(
                                destination = currentDestination,
                                viewModel = viewModel,
                                onNavigate = { currentDestination = it }
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile Compact Layout with TopAppBar and Bottom NavigationBar
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "FitTrack",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 8.dp
                    ) {
                        FitTrackDestination.values().forEach { dest ->
                            NavigationBarItem(
                                selected = currentDestination == dest,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(imageVector = dest.icon, contentDescription = dest.title)
                                },
                                label = { Text(dest.title, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    indicatorColor = EmeraldPrimary
                                ),
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        viewModel = viewModel,
                        onNavigate = { currentDestination = it }
                    )
                }
            }
        }
    }

    // Goal Celebration / Milestone Dialog
    celebrationMessage?.let { msg ->
        CelebrationDialog(
            message = msg,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}

@Composable
fun ScreenContent(
    destination: FitTrackDestination,
    viewModel: FitTrackViewModel,
    onNavigate: (FitTrackDestination) -> Unit
) {
    when (destination) {
        FitTrackDestination.HOME -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToWorkout = { onNavigate(FitTrackDestination.WORKOUT) },
            onNavigateToActivity = { onNavigate(FitTrackDestination.ACTIVITY) },
            onNavigateToNutrition = { onNavigate(FitTrackDestination.NUTRITION) },
            onNavigateToWearables = { onNavigate(FitTrackDestination.WEARABLES) }
        )
        FitTrackDestination.ACTIVITY -> ActivityScreen(
            viewModel = viewModel
        )
        FitTrackDestination.WORKOUT -> WorkoutScreen(
            viewModel = viewModel
        )
        FitTrackDestination.WEARABLES -> WearablesScreen(
            viewModel = viewModel
        )
        FitTrackDestination.NUTRITION -> NutritionScreen(
            viewModel = viewModel
        )
        FitTrackDestination.REMINDERS -> RemindersScreen(
            viewModel = viewModel
        )
        FitTrackDestination.PROFILE -> ProfileScreen(
            viewModel = viewModel,
            onNavigateToWearables = { onNavigate(FitTrackDestination.WEARABLES) }
        )
    }
}
