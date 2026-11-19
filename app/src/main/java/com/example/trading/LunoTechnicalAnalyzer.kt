package com.example.trading

import kotlin.math.abs

enum class TrendBias(val labelMs: String) {
    BULLISH_MOMENTUM("Bullish Momentum"),
    OVERSOLD_BOUNCE("Oversold Peluang Pantulan"),
    OVERBOUGHT_CAUTION("Overbought • Elak FOMO"),
    BEARISH_PRESSURE("Tekanan Bearish"),
    SIDEWAYS_RANGE("Mendatar / Konsolidasi")
}

data class MicroCapitalPlan(
    val capitalMyr: Double,
    val riskPercent: Double,
    val maxRiskMyr: Double,
    val suggestedEntryMyr: Double,
    val suggestedStopLossMyr: Double,
    val suggestedTp1Myr: Double,
    val suggestedTp2Myr: Double,
    val estimatedCoinUnits: Double,
    val makerFeeMyr: Double,
    val takerFeeMyr: Double,
    val instantBuyEstCostMyr: Double,
    val riskRewardRatio: Double
)

data class LunoMarketAnalysis(
    val pair: String,
    val currentPrice: Double,
    val change24hPct: Double,
    val high24h: Double,
    val low24h: Double,
    val spreadPct: Double,
    val rsi14: Double,
    val ema9: Double,
    val ema21: Double,
    val ema9Series: List<Double>,
    val ema21Series: List<Double>,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val bidPressurePct: Double,
    val trendBias: TrendBias,
    val microPlan: MicroCapitalPlan
) {
    fun toPromptSummary(): String = buildString {
        appendLine("Pair: $pair")
        appendLine("Last Price: RM ${formatPrice(currentPrice)} | 24h Change: ${"%.2f".format(change24hPct)}%")
        appendLine("24h High: RM ${formatPrice(high24h)} | 24h Low: RM ${formatPrice(low24h)}")
        appendLine("Bid/Ask Spread: ${"%.2f".format(spreadPct)}% | Bid Wall Pressure (Top 20): ${"%.1f".format(bidPressurePct)}%")
        appendLine("RSI(14): ${"%.1f".format(rsi14)} | EMA(9): RM ${formatPrice(ema9)} | EMA(21): RM ${formatPrice(ema21)}")
        appendLine("Support: RM ${formatPrice(supportLevel)} | Resistance: RM ${formatPrice(resistanceLevel)}")
        appendLine("Quant Bias: ${trendBias.labelMs}")
        appendLine("Modal Pengguna: RM ${"%.2f".format(microPlan.capitalMyr)} | Risiko Maks: RM ${"%.2f".format(microPlan.maxRiskMyr)} (${"%.1f".format(microPlan.riskPercent)}%)")
        appendLine("Limit Entry Cadangan: RM ${formatPrice(microPlan.suggestedEntryMyr)} | Stop-Loss: RM ${formatPrice(microPlan.suggestedStopLossMyr)} | TP1: RM ${formatPrice(microPlan.suggestedTp1Myr)} | TP2: RM ${formatPrice(microPlan.suggestedTp2Myr)}")
        appendLine("Est. Fee Exchange Limit (~0.25%): RM ${"%.3f".format(microPlan.makerFeeMyr)} berbanding Instant Buy (~2%): RM ${"%.2f".format(microPlan.instantBuyEstCostMyr)}")
    }

    companion object {
        fun formatPrice(price: Double): String {
            return when {
                price >= 1000.0 -> "%,.2f".format(price)
                price >= 1.0 -> "%.3f".format(price)
                price >= 0.01 -> "%.4f".format(price)
                else -> "%.6f".format(price)
            }
        }
    }
}

object LunoTechnicalAnalyzer {

    fun computeRsi(closes: List<Double>, period: Int = 14): Double {
        if (closes.size <= period) return 50.0
        var gainSum = 0.0
        var lossSum = 0.0
        for (i in 1..period) {
            val diff = closes[i] - closes[i - 1]
            if (diff > 0) gainSum += diff else lossSum += abs(diff)
        }
        var avgGain = gainSum / period
        var avgLoss = lossSum / period

        for (i in (period + 1) until closes.size) {
            val diff = closes[i] - closes[i - 1]
            val gain = if (diff > 0) diff else 0.0
            val loss = if (diff < 0) abs(diff) else 0.0
            avgGain = ((avgGain * (period - 1)) + gain) / period
            avgLoss = ((avgLoss * (period - 1)) + loss) / period
        }

        if (avgGain == 0.0 && avgLoss == 0.0) return 50.0
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return (100.0 - (100.0 / (1.0 + rs))).coerceIn(0.0, 100.0)
    }

    fun computeEmaSeries(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val k = 2.0 / (period + 1.0)
        val out = ArrayList<Double>(values.size)
        var ema = values.first()
        out.add(ema)
        for (i in 1 until values.size) {
            ema = (values[i] * k) + (ema * (1.0 - k))
            out.add(ema)
        }
        return out
    }

