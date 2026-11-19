package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.game.ArenaGameType
import com.example.game.Connect4State
import com.example.game.Cyber2048State
import com.example.game.TicTacToeState
import com.example.viewmodel.NovaMainUiState

@Composable
fun GameArenaScreen(
    uiState: NovaMainUiState,
    onSelectGame: (ArenaGameType) -> Unit,
    onResetGame: () -> Unit,
    onPlayTicTacToe: (Int) -> Unit,
    onPlayConnect4: (Int) -> Unit,
    onPlay2048: (String) -> Unit,
    onTriggerAiMove: () -> Unit,
    onToggleAiAutoPlay: () -> Unit,
    onLaunchExternalGame: (String, String) -> Unit,
    onStartExternalMacro: (String, Float, Float, Long) -> Unit,
    onStopExternalMacro: () -> Unit,
    onOpenAccessibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Frameless Header & Game Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Game Arena AI", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Lawan AI atau jalankan Auto-Play & Co-Pilot Game",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onResetGame, modifier = Modifier.testTag("reset_game_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset game")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArenaGameType.entries.forEach { game ->
                    FilterChip(
                        selected = uiState.activeGame == game,
                        onClick = { onSelectGame(game) },
                        label = { Text(game.title) },
                        modifier = Modifier.testTag("game_tab_${game.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Commentary + Controls
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.aiGameThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.aiGameThinking) "NovaCore sedang mengira langkah terbaik..." else uiState.aiGameComment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTriggerAiMove,
                    enabled = !uiState.aiGameThinking && !uiState.aiAutoPlayRunning,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_single_move_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Main 1 Langkah")
                }

                Button(
                    onClick = onToggleAiAutoPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.aiAutoPlayRunning) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        contentColor = if (uiState.aiAutoPlayRunning) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onSecondary
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_autoplay_button")
                ) {
                    Icon(
                        if (uiState.aiAutoPlayRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.aiAutoPlayRunning) "Henti Auto-Play" else "AI Auto-Play")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Board
            when (uiState.activeGame) {
                ArenaGameType.TICTACTOE -> TicTacToeBoard(
                    state = uiState.tttState,
                    onCellClick = onPlayTicTacToe
                )
                ArenaGameType.CONNECT4 -> Connect4Board(
                    state = uiState.c4State,
                    onColumnClick = onPlayConnect4
                )
                ArenaGameType.CYBER_2048 -> Cyber2048Board(
                    state = uiState.state2048,
                    onMove = onPlay2048
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ExternalGameCopilotCard(
                uiState = uiState,
                onLaunchExternalGame = onLaunchExternalGame,
                onStartExternalMacro = onStartExternalMacro,
                onStopExternalMacro = onStopExternalMacro,
                onOpenAccessibility = onOpenAccessibility
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExternalGameCopilotCard(
    uiState: NovaMainUiState,
    onLaunchExternalGame: (String, String) -> Unit,
    onStartExternalMacro: (String, Float, Float, Long) -> Unit,
    onStopExternalMacro: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    var searchGame by rememberSaveable { mutableStateOf("") }
    var macroMode by rememberSaveable { mutableStateOf("TAP") }
    var xPct by rememberSaveable { mutableFloatStateOf(50f) }
    var yPct by rememberSaveable { mutableFloatStateOf(72f) }
    var intervalMs by rememberSaveable { mutableFloatStateOf(1200f) }

    val matchingApps = remember(uiState.installedApps, searchGame) {
        val base = if (searchGame.isBlank()) {
            val games = uiState.installedApps.filter { it.isGame }
            if (games.isNotEmpty()) games else uiState.installedApps
        } else {
            uiState.installedApps.filter {
                it.appName.contains(searchGame, ignoreCase = true) ||
                    it.packageName.contains(searchGame, ignoreCase = true)
            }
        }
        base.take(8)
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Co-Pilot Game Luar (Online / Offline)", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = uiState.externalGameStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!uiState.accessibilityReady) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aktifkan Accessibility untuk Auto-Tap/Swipe Game")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchGame,
                onValueChange = { searchGame = it },
                placeholder = { Text("Cari mana-mana game online/offline di telefon...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (matchingApps.isEmpty()) {
                Text(
                    text = "Tiada game dikesan buat masa ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                matchingApps.forEach { app ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onLaunchExternalGame(app.packageName, app.appName) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (app.isGame) {
                                Text(
                                    text = "GAME",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = "Launch game", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Mod Kawalan Skrin Automatik", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("TAP" to "Tap", "SWIPE_UP" to "Leret Atas", "SWIPE_LEFT" to "Kiri", "SWIPE_RIGHT" to "Kanan").forEach { (id, label) ->
                    FilterChip(
                        selected = macroMode == id,
                        onClick = { macroMode = id },
                        label = { Text(label) }
                    )
                }
            }

            if (macroMode == "TAP") {
                Text("Koordinat Tap: X=${xPct.toInt()}%, Y=${yPct.toInt()}%", style = MaterialTheme.typography.labelSmall)
                Slider(value = xPct, onValueChange = { xPct = it }, valueRange = 10f..90f)
                Slider(value = yPct, onValueChange = { yPct = it }, valueRange = 10f..90f)
            }

            Text("Selang Masa: ${intervalMs.toInt()} ms", style = MaterialTheme.typography.labelSmall)
            Slider(value = intervalMs, onValueChange = { intervalMs = it }, valueRange = 400f..5000f)

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    if (uiState.externalGameMacroRunning) {
                        onStopExternalMacro()
                    } else {
                        onStartExternalMacro(macroMode, xPct, yPct, intervalMs.toLong())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.externalGameMacroRunning) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("external_game_copilot_button")
            ) {
                Icon(
                    if (uiState.externalGameMacroRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (uiState.externalGameMacroRunning) "Hentikan Co-Pilot Game" else "Mula Co-Pilot Gesture Luar")
            }
        }
    }
}

@Composable
private fun TicTacToeBoard(
    state: TicTacToeState,
    onCellClick: (Int) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (state.winner) {
                    "X" -> "Pemenang: ANDA (X)"
                    "O" -> "Pemenang: NOVACORE AI (O)"
                    "DRAW" -> "KEPUTUSAN: SERI"
                    else -> "Giliran Semasa: ${state.currentTurn}"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            for (r in 0..2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    for (c in 0..2) {
                        val idx = r * 3 + c
                        val mark = state.board[idx]
                        val isWinCell = idx in state.winningLine
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = when {
                                isWinCell -> MaterialTheme.colorScheme.secondaryContainer
                                mark.isNotEmpty() -> MaterialTheme.colorScheme.surfaceContainerHigh
                                else -> MaterialTheme.colorScheme.background
                            },
                            border = BorderStroke(
                                width = if (isWinCell) 2.dp else 1.dp,
                                color = if (isWinCell) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .size(92.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(enabled = mark.isEmpty() && state.winner == null) {
                                    onCellClick(idx)
                                }
                                .testTag("ttt_cell_$idx")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mark,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mark == "X") {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.tertiary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Connect4Board(
    state: Connect4State,
    onColumnClick: (Int) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (state.winner) {
                    1 -> "Pemenang: ANDA (Cyan)"
                    2 -> "Pemenang: NOVACORE AI (Amber)"
                    0 -> "KEPUTUSAN: SERI"
                    else -> "Ketuk mana-mana lajur untuk jatuhkan cip"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            for (r in 0 until state.rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in 0 until state.cols) {
                        val cell = state.board[r * state.cols + c]
                        val fillColor = when (cell) {
                            1 -> MaterialTheme.colorScheme.primary
                            2 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.background
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(fillColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable(enabled = state.winner == null) { onColumnClick(c) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Cyber2048Board(
    state: Cyber2048State,
    onMove: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skor: ${state.score}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (state.gameOver) {
                    Text(
                        text = "TAMAT!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            for (r in 0..3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in 0..3) {
                        val v = state.grid[r * 4 + c]
                        val tileBg = when {
                            v == 0 -> MaterialTheme.colorScheme.background
                            v <= 4 -> MaterialTheme.colorScheme.surfaceContainerHigh
                            v <= 16 -> MaterialTheme.colorScheme.primaryContainer
                            v <= 64 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                        val tileFg = when {
                            v <= 4 -> MaterialTheme.colorScheme.onSurface
                            v <= 16 -> MaterialTheme.colorScheme.onPrimaryContainer
                            v <= 64 -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tileBg)
                        ) {
                            if (v != 0) {
                                Text(
                                    text = v.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = tileFg
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Directional Pad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedButton(onClick = { onMove("UP") }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { onMove("LEFT") }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Move Left")
                    }
                    OutlinedButton(onClick = { onMove("DOWN") }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                    }
                    OutlinedButton(onClick = { onMove("RIGHT") }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Move Right")
                    }
                }
            }
        }
    }
}
