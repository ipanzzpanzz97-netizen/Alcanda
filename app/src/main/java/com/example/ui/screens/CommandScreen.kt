package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import com.example.R
import com.example.data.ChatMessageEntity
import com.example.data.UserSettings
import com.example.device.VoiceUiState
import com.example.viewmodel.AiOpState
import com.example.viewmodel.NovaMainUiState

@Composable
fun CommandScreen(
    uiState: NovaMainUiState,
    activeAiLabel: String,
    unresolvedErrorCount: Int,
    voiceState: VoiceUiState,
    settings: UserSettings,
    messages: List<ChatMessageEntity>,
    onVoiceOrbClick: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onToggleLocale: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenCamera: () -> Unit,
    onPickFile: () -> Unit,
    onPickPhoto: () -> Unit,
    onOpenAiModels: () -> Unit,
    onOpenLunoTrading: () -> Unit,
    onOpenApkBuilder: () -> Unit,
    onOpenAutopilot: () -> Unit,
    onOpenTermux: () -> Unit,
    onOpenGameArena: () -> Unit,
    onOpenRepairCenter: () -> Unit,
    onClearChat: () -> Unit,
    onClearVoiceError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
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

            // Top AI Command Header Card
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_novacore_avatar),
                        contentDescription = "NovaCore AI Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenAiModels)
                            .testTag("switch_ai_header_button")
                    ) {
                        Text(
                            text = "NovaCore AI",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$activeAiLabel • Tukar AI ▾",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Locale switch BM / EN
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleLocale)
                            .testTag("locale_toggle_button")
                    ) {
                        Text(
                            text = if (settings.speechLocale.startsWith("ms")) "BM" else "EN",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenRepairCenter,
                        modifier = Modifier.testTag("repair_center_header_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unresolvedErrorCount > 0) {
                                    Badge { Text(unresolvedErrorCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Pusat Baiki & Diagnostik",
                                tint = if (unresolvedErrorCount > 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { onToggleAutoSpeak(!settings.autoSpeak) },
                        modifier = Modifier.testTag("tts_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (settings.autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle voice reply",
                            tint = if (settings.autoSpeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.testTag("clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear conversation",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Voice Command Orb Banner (Tap Icon to Talk & AI Obeys)
            VoiceCommandOrbCard(
                voiceState = voiceState,
                isAiBusy = uiState.commandOpState is AiOpState.Loading,
                onVoiceOrbClick = onVoiceOrbClick,
                onDismissError = onClearVoiceError
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onOpenApkBuilder,
                    label = { Text("Bina APK") },
                    leadingIcon = {
                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_builder_chip")
                )
                AssistChip(
                    onClick = onOpenAutopilot,
                    label = { Text("Autopilot") },
                    leadingIcon = {
                        Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_autopilot_chip")
                )
                AssistChip(
                    onClick = onOpenLunoTrading,
                    label = { Text("Luno AI") },
                    leadingIcon = {
                        Icon(Icons.Default.CandlestickChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_luno_chip")
                )
                AssistChip(
                    onClick = onOpenTermux,
                    label = { Text("Termux") },
                    leadingIcon = {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_termux_chip")
                )
                AssistChip(
                    onClick = onOpenGameArena,
                    label = { Text("Game Arena") },
                    leadingIcon = {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_game_chip")
                )
                AssistChip(
                    onClick = onOpenRepairCenter,
                    label = { Text(if (unresolvedErrorCount > 0) "Baiki ($unresolvedErrorCount)" else "Diagnostik") },
                    leadingIcon = {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_repair_chip")
                )
                AssistChip(
                    onClick = onOpenCamera,
                    label = { Text("Kamera AI") },
                    leadingIcon = {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_camera_chip")
                )
                AssistChip(
                    onClick = onPickFile,
                    label = { Text("Akses Fail") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier.testTag("quick_file_chip")
                )
                AssistChip(
                    onClick = onToggleTorch,
                    label = { Text(if (uiState.torchOn) "Flash ON" else "Flash OFF") },
                    leadingIcon = {
                        Icon(
                            if (uiState.torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (uiState.torchOn) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    modifier = Modifier.testTag("torch_toggle_button")
                )
                AssistChip(
                    onClick = { onSendPrompt("Kunci skrin telefon sekarang") },
                    label = { Text("Kunci Skrin") }
                )
                AssistChip(
                    onClick = { onSendPrompt("Buka menu power off telefon") },
                    label = { Text("Off Telefon") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Conversation Feed
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    EmptyCommandFeed(onSuggestionClick = onSendPrompt)
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(message = msg)
                        }
                        if (uiState.commandOpState is AiOpState.Loading) {
                            item {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    modifier = Modifier.padding(end = 48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "NovaCore sedang berfikir & melaksanakan arahan...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickPhoto,
                    modifier = Modifier.testTag("pick_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Select photo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Taip atau tekan ikon suara...",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendPrompt(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && uiState.commandOpState !is AiOpState.Loading,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send command"
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceCommandOrbCard(
    voiceState: VoiceUiState,
    isAiBusy: Boolean,
    onVoiceOrbClick: () -> Unit,
    onDismissError: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )
    val rmsScale by animateFloatAsState(
        targetValue = if (voiceState.isListening) 1.08f + (voiceState.rmsLevel * 0.25f) else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "rms_scale"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
        )
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = if (voiceState.isListening) 1.5.dp else 1.dp,
            color = if (voiceState.isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(if (voiceState.isListening) rmsScale else idlePulse)
                        .clip(CircleShape)
                        .background(
                            if (voiceState.isListening) {
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.error,
                                        MaterialTheme.colorScheme.errorContainer
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        )
                        .clickable(enabled = !isAiBusy, onClick = onVoiceOrbClick)
                        .testTag("voice_orb_button")
                ) {
                    Icon(
                        imageVector = if (voiceState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (voiceState.isListening) "Stop listening" else "Tap to speak to NovaCore",
                        tint = if (voiceState.isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            voiceState.isListening -> "Sedang mendengar..."
                            isAiBusy -> "Melaksanakan arahan..."
                            else -> "Klik ikon & bercakap dengan saya"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            voiceState.partialTranscript.isNotBlank() -> "\"${voiceState.partialTranscript}\""
                            voiceState.isListening -> "Sebut arahan anda (BM / English)..."
                            else -> "Contoh: \"Buka kamera\", \"Semak bateri\", \"Coding Termux\", \"Main game\""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(visible = voiceState.voiceError != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable(onClick = onDismissError)
                ) {
                    Text(
                        text = "${voiceState.voiceError.orEmpty()} (Ketuk untuk tutup)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCommandFeed(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "Semak status bateri, RAM, dan storan telefon saya",
        "Kodkan skrip Termux Python untuk ujian kelajuan rangkaian",
        "Buka kamera supaya awak boleh lihat dan terangkan objek",
        "Jom main Tic-Tac-Toe, saya X dan awak O!"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pembantu AI Peranti Anda Sedia",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Saya boleh kawal kamera, akses fail, baca sensor telefon, kod Termux, dan bermain game bersama anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        suggestions.forEach { tip ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onSuggestionClick(tip) }
            ) {
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser && message.actionBadge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "ACTION: ${message.actionBadge}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
