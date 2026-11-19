package com.example.ui.screens

import android.media.AudioManager
import android.text.format.Formatter
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Terminal
import com.example.device.LiveSensorReading
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.AiOpState
import com.example.viewmodel.NovaMainUiState

@Composable
fun DeviceHubScreen(
    uiState: NovaMainUiState,
    unresolvedErrorCount: Int,
    liveSensors: LiveSensorReading,
    onRefreshTelemetry: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenSettingsPanel: (String) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenWriteSettings: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onOpenRepairCenter: () -> Unit,
    onOpenAiModels: () -> Unit,
    onOpenTermux: () -> Unit,
    onOpenGameArena: () -> Unit,
    onLockScreen: () -> Unit,
    onPowerMenu: () -> Unit,
    onScreenshot: () -> Unit,
    onGlobalHome: () -> Unit,
    onVolumeAdjust: (Int) -> Unit,
    onMediaPlayPause: () -> Unit,
    onBrightnessAdjust: (Int) -> Unit,
    onPickFile: () -> Unit,
    onPickPhoto: () -> Unit,
    onOpenCamera: () -> Unit,
    onAnalyzeSelectedFile: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    val sections = listOf("Peranti", "Fail & Kamera", "Aplikasi")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                sections.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
                        modifier = Modifier.testTag("device_tab_$index")
                    ) {
                        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenRepairCenter,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_open_repair_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (unresolvedErrorCount > 0) "Baiki ($unresolvedErrorCount)" else "Pusat Baiki",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(
                    onClick = onOpenAiModels,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_open_models_button")
                ) {
                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tukar AI API", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenTermux,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_open_termux_button")
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Termux Studio", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onOpenGameArena,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("device_open_arena_button")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Game Arena", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedSection) {
                0 -> TelemetrySection(
                    uiState = uiState,
                    liveSensors = liveSensors,
                    onRefreshTelemetry = onRefreshTelemetry,
                    onToggleTorch = onToggleTorch,
                    onOpenSettingsPanel = onOpenSettingsPanel,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onOpenWriteSettings = onOpenWriteSettings,
                    onOpenAppInfo = onOpenAppInfo,
                    onLockScreen = onLockScreen,
                    onPowerMenu = onPowerMenu,
                    onScreenshot = onScreenshot,
                    onGlobalHome = onGlobalHome,
                    onVolumeAdjust = onVolumeAdjust,
                    onMediaPlayPause = onMediaPlayPause,
                    onBrightnessAdjust = onBrightnessAdjust
                )
                1 -> FilesAndVisionSection(
                    uiState = uiState,
                    onPickFile = onPickFile,
                    onPickPhoto = onPickPhoto,
                    onOpenCamera = onOpenCamera,
                    onAnalyzeSelectedFile = onAnalyzeSelectedFile
                )
                2 -> InstalledAppsSection(
                    uiState = uiState,
                    onRefresh = onRefreshTelemetry,
                    onLaunchApp = onLaunchApp
                )
            }
        }
    }
}

