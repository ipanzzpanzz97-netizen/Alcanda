package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AutopilotActionType
import com.example.ai.DeviceAction
import com.example.ai.LunoVeteranAiReport
import com.example.ai.NovaAiRepository
import com.example.data.AiProviderProfileEntity
import com.example.data.AiProviderType
import com.example.data.ChatMessageEntity
import com.example.data.FreeAiPresets
import com.example.data.GeneratedAppProjectEntity
import com.example.data.NovaDatabase
import com.example.data.PreferencesStore
import com.example.data.SystemErrorEntity
import com.example.data.TermuxScriptEntity
import com.example.data.UserSettings
import com.example.device.AutopilotRuntime
import com.example.device.AutopilotRuntimeState
import com.example.device.AutopilotStepLog
import com.example.device.DeviceTelemetry
import com.example.device.DeviceTelemetryManager
import com.example.device.FileAccessHelper
import com.example.device.InstalledAppInfo
import com.example.device.LiveSensorReading
import com.example.device.NovaAccessibilityService
import com.example.device.SelectedFileInfo
import com.example.device.TermuxBridge
import com.example.device.VoiceController
import com.example.device.VoiceUiState
import com.example.game.ArenaGameType
import com.example.game.Connect4State
import com.example.game.Cyber2048State
import com.example.game.GameEngine
import com.example.game.TicTacToeState
import com.example.trading.LunoCandle
import com.example.trading.LunoMarketAnalysis
import com.example.trading.LunoOrderBookTop
import com.example.trading.LunoTechnicalAnalyzer
import com.example.trading.LunoTicker
import com.example.trading.LunoTradingClient
import com.example.trading.LunoWalletBalance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AiOpState {
    data object Idle : AiOpState
    data object Loading : AiOpState
    data class Streaming(val text: String) : AiOpState
    data class Success(val text: String) : AiOpState
    data class Error(val message: String) : AiOpState
}

sealed interface UiOneShotEvent {
    data class Navigate(val route: String) : UiOneShotEvent
    data object LaunchFilePicker : UiOneShotEvent
    data object LaunchPhotoPicker : UiOneShotEvent
    data object RequestTermuxPermission : UiOneShotEvent
    data class ToastMessage(val message: String) : UiOneShotEvent
}

data class HealthCheckStatus(
    val internetOk: Boolean = false,
    val aiEngineOk: Boolean? = null,
    val aiEngineNote: String = "Belum diuji",
    val accessibilityOk: Boolean = false,
    val writeSettingsOk: Boolean = false,
    val termuxOk: Boolean = false,
    val termuxPermissionOk: Boolean = false,
    val voiceOk: Boolean = false,
    val roomDbOk: Boolean = true,
    val lunoApiOk: Boolean? = null,
    val lunoApiNote: String = "Belum diuji",
    val isRunning: Boolean = false,
    val isAutoHealing: Boolean = false,
    val lastHealReport: String? = null
)

data class LunoUiState(
    val isLoadingMarket: Boolean = false,
    val marketError: String? = null,
    val tickers: List<LunoTicker> = emptyList(),
    val selectedPair: String = "XBTMYR",
    val candles: List<LunoCandle> = emptyList(),
    val orderBook: LunoOrderBookTop? = null,
    val analysis: LunoMarketAnalysis? = null,
    val capitalMyrInput: String = "50",
    val riskPercent: Double = 3.0,
    val aiAnalyzing: Boolean = false,
    val aiReport: LunoVeteranAiReport? = null,
    val aiError: String? = null,
    val walletBalances: List<LunoWalletBalance> = emptyList(),
    val walletLoading: Boolean = false,
    val walletError: String? = null
)

