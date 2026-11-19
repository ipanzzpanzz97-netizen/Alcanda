package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AiProviderProfileEntity
import com.example.data.AiProviderType
import com.example.viewmodel.AiOpState

@Composable
fun AiModelsScreen(
    profiles: List<AiProviderProfileEntity>,
    activeProfile: AiProviderProfileEntity?,
    connectionTestState: AiOpState,
    onToggleProfileSwitch: (AiProviderProfileEntity, Boolean) -> Unit,
    onSaveProfile: (
        existingId: Long?,
        name: String,
        providerType: AiProviderType,
        apiKey: String,
        baseUrl: String,
        modelName: String,
        activateNow: Boolean,
        requiresKey: Boolean,
        signupUrl: String
    ) -> Unit,
    onDeleteProfile: (AiProviderProfileEntity) -> Unit,
    onTestConnection: (
        providerType: AiProviderType,
        apiKey: String,
        baseUrl: String,
        modelName: String,
        requiresKey: Boolean
    ) -> Unit,
    onRestoreFreePresets: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onClearConnectionTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var selectedType by remember { mutableStateOf(AiProviderType.GROQ) }
    var profileName by rememberSaveable { mutableStateOf("") }
    var apiKeyInput by rememberSaveable { mutableStateOf("") }
    var baseUrlInput by rememberSaveable { mutableStateOf(AiProviderType.GROQ.defaultBaseUrl) }
    var modelInput by rememberSaveable { mutableStateOf(AiProviderType.GROQ.defaultModel) }
    var requiresKeyFlag by rememberSaveable { mutableStateOf(true) }
    var signupUrlInput by rememberSaveable { mutableStateOf("") }
    var showApiKey by rememberSaveable { mutableStateOf(false) }

    fun openNewForm(type: AiProviderType) {
        editingId = null
        selectedType = type
        profileName = type.displayLabel.substringBefore(" (")
        apiKeyInput = ""
        baseUrlInput = type.defaultBaseUrl
        modelInput = type.defaultModel
        requiresKeyFlag = type.requiresKey
        signupUrlInput = ""
        showApiKey = false
        onClearConnectionTest()
        showEditor = true
    }

    fun openEditForm(profile: AiProviderProfileEntity) {
        val t = profile.typeEnum()
        editingId = profile.id
        selectedType = t
        profileName = profile.name
        apiKeyInput = profile.apiKey
        baseUrlInput = profile.baseUrl.ifBlank { t.defaultBaseUrl }
        modelInput = profile.modelName.ifBlank { t.defaultModel }
        requiresKeyFlag = profile.requiresKey
        signupUrlInput = profile.signupUrl
        showApiKey = false
        onClearConnectionTest()
        showEditor = true
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enjin AI & API",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Aktif: ${activeProfile?.name ?: "NovaCore Gemini 3.8 Flash"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onRestoreFreePresets,
                            modifier = Modifier.testTag("restore_free_ai_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restore free AI list")
                        }
                        Button(
                            onClick = {
                                if (showEditor) showEditor = false else openNewForm(AiProviderType.GROQ)
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("add_ai_profile_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showEditor) "Tutup" else "Tambah")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hidupkan satu AI yang anda mahu pakai — AI lain akan OFF secara automatik.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Add / Edit Provider Form
        item {
            AnimatedVisibility(visible = showEditor) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (editingId == null) "Tambah API AI Baharu" else "Tetapan: $profileName",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (editingId == null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AiProviderType.entries
                                        .filter { it != AiProviderType.FIREBASE_AI_LOGIC }
                                        .forEach { type ->
                                            FilterChip(
                                                selected = selectedType == type,
                                                onClick = {
                                                    selectedType = type
                                                    profileName = type.displayLabel.substringBefore(" (")
                                                    baseUrlInput = type.defaultBaseUrl
                                                    modelInput = type.defaultModel
                                                    requiresKeyFlag = type.requiresKey
                                                    onClearConnectionTest()
                                                },
                                                label = { Text(type.displayLabel.substringBefore(" (")) },
                                                modifier = Modifier.testTag("provider_chip_${type.name.lowercase()}")
                                            )
                                        }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (signupUrlInput.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { onOpenUrl(signupUrlInput) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dapatkan Free API Key di pelayar")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text("Nama Profil") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_profile_name_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = modelInput,
                                onValueChange = { modelInput = it },
                                label = { Text("Model ID") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_model_input")
                            )

                            if (selectedType != AiProviderType.FIREBASE_AI_LOGIC) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = { apiKeyInput = it },
                                    label = {
                                        Text(
                                            if (requiresKeyFlag) "API Key (Free / Berbayar)" else "API Key (Kosongkan jika tiada key)"
                                        )
                                    },
                                    singleLine = true,
                                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showApiKey = !showApiKey }) {
                                            Icon(
                                                if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle API key visibility"
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_api_key_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = baseUrlInput,
                                    onValueChange = { baseUrlInput = it },
                                    label = { Text("Base URL API") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_base_url_input")
                                )
                            }

                            if (connectionTestState !is AiOpState.Idle) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = when (connectionTestState) {
                                        is AiOpState.Success -> MaterialTheme.colorScheme.secondaryContainer
                                        is AiOpState.Error -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (connectionTestState is AiOpState.Loading) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Menguji sambungan API...")
                                        } else {
                                            val msg = when (connectionTestState) {
                                                is AiOpState.Success -> connectionTestState.text
                                                is AiOpState.Error -> connectionTestState.message
                                                else -> ""
                                            }
                                            Text(text = msg, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onTestConnection(selectedType, apiKeyInput, baseUrlInput, modelInput, requiresKeyFlag)
                                    },
                                    enabled = connectionTestState !is AiOpState.Loading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_ai_connection_button")
                                ) {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Uji API")
                                }
                                Button(
                                    onClick = {
                                        onSaveProfile(
                                            editingId,
                                            profileName,
                                            selectedType,
                                            apiKeyInput,
                                            baseUrlInput,
                                            modelInput,
                                            true,
                                            requiresKeyFlag,
                                            signupUrlInput
                                        )
                                        showEditor = false
                                    },
                                    enabled = !requiresKeyFlag || apiKeyInput.isNotBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_ai_profile_button")
                                ) {
                                    Text("Simpan & ON")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saved Profiles List with Mutual-Exclusion Switch
        items(profiles, key = { it.id }) { profile ->
            val type = profile.typeEnum()
            val needsKeySetup = profile.requiresKey && profile.apiKey.isBlank()

            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (profile.isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                border = BorderStroke(
                    width = if (profile.isActive) 1.5.dp else 1.dp,
                    color = if (profile.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .testTag("ai_profile_card_${profile.id}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (profile.badgeText.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (!profile.requiresKey) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = profile.badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (!profile.requiresKey) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Model: ${profile.modelName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = when {
                                    !profile.requiresKey -> "Status: Sedia Terus (Tanpa API Key)"
                                    profile.apiKey.isNotBlank() -> {
                                        val masked = if (profile.apiKey.length > 8) {
                                            "${profile.apiKey.take(4)}••••${profile.apiKey.takeLast(4)}"
                                        } else "••••••"
                                        "API Key: $masked (Sedia)"
                                    }
                                    else -> "Perlu Free API Key — ketuk Edit atau Dapatkan Key"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (needsKeySetup) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }

                        Switch(
                            checked = profile.isActive,
                            onCheckedChange = { wantOn ->
                                if (wantOn && needsKeySetup) {
                                    openEditForm(profile)
                                } else {
                                    onToggleProfileSwitch(profile, wantOn)
                                }
                            },
                            modifier = Modifier.testTag("ai_switch_${profile.id}")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (profile.signupUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onOpenUrl(profile.signupUrl) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Free Key", maxLines = 1)
                            }
                        }
                        if (type != AiProviderType.FIREBASE_AI_LOGIC) {
                            OutlinedButton(
                                onClick = { openEditForm(profile) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (needsKeySetup) "Isi Key" else "Edit", maxLines = 1)
                            }
                            IconButton(onClick = { onDeleteProfile(profile) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete ${profile.name}",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
