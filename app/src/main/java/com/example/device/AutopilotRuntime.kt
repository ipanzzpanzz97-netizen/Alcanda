package com.example.device

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

data class AutopilotStepLog(
    val step: Int,
    val screenPackage: String,
    val action: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutopilotRuntimeState(
    val isRunning: Boolean = false,
    val isPausedForConfirm: Boolean = false,
    val targetPackage: String = "co.bitx.android.wallet",
    val targetName: String = "Luno",
    val goal: String = "Buka Exchange di Luno, pantau harga pasangan koin murah (APE/XRP), dan siapkan Limit Order ikut analisa veteran.",
    val financialSafeMode: Boolean = true,
    val currentStep: Int = 0,
    val maxSteps: Int = 12,
    val statusHeadline: String = "Sedia untuk autopilot aplikasi luar termasuk Luno Ori.",
    val logs: List<AutopilotStepLog> = emptyList()
)

object AutopilotRuntime {
    private val _state = MutableStateFlow(AutopilotRuntimeState())
    val state: StateFlow<AutopilotRuntimeState> = _state.asStateFlow()

    private val stopRequested = AtomicBoolean(false)

    fun updateConfig(targetPkg: String, targetName: String, goal: String, financialSafeMode: Boolean) {
        _state.update {
            it.copy(
                targetPackage = targetPkg,
                targetName = targetName,
                goal = goal,
                financialSafeMode = financialSafeMode
            )
        }
    }

    fun markStarted(targetPkg: String, targetName: String, goal: String, financialSafeMode: Boolean, maxSteps: Int) {
        stopRequested.set(false)
        _state.value = AutopilotRuntimeState(
            isRunning = true,
            isPausedForConfirm = false,
            targetPackage = targetPkg,
            targetName = targetName,
            goal = goal,
            financialSafeMode = financialSafeMode,
            currentStep = 0,
            maxSteps = maxSteps,
            statusHeadline = "Autopilot bermula untuk $targetName...",
            logs = emptyList()
        )
    }

    fun updateStep(step: Int, headline: String, logEntry: AutopilotStepLog? = null) {
        _state.update { curr ->
            val updatedLogs = if (logEntry != null) (curr.logs + logEntry).takeLast(40) else curr.logs
            curr.copy(
                currentStep = step,
                statusHeadline = headline,
                logs = updatedLogs
            )
        }
    }

    fun markPausedForConfirm(headline: String, logEntry: AutopilotStepLog) {
        _state.update { curr ->
            curr.copy(
                isRunning = false,
                isPausedForConfirm = true,
                statusHeadline = headline,
                logs = (curr.logs + logEntry).takeLast(40)
            )
        }
    }

    fun markStopped(headline: String) {
        stopRequested.set(true)
        _state.update { curr ->
            curr.copy(
                isRunning = false,
                isPausedForConfirm = false,
                statusHeadline = headline
            )
        }
    }

    fun requestStop() {
        stopRequested.set(true)
        _state.update { curr ->
            curr.copy(
                isRunning = false,
                isPausedForConfirm = false,
                statusHeadline = "Autopilot dihentikan oleh pengguna."
            )
        }
    }

    fun isStopRequested(): Boolean = stopRequested.get()
}
