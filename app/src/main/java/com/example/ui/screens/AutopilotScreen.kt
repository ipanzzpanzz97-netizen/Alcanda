package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.AutopilotRuntimeState
import com.example.device.InstalledAppInfo
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun AutopilotScreen(
    autopilotState: AutopilotRuntimeState,
    accessibilityReady: Boolean,
    installedApps: List<InstalledAppInfo>,
    onUpdateConfig: (targetPkg: String, targetName: String, goal: String, financialSafeMode: Boolean) -> Unit,
    onStartAutopilot: (targetPkg: String, targetName: String, goal: String, financialSafeMode: Boolean) -> Unit,
    onStopAutopilot: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var appSearch by rememberSaveable { mutableStateOf("") }

    val quickApps = remember(installedApps, appSearch) {
        val base = installedApps.filter {
            appSearch.isBlank() ||
                it.appName.contains(appSearch, ignoreCase = true) ||
                it.packageName.contains(appSearch, ignoreCase = true)
        }
        base.take(15)
    }

    val lunoGoals = listOf(
        "Buka Exchange di Luno Ori, semak pasangan MYR, dan pantau harga koin murah (APE/XRP).",
        "Cari pasangan APEMYR atau XRPMYR dalam Luno Exchange dan buka papan Limit Order (modal RM40).",
        "Baca baki & senarai aset dalam dompet Luno saya dan laporkan."
    )

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

            // Header Card
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Autopilot Luar APK", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Ejen AI baca skrin & ketuk automatik di Luno Ori atau mana-mana aplikasi lain",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Accessibility Status Banner
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = if (accessibilityReady) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (accessibilityReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (accessibilityReady) Icons.Default.CheckCircle else Icons.Default.AccessibilityNew,
                                    contentDescription = null,
                                    tint = if (accessibilityReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (accessibilityReady) {
                                        "Enjin Kawalan Luar APK AKTIF (Accessibility Ready)"
                                    } else {
                                        "Accessibility Service Belum Aktif"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!accessibilityReady) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Untuk membenarkan AI mengawal Luno Ori & APK lain di luar app ini: ketuk App Info -> Allow restricted settings, kemudian aktifkan 'NovaCore System Control' dalam Accessibility.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(onClick = onOpenAppInfo, modifier = Modifier.weight(1f)) {
                                        Text("1. App Info", maxLines = 1)
                                    }
                                    Button(onClick = onOpenAccessibility, modifier = Modifier.weight(1f)) {
                                        Text("2. Accessibility", maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Target App & Financial Guardrail Configuration
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("1. Pilih Aplikasi Sasaran", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Featured Luno Ori Chip + Installed APKs
                            OutlinedTextField(
                                value = appSearch,
                                onValueChange = { appSearch = it },
                                placeholder = { Text("Tapis aplikasi terpasang (cth: Luno, Termux, Game)...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isLunoSelected = autopilotState.targetPackage == "co.bitx.android.wallet"
                                FilterChip(
                                    selected = isLunoSelected,
                                    onClick = {
                                        onUpdateConfig(
                                            "co.bitx.android.wallet",
                                            "Luno",
                                            autopilotState.goal,
                                            autopilotState.financialSafeMode
                                        )
                                    },
                                    label = { Text("Luno Ori (Utama)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CandlestickChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier.testTag("select_luno_ori_chip")
                                )

                                quickApps
                                    .filter { it.packageName != "co.bitx.android.wallet" }
                                    .forEach { app ->
                                        FilterChip(
                                            selected = autopilotState.targetPackage == app.packageName,
                                            onClick = {
                                                onUpdateConfig(
                                                    app.packageName,
                                                    app.appName,
                                                    autopilotState.goal,
                                                    autopilotState.financialSafeMode
                                                )
                                            },
                                            label = { Text(app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                        )
                                    }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sasaran aktif: ${autopilotState.targetName} (${autopilotState.targetPackage})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("2. Matlamat Tugas AI di Luar APK", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                lunoGoals.forEach { preset ->
                                    AssistChip(
                                        onClick = {
                                            onUpdateConfig(
                                                autopilotState.targetPackage,
                                                autopilotState.targetName,
                                                preset,
                                                autopilotState.financialSafeMode
                                            )
                                        },
                                        label = { Text(preset, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = autopilotState.goal,
                                onValueChange = {
                                    onUpdateConfig(
                                        autopilotState.targetPackage,
                                        autopilotState.targetName,
                                        it,
                                        autopilotState.financialSafeMode
                                    )
                                },
                                label = { Text("Arahan Autopilot Anda") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("autopilot_goal_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Financial Safe Mode Switch
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = if (autopilotState.financialSafeMode) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (autopilotState.financialSafeMode) Icons.Default.Shield else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (autopilotState.financialSafeMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Mod Keselamatan Kewangan",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (autopilotState.financialSafeMode) {
                                                "AKTIF: AI berhenti sebelum menekan butang Beli/Jual/Confirm/PIN supaya anda semak dahulu."
                                            } else {
                                                "AMARAN: AI dibenarkan mengetuk tanpa sekatan kata kunci kewangan."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = autopilotState.financialSafeMode,
                                        onCheckedChange = { checked ->
                                            onUpdateConfig(
                                                autopilotState.targetPackage,
                                                autopilotState.targetName,
                                                autopilotState.goal,
                                                checked
                                            )
                                        },
                                        modifier = Modifier.testTag("financial_safe_mode_switch")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            if (autopilotState.isRunning) {
                                Button(
                                    onClick = onStopAutopilot,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("stop_autopilot_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("HENTI KECEMASAN AUTOPILOT")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onStartAutopilot(
                                            autopilotState.targetPackage,
                                            autopilotState.targetName,
                                            autopilotState.goal,
                                            autopilotState.financialSafeMode
                                        )
                                    },
                                    enabled = autopilotState.goal.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_autopilot_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lancarkan ${autopilotState.targetName} & Mula Autopilot")
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tip: Ketika AI berada dalam aplikasi luar (seperti Luno), tarik Quick Settings Poco X7 Pro dan ketuk jubin 'NovaCore / Henti Autopilot' untuk henti kecemasan 1-sentuh.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Live Autopilot Status Card
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = if (autopilotState.isPausedForConfirm) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(
                            1.dp,
                            if (autopilotState.isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status Langsung",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Langkah ${autopilotState.currentStep}/${autopilotState.maxSteps}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (autopilotState.isRunning) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        (autopilotState.currentStep.toFloat() / autopilotState.maxSteps.coerceAtLeast(1))
                                            .coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = autopilotState.statusHeadline,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Live Step-by-Step Logs
                if (autopilotState.logs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Log Tindakan Autopilot (${autopilotState.logs.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(autopilotState.logs.reversed()) { log ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${log.step} • ${log.action}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = log.screenPackage.substringAfterLast('.'),
                                        style = TextStyle(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.detail,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}
