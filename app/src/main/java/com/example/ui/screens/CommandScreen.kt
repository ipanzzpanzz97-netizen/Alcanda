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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                .widthIn(max = 820.dp)
        ) {
            // Minimalist Flagship Top Header (No bulky outer box)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_novacore_avatar),
                    contentDescription = "NovaCore Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))

                // Interactive Model Selector Pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    onClick = onOpenAiModels,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag("switch_ai_header_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = activeAiLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Tukar model AI",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Compact BM / EN pill
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onToggleLocale)
                        .testTag("locale_toggle_button")
                ) {
                    Text(
                        text = if (settings.speechLocale.startsWith("ms")) "BM" else "EN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                        imageVector = if (settings.autoSpeak) {
                            Icons.AutoMirrored.Filled.VolumeUp
                        } else {
                            Icons.AutoMirrored.Filled.VolumeOff
                        },
                        contentDescription = "Toggle voice reply",
                        tint = if (settings.autoSpeak) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
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

            // Soft Borderless Quick Action Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernShortcutPill(
                    label = "Bina APK",
                    icon = Icons.Default.Android,
                    onClick = onOpenApkBuilder,
                    modifier = Modifier.testTag("quick_builder_chip")
                )
                ModernShortcutPill(
                    label = "Autopilot",
                    icon = Icons.Default.AutoMode,
                    onClick = onOpenAutopilot,
                    modifier = Modifier.testTag("quick_autopilot_chip")
                )
                ModernShortcutPill(
                    label = "Luno AI",
                    icon = Icons.Default.CandlestickChart,
                    onClick = onOpenLunoTrading,
                    modifier = Modifier.testTag("quick_luno_chip")
                )
                ModernShortcutPill(
                    label = "Termux",
                    icon = Icons.Default.Terminal,
                    onClick = onOpenTermux,
                    modifier = Modifier.testTag("quick_termux_chip")
                )
                ModernShortcutPill(
                    label = "Game Arena",
                    icon = Icons.Default.SportsEsports,
                    onClick = onOpenGameArena,
                    modifier = Modifier.testTag("quick_game_chip")
                )
                ModernShortcutPill(
                    label = if (unresolvedErrorCount > 0) "Baiki ($unresolvedErrorCount)" else "Diagnostik",
                    icon = Icons.Default.Build,
                    highlighted = unresolvedErrorCount > 0,
                    onClick = onOpenRepairCenter,
                    modifier = Modifier.testTag("quick_repair_chip")
                )
                ModernShortcutPill(
                    label = "Kamera AI",
                    icon = Icons.Default.CameraAlt,
                    onClick = onOpenCamera,
                    modifier = Modifier.testTag("quick_camera_chip")
                )
                ModernShortcutPill(
                    label = "Akses Fail",
                    icon = Icons.Default.AttachFile,
                    onClick = onPickFile,
                    modifier = Modifier.testTag("quick_file_chip")
                )
                ModernShortcutPill(
                    label = if (uiState.torchOn) "Flash ON" else "Flash",
                    icon = if (uiState.torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    highlighted = uiState.torchOn,
                    onClick = onToggleTorch,
                    modifier = Modifier.testTag("torch_toggle_button")
                )
                ModernShortcutPill(
                    label = "Kunci Skrin",
                    onClick = { onSendPrompt("Kunci skrin telefon sekarang") }
                )
                ModernShortcutPill(
                    label = "Off Telefon",
                    onClick = { onSendPrompt("Buka menu power off telefon") }
                )
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Main Conversation Feed
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    EmptyCommandFeed(onSuggestionClick = onSendPrompt)
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(message = msg)
                        }
                        if (uiState.commandOpState is AiOpState.Loading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "NovaCore sedang memproses arahan...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Voice Transcript / Error Strip (Appears smoothly only when active)
            AnimatedVisibility(
                visible = voiceState.isListening || voiceState.partialTranscript.isNotBlank() || voiceState.voiceError != null
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    if (voiceState.isListening || voiceState.partialTranscript.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LivePulseDot()
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (voiceState.partialTranscript.isNotBlank()) {
                                        "\"${voiceState.partialTranscript}\""
                                    } else {
                                        "Sedang mendengar (${if (settings.speechLocale.startsWith("ms")) "BM" else "EN"})..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    if (voiceState.voiceError != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onClearVoiceError)
                        ) {
                            Text(
                                text = "${voiceState.voiceError} • Ketuk untuk tutup",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Docked Flagship Capsule Composer (ChatGPT / Gemini style with inline Mic + Send)
            DockedComposerPill(
                inputText = inputText,
                onInputChange = { inputText = it },
                isAiBusy = uiState.commandOpState is AiOpState.Loading,
                voiceState = voiceState,
                onPickPhoto = onPickPhoto,
                onVoiceOrbClick = onVoiceOrbClick,
                onSend = {
                    if (inputText.isNotBlank()) {
                        onSendPrompt(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun ModernShortcutPill(
    label: String,
    icon: ImageVector? = null,
    highlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlighted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DockedComposerPill(
    inputText: String,
    onInputChange: (String) -> Unit,
    isAiBusy: Boolean,
    voiceState: VoiceUiState,
    onPickPhoto: () -> Unit,
    onVoiceOrbClick: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rmsScale by animateFloatAsState(
        targetValue = if (voiceState.isListening) 1.08f + (voiceState.rmsLevel * 0.16f) else 1f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "mic_rms_scale"
    )

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (voiceState.isListening) 1.5.dp else 1.dp,
            color = if (voiceState.isListening) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPickPhoto,
                modifier = Modifier.testTag("pick_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Select photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        text = if (voiceState.isListening) "Mendengar suara anda..." else "Tanya atau arahkan NovaCore...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input")
            )

            // Inline Voice Mic / Stop Button
            FilledIconButton(
                onClick = onVoiceOrbClick,
                enabled = !isAiBusy,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (voiceState.isListening) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (voiceState.isListening) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                modifier = Modifier
                    .size(42.dp)
                    .scale(rmsScale)
                    .testTag("voice_orb_button")
            ) {
                Icon(
                    imageVector = if (voiceState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (voiceState.isListening) "Stop listening" else "Tap to speak to NovaCore",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button
            FilledIconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isAiBusy,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .size(42.dp)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send command",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LivePulseDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_dot")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .scale(pulse)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun EmptyCommandFeed(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "Semak status bateri, RAM, dan storan telefon saya",
        "Analisa pasaran Luno MYR & cadang entry modal RM40",
        "Bina skrip Termux Python untuk semak rangkaian",
        "Buka kamera supaya awak boleh lihat & terangkan objek"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_novacore_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Apa yang anda mahu buat?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Taip atau ketuk ikon mikrofon untuk kawal peranti, bina APK, kod Termux, atau trading Luno.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        suggestions.forEach { tip ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSuggestionClick(tip) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    if (isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 6.dp
                ),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)
                )
            }
        }
    } else {
        // Editorial open AI reply (Perplexity / ChatGPT style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_novacore_avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (message.actionBadge.isNotBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = message.actionBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
