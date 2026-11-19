package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SystemErrorEntity
import com.example.viewmodel.HealthCheckStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepairScreen(
    healthCheck: HealthCheckStatus,
    errors: List<SystemErrorEntity>,
    diagnosingErrorId: Long?,
    onRunDiagnostics: () -> Unit,
    onRunAutoHealAll: () -> Unit,
    onRunAiRepairIssue: (String) -> Unit,
    onApplyQuickFixKey: (String) -> Unit,
    onApplyAutoFix: (SystemErrorEntity) -> Unit,
    onAskAiDiagnose: (SystemErrorEntity) -> Unit,
    onResolveError: (Long) -> Unit,
    onClearResolved: () -> Unit,
    onClearAll: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAiModels: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bugReportInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onRunDiagnostics()
    }

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

            // Frameless Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("repair_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Doktor & Pusat Baiki", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Diagnostik & pemulihan automatik",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onRunDiagnostics,
                    enabled = !healthCheck.isRunning,
                    modifier = Modifier.testTag("run_diagnostics_button")
                ) {
                    if (healthCheck.isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan health")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // AI Self-Healing Doctor Card
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Pembaiki APK Automatik", style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Jika ada ciri rosak (AI gagal sambung, suara tersekat, Termux atau Luno bermasalah), AI akan uji & pulihkan modul secara automatik.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onRunAutoHealAll,
                                enabled = !healthCheck.isAutoHealing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_auto_heal_all_button")
                            ) {
                                if (healthCheck.isAutoHealing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                } else {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (healthCheck.isAutoHealing) "Sedang Membaiki..." else "AI Baiki Semua Automatik")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = bugReportInput,
                                onValueChange = { bugReportInput = it },
                                label = { Text("Terangkan kerosakan / fitur tidak berfungsi") },
                                placeholder = { Text("Contoh: AI tak jawab / Mic tak dengar / Termux error") },
                                minLines = 2,
                                maxLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("repair_issue_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onRunAiRepairIssue(bugReportInput)
                                    bugReportInput = ""
                                },
                                enabled = bugReportInput.isNotBlank() && !healthCheck.isAutoHealing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_diagnose_repair_button")
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suruh AI Kesan & Baiki Masalah Ini")
                            }

                            if (!healthCheck.lastHealReport.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = healthCheck.lastHealReport,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Health Check Card
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Status Kesihatan Semua Modul", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(10.dp))

                            HealthRow(
                                title = "Sambungan Internet",
                                ok = healthCheck.internetOk,
                                detail = if (healthCheck.internetOk) "Aktif & Disahkan" else "Tiada internet dikesan"
                            )
                            HealthRow(
                                title = "AI Engine Aktif",
                                ok = healthCheck.aiEngineOk,
                                detail = healthCheck.aiEngineNote,
                                actionLabel = if (healthCheck.aiEngineOk == false) "Auto-Tukar" else "Urus AI",
                                onAction = if (healthCheck.aiEngineOk == false) {
                                    { onApplyQuickFixKey("FIX_AUTO_FAILOVER_FREE_AI") }
                                } else {
                                    onOpenAiModels
                                }
                            )
                            HealthRow(
                                title = "Pasaran Luno API (api.luno.com)",
                                ok = healthCheck.lunoApiOk,
                                detail = healthCheck.lunoApiNote,
                                actionLabel = if (healthCheck.lunoApiOk == false) "Cuba Lagi" else null,
                                onAction = if (healthCheck.lunoApiOk == false) {
                                    { onApplyQuickFixKey("FIX_RETRY_LUNO") }
                                } else null
                            )
                            HealthRow(
                                title = "Jambatan Termux & Izin RUN_COMMAND",
                                ok = healthCheck.termuxOk && healthCheck.termuxPermissionOk,
                                detail = when {
                                    !healthCheck.termuxOk -> "Termux belum dipasang (mod salin/eksport tetap aktif)"
                                    !healthCheck.termuxPermissionOk -> "Termux ada • Izin RUN_COMMAND belum diberi"
                                    else -> "Termux & RUN_COMMAND Sedia"
                                },
                                actionLabel = when {
                                    healthCheck.termuxOk && !healthCheck.termuxPermissionOk -> "Minta Izin"
                                    healthCheck.termuxOk -> "Setup"
                                    else -> null
                                },
                                onAction = if (healthCheck.termuxOk && !healthCheck.termuxPermissionOk) {
                                    { onApplyQuickFixKey("FIX_TERMUX_PERMISSION") }
                                } else if (healthCheck.termuxOk) {
                                    { onApplyQuickFixKey("FIX_TERMUX_SETUP") }
                                } else null
                            )
                            HealthRow(
                                title = "Mikrofon & Suara (SpeechRecognizer)",
                                ok = healthCheck.voiceOk,
                                detail = if (healthCheck.voiceOk) "Enjin pengecaman suara & TTS tersedia" else "Perkhidmatan suara Google/HyperOS tidak ditemui",
                                actionLabel = "Reset Mic",
                                onAction = { onApplyQuickFixKey("FIX_RESET_VOICE") }
                            )
                            HealthRow(
                                title = "Accessibility Kawalan Luar (Poco X7 Pro)",
                                ok = healthCheck.accessibilityOk,
                                detail = if (healthCheck.accessibilityOk) "Aktif untuk Autopilot & kawalan luar" else "Belum diaktifkan dalam Accessibility Settings",
                                actionLabel = if (!healthCheck.accessibilityOk) "Aktifkan" else null,
                                onAction = onOpenAccessibility
                            )
                            HealthRow(
                                title = "Pangkalan Data Room Tempatan",
                                ok = healthCheck.roomDbOk,
                                detail = if (healthCheck.roomDbOk) "Simpanan sembang, skrip & projek APK sihat" else "Ralat akses pangkalan data"
                            )
                        }
                    }
                }

                // Error Log Header + Bulk Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rekod Ralat (${errors.count { !it.isResolved }} belum selesai)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (errors.any { it.isResolved }) {
                            OutlinedButton(onClick = onClearResolved) {
                                Text("Buang Selesai", maxLines = 1)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (errors.isNotEmpty()) {
                            IconButton(onClick = onClearAll) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all errors")
                            }
                        }
                    }
                }

                if (errors.isEmpty()) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Tiada ralat direkodkan setakat ini. Semua modul NovaCore berjalan lancar!",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    items(errors, key = { it.id }) { err ->
                        SystemErrorCard(
                            error = err,
                            isDiagnosing = diagnosingErrorId == err.id,
                            onApplyFix = { onApplyAutoFix(err) },
                            onAskAi = { onAskAiDiagnose(err) },
                            onResolve = { onResolveError(err.id) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HealthRow(
    title: String,
    ok: Boolean?,
    detail: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val tint = when (ok) {
        true -> MaterialTheme.colorScheme.secondary
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.tertiary
    }
    val icon = when (ok) {
        true -> Icons.Default.CheckCircle
        false -> Icons.Default.ErrorOutline
        null -> Icons.Default.WarningAmber
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = detail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onAction) {
                Text(actionLabel, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SystemErrorCard(
    error: SystemErrorEntity,
    isDiagnosing: Boolean,
    onApplyFix: () -> Unit,
    onAskAi: () -> Unit,
    onResolve: () -> Unit
) {
    val timeText = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(error.timestamp))
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            1.dp,
            if (error.isResolved) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (error.isResolved) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (error.isResolved) "${error.module} • SELESAI" else error.module,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (error.isResolved) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(timeText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (error.aiDiagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cadangan AI:\n${error.aiDiagnosis}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (!error.isResolved) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (error.suggestedFixKey.isNotBlank()) {
                        Button(
                            onClick = onApplyFix,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Baiki", maxLines = 1)
                        }
                    }

                    OutlinedButton(
                        onClick = onAskAi,
                        enabled = !isDiagnosing,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isDiagnosing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analisa AI", maxLines = 1)
                    }

                    OutlinedButton(onClick = onResolve) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}