@Composable
private fun TelemetrySection(
    uiState: NovaMainUiState,
    liveSensors: LiveSensorReading,
    onRefreshTelemetry: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenSettingsPanel: (String) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenWriteSettings: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onLockScreen: () -> Unit,
    onPowerMenu: () -> Unit,
    onScreenshot: () -> Unit,
    onGlobalHome: () -> Unit,
    onVolumeAdjust: (Int) -> Unit,
    onMediaPlayPause: () -> Unit,
    onBrightnessAdjust: (Int) -> Unit
) {
    val t = uiState.telemetry
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Out-of-App System Control Card (Poco X7 Pro / HyperOS)
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    if (uiState.accessibilityReady) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = if (uiState.accessibilityReady) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.tertiary
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kawalan Luar APK (Sistem OS)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (uiState.accessibilityReady) {
                                    "Accessibility AKTIF • AI boleh kunci skrin, buka menu power, screenshot & ketuk luar app"
                                } else {
                                    "Accessibility BELUM AKTIF • Aktifkan untuk kawal luar aplikasi"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.accessibilityReady) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                            )
                        }
                    }

                    if (!uiState.accessibilityReady) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Panduan Poco X7 Pro (HyperOS): Ketuk 'App Info' -> menu 3 titik di atas -> 'Allow restricted settings', kemudian buka 'Accessibility' dan aktifkan 'NovaCore System Control'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenAppInfo,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("1. App Info", maxLines = 1)
                            }
                            Button(
                                onClick = onOpenAccessibilitySettings,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("enable_accessibility_button")
                            ) {
                                Text("2. Accessibility", maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLockScreen,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("lock_screen_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kunci Skrin")
                        }
                        Button(
                            onClick = onPowerMenu,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("power_menu_button")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Menu Off")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onScreenshot,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Screenshot, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Screenshot")
                        }
                        OutlinedButton(
                            onClick = onGlobalHome,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Home OS")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onVolumeAdjust(AudioManager.ADJUST_LOWER) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Volume Down", modifier = Modifier.size(18.dp))
                        }
                        OutlinedButton(
                            onClick = onMediaPlayPause,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause Media", modifier = Modifier.size(18.dp))
                        }
                        OutlinedButton(
                            onClick = { onVolumeAdjust(AudioManager.ADJUST_RAISE) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume Up", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onBrightnessAdjust(-45) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.BrightnessLow, contentDescription = "Dim screen", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gelap")
                        }
                        OutlinedButton(
                            onClick = { onBrightnessAdjust(45) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.BrightnessHigh, contentDescription = "Brighten screen", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cerah")
                        }
                        if (!uiState.writeSettingsReady) {
                            OutlinedButton(onClick = onOpenWriteSettings) {
                                Text("Izin")
                            }
                        }
                    }
                }
            }
        }

        // Hardware Telemetry Card
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${t?.manufacturer.orEmpty()} ${t?.deviceModel ?: "Android Device"}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Android ${t?.androidVersion ?: "-"} (API ${t?.sdkInt ?: "-"}) • ${t?.networkTransport ?: "-"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onRefreshTelemetry,
                            modifier = Modifier.testTag("refresh_telemetry_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh device telemetry")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val ramUsed = (t?.ramTotalMb ?: 1L) - (t?.ramAvailableMb ?: 0L)
                    val ramRatio = if ((t?.ramTotalMb ?: 0L) > 0) ramUsed.toFloat() / t!!.ramTotalMb else 0f
                    MetricBarRow(
                        icon = Icons.Default.Memory,
                        label = "RAM Digunakan",
                        detail = "${ramUsed}MB / ${t?.ramTotalMb ?: 0}MB",
                        progress = ramRatio
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val storageTotal = t?.storageTotalGb ?: 1.0
                    val storageUsed = (storageTotal - (t?.storageFreeGb ?: 0.0)).coerceAtLeast(0.0)
                    val storageRatio = if (storageTotal > 0) (storageUsed / storageTotal).toFloat() else 0f
                    MetricBarRow(
                        icon = Icons.Default.Storage,
                        label = "Storan Dalaman",
                        detail = "${"%.1f".format(storageUsed)}GB / ${"%.1f".format(storageTotal)}GB",
                        progress = storageRatio
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MetricBarRow(
                        icon = Icons.Default.BatteryChargingFull,
                        label = "Bateri (${if (t?.isCharging == true) "Mengecas" else "Aktif"})",
                        detail = "${t?.batteryPercent ?: 0}% • ${t?.batteryTempC ?: 0f}°C",
                        progress = (t?.batteryPercent ?: 0) / 100f
                    )
                }
            }
        }

        // Live Sensors & Torch Control
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensor Langsung (${t?.sensorCount ?: 0} dikesan)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accelerometer: X=${"%.2f".format(liveSensors.accelX)}  Y=${"%.2f".format(liveSensors.accelY)}  Z=${"%.2f".format(liveSensors.accelZ)} m/s²",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cahaya (Lux): ${liveSensors.lightLux?.let { "%.1f lx".format(it) } ?: "N/A"}  •  ABI: ${t?.abis ?: "-"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggleTorch,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (uiState.torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.torchOn) "Matikan Lampu" else "Pasang Lampu")
                        }
                        OutlinedButton(
                            onClick = { onOpenSettingsPanel("WIFI") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wi-Fi")
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenSettingsPanel("BLUETOOTH") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bluetooth")
                        }
                        OutlinedButton(
                            onClick = { onOpenSettingsPanel("SYSTEM") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tetapan")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun MetricBarRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    detail: String,
    progress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(text = detail, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun FilesAndVisionSection(
    uiState: NovaMainUiState,
    onPickFile: () -> Unit,
    onPickPhoto: () -> Unit,
    onOpenCamera: () -> Unit,
    onAnalyzeSelectedFile: (String) -> Unit
) {
    val context = LocalContext.current
    var filePrompt by rememberSaveable { mutableStateOf("Terangkan kandungan fail ini dan cari jika ada masalah atau kod penting.") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Akses Terus Fail & Kamera", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Buka mana-mana dokumen, kod sumber, log, atau foto daripada storan peranti atau tangkap terus menggunakan KameraX.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPickFile,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_file_button")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Fail")
                        }
                        Button(
                            onClick = onOpenCamera,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KameraX")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onPickPhoto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih Foto dari Galeri (Photo Picker)")
                    }
                }
            }
        }

        val selected = uiState.selectedFile
        if (selected != null) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selected.displayName, style = MaterialTheme.typography.titleMedium)
                                val sizeStr = if (selected.sizeBytes >= 0) {
                                    Formatter.formatShortFileSize(context, selected.sizeBytes)
                                } else "Saiz tidak diketahui"
                                Text(
                                    text = "${selected.mimeType} • $sizeStr",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selected.isImage && uiState.capturedBitmap != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Image(
                                bitmap = uiState.capturedBitmap.asImageBitmap(),
                                contentDescription = "Selected image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        } else if (selected.textPreview != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = selected.textPreview.take(1200),
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = filePrompt,
                            onValueChange = { filePrompt = it },
                            label = { Text("Arahan AI untuk fail ini") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onAnalyzeSelectedFile(filePrompt) },
                            enabled = uiState.fileOpState !is AiOpState.Loading && uiState.fileOpState !is AiOpState.Streaming,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.fileOpState is AiOpState.Loading || uiState.fileOpState is AiOpState.Streaming) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Analisis Fail Dengan NovaCore AI")
                        }

                        when (val op = uiState.fileOpState) {
                            is AiOpState.Streaming -> AiAnalysisBox(text = op.text, isError = false)
                            is AiOpState.Success -> AiAnalysisBox(text = op.text, isError = false)
                            is AiOpState.Error -> AiAnalysisBox(text = op.message, isError = true)
                            else -> Unit
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun AiAnalysisBox(text: String, isError: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun InstalledAppsSection(
    uiState: NovaMainUiState,
    onRefresh: () -> Unit,
    onLaunchApp: (String) -> Unit
) {
    var search by rememberSaveable { mutableStateOf("") }
    var gamesOnly by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(uiState.installedApps, search, gamesOnly) {
        uiState.installedApps.filter { app ->
            val matchesQuery = search.isBlank() ||
                app.appName.contains(search, ignoreCase = true) ||
                app.packageName.contains(search, ignoreCase = true)
            val matchesGame = !gamesOnly || app.isGame
            matchesQuery && matchesGame
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Cari app atau game...") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = gamesOnly,
                onClick = { gamesOnly = !gamesOnly },
                label = { Text("Games") },
                leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh apps")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tiada aplikasi sepadan ditemui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onLaunchApp(app.packageName) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (app.isGame) Icons.Default.SportsEsports else Icons.Default.Apps,
                                contentDescription = null,
                                tint = if (app.isGame) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (app.isGame) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = "GAME",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Launch ${app.appName}",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}