    fun buildMicroCapitalPlan(
        currentPrice: Double,
        supportLevel: Double,
        resistanceLevel: Double,
        capitalMyr: Double,
        riskPercent: Double
    ): MicroCapitalPlan {
        val safeCapital = capitalMyr.coerceAtLeast(1.0)
        val safeRiskPct = riskPercent.coerceIn(0.5, 25.0)
        val safePrice = currentPrice.coerceAtLeast(0.000001)

        // Limit order entry slightly near support or current bid to avoid slippage & instant buy fee
        val entry = if (supportLevel > 0.0 && supportLevel < safePrice) {
            val midSupport = (safePrice * 0.7) + (supportLevel * 0.3)
            midSupport.coerceAtMost(safePrice)
        } else {
            safePrice * 0.992
        }

        val rawSl = if (supportLevel > 0.0 && supportLevel < entry * 0.985) {
            supportLevel * 0.985
        } else {
            entry * 0.95
        }
        val stopLoss = rawSl.coerceIn(entry * 0.85, entry * 0.98)

        val riskPerUnit = (entry - stopLoss).coerceAtLeast(entry * 0.02)
        val tp1 = (entry + riskPerUnit * 1.8).coerceAtLeast(
            if (resistanceLevel > entry) minOf(resistanceLevel, entry + riskPerUnit * 2.5) else entry + riskPerUnit * 1.8
        )
        val tp2 = entry + riskPerUnit * 3.0

        val coinUnits = safeCapital / entry
        val maxRiskMyr = safeCapital * (safeRiskPct / 100.0)
        val makerFee = safeCapital * 0.0025 // Luno Exchange maker/limit fee ~0.25%
        val takerFee = safeCapital * 0.005  // Luno Exchange taker fee ~0.50%
        val instantFee = safeCapital * 0.02 // Instant buy/sell spread+fee ~2.0%
        val rr = (tp1 - entry) / riskPerUnit

        return MicroCapitalPlan(
            capitalMyr = safeCapital,
            riskPercent = safeRiskPct,
            maxRiskMyr = maxRiskMyr,
            suggestedEntryMyr = entry,
            suggestedStopLossMyr = stopLoss,
            suggestedTp1Myr = tp1,
            suggestedTp2Myr = tp2,
            estimatedCoinUnits = coinUnits,
            makerFeeMyr = makerFee,
            takerFeeMyr = takerFee,
            instantBuyEstCostMyr = instantFee,
            riskRewardRatio = rr
        )
    }

    fun analyzeMarket(
        ticker: LunoTicker,
        candles: List<LunoCandle>,
        orderBook: LunoOrderBookTop,
        capitalMyr: Double,
        riskPercent: Double = 3.0
    ): LunoMarketAnalysis {
        val closes = if (candles.isNotEmpty()) candles.map { it.close } else listOf(ticker.lastTrade)
        val highs = if (candles.isNotEmpty()) candles.map { it.high } else listOf(ticker.lastTrade)
        val lows = if (candles.isNotEmpty()) candles.map { it.low } else listOf(ticker.lastTrade)

        val rsi = computeRsi(closes, 14)
        val ema9Series = computeEmaSeries(closes, 9)
        val ema21Series = computeEmaSeries(closes, 21)
        val ema9 = ema9Series.lastOrNull() ?: ticker.lastTrade
        val ema21 = ema21Series.lastOrNull() ?: ticker.lastTrade

        val recentSlice = candles.takeLast(24)
        val high24h = recentSlice.maxOfOrNull { it.high } ?: highs.maxOrNull() ?: ticker.lastTrade
        val low24h = recentSlice.minOfOrNull { it.low } ?: lows.minOrNull() ?: ticker.lastTrade
        val first24h = recentSlice.firstOrNull()?.open ?: closes.firstOrNull() ?: ticker.lastTrade
        val changePct = if (first24h > 0.0) ((ticker.lastTrade - first24h) / first24h) * 100.0 else 0.0

        val spreadPct = if (ticker.ask > 0.0 && ticker.ask >= ticker.bid) {
            ((ticker.ask - ticker.bid) / ticker.ask) * 100.0
        } else 0.0

        val support = candles.takeLast(20).minOfOrNull { it.low } ?: (ticker.lastTrade * 0.96)
        val resistance = candles.takeLast(20).maxOfOrNull { it.high } ?: (ticker.lastTrade * 1.04)

        val totalBidsVol = orderBook.bids.sumOf { it.volume * it.price }
        val totalAsksVol = orderBook.asks.sumOf { it.volume * it.price }
        val totalOb = totalBidsVol + totalAsksVol
        val bidPressure = if (totalOb > 0.0) (totalBidsVol / totalOb) * 100.0 else 50.0

        val bias = when {
            rsi <= 33.0 -> TrendBias.OVERSOLD_BOUNCE
            rsi >= 70.0 -> TrendBias.OVERBOUGHT_CAUTION
            ema9 > ema21 * 1.003 && rsi in 45.0..68.0 -> TrendBias.BULLISH_MOMENTUM
            ema9 < ema21 * 0.997 && rsi < 50.0 -> TrendBias.BEARISH_PRESSURE
            else -> TrendBias.SIDEWAYS_RANGE
        }

        val plan = buildMicroCapitalPlan(
            currentPrice = ticker.lastTrade,
            supportLevel = support,
            resistanceLevel = resistance,
            capitalMyr = capitalMyr,
            riskPercent = riskPercent
        )

        return LunoMarketAnalysis(
            pair = ticker.pair,
            currentPrice = ticker.lastTrade,
            change24hPct = changePct,
            high24h = high24h,
            low24h = low24h,
            spreadPct = spreadPct,
            rsi14 = rsi,
            ema9 = ema9,
            ema21 = ema21,
            ema9Series = ema9Series,
            ema21Series = ema21Series,
            supportLevel = support,
            resistanceLevel = resistance,
            bidPressurePct = bidPressure,
            trendBias = bias,
            microPlan = plan
        )
    }
}
