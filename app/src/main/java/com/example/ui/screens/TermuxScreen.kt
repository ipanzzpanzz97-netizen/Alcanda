package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.TermuxScriptEntity
import com.example.device.TermuxBridge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.AiOpState
import com.example.viewmodel.NovaMainUiState

@Composable
fun TermuxScreen(
    uiState: NovaMainUiState,
    savedScripts: List<TermuxScriptEntity>,
    onUpdateEditor: (String, String) -> Unit,
    onGenerateCode: (String) -> Unit,
    onRunInTermux: () -> Unit,
    onSaveScript: () -> Unit,
    onCopyCode: () -> Unit,
    onExportScript: () -> Unit,
    onOpenTermux: () -> Unit,
    onCopySetupCommand: () -> Unit,
    onRequestTermuxPermission: () -> Unit,
    onLoadScript: (TermuxScriptEntity) -> Unit,
    onDeleteScript: (TermuxScriptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var aiGoalPrompt by rememberSaveable { mutableStateOf("") }
    var showSetupGuide by rememberSaveable { mutableStateOf(false) }

    val presets = listOf(
        "Update Termux & pasang Python + Git" to "pkg update -y && pkg upgrade -y && pkg install -y python git curl",
        "Python HTTP Server (Port 8080)" to "python3 -m http.server 8080",
        "Semak IP, Port & Disk" to "ip addr show | grep inet && df -h && free -m",
        "Setup Node.js Express" to "pkg install -y nodejs && mkdir -p ~/nova_server && cd ~/nova_server && npm init -y && npm install express"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Frameless Modern Header + Collapsible Setup Guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Termux AI Studio",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = if (uiState.termuxInstalled) {
                                "Termux dikesan • RUN_COMMAND sedia"
                            } else {
                                "Mod editor & eksport skrip .sh aktif"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.termuxInstalled) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = { showSetupGuide = !showSetupGuide },
                        modifier = Modifier.testTag("termux_setup_guide_button")
                    ) {
                        Text(if (showSetupGuide) "Tutup" else "Panduan")
                    }
                }

                AnimatedVisibility(visible = showSetupGuide) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Untuk membenarkan NovaCore menghantar arahan terus ke Termux, jalankan arahan ini sekali sahaja di dalam Termux anda:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = androidx.compose.ui.graphics.Color(0xFF11141B)
                            ) {
                                Text(
                                    text = TermuxBridge.TERMUX_ALLOW_EXTERNAL_CMD,
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = androidx.compose.ui.graphics.Color(0xFF34D399)
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = onCopySetupCommand,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Arahan Setup Termux")
                            }
                            if (uiState.termuxInstalled && !uiState.termuxPermissionGranted) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onRequestTermuxPermission,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("request_termux_permission_button")
                                ) {
                                    Text("Minta Izin RUN_COMMAND Termux")
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Script Generator Box
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Jana Skrip Termux Dengan AI",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = aiGoalPrompt,
                            onValueChange = { aiGoalPrompt = it },
                            placeholder = { Text("Contoh: Buat skrip Python automasi backup folder...") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("termux_ai_prompt_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presets.forEach { (label, cmd) ->
                                    AssistChip(
                                        onClick = { onUpdateEditor("preset.sh", "#!/data/data/com.termux/files/usr/bin/bash\n$cmd\n") },
                                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onGenerateCode(aiGoalPrompt) },
                                enabled = aiGoalPrompt.isNotBlank() && uiState.termuxOpState !is AiOpState.Loading && uiState.termuxOpState !is AiOpState.Streaming,
                                modifier = Modifier.testTag("generate_termux_button")
                            ) {
                                if (uiState.termuxOpState is AiOpState.Loading || uiState.termuxOpState is AiOpState.Streaming) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kodkan")
                            }
                        }
                        if (uiState.termuxOpState is AiOpState.Error) {
                            Text(
                                text = uiState.termuxOpState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Terminal Code Editor
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = uiState.termuxScriptName,
                            onValueChange = { onUpdateEditor(it, uiState.termuxCode) },
                            label = { Text("Nama Fail Skrip") },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = JetBrainsMonoFontFamily, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.termuxCode,
                            onValueChange = { onUpdateEditor(uiState.termuxScriptName, it) },
                            label = { Text("Kod Termux (Bash / Python)") },
                            minLines = 7,
                            maxLines = 14,
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("termux_code_editor")
                        )

                        AnimatedVisibility(visible = uiState.termuxStatusBanner != null) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = uiState.termuxStatusBanner.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onRunInTermux,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("run_termux_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Termux")
                            }
                            OutlinedButton(
                                onClick = onOpenTermux,
                                modifier = Modifier.testTag("open_termux_app_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onCopyCode,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin")
                            }
                            OutlinedButton(
                                onClick = onSaveScript,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simpan")
                            }
                            OutlinedButton(
                                onClick = onExportScript,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Eksport")
                            }
                        }
                    }
                }
            }
        }

        // Saved Scripts Library
        if (savedScripts.isNotEmpty()) {
            item {
                Text(
                    text = "Pustaka Skrip Termux Tersimpan",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                )
            }
            items(savedScripts, key = { it.id }) { script ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .clickable { onLoadScript(script) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = script.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = script.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = script.code.lineSequence().take(2).joinToString(" "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeleteScript(script) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete script",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
