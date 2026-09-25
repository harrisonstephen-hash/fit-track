package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.WearableDeviceEntity
import com.example.data.local.entities.WearableSyncLogEntity
import com.example.data.model.DiscoveredWearable
import com.example.data.model.HeartRateZone
import com.example.data.model.WearableBrand
import com.example.ui.components.HeartRateCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.TimeColor
import com.example.ui.viewmodel.FitTrackViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WearablesScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.wearableDevices.collectAsState()
    val isSyncing by viewModel.isSyncingWearables.collectAsState()
    val syncStatusText by viewModel.syncStatusText.collectAsState()
    val liveHr by viewModel.liveHeartRate.collectAsState()
    val restingHr by viewModel.restingHeartRate.collectAsState()
    val syncLogs by viewModel.recentSyncLogs.collectAsState()
    val isScanning by viewModel.isScanningDevices.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    var showPairDialog by remember { mutableStateOf(false) }
    var selectedDeviceForConfig by remember { mutableStateOf<WearableDeviceEntity?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wearable Integrations",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Sync steps, distance, HR & workouts from your watch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.syncAllWearables() },
                    enabled = !isSyncing && devices.any { it.isConnected },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("sync_all_wearables_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) "Syncing..." else "Sync All",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Live Telemetry & Optical Heart Rate Monitor Card
        item {
            val connectedDevice = devices.firstOrNull { it.isConnected }
            HeartRateCard(
                currentBpm = liveHr,
                restingBpm = restingHr,
                sourceDeviceName = connectedDevice?.name ?: "None"
            )
        }

        // Supported Ecosystems Summary Pill Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Supported Wearable Ecosystems",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EcosystemBadge("Garmin", Color(0xFF007CC3))
                        EcosystemBadge("Fitbit", Color(0xFF00B0B9))
                        EcosystemBadge("Apple Watch", Color(0xFFFA2D48))
                        EcosystemBadge("Wear OS", Color(0xFF4285F4))
                    }
                }
            }
        }

        // Paired Devices List Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Paired Devices (${devices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                OutlinedButton(
                    onClick = {
                        showPairDialog = true
                        viewModel.startWearableScanning()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("pair_new_device_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Device", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Device Cards
        if (devices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No wearable devices connected yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap 'Add Device' to pair Fitbit, Apple Watch, or Garmin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(devices, key = { it.id }) { device ->
                WearableDeviceCard(
                    device = device,
                    isSyncing = isSyncing,
                    onToggleConnection = { connect -> viewModel.toggleWearableConnection(device.id, connect) },
                    onSyncNow = { viewModel.syncWearableDevice(device.id) },
                    onConfigureClick = { selectedDeviceForConfig = device },
                    onUnpair = { viewModel.unpairWearable(device) }
                )
            }
        }

        // Sync Audit History Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Synchronization Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (syncLogs.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearWearableSyncLogs() }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (syncLogs.isEmpty()) {
            item {
                Text(
                    text = "No sync history yet. Connect a wearable to start automatic sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(syncLogs.take(5), key = { it.id }) { log ->
                SyncLogCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Pair New Device Dialog
    if (showPairDialog) {
        PairDeviceDialog(
            isScanning = isScanning,
            discoveredDevices = discoveredDevices,
            onPairClick = { discovered ->
                viewModel.pairDiscoveredWearable(discovered)
                showPairDialog = false
            },
            onDismiss = {
                viewModel.stopWearableScanning()
                showPairDialog = false
            }
        )
    }

    // Configure Device Sync Settings Dialog
    selectedDeviceForConfig?.let { device ->
        WearableConfigDialog(
            device = device,
            onSave = { steps, dist, hr, workouts, autoSync ->
                viewModel.updateWearablePreferences(
                    device = device,
                    syncSteps = steps,
                    syncDistance = dist,
                    syncHeartRate = hr,
                    syncWorkouts = workouts
                )
                viewModel.toggleWearableAutoSync(device.id, autoSync)
                selectedDeviceForConfig = null
            },
            onDismiss = { selectedDeviceForConfig = null }
        )
    }
}

@Composable
fun EcosystemBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun WearableDeviceCard(
    device: WearableDeviceEntity,
    isSyncing: Boolean,
    onToggleConnection: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onConfigureClick: () -> Unit,
    onUnpair: () -> Unit
) {
    val brand = WearableBrand.fromString(device.brand)
    val brandColor = brand.primaryColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wearable_card_${device.id}"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Brand Icon + Device Name + Connect Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = brandColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = brandColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${brand.displayName} • ${device.model}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = device.isConnected,
                    onCheckedChange = { onToggleConnection(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary),
                    modifier = Modifier.testTag("wearable_toggle_${device.id}")
                )
            }

            // Status Badges Row (Connection, Battery, Auto-Sync)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Connection Status Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (device.isConnected) EmeraldPrimary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (device.isConnected) EmeraldPrimary else Color.Gray,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (device.isConnected) "Connected" else "Disconnected",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (device.isConnected) EmeraldPrimary else Color.Gray
                        )
                    }
                }

                // Battery Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "🔋 ${device.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Auto-Sync Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (device.autoSyncEnabled) TimeColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (device.autoSyncEnabled) "Auto-Sync: ON" else "Auto-Sync: OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (device.autoSyncEnabled) TimeColor else Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Synced Metrics Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Synced Steps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "%,d".format(device.syncedStepsToday),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Synced Dist", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${device.syncedDistanceKmToday} km",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TimeColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Optical HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${device.currentHeartRate} bpm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252)
                    )
                }
            }

            // Last Sync & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last synced: ${formatRelativeTime(device.lastSyncTimestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onConfigureClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(18.dp))
                    }

                    FilledTonalButton(
                        onClick = onSyncNow,
                        enabled = !isSyncing && device.isConnected,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sync_device_${device.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncLogCard(log: WearableSyncLogEntity) {
    val brand = WearableBrand.fromString(log.brand)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = brand.primaryColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = brand.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.deviceName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatRelativeTime(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = log.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PairDeviceDialog(
    isScanning: Boolean,
    discoveredDevices: List<DiscoveredWearable>,
    onPairClick: (DiscoveredWearable) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discover Nearby Wearables", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Searching for Bluetooth LE fitness trackers, smartwatches, and Health Connect companion devices...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Scanning for Garmin, Fitbit & Wear OS...", style = MaterialTheme.typography.labelMedium)
                    }
                }

                discoveredDevices.forEach { discovered ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPairClick(discovered) }
                            .testTag("pair_item_${discovered.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = discovered.brand.primaryColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Watch,
                                            contentDescription = null,
                                            tint = discovered.brand.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = discovered.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${discovered.brand.displayName} • Signal: ${discovered.rssiDbm} dBm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { onPairClick(discovered) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pair", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WearableConfigDialog(
    device: WearableDeviceEntity,
    onSave: (syncSteps: Boolean, syncDist: Boolean, syncHr: Boolean, syncWorkouts: Boolean, autoSync: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var syncSteps by remember { mutableStateOf(device.syncSteps) }
    var syncDist by remember { mutableStateOf(device.syncDistance) }
    var syncHr by remember { mutableStateOf(device.syncHeartRate) }
    var syncWorkouts by remember { mutableStateOf(device.syncWorkouts) }
    var autoSync by remember { mutableStateOf(device.autoSyncEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = device.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "Data Synchronization Settings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto Sync Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Automatic Background Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("Sync continuously when app is running", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Data Types to Sync:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                // Sync Steps
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = syncSteps, onCheckedChange = { syncSteps = it })
                    Text("Daily Steps & Pedometer", style = MaterialTheme.typography.bodyMedium)
                }

                // Sync Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = syncDist, onCheckedChange = { syncDist = it })
                    Text("GPS & Stride Distance (km)", style = MaterialTheme.typography.bodyMedium)
                }

                // Sync Heart Rate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = syncHr, onCheckedChange = { syncHr = it })
                    Text("Heart Rate & Resting BPM", style = MaterialTheme.typography.bodyMedium)
                }

                // Sync Workouts
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = syncWorkouts, onCheckedChange = { syncWorkouts = it })
                    Text("Workouts & Recorded Activities", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(syncSteps, syncDist, syncHr, syncWorkouts, autoSync) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun formatRelativeTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val diffSec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}
