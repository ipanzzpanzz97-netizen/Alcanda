package com.example

import com.example.ai.AutopilotActionType
import com.example.device.AutopilotRuntime
import com.example.device.AutopilotStepLog
import com.example.game.Connect4State
import com.example.game.GameEngine
import com.example.game.TicTacToeState
import com.example.trading.LunoCandle
import com.example.trading.LunoOrderBookEntry
import com.example.trading.LunoOrderBookTop
import com.example.trading.LunoTechnicalAnalyzer
import com.example.trading.LunoTicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun ticTacToeDetectsWinCorrectly() {
        var state = TicTacToeState()
        state = GameEngine.tttApplyMove(state, 0) // X
        state = GameEngine.tttApplyMove(state, 3) // O
        state = GameEngine.tttApplyMove(state, 1) // X
        state = GameEngine.tttApplyMove(state, 4) // O
        state = GameEngine.tttApplyMove(state, 2) // X wins top row
        assertEquals("X", state.winner)
        assertEquals(listOf(0, 1, 2), state.winningLine)
    }

    @Test
    fun connect4DropsChipToBottomRow() {
        val state = GameEngine.c4ApplyMove(Connect4State(), col = 3)
        // Bottom row (row 5), col 3 -> index 5 * 7 + 3 = 38
        assertEquals(1, state.board[38])
        assertEquals(2, state.currentTurn)
    }

    @Test
    fun cyber2048SpawnsTwoTilesAndHasValidMoves() {
        val state = GameEngine.new2048()
        assertEquals(2, state.grid.count { it != 0 })
        assertTrue(GameEngine.valid2048Moves(state).isNotEmpty())
    }

    @Test
    fun lunoRsiAndEmaDetectUptrend() {
        val uptrendCloses = (1..25).map { 0.30 + (it * 0.01) }
        val rsi = LunoTechnicalAnalyzer.computeRsi(uptrendCloses, period = 14)
        val ema9 = LunoTechnicalAnalyzer.computeEmaSeries(uptrendCloses, 9)
        val ema21 = LunoTechnicalAnalyzer.computeEmaSeries(uptrendCloses, 21)

        assertTrue("RSI should be overbought on pure uptrend", rsi > 70.0)
        assertEquals(uptrendCloses.size, ema9.size)
        assertTrue("EMA9 should lead EMA21 in uptrend", ema9.last() > ema21.last())
    }

    @Test
    fun lunoMicroCapitalPlanCalculatesLimitFeesAndRiskReward() {
        val ticker = LunoTicker(
            pair = "APEMYR",
            baseAsset = "APE",
            lastTrade = 0.33,
            bid = 0.329,
            ask = 0.331,
            volume24h = 25000.0,
            status = "ACTIVE",
            timestamp = 1700000000000L
        )
        val candles = (1..20).map { i ->
            LunoCandle(
                timestamp = 1700000000000L + i * 3600_000L,
                open = 0.31 + i * 0.001,
                high = 0.34,
                low = 0.30,
                close = 0.31 + i * 0.001,
                volume = 1200.0
            )
        }
        val ob = LunoOrderBookTop(
            bids = listOf(LunoOrderBookEntry(0.329, 500.0)),
            asks = listOf(LunoOrderBookEntry(0.331, 300.0))
        )
        val analysis = LunoTechnicalAnalyzer.analyzeMarket(
            ticker = ticker,
            candles = candles,
            orderBook = ob,
            capitalMyr = 40.0,
            riskPercent = 3.0
        )

        assertEquals("APEMYR", analysis.pair)
        assertEquals(40.0, analysis.microPlan.capitalMyr, 0.001)
        assertTrue(analysis.microPlan.makerFeeMyr < analysis.microPlan.instantBuyEstCostMyr)
        assertTrue(analysis.microPlan.suggestedStopLossMyr < analysis.microPlan.suggestedEntryMyr)
        assertTrue(analysis.microPlan.suggestedTp1Myr > analysis.microPlan.suggestedEntryMyr)
        assertTrue(analysis.microPlan.riskRewardRatio >= 1.5)
    }

    @Test
    fun autopilotLifecycleTransitionsWorkSafely() {
        assertTrue(AutopilotActionType.entries.contains(AutopilotActionType.PAUSE_FOR_CONFIRM))

        AutopilotRuntime.markStarted(
            targetPkg = "co.bitx.android.wallet",
            targetName = "Luno",
            goal = "Buka Exchange",
            financialSafeMode = true,
            maxSteps = 10
        )
        assertTrue(AutopilotRuntime.state.value.isRunning)
        assertFalse(AutopilotRuntime.isStopRequested())

        AutopilotRuntime.updateStep(
            step = 1,
            headline = "Ketuk Exchange",
            logEntry = AutopilotStepLog(1, "co.bitx.android.wallet", "CLICK_TEXT: Exchange", "Found tab")
        )
        assertEquals(1, AutopilotRuntime.state.value.currentStep)
        assertEquals(1, AutopilotRuntime.state.value.logs.size)

        AutopilotRuntime.markPausedForConfirm(
            headline = "Sahkan belian",
            logEntry = AutopilotStepLog(2, "co.bitx.android.wallet", "PAUSE_FOR_CONFIRM", "Safety pause")
        )
        assertFalse(AutopilotRuntime.state.value.isRunning)
        assertTrue(AutopilotRuntime.state.value.isPausedForConfirm)
        assertEquals(2, AutopilotRuntime.state.value.logs.size)

        AutopilotRuntime.requestStop()
        assertFalse(AutopilotRuntime.state.value.isRunning)
        assertFalse(AutopilotRuntime.state.value.isPausedForConfirm)
        assertTrue(AutopilotRuntime.isStopRequested())
    }

    @Test
    fun lunoFlatMarketRsiReturnsNeutralFifty() {
        val flatCloses = List(20) { 100.0 }
        val rsi = LunoTechnicalAnalyzer.computeRsi(flatCloses, period = 14)
        assertEquals(50.0, rsi, 0.001)
    }

    @Test
    fun freeAiPresetsContainKeylessFallbackAndOnlyOneActiveDefault() {
        val presets = com.example.data.FreeAiPresets.defaultProfiles()
        assertEquals(1, presets.count { it.isActive })
        assertTrue(presets.any { !it.requiresKey && it.name.contains("Pollinations", ignoreCase = true) })
        assertTrue(presets.any { it.name.contains("Groq", ignoreCase = true) })
    }
}