data class NovaMainUiState(
    val commandOpState: AiOpState = AiOpState.Idle,
    val visionOpState: AiOpState = AiOpState.Idle,
    val fileOpState: AiOpState = AiOpState.Idle,
    val termuxOpState: AiOpState = AiOpState.Idle,
    val connectionTestState: AiOpState = AiOpState.Idle,
    val telemetry: DeviceTelemetry? = null,
    val torchOn: Boolean = false,
    val termuxInstalled: Boolean = false,
    val termuxPermissionGranted: Boolean = false,
    val accessibilityReady: Boolean = false,
    val writeSettingsReady: Boolean = false,
    val termuxScriptName: String = "system_check.sh",
    val termuxCode: String = "#!/data/data/com.termux/files/usr/bin/bash\necho '=== NovaCore Termux Ready ==='\nuname -a\ndf -h /data\n",
    val termuxStatusBanner: String? = null,
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val selectedFile: SelectedFileInfo? = null,
    val capturedBitmap: Bitmap? = null,
    val activeGame: ArenaGameType = ArenaGameType.TICTACTOE,
    val tttState: TicTacToeState = TicTacToeState(),
    val c4State: Connect4State = Connect4State(),
    val state2048: Cyber2048State = GameEngine.new2048(),
    val aiGameThinking: Boolean = false,
    val aiAutoPlayRunning: Boolean = false,
    val aiGameComment: String = "Pilih permainan atau suruh saya main dengan anda!",
    val externalGameMacroRunning: Boolean = false,
    val externalGameStatus: String = "Sedia untuk lancarkan game online/offline dan kawal secara automatik.",
    val diagnosingErrorId: Long? = null,
    val healthCheck: HealthCheckStatus = HealthCheckStatus(),
    val luno: LunoUiState = LunoUiState(),
    val builderOpState: AiOpState = AiOpState.Idle,
    val selectedProjectId: Long? = null
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = NovaAiRepository()
    private val lunoClient = LunoTradingClient()
    private val db = NovaDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val scriptDao = db.termuxScriptDao()
    private val aiProfileDao = db.aiProfileDao()
    private val systemErrorDao = db.systemErrorDao()
    private val generatedAppDao = db.generatedAppDao()
    private val prefsStore = PreferencesStore(application)
    private val telemetryManager = DeviceTelemetryManager(application)
    private val termuxBridge = TermuxBridge(application)
    private val fileHelper = FileAccessHelper(application)
    private val voiceController = VoiceController(application)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savedScripts: StateFlow<List<TermuxScriptEntity>> = scriptDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val generatedProjects: StateFlow<List<GeneratedAppProjectEntity>> = generatedAppDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val aiProfiles: StateFlow<List<AiProviderProfileEntity>> = aiProfileDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeAiProfile: StateFlow<AiProviderProfileEntity?> = aiProfiles
        .map { list -> list.firstOrNull { it.isActive } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val systemErrors: StateFlow<List<SystemErrorEntity>> = systemErrorDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unresolvedErrorCount: StateFlow<Int> = systemErrors
        .map { list -> list.count { !it.isResolved } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val settings: StateFlow<UserSettings> = prefsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    val voiceState: StateFlow<VoiceUiState> = voiceController.state
    val autopilotState: StateFlow<AutopilotRuntimeState> = AutopilotRuntime.state

    val liveSensors: StateFlow<LiveSensorReading> = telemetryManager.observeLiveSensors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveSensorReading())

    private val _uiState = MutableStateFlow(NovaMainUiState())
    val uiState: StateFlow<NovaMainUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UiOneShotEvent>()
    val events: SharedFlow<UiOneShotEvent> = _events.asSharedFlow()

    private val autopilotScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var autoPlayJob: Job? = null
    private var externalMacroJob: Job? = null
    private var autopilotJob: Job? = null

    init {
        refreshDeviceSnapshot()
        ensureFreeAiPresetsSeeded()
        viewModelScope.launch {
            NovaAccessibilityService.isConnected.collect { connected ->
                _uiState.update {
                    it.copy(
                        accessibilityReady = connected ||
                            NovaAccessibilityService.isEnabledInSettings(getApplication())
                    )
                }
            }
        }
    }

    private fun ensureFreeAiPresetsSeeded() {
        viewModelScope.launch {
            if (aiProfileDao.count() == 0) {
                aiProfileDao.insertAll(FreeAiPresets.defaultProfiles())
            }
        }
    }

    fun restoreFreeAiPresets() {
        viewModelScope.launch {
            val existingNames = aiProfiles.value.map { it.name }.toSet()
            val missing = FreeAiPresets.defaultProfiles().filter { it.name !in existingNames }
                .map { it.copy(isActive = false) }
            if (missing.isNotEmpty()) {
                aiProfileDao.insertAll(missing)
                _events.emit(UiOneShotEvent.ToastMessage("${missing.size} profil Free AI dipulihkan."))
            } else {
                _events.emit(UiOneShotEvent.ToastMessage("Semua senarai Free AI sudah lengkap."))
            }
        }
    }

    fun refreshDeviceSnapshot() {
        val snap = telemetryManager.collectSnapshot()
        val apps = telemetryManager.getInstalledLauncherApps()
        val accConnected = NovaAccessibilityService.instance != null ||
            NovaAccessibilityService.isEnabledInSettings(getApplication())
        _uiState.update {
            it.copy(
                telemetry = snap,
                torchOn = telemetryManager.isTorchEnabled(),
                termuxInstalled = termuxBridge.isTermuxInstalled(),
                termuxPermissionGranted = termuxBridge.hasRunCommandPermission(),
                accessibilityReady = accConnected,
                writeSettingsReady = telemetryManager.canWriteSystemSettings(),
                installedApps = apps
            )
        }
    }

    fun requestTermuxPermission() {
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.RequestTermuxPermission)
        }
    }

    // --- AI Provider Profiles & Mutual-Exclusion Switch ---

    fun toggleAiProfileSwitch(profile: AiProviderProfileEntity, turnOn: Boolean) {
        viewModelScope.launch {
            if (turnOn) {
                aiProfileDao.deactivateAll()
                aiProfileDao.activateById(profile.id)
                _events.emit(
                    UiOneShotEvent.ToastMessage("${profile.name} ON • AI lain automatik OFF.")
                )
            } else {
                // Turning off active profile reverts to default built-in keyless Gemini so AI stays available
                val builtIn = aiProfiles.value.firstOrNull {
                    it.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC && it.id != profile.id
                }
                if (builtIn != null) {
                    aiProfileDao.deactivateAll()
                    aiProfileDao.activateById(builtIn.id)
                    _events.emit(
                        UiOneShotEvent.ToastMessage("${profile.name} OFF • Kembali ke ${builtIn.name}.")
                    )
                }
            }
        }
    }

    fun saveAndActivateAiProfile(
        existingId: Long?,
        name: String,
        providerType: AiProviderType,
        apiKey: String,
        baseUrl: String,
        modelName: String,
        activateNow: Boolean = true,
        requiresKey: Boolean = providerType.requiresKey,
        signupUrl: String = ""
    ) {
        viewModelScope.launch {
            val cleanName = name.trim().ifBlank { providerType.displayLabel }
            val cleanModel = modelName.trim().ifBlank { providerType.defaultModel }
            val cleanUrl = baseUrl.trim().ifBlank { providerType.defaultBaseUrl }
            val badge = if (requiresKey) "FREE / CUSTOM KEY" else "FREE • TANPA KEY"
            if (activateNow) {
                aiProfileDao.deactivateAll()
            }
            aiProfileDao.insert(
                AiProviderProfileEntity(
                    id = existingId ?: 0L,
                    name = cleanName,
                    providerType = providerType.name,
                    apiKey = apiKey.trim(),
                    baseUrl = cleanUrl,
                    modelName = cleanModel,
                    isActive = activateNow,
                    requiresKey = requiresKey,
                    isFreeTier = true,
                    signupUrl = signupUrl,
                    badgeText = badge,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(connectionTestState = AiOpState.Idle) }
            _events.emit(
                UiOneShotEvent.ToastMessage(
                    "'$cleanName' disimpan${if (activateNow) " & ON (AI lain automatik OFF)" else ""}."
                )
            )
        }
    }

    fun deleteAiProfile(profile: AiProviderProfileEntity) {
        if (profile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC &&
            profile.name.contains("Gemini 3.8", ignoreCase = true)
        ) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Profil lalai utama tidak boleh dipadam."))
            }
            return
        }
        viewModelScope.launch {
            val wasActive = profile.isActive
            aiProfileDao.delete(profile)
            if (wasActive) {
                val fallback = aiProfiles.value.firstOrNull { it.id != profile.id }
                if (fallback != null) {
                    aiProfileDao.activateById(fallback.id)
                } else {
                    ensureFreeAiPresetsSeeded()
                }
            }
        }
    }

    fun testAiProfileConnection(
        providerType: AiProviderType,
        apiKey: String,
        baseUrl: String,
        modelName: String,
        requiresKey: Boolean = providerType.requiresKey
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionTestState = AiOpState.Loading) }
            val tempProfile = AiProviderProfileEntity(
                name = providerType.displayLabel,
                providerType = providerType.name,
                apiKey = apiKey.trim(),
                baseUrl = baseUrl.trim().ifBlank { providerType.defaultBaseUrl },
                modelName = modelName.trim().ifBlank { providerType.defaultModel },
                isActive = false,
                requiresKey = requiresKey
            )
            try {
                val res = aiRepository.testProviderConnection(tempProfile)
                _uiState.update { it.copy(connectionTestState = AiOpState.Success("Sambungan berjaya: $res")) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        connectionTestState = AiOpState.Error(
                            e.localizedMessage ?: "Sambungan gagal. Semak API Key, Base URL, dan nama model."
                        )
                    )
                }
            }
        }
    }

    fun clearConnectionTest() {
        _uiState.update { it.copy(connectionTestState = AiOpState.Idle) }
    }

    fun openUrlExternal(url: String) {
        if (!telemetryManager.openUrl(url)) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Tidak dapat membuka pautan pelayar."))
            }
        }
    }

    // --- Out-of-App System Controls (Poco X7 Pro / HyperOS) ---

    fun openAccessibilitySettings() = telemetryManager.openAccessibilitySettings()
    fun openWriteSettingsScreen() = telemetryManager.openWriteSettingsScreen()
    fun openAppInfoSettings() = telemetryManager.openAppInfoSettings()

    fun triggerLockScreen() {
        val service = NovaAccessibilityService.instance
        if (service == null || !service.lockPhoneScreen()) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Aktifkan 'NovaCore System Control' dalam Accessibility Settings dahulu."))
            }
            telemetryManager.openAccessibilitySettings()
        }
    }

    fun triggerPowerDialog() {
        val service = NovaAccessibilityService.instance
        if (service == null || !service.openPowerMenu()) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Aktifkan 'NovaCore System Control' dalam Accessibility Settings untuk kawal menu kuasa."))
            }
            telemetryManager.openAccessibilitySettings()
        }
    }

    fun triggerScreenshot() {
        val service = NovaAccessibilityService.instance
        if (service == null || !service.takeSystemScreenshot()) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Aktifkan Accessibility Service untuk tangkap skrin automatik."))
            }
        }
    }

    fun triggerGlobalHome() {
        NovaAccessibilityService.instance?.goHome() ?: openAccessibilitySettings()
    }

    fun triggerVolumeAdjust(direction: Int) {
        telemetryManager.adjustMediaVolume(direction)
    }

    fun triggerMediaKey(keyCode: Int) {
        telemetryManager.dispatchMediaKey(keyCode)
    }

    fun triggerBrightnessAdjust(delta: Int) {
        val res = telemetryManager.adjustBrightness(delta)
        if (res.isFailure) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage(res.exceptionOrNull()?.message ?: "Perlu kebenaran Write Settings."))
            }
            telemetryManager.openWriteSettingsScreen()
        }
    }

    // --- Voice & Command Center ---

    fun startVoiceListen() {
        val locale = settings.value.speechLocale
        voiceController.startListening(locale) { transcript ->
            sendCommand(transcript)
        }
    }

    fun stopVoiceListen() {
        voiceController.stopListening()
    }

    fun clearVoiceError() {
        voiceController.clearError()
    }

    fun toggleAutoSpeak(enabled: Boolean) {
        viewModelScope.launch {
            prefsStore.setAutoSpeak(enabled)
            if (!enabled) voiceController.stopSpeaking()
        }
    }

    fun setSpeechLocale(locale: String) {
        viewModelScope.launch {
            prefsStore.setSpeechLocale(locale)
            voiceController.setTtsLocale(locale)
        }
    }

    private fun logSystemError(module: String, message: String, suggestedFixKey: String = "") {
        viewModelScope.launch {
            systemErrorDao.insert(
                SystemErrorEntity(
                    module = module,
                    message = message.take(400),
                    suggestedFixKey = suggestedFixKey
                )
            )
        }
    }

    fun sendCommand(prompt: String) {
        val clean = prompt.trim()
        if (clean.isEmpty()) return

        viewModelScope.launch {
            chatDao.insert(ChatMessageEntity(role = "user", text = clean))
            _uiState.update { it.copy(commandOpState = AiOpState.Loading) }

            val snap = telemetryManager.collectSnapshot()
            _uiState.update { it.copy(telemetry = snap) }

            val allApps = _uiState.value.installedApps
            val appsSummary = buildString {
                append("Total APKs: ${allApps.size}. ")
                append(allApps.take(80).joinToString(", ") {
                    if (it.isGame) "${it.appName}[GAME]" else it.appName
                })
            }
            val history = chatMessages.value.map { it.role to it.text }
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            val accService = NovaAccessibilityService.instance
            val screenText = accService?.readVisibleScreenSummary().orEmpty()

            try {
                val result = aiRepository.processCommand(
                    activeProfile = profile,
                    userPrompt = clean,
                    telemetrySummary = snap.toPromptSummary(),
                    installedAppsSample = appsSummary,
                    visibleScreenSummary = screenText,
                    accessibilityReady = accService != null,
                    recentHistory = history
                )
                val badge = executeDeviceAction(result)
                chatDao.insert(
                    ChatMessageEntity(
                        role = "model",
                        text = result.reply,
                        actionBadge = badge
                    )
                )
                _uiState.update { it.copy(commandOpState = AiOpState.Success(result.reply)) }
                if (settings.value.autoSpeak) {
                    voiceController.speak(result.reply, settings.value.speechLocale)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal memproses arahan AI."
                val fixKey = if (profile == null || profile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                    "FIX_AUTO_FAILOVER_FREE_AI"
                } else {
                    "FIX_SWITCH_GEMINI"
                }
                logSystemError("AI_API", msg, fixKey)
                _uiState.update { it.copy(commandOpState = AiOpState.Error(msg)) }
                chatDao.insert(
                    ChatMessageEntity(
                        role = "model",
                        text = "Ralat AI: $msg (Rekod disimpan di Pusat Baiki • ketuk Baiki untuk Auto-Failover AI)",
                        actionBadge = "ERROR"
                    )
                )
            }
        }
    }

    private suspend fun executeDeviceAction(result: com.example.ai.AiCommandResult): String {
        val acc = NovaAccessibilityService.instance
        return when (result.action) {
            DeviceAction.OPEN_CAMERA -> {
                _events.emit(UiOneShotEvent.Navigate("camera"))
                "CAMERA STUDIO"
            }
            DeviceAction.OPEN_FILE_PICKER -> {
                _events.emit(UiOneShotEvent.Navigate("device"))
                _events.emit(UiOneShotEvent.LaunchFilePicker)
                "FILE ACCESS"
            }
            DeviceAction.OPEN_PHOTO_PICKER -> {
                _events.emit(UiOneShotEvent.LaunchPhotoPicker)
                "PHOTO PICKER"
            }
            DeviceAction.TOGGLE_FLASHLIGHT_ON -> {
                val res = telemetryManager.setTorch(true)
                _uiState.update { it.copy(torchOn = res.getOrDefault(it.torchOn)) }
                if (res.isSuccess) "FLASHLIGHT ON" else "FLASHLIGHT N/A"
            }
            DeviceAction.TOGGLE_FLASHLIGHT_OFF -> {
                val res = telemetryManager.setTorch(false)
                _uiState.update { it.copy(torchOn = false) }
                if (res.isSuccess) "FLASHLIGHT OFF" else "FLASHLIGHT N/A"
            }
            DeviceAction.SHOW_TELEMETRY -> {
                refreshDeviceSnapshot()
                _events.emit(UiOneShotEvent.Navigate("device"))
                "DEVICE TELEMETRY"
            }
            DeviceAction.OPEN_TERMUX_STUDIO, DeviceAction.RUN_TERMUX_SCRIPT -> {
                val code = result.termuxCode.ifBlank { _uiState.value.termuxCode }
                val name = result.termuxScriptName.ifBlank { "ai_task.sh" }
                _uiState.update {
                    it.copy(
                        termuxScriptName = name,
                        termuxCode = code
                    )
                }
                if (result.action == DeviceAction.RUN_TERMUX_SCRIPT && result.termuxCode.isNotBlank()) {
                    val dispatch = termuxBridge.runInTermux(code, settings.value.termuxWorkdir)
                    _uiState.update {
                        it.copy(
                            termuxInstalled = dispatch.termuxInstalled,
                            termuxPermissionGranted = termuxBridge.hasRunCommandPermission(),
                            termuxStatusBanner = dispatch.message
                        )
                    }
                    if (!dispatch.dispatchedViaService) {
                        val fixKey = if (dispatch.needsPermission) "FIX_TERMUX_PERMISSION" else "FIX_TERMUX_SETUP"
                        logSystemError("TERMUX", dispatch.message, fixKey)
                    }
                    scriptDao.insert(
                        TermuxScriptEntity(
                            name = name,
                            language = if (code.contains("python")) "Python" else "Bash",
                            code = code,
                            description = "Generated via Voice/Chat Command"
                        )
                    )
                    "TERMUX EXEC"
                } else {
                    _events.emit(UiOneShotEvent.Navigate("termux"))
                    "TERMUX STUDIO"
                }
            }
            DeviceAction.PLAY_GAME_TICTACTOE -> {
                selectGame(ArenaGameType.TICTACTOE)
                _events.emit(UiOneShotEvent.Navigate("arena"))
                "GAME: TIC-TAC-TOE"
            }
            DeviceAction.PLAY_GAME_CONNECT4 -> {
                selectGame(ArenaGameType.CONNECT4)
                _events.emit(UiOneShotEvent.Navigate("arena"))
                "GAME: CONNECT-4"
            }
            DeviceAction.PLAY_GAME_2048 -> {
                selectGame(ArenaGameType.CYBER_2048)
                _events.emit(UiOneShotEvent.Navigate("arena"))
                "GAME: 2048"
            }
            DeviceAction.LAUNCH_APP -> {
                val query = result.actionParam
                if (query.isNotBlank()) {
                    val launched = telemetryManager.launchAppByQuery(query)
                    if (launched.isSuccess) {
                        "LAUNCH: ${launched.getOrNull()}"
                    } else {
                        val errMsg = launched.exceptionOrNull()?.message ?: "App not found"
                        logSystemError("APK_LAUNCH", errMsg, "")
                        _events.emit(UiOneShotEvent.ToastMessage(errMsg))
                        "APP NOT FOUND"
                    }
                } else ""
            }
            DeviceAction.OPEN_LUNO_TRADING -> {
                _events.emit(UiOneShotEvent.Navigate("luno"))
                loadLunoMarket()
                "LUNO VETERAN DESK"
            }
            DeviceAction.OPEN_REPAIR_CENTER -> {
                _events.emit(UiOneShotEvent.Navigate("repair"))
                runFullSystemDiagnostics()
                "REPAIR CENTER"
            }
            DeviceAction.OPEN_APK_BUILDER -> {
                _events.emit(UiOneShotEvent.Navigate("builder"))
                if (result.actionParam.isNotBlank()) {
                    generateApkProjectFromIdea(result.actionParam)
                }
                "APK BUILDER AI"
            }
            DeviceAction.OPEN_AUTOPILOT -> {
                _events.emit(UiOneShotEvent.Navigate("autopilot"))
                "AUTOPILOT HUB"
            }
            DeviceAction.START_LUNO_AUTOPILOT -> {
                val goal = result.actionParam.ifBlank { AutopilotRuntime.state.value.goal }
                startCrossAppAutopilot(
                    targetPkg = "co.bitx.android.wallet",
                    targetName = "Luno",
                    goal = goal,
                    financialSafeMode = AutopilotRuntime.state.value.financialSafeMode
                )
                "LUNO AUTOPILOT"
            }
            DeviceAction.STOP_AUTOPILOT -> {
                stopCrossAppAutopilot()
                "AUTOPILOT STOPPED"
            }
            DeviceAction.OPEN_WIFI_SETTINGS -> {
                telemetryManager.openSystemSettings("WIFI")
                "WIFI SETTINGS"
            }
            DeviceAction.OPEN_BLUETOOTH_SETTINGS -> {
                telemetryManager.openSystemSettings("BLUETOOTH")
                "BLUETOOTH SETTINGS"
            }
            DeviceAction.OPEN_BATTERY_SETTINGS -> {
                telemetryManager.openSystemSettings("BATTERY")
                "BATTERY SETTINGS"
            }
            DeviceAction.LOCK_SCREEN -> {
                if (acc != null && acc.lockPhoneScreen()) {
                    "LOCK SCREEN"
                } else {
                    logSystemError("ACCESSIBILITY", "Accessibility Service belum aktif untuk kunci skrin.", "FIX_ACCESSIBILITY")
                    _events.emit(UiOneShotEvent.ToastMessage("Aktifkan Accessibility 'NovaCore System Control' dahulu."))
                    telemetryManager.openAccessibilitySettings()
                    "NEED ACCESSIBILITY"
                }
            }
            DeviceAction.POWER_OFF_DEVICE -> {
                if (acc != null && acc.openPowerMenu()) {
                    delay(550L)
                    acc.clickNodeByText("Power off") || acc.clickNodeByText("Matikan")
                    "POWER MENU"
                } else {
                    logSystemError("ACCESSIBILITY", "Accessibility Service belum aktif untuk menu Power Off.", "FIX_ACCESSIBILITY")
                    _events.emit(UiOneShotEvent.ToastMessage("Aktifkan Accessibility 'NovaCore System Control' untuk buka menu Power Off."))
                    telemetryManager.openAccessibilitySettings()
                    "NEED ACCESSIBILITY"
                }
            }
            DeviceAction.SYSTEM_HOME -> {
                if (acc != null && acc.goHome()) "HOME" else {
                    telemetryManager.openAccessibilitySettings()
                    "NEED ACCESSIBILITY"
                }
            }
            DeviceAction.SYSTEM_BACK -> {
                if (acc != null && acc.goBack()) "BACK" else "NEED ACCESSIBILITY"
            }
            DeviceAction.SYSTEM_RECENTS -> {
                if (acc != null && acc.openRecents()) "RECENTS" else "NEED ACCESSIBILITY"
            }
            DeviceAction.SYSTEM_NOTIFICATIONS -> {
                if (acc != null && acc.openNotifications()) "NOTIFICATIONS" else "NEED ACCESSIBILITY"
            }
            DeviceAction.SYSTEM_QUICK_SETTINGS -> {
                if (acc != null && acc.openQuickSettings()) "QUICK SETTINGS" else "NEED ACCESSIBILITY"
            }
            DeviceAction.TAKE_SCREENSHOT -> {
                if (acc != null && acc.takeSystemScreenshot()) "SCREENSHOT" else "NEED ACCESSIBILITY"
            }
            DeviceAction.READ_OUTSIDE_SCREEN -> {
                if (acc != null) {
                    val nodes = acc.readDetailedScreenNodes()
                    chatDao.insert(ChatMessageEntity(role = "model", text = "Elemen skrin luar:\n$nodes", actionBadge = "SCREEN MAP"))
                    "READ SCREEN"
                } else "NEED ACCESSIBILITY"
            }
            DeviceAction.CLICK_SCREEN_TEXT -> {
                val target = result.actionParam
                if (acc != null && target.isNotBlank() && acc.clickNodeByText(target)) {
                    "TAP: $target"
                } else {
                    "TAP FAILED"
                }
            }
            DeviceAction.TAP_SCREEN_COORDS -> {
                val parts = result.actionParam.split(",")
                val x = (parts.getOrNull(0)?.trim()?.toFloatOrNull() ?: 50f) / 100f
                val y = (parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 50f) / 100f
                if (acc != null && acc.dispatchTapAtPercent(x, y)) {
                    "TAP ${result.actionParam}%"
                } else "NEED ACCESSIBILITY"
            }
            DeviceAction.SWIPE_DIRECTION -> {
                val dir = result.actionParam.ifBlank { "UP" }
                if (acc != null && acc.swipeDirection(dir)) "SWIPE $dir" else "NEED ACCESSIBILITY"
            }
            DeviceAction.TYPE_SCREEN_TEXT -> {
                val txt = result.actionParam
                if (acc != null && txt.isNotBlank() && acc.typeTextIntoFocusedOrFirstInput(txt)) {
                    "TYPE TEXT"
                } else "TYPE FAILED"
            }
            DeviceAction.SCROLL_DOWN -> {
                if (acc != null && acc.scrollActiveWindow(true)) "SCROLL DOWN" else "NEED ACCESSIBILITY"
            }
            DeviceAction.SCROLL_UP -> {
                if (acc != null && acc.scrollActiveWindow(false)) "SCROLL UP" else "NEED ACCESSIBILITY"
            }
            DeviceAction.VOLUME_UP -> {
                telemetryManager.adjustMediaVolume(AudioManager.ADJUST_RAISE)
                "VOLUME UP"
            }
            DeviceAction.VOLUME_DOWN -> {
                telemetryManager.adjustMediaVolume(AudioManager.ADJUST_LOWER)
                "VOLUME DOWN"
            }
            DeviceAction.VOLUME_MUTE -> {
                telemetryManager.adjustMediaVolume(AudioManager.ADJUST_MUTE)
                "VOLUME MUTE"
            }
            DeviceAction.MEDIA_PLAY_PAUSE -> {
                telemetryManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                "MEDIA PLAY/PAUSE"
            }
            DeviceAction.MEDIA_NEXT -> {
                telemetryManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                "MEDIA NEXT"
            }
            DeviceAction.MEDIA_PREVIOUS -> {
                telemetryManager.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                "MEDIA PREV"
            }
            DeviceAction.BRIGHTNESS_UP -> {
                if (telemetryManager.adjustBrightness(45).isSuccess) "BRIGHTNESS +" else {
                    telemetryManager.openWriteSettingsScreen()
                    "NEED WRITE SETTINGS"
                }
            }
            DeviceAction.BRIGHTNESS_DOWN -> {
                if (telemetryManager.adjustBrightness(-45).isSuccess) "BRIGHTNESS -" else {
                    telemetryManager.openWriteSettingsScreen()
                    "NEED WRITE SETTINGS"
                }
            }
            DeviceAction.SET_TIMER -> {
                val secs = result.actionParam.toIntOrNull() ?: 60
                telemetryManager.setTimer(secs)
                "TIMER ${secs}s"
            }
            DeviceAction.DIAL_NUMBER -> {
                if (telemetryManager.dialPhoneNumber(result.actionParam)) "DIAL ${result.actionParam}" else "DIAL FAILED"
            }
            DeviceAction.WEB_SEARCH -> {
                if (telemetryManager.webSearch(result.actionParam)) "SEARCH: ${result.actionParam}" else "SEARCH FAILED"
            }
            DeviceAction.NONE -> ""
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearAll()
        }
    }

    // --- Device, Torch & Apps ---

    fun toggleTorch() {
        val desired = !_uiState.value.torchOn
        val res = telemetryManager.setTorch(desired)
        if (res.isSuccess) {
            _uiState.update { it.copy(torchOn = desired) }
        } else {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage(res.exceptionOrNull()?.message ?: "Flashlight error"))
            }
        }
    }

    fun launchInstalledApp(packageName: String) {
        val res = telemetryManager.launchPackage(packageName)
        if (res.isFailure) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage(res.exceptionOrNull()?.message ?: "Gagal membuka aplikasi"))
            }
        }
    }

    fun openSystemSettings(panel: String) {
        telemetryManager.openSystemSettings(panel)
    }

    // --- Files & Vision ---

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            fileHelper.inspectUri(uri).onSuccess { info ->
                _uiState.update { it.copy(selectedFile = info, fileOpState = AiOpState.Idle) }
                if (info.isImage) {
                    fileHelper.loadScaledBitmap(uri).onSuccess { bmp ->
                        _uiState.update { it.copy(capturedBitmap = bmp) }
                    }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(fileOpState = AiOpState.Error(err.localizedMessage ?: "Gagal membaca fail.")) }
            }
        }
    }

    fun analyzeSelectedFile(prompt: String) {
        val file = _uiState.value.selectedFile ?: return
        viewModelScope.launch {
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            if (file.isImage) {
                val bmp = _uiState.value.capturedBitmap
                    ?: fileHelper.loadScaledBitmap(file.uri).getOrNull()
                if (bmp == null) {
                    _uiState.update { it.copy(fileOpState = AiOpState.Error("Gagal memuatkan imej.")) }
                    return@launch
                }
                _uiState.update { it.copy(fileOpState = AiOpState.Loading) }
                try {
                    aiRepository.analyzeImageStream(profile, bmp, prompt).collect { partial ->
                        _uiState.update { it.copy(fileOpState = AiOpState.Streaming(partial)) }
                    }
                    val final = (_uiState.value.fileOpState as? AiOpState.Streaming)?.text.orEmpty()
                    _uiState.update { it.copy(fileOpState = AiOpState.Success(final)) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(fileOpState = AiOpState.Error(e.localizedMessage ?: "Ralat analisis imej.")) }
                }
            } else {
                val text = file.textPreview
                if (text == null) {
                    _uiState.update {
                        it.copy(fileOpState = AiOpState.Error("Fail binari (${file.mimeType}) tidak boleh dibaca sebagai teks. Cuba pilih teks, kod, log, JSON atau imej."))
                    }
                    return@launch
                }
                _uiState.update { it.copy(fileOpState = AiOpState.Loading) }
                try {
                    aiRepository.analyzeFileStream(profile, file.displayName, file.mimeType, text, prompt)
                        .collect { partial ->
                            _uiState.update { it.copy(fileOpState = AiOpState.Streaming(partial)) }
                        }
                    val final = (_uiState.value.fileOpState as? AiOpState.Streaming)?.text.orEmpty()
                    _uiState.update { it.copy(fileOpState = AiOpState.Success(final)) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(fileOpState = AiOpState.Error(e.localizedMessage ?: "Ralat analisis fail.")) }
                }
            }
        }
    }

    fun analyzeCameraBitmap(bitmap: Bitmap, prompt: String) {
        viewModelScope.launch {
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            _uiState.update { it.copy(capturedBitmap = bitmap, visionOpState = AiOpState.Loading) }
            try {
                aiRepository.analyzeImageStream(profile, bitmap, prompt).collect { partial ->
                    _uiState.update { it.copy(visionOpState = AiOpState.Streaming(partial)) }
                }
                val final = (_uiState.value.visionOpState as? AiOpState.Streaming)?.text.orEmpty()
                _uiState.update { it.copy(visionOpState = AiOpState.Success(final)) }
                if (settings.value.autoSpeak && final.isNotBlank()) {
                    voiceController.speak(final, settings.value.speechLocale)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(visionOpState = AiOpState.Error(e.localizedMessage ?: "Gagal menganalisis imej kamera."))
                }
            }
        }
    }

    fun clearVisionResult() {
        _uiState.update { it.copy(visionOpState = AiOpState.Idle, capturedBitmap = null) }
    }

    // --- Termux Studio ---

    fun updateTermuxEditor(name: String, code: String) {
        _uiState.update { it.copy(termuxScriptName = name, termuxCode = code) }
    }

    fun generateTermuxScript(goal: String) {
        if (goal.isBlank()) return
        viewModelScope.launch {
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            _uiState.update { it.copy(termuxOpState = AiOpState.Loading, termuxStatusBanner = null) }
            try {
                aiRepository.generateTermuxCodeStream(profile, goal, _uiState.value.termuxCode).collect { partial ->
                    _uiState.update {
                        it.copy(
                            termuxCode = partial,
                            termuxOpState = AiOpState.Streaming(partial)
                        )
                    }
                }
                val finalCode = _uiState.value.termuxCode
                _uiState.update { it.copy(termuxOpState = AiOpState.Success(finalCode)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(termuxOpState = AiOpState.Error(e.localizedMessage ?: "Gagal menjana kod Termux."))
                }
            }
        }
    }

    fun runCurrentTermuxScript() {
        val code = _uiState.value.termuxCode
        if (code.isBlank()) return
        val dispatch = termuxBridge.runInTermux(code, settings.value.termuxWorkdir)
        _uiState.update {
            it.copy(
                termuxInstalled = dispatch.termuxInstalled,
                termuxPermissionGranted = termuxBridge.hasRunCommandPermission(),
                termuxStatusBanner = dispatch.message
            )
        }
        if (!dispatch.dispatchedViaService) {
            val fixKey = if (dispatch.needsPermission) "FIX_TERMUX_PERMISSION" else "FIX_TERMUX_SETUP"
            logSystemError("TERMUX", dispatch.message, fixKey)
        }
    }

    fun saveCurrentTermuxScript() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.termuxCode.isBlank()) return@launch
            val lang = if (state.termuxCode.contains("python")) "Python" else "Bash"
            scriptDao.insert(
                TermuxScriptEntity(
                    name = state.termuxScriptName.ifBlank { "script.sh" },
                    language = lang,
                    code = state.termuxCode,
                    description = "Saved in Termux Studio"
                )
            )
            _events.emit(UiOneShotEvent.ToastMessage("Skrip disimpan ke pustaka Termux."))
        }
    }

    fun loadSavedScript(script: TermuxScriptEntity) {
        _uiState.update {
            it.copy(termuxScriptName = script.name, termuxCode = script.code)
        }
    }

    fun deleteSavedScript(script: TermuxScriptEntity) {
        viewModelScope.launch { scriptDao.delete(script) }
    }

    fun exportTermuxScriptToUri(uri: Uri) {
        viewModelScope.launch {
            val res = fileHelper.writeTextToUri(uri, _uiState.value.termuxCode)
            val msg = if (res.isSuccess) "Fail skrip berjaya dieksport!" else "Gagal mengeksport skrip."
            _events.emit(UiOneShotEvent.ToastMessage(msg))
        }
    }

    fun copyTermuxSetupCommand() {
        termuxBridge.copyToClipboard("Termux Setup", TermuxBridge.TERMUX_ALLOW_EXTERNAL_CMD)
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.ToastMessage("Arahan tetapan Termux disalin ke papan klip!"))
        }
    }

    fun copyTermuxCode() {
        termuxBridge.copyToClipboard(_uiState.value.termuxScriptName, _uiState.value.termuxCode)
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.ToastMessage("Kod Termux disalin!"))
        }
    }

    fun openTermuxApp() {
        if (!termuxBridge.openTermuxApp()) {
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Termux tidak ditemui pada peranti ini."))
            }
        }
    }

    // --- AI Game Arena ---

    fun selectGame(type: ArenaGameType) {
        stopAiAutoPlay()
        _uiState.update { it.copy(activeGame = type) }
    }

    fun resetActiveGame() {
        stopAiAutoPlay()
        _uiState.update { state ->
            when (state.activeGame) {
                ArenaGameType.TICTACTOE -> state.copy(
                    tttState = TicTacToeState(),
                    aiGameComment = "Papan Tic-Tac-Toe baharu! Giliran anda (X)."
                )
                ArenaGameType.CONNECT4 -> state.copy(
                    c4State = Connect4State(),
                    aiGameComment = "Connect-4 dimulakan! Pilih lajur untuk jatuhkan cip anda."
                )
                ArenaGameType.CYBER_2048 -> state.copy(
                    state2048 = GameEngine.new2048(),
                    aiGameComment = "Cyber 2048 sedia! Anda boleh leret atau biar saya main."
                )
            }
        }
    }

    fun playUserTicTacToe(index: Int) {
        val current = _uiState.value.tttState
        if (current.winner != null || current.currentTurn != "X" || _uiState.value.aiGameThinking) return
        val next = GameEngine.tttApplyMove(current, index)
        if (next == current) return
        _uiState.update { it.copy(tttState = next) }
        if (next.winner == null) {
            triggerAiTurnForCurrentGame()
        } else {
            updateEndGameComment(next.winner)
        }
    }

    fun playUserConnect4(col: Int) {
        val current = _uiState.value.c4State
        if (current.winner != null || current.currentTurn != 1 || _uiState.value.aiGameThinking) return
        val next = GameEngine.c4ApplyMove(current, col)
        if (next == current) return
        _uiState.update { it.copy(c4State = next) }
        if (next.winner == null) {
            triggerAiTurnForCurrentGame()
        } else {
            val w = when (next.winner) {
                1 -> "Anda menang Connect-4! Tahniah!"
                2 -> "NovaCore AI menang Connect-4!"
                else -> "Seri!"
            }
            _uiState.update { it.copy(aiGameComment = w) }
        }
    }

    fun playUser2048(direction: String) {
        val current = _uiState.value.state2048
        val next = GameEngine.move2048(current, direction)
        _uiState.update { it.copy(state2048 = next) }
    }

    fun triggerAiTurnForCurrentGame() {
        if (_uiState.value.aiGameThinking) return
        viewModelScope.launch {
            executeSingleAiGameStep()
        }
    }

    fun toggleAiAutoPlay() {
        if (_uiState.value.aiAutoPlayRunning) {
            stopAiAutoPlay()
            return
        }
        _uiState.update { it.copy(aiAutoPlayRunning = true) }
        autoPlayJob = viewModelScope.launch {
            while (_uiState.value.aiAutoPlayRunning) {
                val keepGoing = executeSingleAiGameStep()
                if (!keepGoing) {
                    stopAiAutoPlay()
                    break
                }
                delay(650L)
            }
        }
    }

    fun stopAiAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
        _uiState.update { it.copy(aiAutoPlayRunning = false) }
    }

    private suspend fun executeSingleAiGameStep(): Boolean {
        val state = _uiState.value
        val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
        when (state.activeGame) {
            ArenaGameType.TICTACTOE -> {
                val ttt = state.tttState
                if (ttt.winner != null) return false
                val valid = GameEngine.tttValidMoves(ttt)
                if (valid.isEmpty()) return false
                _uiState.update { it.copy(aiGameThinking = true) }
                try {
                    val res = aiRepository.pickGameMove(
                        activeProfile = profile,
                        gameTitle = "Tic-Tac-Toe",
                        boardDescription = GameEngine.tttDescribe(ttt),
                        validMoves = valid
                    )
                    val moveIdx = res.move.toIntOrNull() ?: valid.first().toInt()
                    val next = GameEngine.tttApplyMove(ttt, moveIdx)
                    _uiState.update {
                        it.copy(
                            tttState = next,
                            aiGameThinking = false,
                            aiGameComment = res.comment
                        )
                    }
                    if (next.winner != null) {
                        updateEndGameComment(next.winner)
                        return false
                    }
                    return true
                } catch (e: CancellationException) {
                    _uiState.update { it.copy(aiGameThinking = false) }
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            aiGameThinking = false,
                            aiGameComment = "Ralat AI: ${e.localizedMessage ?: "Cuba lagi"}"
                        )
                    }
                    return false
                }
            }
            ArenaGameType.CONNECT4 -> {
                val c4 = state.c4State
                if (c4.winner != null) return false
                val valid = GameEngine.c4ValidMoves(c4)
                if (valid.isEmpty()) return false
                _uiState.update { it.copy(aiGameThinking = true) }
                try {
                    val res = aiRepository.pickGameMove(
                        activeProfile = profile,
                        gameTitle = "Connect-4",
                        boardDescription = GameEngine.c4Describe(c4),
                        validMoves = valid
                    )
                    val col = res.move.toIntOrNull() ?: valid.first().toInt()
                    val next = GameEngine.c4ApplyMove(c4, col)
                    _uiState.update {
                        it.copy(
                            c4State = next,
                            aiGameThinking = false,
                            aiGameComment = res.comment
                        )
                    }
                    if (next.winner != null) {
                        val msg = when (next.winner) {
                            1 -> "Pemain 1 (X) menang Connect-4!"
                            2 -> "NovaCore AI (O) menang Connect-4!"
                            else -> "Permainan seri!"
                        }
                        _uiState.update { it.copy(aiGameComment = msg) }
                        return false
                    }
                    return true
                } catch (e: CancellationException) {
                    _uiState.update { it.copy(aiGameThinking = false) }
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            aiGameThinking = false,
                            aiGameComment = "Ralat AI: ${e.localizedMessage ?: "Cuba lagi"}"
                        )
                    }
                    return false
                }
            }
            ArenaGameType.CYBER_2048 -> {
                val g = state.state2048
                if (g.gameOver) return false
                val valid = GameEngine.valid2048Moves(g)
                if (valid.isEmpty()) return false
                _uiState.update { it.copy(aiGameThinking = true) }
                try {
                    val res = aiRepository.pickGameMove(
                        activeProfile = profile,
                        gameTitle = "2048 Puzzle",
                        boardDescription = GameEngine.describe2048(g),
                        validMoves = valid
                    )
                    val next = GameEngine.move2048(g, res.move)
                    _uiState.update {
                        it.copy(
                            state2048 = next,
                            aiGameThinking = false,
                            aiGameComment = "${res.move}: ${res.comment}"
                        )
                    }
                    return !next.gameOver
                } catch (e: CancellationException) {
                    _uiState.update { it.copy(aiGameThinking = false) }
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            aiGameThinking = false,
                            aiGameComment = "Ralat AI: ${e.localizedMessage ?: "Cuba lagi"}"
                        )
                    }
                    return false
                }
            }
        }
    }

    private fun updateEndGameComment(winner: String) {
        val msg = when (winner) {
            "X" -> "Tahniah! Anda (X) menang pusingan ini!"
            "O" -> "NovaCore AI (O) menang! Nak cuba lagi?"
            else -> "Seri! Pertarungan yang sengit."
        }
        _uiState.update { it.copy(aiGameComment = msg) }
    }

    // --- External Game Co-Pilot (Online / Offline Games on Device) ---

    fun launchExternalGameAndPrepare(packageName: String, appName: String) {
        val res = telemetryManager.launchPackage(packageName)
        if (res.isSuccess) {
            _uiState.update {
                it.copy(
                    externalGameStatus = "Melancarkan $appName. Aktifkan Accessibility untuk Co-Pilot tap/swipe luar skrin."
                )
            }
        } else {
            val msg = res.exceptionOrNull()?.message ?: "Gagal membuka $appName"
            logSystemError("GAME", msg, "")
            viewModelScope.launch { _events.emit(UiOneShotEvent.ToastMessage(msg)) }
        }
    }

    fun startExternalGameMacro(
        mode: String, // "TAP", "SWIPE_UP", "SWIPE_DOWN", "SWIPE_LEFT", "SWIPE_RIGHT"
        xPercent: Float,
        yPercent: Float,
        intervalMs: Long
    ) {
        val acc = NovaAccessibilityService.instance
        if (acc == null) {
            logSystemError("ACCESSIBILITY", "Accessibility Service diperlukan untuk Co-Pilot Game Luar.", "FIX_ACCESSIBILITY")
            telemetryManager.openAccessibilitySettings()
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Aktifkan 'NovaCore System Control' dalam Accessibility Settings dahulu."))
            }
            return
        }
        externalMacroJob?.cancel()
        val safeDelay = intervalMs.coerceIn(350L, 10_000L)
        _uiState.update {
            it.copy(
                externalGameMacroRunning = true,
                externalGameStatus = "Co-Pilot AKTIF ($mode setiap ${safeDelay}ms). Tekan Henti untuk tamatkan."
            )
        }
        externalMacroJob = viewModelScope.launch {
            while (_uiState.value.externalGameMacroRunning) {
                val service = NovaAccessibilityService.instance
                if (service == null) {
                    stopExternalGameMacro("Accessibility terputus.")
                    break
                }
                when (mode) {
                    "TAP" -> service.dispatchTapAtPercent(xPercent / 100f, yPercent / 100f)
                    "SWIPE_UP" -> service.swipeDirection("UP")
                    "SWIPE_DOWN" -> service.swipeDirection("DOWN")
                    "SWIPE_LEFT" -> service.swipeDirection("LEFT")
                    "SWIPE_RIGHT" -> service.swipeDirection("RIGHT")
                }
                delay(safeDelay)
            }
        }
    }

    fun stopExternalGameMacro(reason: String = "Co-Pilot dihentikan.") {
        externalMacroJob?.cancel()
        externalMacroJob = null
        _uiState.update {
            it.copy(
                externalGameMacroRunning = false,
                externalGameStatus = reason
            )
        }
    }

    // --- Pusat Diagnostik & AI Self-Healing ---

    fun runFullSystemDiagnostics() {
        viewModelScope.launch {
            refreshDeviceSnapshot()
            val snap = telemetryManager.collectSnapshot()
            val accOk = NovaAccessibilityService.instance != null ||
                NovaAccessibilityService.isEnabledInSettings(getApplication())
            val writeOk = telemetryManager.canWriteSystemSettings()
            val termuxOk = termuxBridge.isTermuxInstalled()
            val termuxPermOk = termuxBridge.hasRunCommandPermission()
            val voiceOk = voiceController.isSpeechAvailable()
            val roomDbOk = runCatching { aiProfileDao.count() >= 0 }.getOrDefault(false)

            _uiState.update {
                it.copy(
                    healthCheck = it.healthCheck.copy(
                        internetOk = snap.hasInternet,
                        accessibilityOk = accOk,
                        writeSettingsOk = writeOk,
                        termuxOk = termuxOk,
                        termuxPermissionOk = termuxPermOk,
                        voiceOk = voiceOk,
                        roomDbOk = roomDbOk,
                        isRunning = true,
                        aiEngineNote = "Menguji AI Engine...",
                        lunoApiNote = "Menguji api.luno.com..."
                    )
                )
            }

            // Test Active AI Engine
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            val aiResult = runCatching {
                if (profile != null) aiRepository.testProviderConnection(profile) else "No active profile"
            }
            val aiOk = aiResult.isSuccess
            val aiNote = aiResult.fold(
                onSuccess = { "OK (${profile?.name ?: "Gemini"})" },
                onFailure = { e ->
                    val err = e.localizedMessage ?: "Gagal sambung AI"
                    val fixKey = if (profile == null || profile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                        "FIX_AUTO_FAILOVER_FREE_AI"
                    } else {
                        "FIX_SWITCH_GEMINI"
                    }
                    logSystemError("AI_API", err, fixKey)
                    err
                }
            )

            // Test Luno Public API
            val lunoRes = runCatching { lunoClient.fetchMyrTickers() }
            val lunoOk = lunoRes.isSuccess
            val lunoNote = lunoRes.fold(
                onSuccess = { list -> "OK (${list.size} pasangan MYR aktif)" },
                onFailure = { e ->
                    val err = e.localizedMessage ?: "Gagal capai api.luno.com"
                    logSystemError("LUNO", err, "FIX_RETRY_LUNO")
                    err
                }
            )

            _uiState.update {
                it.copy(
                    healthCheck = it.healthCheck.copy(
                        internetOk = snap.hasInternet,
                        aiEngineOk = aiOk,
                        aiEngineNote = aiNote,
                        accessibilityOk = accOk,
                        writeSettingsOk = writeOk,
                        termuxOk = termuxOk,
                        termuxPermissionOk = termuxPermOk,
                        voiceOk = voiceOk,
                        roomDbOk = roomDbOk,
                        lunoApiOk = lunoOk,
                        lunoApiNote = lunoNote,
                        isRunning = false
                    )
                )
            }
        }
    }

    private suspend fun executeAutoFixKeyInternal(fixKey: String): String {
        return when (fixKey) {
            "FIX_AUTO_FAILOVER_FREE_AI", "FIX_SWITCH_GEMINI" -> {
                val all = aiProfileDao.getAllOnce()
                val active = all.firstOrNull { it.isActive }
                // Try working keyless candidates first (skipping currently failing one)
                val candidates = all.filter {
                    it.id != active?.id && (!it.requiresKey || it.apiKey.isNotBlank())
                }.sortedByDescending {
                    it.typeEnum() == AiProviderType.CUSTOM_OPENAI || it.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC
                }

                for (candidate in candidates) {
                    val testOk = runCatching { aiRepository.testProviderConnection(candidate) }.isSuccess
                    if (testOk) {
                        aiProfileDao.deactivateAll()
                        aiProfileDao.activateById(candidate.id)
                        return "Ditukar automatik ke ${candidate.name} (Sambungan AI disahkan OK!)."
                    }
                }
                // Fallback even if offline test couldn't reach network: switch away from failing provider to Pollinations or Gemini
                val fallback = candidates.firstOrNull() ?: all.firstOrNull()
                if (fallback != null) {
                    aiProfileDao.deactivateAll()
                    aiProfileDao.activateById(fallback.id)
                    "Ditukar ke profil sandaran '${fallback.name}'. Semak internet jika masih ralat."
                } else {
                    ensureFreeAiPresetsSeeded()
                    "Profil Free AI lalai dipulihkan."
                }
            }
            "FIX_ACCESSIBILITY" -> {
                telemetryManager.openAccessibilitySettings()
                "Tetapan Accessibility dibuka — sila aktifkan 'NovaCore System Control'."
            }
            "FIX_WRITE_SETTINGS" -> {
                telemetryManager.openWriteSettingsScreen()
                "Tetapan 'Ubah Tetapan Sistem' dibuka untuk kawalan kecerahan."
            }
            "FIX_TERMUX_PERMISSION" -> {
                _events.emit(UiOneShotEvent.RequestTermuxPermission)
                copyTermuxSetupCommand()
                "Dialog izin RUN_COMMAND Termux dibuka & skrip setup disalin."
            }
            "FIX_TERMUX_SETUP" -> {
                copyTermuxSetupCommand()
                termuxBridge.openTermuxApp()
                "Arahan 'allow-external-apps=true' disalin & Termux dibuka."
            }
            "FIX_RETRY_LUNO" -> {
                loadLunoMarket()
                "Muat semula data pasaran Luno MYR dijalankan."
            }
            "FIX_RESET_VOICE" -> {
                voiceController.resetVoiceEngine(settings.value.speechLocale)
                "Enjin pengecaman suara & TTS berjaya direset."
            }
            "FIX_STOP_AUTOPILOT" -> {
                stopCrossAppAutopilot()
                "Sesi Autopilot & makro luaran telah direset."
            }
            else -> "Tiada tindakan khas diperlukan."
        }
    }

    fun applyAutoFix(error: SystemErrorEntity) {
        viewModelScope.launch {
            val key = error.suggestedFixKey.ifBlank { "NONE" }
            val resultMsg = if (key != "NONE") {
                executeAutoFixKeyInternal(key)
            } else {
                runFullSystemDiagnostics()
                "Diagnostik penuh dijalankan semula."
            }
            if (key in setOf("FIX_AUTO_FAILOVER_FREE_AI", "FIX_SWITCH_GEMINI", "FIX_RETRY_LUNO", "FIX_RESET_VOICE", "FIX_STOP_AUTOPILOT")) {
                systemErrorDao.markResolved(error.id)
            }
            _uiState.update { it.copy(healthCheck = it.healthCheck.copy(lastHealReport = resultMsg)) }
            _events.emit(UiOneShotEvent.ToastMessage(resultMsg))
            if (key == "FIX_AUTO_FAILOVER_FREE_AI" || key == "FIX_SWITCH_GEMINI") {
                runFullSystemDiagnostics()
            }
        }
    }

    fun applyQuickFixKey(fixKey: String) {
        viewModelScope.launch {
            val resultMsg = executeAutoFixKeyInternal(fixKey)
            _uiState.update { it.copy(healthCheck = it.healthCheck.copy(lastHealReport = resultMsg)) }
            _events.emit(UiOneShotEvent.ToastMessage(resultMsg))
            runFullSystemDiagnostics()
        }
    }

    fun runAiSelfRepairForIssue(issueText: String) {
        val clean = issueText.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(healthCheck = it.healthCheck.copy(isAutoHealing = true, lastHealReport = "AI sedang menganalisa & membaiki: \"$clean\"..."))
            }
            try {
                refreshDeviceSnapshot()
                val hc = _uiState.value.healthCheck
                val snap = telemetryManager.collectSnapshot()
                val healthSummary = buildString {
                    appendLine(snap.toPromptSummary())
                    appendLine("Internet OK: ${hc.internetOk}")
                    appendLine("AI Engine OK: ${hc.aiEngineOk} (${hc.aiEngineNote})")
                    appendLine("Luno API OK: ${hc.lunoApiOk} (${hc.lunoApiNote})")
                    appendLine("Termux Installed: ${hc.termuxOk}, RUN_COMMAND Perm: ${hc.termuxPermissionOk}")
                    appendLine("Voice Recognition Ready: ${hc.voiceOk}")
                    appendLine("Accessibility Active: ${hc.accessibilityOk}")
                    appendLine("Write Settings: ${hc.writeSettingsOk}")
                }

                val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
                val plan = runCatching {
                    aiRepository.generateSelfRepairPlan(profile, clean, healthSummary)
                }.getOrElse {
                    // Deterministic Self-Healing fallback when active AI itself is down
                    inferOfflineSelfRepairPlan(clean)
                }

                val fixOutcome = if (plan.fixKey != "NONE") {
                    executeAutoFixKeyInternal(plan.fixKey)
                } else {
                    "Tiada perubahan paksa diperlukan."
                }

                val report = buildString {
                    appendLine("Diagnosa AI: ${plan.diagnosis}")
                    appendLine("Tindakan Auto-Baiki [${plan.fixKey}]: $fixOutcome")
                    if (plan.repairSteps.isNotEmpty()) {
                        appendLine("Langkah Disahkan:")
                        plan.repairSteps.forEachIndexed { idx, step ->
                            appendLine("${idx + 1}. $step")
                        }
                    }
                }.trim()

                _uiState.update {
                    it.copy(healthCheck = it.healthCheck.copy(isAutoHealing = false, lastHealReport = report))
                }
                runFullSystemDiagnostics()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        healthCheck = it.healthCheck.copy(
                            isAutoHealing = false,
                            lastHealReport = "Pembaikan automatik gagal: ${e.localizedMessage}"
                        )
                    )
                }
            }
        }
    }

    fun runAutoHealAll() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(healthCheck = it.healthCheck.copy(isAutoHealing = true, lastHealReport = "Menjalankan AI Auto-Heal menyeluruh..."))
            }
            val logs = mutableListOf<String>()
            refreshDeviceSnapshot()

            // 1. Check & heal AI Engine if failing
            val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
            val aiAlive = profile != null && runCatching { aiRepository.testProviderConnection(profile) }.isSuccess
            if (!aiAlive) {
                val msg = executeAutoFixKeyInternal("FIX_AUTO_FAILOVER_FREE_AI")
                logs.add("• AI Engine: $msg")
            } else {
                logs.add("• AI Engine (${profile?.name}): Sihat & responsif.")
            }

            // 2. Reset Voice recognizer & TTS engine to clear MIUI/HyperOS stuck locks
            voiceController.resetVoiceEngine(settings.value.speechLocale)
            logs.add("• Suara & Mikrofon: Direset segar & sedia.")

            // 3. Ensure Luno MYR market connection is healthy
            val lunoOk = runCatching { lunoClient.fetchMyrTickers() }.isSuccess
            if (!lunoOk) {
                loadLunoMarket()
                logs.add("• Luno API: Muat semula pasaran dijalankan.")
            } else {
                logs.add("• Luno API: Sambungan MYR langsung OK.")
            }

            // 4. Termux permission guidance check
            if (termuxBridge.isTermuxInstalled() && !termuxBridge.hasRunCommandPermission()) {
                _events.emit(UiOneShotEvent.RequestTermuxPermission)
                logs.add("• Termux: Meminta izin RUN_COMMAND daripada sistem.")
            }

            // 5. Mark auto-healed transient AI/Luno errors resolved
            val unresolved = systemErrors.value.filter { !it.isResolved }
            unresolved.filter { it.module in setOf("AI_API", "LUNO", "LUNO_AI") }.forEach {
                systemErrorDao.markResolved(it.id)
            }

            val finalReport = "Laporan AI Auto-Heal:\n" + logs.joinToString("\n")
            _uiState.update {
                it.copy(healthCheck = it.healthCheck.copy(isAutoHealing = false, lastHealReport = finalReport))
            }
            _events.emit(UiOneShotEvent.ToastMessage("AI Auto-Heal selesai!"))
            runFullSystemDiagnostics()
        }
    }

    private fun inferOfflineSelfRepairPlan(issueText: String): com.example.ai.AiSelfRepairPlan {
        val lower = issueText.lowercase()
        val key = when {
            lower.contains("ai") || lower.contains("api") || lower.contains("gemini") || lower.contains("jawab") ->
                "FIX_AUTO_FAILOVER_FREE_AI"
            lower.contains("termux") ->
                if (termuxBridge.isTermuxInstalled() && !termuxBridge.hasRunCommandPermission()) {
                    "FIX_TERMUX_PERMISSION"
                } else {
                    "FIX_TERMUX_SETUP"
                }
            lower.contains("suara") || lower.contains("mic") || lower.contains("voice") || lower.contains("dengar") ->
                "FIX_RESET_VOICE"
            lower.contains("luno") || lower.contains("trading") || lower.contains("graf") || lower.contains("chart") ->
                "FIX_RETRY_LUNO"
            lower.contains("autopilot") || lower.contains("sangkut") || lower.contains("berhenti") ->
                "FIX_STOP_AUTOPILOT"
            lower.contains("luar") || lower.contains("power") || lower.contains("kunci") || lower.contains("accessibility") ->
                "FIX_ACCESSIBILITY"
            else -> "FIX_AUTO_FAILOVER_FREE_AI"
        }
        return com.example.ai.AiSelfRepairPlan(
            diagnosis = "Analisa tempatan mengesan isu berkaitan modul '$key'. Menjalankan pembaikan automatik sekarang.",
            fixKey = key,
            repairSteps = listOf("Menjalankan tindakan pembaikan '$key' dan mengimbas semula modul APK.")
        )
    }

    fun askAiToDiagnoseError(error: SystemErrorEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(diagnosingErrorId = error.id) }
            try {
                val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
                val snap = telemetryManager.collectSnapshot()
                val diagnosis = aiRepository.diagnoseSystemError(
                    activeProfile = profile,
                    module = error.module,
                    errorMessage = error.message,
                    deviceSummary = snap.toPromptSummary()
                )
                systemErrorDao.updateDiagnosis(error.id, diagnosis)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(UiOneShotEvent.ToastMessage(e.localizedMessage ?: "Gagal diagnosa AI. Gunakan AI Auto-Heal untuk tukar enjin percuma."))
            } finally {
                _uiState.update { it.copy(diagnosingErrorId = null) }
            }
        }
    }

    fun resolveSystemError(id: Long) {
        viewModelScope.launch { systemErrorDao.markResolved(id) }
    }

    fun clearResolvedSystemErrors() {
        viewModelScope.launch { systemErrorDao.clearResolved() }
    }

    fun clearAllSystemErrors() {
        viewModelScope.launch { systemErrorDao.clearAll() }
    }

    // --- Veteran Luno Malaysia Trading Desk ---

    fun loadLunoMarket(pairOverride: String? = null) {
        viewModelScope.launch {
            val currentCapital = _uiState.value.luno.capitalMyrInput.ifBlank { settings.value.lunoCapitalMyr }
            _uiState.update {
                it.copy(
                    luno = it.luno.copy(
                        isLoadingMarket = true,
                        marketError = null,
                        capitalMyrInput = currentCapital
                    )
                )
            }
            try {
                val tickers = lunoClient.fetchMyrTickers()
                val targetPair = pairOverride
                    ?: _uiState.value.luno.selectedPair.takeIf { sel -> tickers.any { it.pair == sel } }
                    ?: tickers.firstOrNull()?.pair
                    ?: "XBTMYR"

                val selectedTicker = tickers.firstOrNull { it.pair == targetPair }
                val candles = runCatching { lunoClient.fetchCandles(targetPair, durationSec = 3600, limit = 60) }
                    .getOrDefault(emptyList())
                val orderBook = runCatching { lunoClient.fetchOrderBookTop(targetPair) }.getOrNull()

                val capDouble = currentCapital.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 50.0
                val analysis = if (selectedTicker != null) {
                    LunoTechnicalAnalyzer.analyzeMarket(
                        ticker = selectedTicker,
                        candles = candles,
                        orderBook = orderBook ?: LunoOrderBookTop(emptyList(), emptyList()),
                        capitalMyr = capDouble,
                        riskPercent = _uiState.value.luno.riskPercent
                    )
                } else null

                _uiState.update {
                    it.copy(
                        luno = it.luno.copy(
                            isLoadingMarket = false,
                            tickers = tickers,
                            selectedPair = targetPair,
                            candles = candles,
                            orderBook = orderBook,
                            analysis = analysis,
                            marketError = null
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Gagal memuat turun data pasaran Luno."
                logSystemError("LUNO", err, "FIX_RETRY_LUNO")
                _uiState.update {
                    it.copy(
                        luno = it.luno.copy(
                            isLoadingMarket = false,
                            marketError = err
                        )
                    )
                }
            }
        }
    }

    fun selectLunoPair(pair: String) {
        _uiState.update {
            it.copy(
                luno = it.luno.copy(
                    selectedPair = pair,
                    aiReport = null,
                    aiError = null
                )
            )
        }
        loadLunoMarket(pairOverride = pair)
    }

    fun updateLunoCapitalAndRisk(capitalInput: String, riskPct: Double) {
        val safeRisk = riskPct.coerceIn(0.5, 25.0)
        val capVal = capitalInput.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 50.0
        viewModelScope.launch {
            prefsStore.setLunoCapitalMyr(capitalInput)
        }
        _uiState.update { state ->
            val currentTicker = state.luno.tickers.firstOrNull { it.pair == state.luno.selectedPair }
            val updatedAnalysis = if (currentTicker != null) {
                LunoTechnicalAnalyzer.analyzeMarket(
                    ticker = currentTicker,
                    candles = state.luno.candles,
                    orderBook = state.luno.orderBook ?: LunoOrderBookTop(emptyList(), emptyList()),
                    capitalMyr = capVal,
                    riskPercent = safeRisk
                )
            } else state.luno.analysis

            state.copy(
                luno = state.luno.copy(
                    capitalMyrInput = capitalInput,
                    riskPercent = safeRisk,
                    analysis = updatedAnalysis
                )
            )
        }
    }

    fun runVeteranLunoAiAnalysis(customQuestion: String = "") {
        val analysis = _uiState.value.luno.analysis ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(luno = it.luno.copy(aiAnalyzing = true, aiError = null))
            }
            try {
                val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
                val recentCandlesText = _uiState.value.luno.candles.takeLast(8).joinToString("\n") { c ->
                    "O:${LunoMarketAnalysis.formatPrice(c.open)} H:${LunoMarketAnalysis.formatPrice(c.high)} L:${LunoMarketAnalysis.formatPrice(c.low)} C:${LunoMarketAnalysis.formatPrice(c.close)}"
                }
                val report = aiRepository.analyzeLunoMarketSetup(
                    activeProfile = profile,
                    analysis = analysis,
                    recentCandlesSummary = recentCandlesText,
                    userQuestion = customQuestion
                )
                _uiState.update {
                    it.copy(luno = it.luno.copy(aiAnalyzing = false, aiReport = report))
                }
                if (settings.value.autoSpeak) {
                    voiceController.speak(
                        "Analisa ${analysis.pair}: ${report.actionSignal}. ${report.strategySummary}",
                        settings.value.speechLocale
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Analisa AI Luno gagal."
                logSystemError("LUNO_AI", msg, "FIX_SWITCH_GEMINI")
                _uiState.update {
                    it.copy(luno = it.luno.copy(aiAnalyzing = false, aiError = msg))
                }
            }
        }
    }

    fun saveAndFetchLunoBalance(keyId: String, keySecret: String) {
        viewModelScope.launch {
            prefsStore.setLunoCredentials(keyId, keySecret)
            _uiState.update {
                it.copy(luno = it.luno.copy(walletLoading = true, walletError = null))
            }
            try {
                val balances = lunoClient.fetchPrivateBalances(keyId, keySecret)
                _uiState.update {
                    it.copy(luno = it.luno.copy(walletLoading = false, walletBalances = balances))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menyemak baki Luno."
                logSystemError("LUNO_AUTH", msg, "")
                _uiState.update {
                    it.copy(luno = it.luno.copy(walletLoading = false, walletError = msg))
                }
            }
        }
    }

    fun launchLunoApp() {
        val direct = telemetryManager.launchPackage("co.bitx.android.wallet")
        if (direct.isFailure) {
            val fallback = telemetryManager.launchAppByQuery("Luno")
            if (fallback.isFailure) {
                viewModelScope.launch {
                    _events.emit(UiOneShotEvent.ToastMessage("Aplikasi Luno tidak ditemui pada peranti ini."))
                }
            }
        }
    }

    // --- AI APK Builder Studio ---

    fun generateApkProjectFromIdea(idea: String) {
        val clean = idea.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(builderOpState = AiOpState.Loading) }
            try {
                val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
                val blueprint = aiRepository.generateAndroidApkProject(profile, clean)
                val entity = GeneratedAppProjectEntity(
                    appName = blueprint.appName,
                    packageId = blueprint.packageId,
                    prompt = clean,
                    summary = blueprint.summary,
                    mainActivityKt = blueprint.mainActivityKt,
                    manifestXml = blueprint.manifestXml,
                    buildGradleKts = blueprint.buildGradleKts,
                    stringsXml = blueprint.stringsXml,
                    termuxBuildSh = blueprint.termuxBuildSh
                )
                val insertedId = generatedAppDao.insert(entity)
                _uiState.update {
                    it.copy(
                        builderOpState = AiOpState.Success("Projek '${blueprint.appName}' berjaya dijana!"),
                        selectedProjectId = insertedId
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menjana projek APK."
                logSystemError("APK_BUILDER", msg, "FIX_SWITCH_GEMINI")
                _uiState.update { it.copy(builderOpState = AiOpState.Error(msg)) }
            }
        }
    }

    fun selectGeneratedProject(id: Long) {
        _uiState.update { it.copy(selectedProjectId = id) }
    }

    fun deleteGeneratedProject(project: GeneratedAppProjectEntity) {
        viewModelScope.launch {
            generatedAppDao.delete(project)
            if (_uiState.value.selectedProjectId == project.id) {
                _uiState.update { it.copy(selectedProjectId = null) }
            }
        }
    }

    fun compileGeneratedProjectInTermux(project: GeneratedAppProjectEntity) {
        val dispatch = termuxBridge.runInTermux(project.termuxBuildSh, settings.value.termuxWorkdir)
        _uiState.update {
            it.copy(
                termuxInstalled = dispatch.termuxInstalled,
                termuxPermissionGranted = termuxBridge.hasRunCommandPermission(),
                termuxStatusBanner = dispatch.message
            )
        }
        if (!dispatch.dispatchedViaService) {
            val fixKey = if (dispatch.needsPermission) "FIX_TERMUX_PERMISSION" else "FIX_TERMUX_SETUP"
            logSystemError("TERMUX", dispatch.message, fixKey)
        }
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.ToastMessage(dispatch.message))
        }
    }

    fun copyGeneratedBuildScript(project: GeneratedAppProjectEntity) {
        termuxBridge.copyToClipboard("${project.appName}_build.sh", project.termuxBuildSh)
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.ToastMessage("Skrip bina APK (${project.appName}) disalin ke papan klip!"))
        }
    }

    fun exportGeneratedBuildSh(uri: Uri, project: GeneratedAppProjectEntity) {
        viewModelScope.launch {
            val res = fileHelper.writeTextToUri(uri, project.termuxBuildSh)
            val msg = if (res.isSuccess) "Skrip bina APK dieksport ke fail .sh!" else "Gagal mengeksport skrip .sh."
            _events.emit(UiOneShotEvent.ToastMessage(msg))
        }
    }

    // --- Cross-App Autopilot Agent (Luno Ori & All APKs) ---

    fun updateAutopilotConfig(
        targetPkg: String,
        targetName: String,
        goal: String,
        financialSafeMode: Boolean
    ) {
        AutopilotRuntime.updateConfig(targetPkg, targetName, goal, financialSafeMode)
    }

    fun prepareAndLaunchLunoAutopilot() {
        val luno = _uiState.value.luno
        val cap = luno.capitalMyrInput.ifBlank { "50" }
        val pair = luno.selectedPair
        val goal = buildString {
            append("Buka tab Exchange di Luno Ori untuk pasangan $pair, pilih Limit Order, dan semak/sediakan order dengan modal RM$cap.")
            luno.analysis?.let {
                append(" Cadangan entry Limit: RM ${LunoMarketAnalysis.formatPrice(it.microPlan.suggestedEntryMyr)}.")
            }
        }
        AutopilotRuntime.updateConfig(
            targetPkg = "co.bitx.android.wallet",
            targetName = "Luno",
            goal = goal,
            financialSafeMode = AutopilotRuntime.state.value.financialSafeMode
        )
        viewModelScope.launch {
            _events.emit(UiOneShotEvent.Navigate("autopilot"))
        }
    }

    fun startCrossAppAutopilot(
        targetPkg: String,
        targetName: String,
        goal: String,
        financialSafeMode: Boolean,
        maxSteps: Int = 12
    ) {
        val acc = NovaAccessibilityService.instance
        if (acc == null) {
            logSystemError("ACCESSIBILITY", "Accessibility Service diperlukan untuk Cross-App Autopilot.", "FIX_ACCESSIBILITY")
            telemetryManager.openAccessibilitySettings()
            viewModelScope.launch {
                _events.emit(UiOneShotEvent.ToastMessage("Aktifkan 'NovaCore System Control' dalam Accessibility dahulu."))
            }
            return
        }

        autopilotJob?.cancel()
        AutopilotRuntime.markStarted(targetPkg, targetName, goal, financialSafeMode, maxSteps)

        val launched = telemetryManager.launchPackage(targetPkg)
        if (launched.isFailure) {
            val fallback = telemetryManager.launchAppByQuery(targetName)
            if (fallback.isFailure) {
                val errMsg = "Aplikasi '$targetName' ($targetPkg) tidak dapat dilancarkan."
                AutopilotRuntime.markStopped(errMsg)
                logSystemError("AUTOPILOT", errMsg, "")
                viewModelScope.launch { _events.emit(UiOneShotEvent.ToastMessage(errMsg)) }
                return
            }
        }

        autopilotJob = autopilotScope.launch {
            val stepHistory = mutableListOf<String>()
            val sensitiveKeywords = listOf(
                "confirm", "sahkan", "buy", "beli", "sell", "jual",
                "place order", "send", "hantar", "withdraw", "keluar", "pin", "pay", "bayar"
            )

            try {
                for (step in 1..maxSteps) {
                    if (AutopilotRuntime.isStopRequested()) break
                    delay(2300L)
                    if (AutopilotRuntime.isStopRequested()) break

                    val service = NovaAccessibilityService.instance
                    if (service == null) {
                        AutopilotRuntime.markStopped("Accessibility terputus di tengah jalan.")
                        logSystemError("ACCESSIBILITY", "NovaAccessibilityService terhenti semasa Autopilot.", "FIX_ACCESSIBILITY")
                        break
                    }

                    val activePkg = service.getActiveWindowPackage()
                    val screenNodes = service.readDetailedScreenNodes(maxNodes = 45)

                    // Detect OS-blocked secure screens (PIN / Biometric)
                    val isLikelySecureScreen = activePkg == targetPkg &&
                        screenNodes.contains("Tiada elemen interaktif ditemui.") &&
                        step > 1

                    if (isLikelySecureScreen) {
                        val pauseMsg = "Skrin PIN/biometrik dikesan dan disekat oleh Android OS untuk keselamatan. Sila sahkan sendiri."
                        AutopilotRuntime.markPausedForConfirm(
                            headline = pauseMsg,
                            logEntry = AutopilotStepLog(step, activePkg, "PAUSE_FOR_CONFIRM", pauseMsg)
                        )
                        if (settings.value.autoSpeak) {
                            voiceController.speak(pauseMsg, settings.value.speechLocale)
                        }
                        break
                    }

                    val profile = activeAiProfile.value ?: aiProfileDao.getActiveOnce()
                    val lunoContext = if (targetPkg == "co.bitx.android.wallet") {
                        _uiState.value.luno.analysis?.toPromptSummary().orEmpty()
                    } else ""

                    val decision = aiRepository.decideNextAutopilotStep(
                        activeProfile = profile,
                        goal = goal,
                        targetPackage = targetPkg,
                        stepNumber = step,
                        maxSteps = maxSteps,
                        financialSafeMode = financialSafeMode,
                        lunoMarketContext = lunoContext,
                        screenNodesSummary = screenNodes,
                        stepHistory = stepHistory
                    )

                    if (AutopilotRuntime.isStopRequested()) break

                    // Programmatic zero-trust financial guardrail
                    val hitsSensitiveTap = financialSafeMode &&
                        decision.action == AutopilotActionType.CLICK_TEXT &&
                        sensitiveKeywords.any { kw -> decision.param.contains(kw, ignoreCase = true) }

                    if (decision.action == AutopilotActionType.PAUSE_FOR_CONFIRM || hitsSensitiveTap) {
                        val reason = if (hitsSensitiveTap) {
                            "Mod Keselamatan Kewangan menahan ketukan '${decision.param}'. Sila semak dan tekan sahkan sendiri."
                        } else {
                            decision.speakUpdate.ifBlank { "Autopilot berhenti untuk pengesahan manusia." }
                        }
                        AutopilotRuntime.markPausedForConfirm(
                            headline = reason,
                            logEntry = AutopilotStepLog(step, activePkg, "PAUSE_FOR_CONFIRM", reason)
                        )
                        if (settings.value.autoSpeak) {
                            voiceController.speak(reason, settings.value.speechLocale)
                        }
                        break
                    }

                    val actionResult = when (decision.action) {
                        AutopilotActionType.CLICK_TEXT -> {
                            val ok = service.clickNodeByText(decision.param)
                            if (ok) "Ketuk '${decision.param}'" else "Butang '${decision.param}' tiada pada skrin"
                        }
                        AutopilotActionType.TAP_COORDS -> {
                            val parts = decision.param.split(",")
                            val x = (parts.getOrNull(0)?.trim()?.toFloatOrNull() ?: 50f) / 100f
                            val y = (parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 50f) / 100f
                            val ok = service.dispatchTapAtPercent(x, y)
                            if (ok) "Ketuk (${decision.param}%)" else "Gagal ketuk koordinat"
                        }
                        AutopilotActionType.TYPE_TEXT -> {
                            val ok = service.typeTextIntoFocusedOrFirstInput(decision.param)
                            if (ok) "Taip '${decision.param}'" else "Tiada medan input aktif"
                        }
                        AutopilotActionType.SWIPE -> {
                            val dir = decision.param.ifBlank { "UP" }
                            service.swipeDirection(dir)
                            "Leret $dir"
                        }
                        AutopilotActionType.SCROLL_DOWN -> {
                            service.scrollActiveWindow(true)
                            "Tatal ke bawah"
                        }
                        AutopilotActionType.SCROLL_UP -> {
                            service.scrollActiveWindow(false)
                            "Tatal ke atas"
                        }
                        AutopilotActionType.PRESS_BACK -> {
                            service.goBack()
                            "Tekan Kembali"
                        }
                        AutopilotActionType.PRESS_HOME -> {
                            service.goHome()
                            "Tekan Home"
                        }
                        AutopilotActionType.WAIT -> "Tunggu skrin memuatkan..."
                        AutopilotActionType.FINISH -> decision.speakUpdate.ifBlank { "Matlamat Autopilot selesai." }
                        AutopilotActionType.PAUSE_FOR_CONFIRM -> decision.speakUpdate
                    }

                    stepHistory.add("${decision.action.name}(${decision.param}) -> $actionResult")
                    AutopilotRuntime.updateStep(
                        step = step,
                        headline = decision.speakUpdate.ifBlank { actionResult },
                        logEntry = AutopilotStepLog(
                            step = step,
                            screenPackage = activePkg.ifBlank { "unknown" },
                            action = "${decision.action.name}: ${decision.param}",
                            detail = "${decision.thought} | $actionResult"
                        )
                    )

                    if (decision.action == AutopilotActionType.FINISH) {
                        AutopilotRuntime.markStopped("Selesai: ${decision.speakUpdate}")
                        if (settings.value.autoSpeak && decision.speakUpdate.isNotBlank()) {
                            voiceController.speak(decision.speakUpdate, settings.value.speechLocale)
                        }
                        break
                    }
                }

                if (AutopilotRuntime.state.value.isRunning && !AutopilotRuntime.isStopRequested()) {
                    AutopilotRuntime.markStopped("Had $maxSteps langkah dicapai.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Ralat Autopilot luar aplikasi."
                AutopilotRuntime.markStopped("Ralat: $msg")
                logSystemError("AUTOPILOT", msg, "FIX_SWITCH_GEMINI")
            }
        }
    }

    fun stopCrossAppAutopilot() {
        autopilotJob?.cancel()
        autopilotJob = null
        AutopilotRuntime.requestStop()
        voiceController.stopSpeaking()
    }

    override fun onCleared() {
        super.onCleared()
        autoPlayJob?.cancel()
        externalMacroJob?.cancel()
        autopilotJob?.cancel()
        autopilotScope.cancel()
        voiceController.release()
    }
}
