package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.trading.LunoCandle
import com.example.trading.LunoMarketAnalysis
import com.example.trading.TrendBias
import com.example.viewmodel.LunoUiState

@Composable
fun LunoTradingScreen(
    lunoState: LunoUiState,
    savedKeyId: String,
    savedKeySecret: String,
    onLoadMarket: () -> Unit,
    onSelectPair: (String) -> Unit,
    onUpdateCapitalAndRisk: (String, Double) -> Unit,
    onRunVeteranAi: (String) -> Unit,
    onFetchBalance: (String, String) -> Unit,
    onLaunchLunoApp: () -> Unit,
    onLaunchLunoAutopilot: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cheapOnly by rememberSaveable { mutableStateOf(false) }
    var customQuestion by rememberSaveable { mutableStateOf("") }
    var showWalletPanel by rememberSaveable { mutableStateOf(false) }
    var keyIdInput by rememberSaveable(savedKeyId) { mutableStateOf(savedKeyId) }
    var keySecretInput by rememberSaveable(savedKeySecret) { mutableStateOf(savedKeySecret) }

    LaunchedEffect(Unit) {
        if (lunoState.tickers.isEmpty() && !lunoState.isLoadingMarket) {
            onLoadMarket()
        }
    }

    val displayedTickers = remember(lunoState.tickers, cheapOnly) {
        if (cheapOnly) {
            lunoState.tickers.filter { it.lastTrade <= 20.0 }
        } else {
            lunoState.tickers
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
                        imageVector = Icons.Default.CandlestickChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Luno Veteran Trading Desk", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Data live api.luno.com • Pasangan MYR & Strategi Modal Mikro",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onLaunchLunoApp, modifier = Modifier.testTag("open_luno_app_button")) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Luno", maxLines = 1)
                    }
                    IconButton(
                        onClick = onLoadMarket,
                        enabled = !lunoState.isLoadingMarket,
                        modifier = Modifier.testTag("refresh_luno_button")
                    ) {
                        if (lunoState.isLoadingMarket) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Luno market")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onLaunchLunoAutopilot,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_luno_autopilot_button")
            ) {
                Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Autopilot di Luno Ori (Sediakan Order)", maxLines = 1)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter + MYR Pairs Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = cheapOnly,
                    onClick = { cheapOnly = !cheapOnly },
                    label = { Text("Koin Murah (≤ RM20)") }
                )
                displayedTickers.forEach { ticker ->
                    FilterChip(
                        selected = ticker.pair == lunoState.selectedPair,
                        onClick = { onSelectPair(ticker.pair) },
                        label = {
                            Text("${ticker.baseAsset} RM${LunoMarketAnalysis.formatPrice(ticker.lastTrade)}")
                        },
                        modifier = Modifier.testTag("luno_pair_${ticker.pair}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (lunoState.marketError != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ralat Sambungan Luno: ${lunoState.marketError}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(onClick = onLoadMarket) { Text("Cuba Semula") }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val analysis = lunoState.analysis
                if (analysis != null) {
                    // Price & Chart Card
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
                                            text = "${analysis.pair} • Luno Exchange",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "RM ${LunoMarketAnalysis.formatPrice(analysis.currentPrice)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    val isPositive = analysis.change24hPct >= 0.0
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isPositive) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.errorContainer
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${if (isPositive) "+" else ""}${"%.2f".format(analysis.change24hPct)}% (24j)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LunoCandlestickCanvas(
                                    candles = lunoState.candles,
                                    ema9 = analysis.ema9Series,
                                    ema21 = analysis.ema21Series,
                                    support = analysis.supportLevel,
                                    resistance = analysis.resistanceLevel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Chart Legend
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "EMA9: RM${LunoMarketAnalysis.formatPrice(analysis.ema9)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00E5FF)
                                    )
                                    Text(
                                        text = "EMA21: RM${LunoMarketAnalysis.formatPrice(analysis.ema21)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFB300)
                                    )
                                    Text(
                                        text = "RSI(14): ${"%.1f".format(analysis.rsi14)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            analysis.rsi14 <= 33.0 -> MaterialTheme.colorScheme.secondary
                                            analysis.rsi14 >= 70.0 -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quant Indicators Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    QuantMetricPill(
                                        title = "Spread Bid/Ask",
                                        value = "${"%.2f".format(analysis.spreadPct)}%",
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuantMetricPill(
                                        title = "Tekanan Beli",
                                        value = "${"%.0f".format(analysis.bidPressurePct)}% Bid",
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuantMetricPill(
                                        title = "Bias Kuant",
                                        value = when (analysis.trendBias) {
                                            TrendBias.BULLISH_MOMENTUM -> "Bullish"
                                            TrendBias.OVERSOLD_BOUNCE -> "Oversold"
                                            TrendBias.OVERBOUGHT_CAUTION -> "Overbought"
                                            TrendBias.BEARISH_PRESSURE -> "Bearish"
                                            TrendBias.SIDEWAYS_RANGE -> "Mendatar"
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Support: RM ${LunoMarketAnalysis.formatPrice(analysis.supportLevel)}  •  Resistance: RM ${LunoMarketAnalysis.formatPrice(analysis.resistanceLevel)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Capital & Fee Risk Planner (Modal RM10 - RM50 - RM500)
                    item {
                        val plan = analysis.microPlan
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Kalkulator Modal & Yuran Luno (RM)", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("10", "20", "40", "50", "100", "500").forEach { preset ->
                                        FilterChip(
                                            selected = lunoState.capitalMyrInput == preset,
                                            onClick = { onUpdateCapitalAndRisk(preset, lunoState.riskPercent) },
                                            label = { Text("RM $preset") }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = lunoState.capitalMyrInput,
                                        onValueChange = { onUpdateCapitalAndRisk(it, lunoState.riskPercent) },
                                        label = { Text("Modal (RM)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Anggaran Unit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${"%.6f".format(plan.estimatedCoinUnits)} ${analysis.pair.removeSuffix("MYR")}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Tip Yuran Veteran: Gunakan 'Exchange -> Limit Order'",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "Yuran Exchange Limit (~0.25%): ~RM ${"%.3f".format(plan.makerFeeMyr)} vs Instant Buy (~2%): ~RM ${"%.2f".format(plan.instantBuyEstCostMyr)}.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Limit Entry: RM ${LunoMarketAnalysis.formatPrice(plan.suggestedEntryMyr)}  •  SL: RM ${LunoMarketAnalysis.formatPrice(plan.suggestedStopLossMyr)}  •  TP1: RM ${LunoMarketAnalysis.formatPrice(plan.suggestedTp1Myr)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Veteran AI Setup Strategist
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Penganalisa Veteran Luno AI", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "Bina setup teknikal berasaskan data langsung & kawalan risiko",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customQuestion,
                                    onValueChange = { customQuestion = it },
                                    placeholder = { Text("Soalan khas (cth: Sesuai tak entry RM50 sekarang?)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onRunVeteranAi(customQuestion) },
                                    enabled = !lunoState.aiAnalyzing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("analyze_luno_ai_button")
                                ) {
                                    if (lunoState.aiAnalyzing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(if (lunoState.aiAnalyzing) "Menjana Setup Veteran..." else "Analisa & Cadang Setup Luno AI")
                                }

                                if (lunoState.aiError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = lunoState.aiError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                val report = lunoState.aiReport
                                if (report != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = report.actionSignal,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = report.confidenceLabel,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Entry Limit: ${report.entryPlanMyr}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Stop-Loss: ${report.stopLossPlanMyr}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                            Text("Take-Profit: ${report.takeProfitPlanMyr}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(report.strategySummary, style = MaterialTheme.typography.bodyMedium)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Nasihat Modal: ${report.smallCapitalAdvice}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = report.riskWarning,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Luno Private Balance Check
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
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dompet Luno Saya (Pilihan API Read-Only)", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Disimpan tempatan di peranti ini sahaja",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { showWalletPanel = !showWalletPanel }) {
                                    Icon(
                                        if (showWalletPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Luno API panel"
                                    )
                                }
                            }

                            if (showWalletPanel) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = keyIdInput,
                                    onValueChange = { keyIdInput = it },
                                    label = { Text("Luno API Key ID") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = keySecretInput,
                                    onValueChange = { keySecretInput = it },
                                    label = { Text("Luno API Secret") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onFetchBalance(keyIdInput, keySecretInput) },
                                    enabled = !lunoState.walletLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (lunoState.walletLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Semak Baki Akaun Luno")
                                }
                                if (lunoState.walletError != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(lunoState.walletError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                if (lunoState.walletBalances.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    lunoState.walletBalances.forEach { bal ->
                                        Text(
                                            text = "${bal.asset}: ${"%.6f".format(bal.balance)} (Reserved: ${"%.4f".format(bal.reserved)})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
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
    }
}

@Composable
private fun QuantMetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LunoCandlestickCanvas(
    candles: List<LunoCandle>,
    ema9: List<Double>,
    ema21: List<Double>,
    support: Double,
    resistance: Double,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val bullColor = Color(0xFF00E676)
    val bearColor = Color(0xFFFF5252)
    val ema9Color = Color(0xFF00E5FF)
    val ema21Color = Color(0xFFFFB300)

    if (candles.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Memuatkan carta lilin Luno...", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minLow = minOf(candles.minOf { it.low }, support).let { if (it <= 0.0) 0.0001 else it }
        val maxHigh = maxOf(candles.maxOf { it.high }, resistance)
        val range = (maxHigh - minLow).coerceAtLeast(minLow * 0.005)

        fun yFor(price: Double): Float {
            val norm = ((price - minLow) / range).toFloat().coerceIn(0f, 1f)
            return h - (norm * (h * 0.90f)) - (h * 0.05f)
        }

        // Subtle horizontal grid
        for (step in 0..3) {
            val gy = h * (0.1f + step * 0.26f)
            drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
        }

        // Support & Resistance dashed lines
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        drawLine(
            color = bullColor.copy(alpha = 0.5f),
            start = Offset(0f, yFor(support)),
            end = Offset(w, yFor(support)),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )
        drawLine(
            color = bearColor.copy(alpha = 0.5f),
            start = Offset(0f, yFor(resistance)),
            end = Offset(w, yFor(resistance)),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )

        val count = candles.size.coerceAtLeast(1)
        val stepX = w / count
        val bodyW = (stepX * 0.58f).coerceIn(3f, 18f)

        candles.forEachIndexed { idx, c ->
            val cx = idx * stepX + stepX / 2f
            val color = if (c.close >= c.open) bullColor else bearColor
            drawLine(
                color = color,
                start = Offset(cx, yFor(c.high)),
                end = Offset(cx, yFor(c.low)),
                strokeWidth = 2f
            )
            val topY = yFor(maxOf(c.open, c.close))
            val botY = yFor(minOf(c.open, c.close))
            val rectH = (botY - topY).coerceAtLeast(2.5f)
            drawRect(
                color = color,
                topLeft = Offset(cx - bodyW / 2f, topY),
                size = Size(bodyW, rectH)
            )
        }

        if (ema9.size == candles.size) {
            val p9 = Path()
            ema9.forEachIndexed { idx, v ->
                val cx = idx * stepX + stepX / 2f
                val cy = yFor(v)
                if (idx == 0) p9.moveTo(cx, cy) else p9.lineTo(cx, cy)
            }
            drawPath(p9, color = ema9Color, style = Stroke(width = 3f))
        }

        if (ema21.size == candles.size) {
            val p21 = Path()
            ema21.forEachIndexed { idx, v ->
                val cx = idx * stepX + stepX / 2f
                val cy = yFor(v)
                if (idx == 0) p21.moveTo(cx, cy) else p21.lineTo(cx, cy)
            }
            drawPath(p21, color = ema21Color, style = Stroke(width = 3f))
        }
    }
}
