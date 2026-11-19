package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeneratedAppProjectEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.AiOpState

@Composable
fun ApkBuilderScreen(
    builderOpState: AiOpState,
    projects: List<GeneratedAppProjectEntity>,
    selectedProjectId: Long?,
    termuxInstalled: Boolean,
    termuxBanner: String?,
    onGenerateProject: (String) -> Unit,
    onSelectProject: (Long) -> Unit,
    onDeleteProject: (GeneratedAppProjectEntity) -> Unit,
    onCompileInTermux: (GeneratedAppProjectEntity) -> Unit,
    onCopyBuildScript: (GeneratedAppProjectEntity) -> Unit,
    onExportBuildSh: (GeneratedAppProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var ideaPrompt by rememberSaveable {
        mutableStateOf("Buat aplikasi Android penjejak bajet & perbelanjaan harian dengan graf kategori dan simpanan tempatan.")
    }
    var selectedCodeTab by rememberSaveable { mutableIntStateOf(0) }

    val selectedProject = remember(projects, selectedProjectId) {
        projects.firstOrNull { it.id == selectedProjectId } ?: projects.firstOrNull()
    }

    val quickIdeas = listOf(
        "Kalkulator Bajet & Rekod Belanja Harian RM",
        "Monitor Harga Kripto Live + Alert Sasaran",
        "Nota Harian & Senarai Semak Kerja Offline",
        "Pemasa Pomodoro Fokus & Tracker Belajar"
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

            // Frameless Studio Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("AI Pembina APK", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Jana projek Android Compose daripada idea anda & compile di Termux",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Prompt & Generator Card
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Terangkan APK yang anda mahu bina", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                quickIdeas.forEach { idea ->
                                    AssistChip(
                                        onClick = { ideaPrompt = idea },
                                        label = { Text(idea, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = ideaPrompt,
                                onValueChange = { ideaPrompt = it },
                                label = { Text("Idea / Fungsi APK Anda") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apk_builder_prompt_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val isLoading = builderOpState is AiOpState.Loading
                            Button(
                                onClick = { onGenerateProject(ideaPrompt) },
                                enabled = !isLoading && ideaPrompt.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_apk_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Sedang Menulis Kod Android...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Jana Projek APK Dengan AI")
                                }
                            }

                            when (builderOpState) {
                                is AiOpState.Success -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = builderOpState.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                is AiOpState.Error -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = builderOpState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> Unit
                            }

                            if (!termuxBanner.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = termuxBanner,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Generated Project Inspector
                if (selectedProject != null) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(selectedProject.appName, style = MaterialTheme.typography.titleLarge)
                                        Text(
                                            text = selectedProject.packageId,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (termuxInstalled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = if (termuxInstalled) "TERMUX READY" else "EXPORTABLE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = selectedProject.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons: Compile in Termux / Copy / Export .sh
                                Button(
                                    onClick = { onCompileInTermux(selectedProject) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("compile_apk_termux_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compile APK di Termux (gradle assembleDebug)")
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onCopyBuildScript(selectedProject) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("copy_build_script_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Salin Skrip", maxLines = 1)
                                    }
                                    OutlinedButton(
                                        onClick = { onExportBuildSh(selectedProject) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("export_build_sh_button")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Eksport .sh", maxLines = 1)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // File Viewer Tabs
                                val tabs = listOf("MainActivity.kt", "build_apk.sh", "Manifest.xml", "build.gradle.kts")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tabs.forEachIndexed { index, title ->
                                        FilterChip(
                                            selected = selectedCodeTab == index,
                                            onClick = { selectedCodeTab = index },
                                            label = { Text(title, maxLines = 1) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val shownCode = when (selectedCodeTab) {
                                    0 -> selectedProject.mainActivityKt
                                    1 -> selectedProject.termuxBuildSh
                                    2 -> selectedProject.manifestXml
                                    else -> selectedProject.buildGradleKts
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = androidx.compose.ui.graphics.Color(0xFF11141B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .horizontalScroll(rememberScrollState())
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = shownCode,
                                            style = TextStyle(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                color = androidx.compose.ui.graphics.Color(0xFFE5E7EB)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // History / Saved Generated APK Projects
                if (projects.isNotEmpty()) {
                    item {
                        Text(
                            text = "Projek APK Dijana (${projects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(projects, key = { it.id }) { proj ->
                        val isSelected = selectedProject?.id == proj.id
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            onClick = { onSelectProject(proj.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proj.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = proj.prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(onClick = { onDeleteProject(proj) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Padam projek")
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        }
    }
}
