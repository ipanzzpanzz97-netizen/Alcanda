package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.GeneratedAppProjectEntity
import com.example.ui.screens.AiModelsScreen
import com.example.ui.screens.ApkBuilderScreen
import com.example.ui.screens.AutopilotScreen
import com.example.ui.screens.CameraStudioScreen
import com.example.ui.screens.CommandScreen
import com.example.ui.screens.DeviceHubScreen
import com.example.ui.screens.GameArenaScreen
import com.example.ui.screens.LunoTradingScreen
import com.example.ui.screens.RepairScreen
import com.example.ui.screens.TermuxScreen
import com.example.viewmodel.NovaViewModel
import com.example.viewmodel.UiOneShotEvent
import kotlinx.coroutines.launch

const val ROUTE_COMMAND = "command"
const val ROUTE_BUILDER = "builder"
const val ROUTE_LUNO = "luno"
const val ROUTE_AUTOPILOT = "autopilot"
const val ROUTE_TERMUX = "termux"
const val ROUTE_DEVICE = "device"
const val ROUTE_ARENA = "arena"
const val ROUTE_MODELS = "models"
const val ROUTE_REPAIR = "repair"
const val ROUTE_CAMERA = "camera"

private data class NavDestinationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun NovaApp(
    autoTriggerVoice: Boolean = false,
    viewModel: NovaViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val savedScripts by viewModel.savedScripts.collectAsStateWithLifecycle()
    val liveSensors by viewModel.liveSensors.collectAsStateWithLifecycle()
    val aiProfiles by viewModel.aiProfiles.collectAsStateWithLifecycle()
    val activeAiProfile by viewModel.activeAiProfile.collectAsStateWithLifecycle()
    val systemErrors by viewModel.systemErrors.collectAsStateWithLifecycle()
    val unresolvedErrorCount by viewModel.unresolvedErrorCount.collectAsStateWithLifecycle()
    val generatedProjects by viewModel.generatedProjects.collectAsStateWithLifecycle()
    val autopilotState by viewModel.autopilotState.collectAsStateWithLifecycle()
    var pendingExportProject by remember { mutableStateOf<GeneratedAppProjectEntity?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDeviceSnapshot()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onFilePicked(uri)
            }
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onFilePicked(uri)
                navController.navigate(ROUTE_DEVICE) {
                    launchSingleTop = true
                }
            }
        }
    )

    val exportScriptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-shellscript"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.exportTermuxScriptToUri(uri)
            }
        }
    )

    val exportBuildScriptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-shellscript"),
        onResult = { uri ->
            val project = pendingExportProject
            pendingExportProject = null
            if (uri != null && project != null) {
                viewModel.exportGeneratedBuildSh(uri, project)
            }
        }
    )

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.startVoiceListen()
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Kebenaran mikrofon diperlukan untuk arahan suara.")
                }
            }
        }
    )

    val termuxPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.refreshDeviceSnapshot()
            coroutineScope.launch {
                val msg = if (granted) {
                    "Izin RUN_COMMAND Termux dibenarkan!"
                } else {
                    "Izin RUN_COMMAND belum diberi — anda masih boleh guna salin/tampal ke Termux."
                }
                snackbarHostState.showSnackbar(msg)
            }
        }
    )

    fun triggerVoiceOrb() {
        if (voiceState.isListening) {
            viewModel.stopVoiceListen()
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startVoiceListen()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(autoTriggerVoice) {
        if (autoTriggerVoice) {
            triggerVoiceOrb()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiOneShotEvent.Navigate -> {
                    navController.navigate(event.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                UiOneShotEvent.LaunchFilePicker -> {
                    filePickerLauncher.launch(arrayOf("*/*"))
                }
                UiOneShotEvent.LaunchPhotoPicker -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                UiOneShotEvent.RequestTermuxPermission -> {
                    termuxPermissionLauncher.launch("com.termux.permission.RUN_COMMAND")
                }
                is UiOneShotEvent.ToastMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val navItems = remember {
        listOf(
            NavDestinationItem(ROUTE_COMMAND, "Suara", Icons.Filled.Mic, Icons.Outlined.Mic),
            NavDestinationItem(ROUTE_BUILDER, "Bina APK", Icons.Filled.Android, Icons.Outlined.Android),
            NavDestinationItem(ROUTE_LUNO, "Luno", Icons.Filled.CandlestickChart, Icons.Outlined.CandlestickChart),
            NavDestinationItem(ROUTE_AUTOPILOT, "Autopilot", Icons.Filled.AutoMode, Icons.Outlined.AutoMode),
            NavDestinationItem(ROUTE_DEVICE, "Peranti", Icons.Filled.Devices, Icons.Outlined.Devices)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_COMMAND
    val showNavigation = currentRoute != ROUTE_CAMERA

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && showNavigation) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag("nav_${item.route}")
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && showNavigation) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.testTag("nav_${item.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_COMMAND,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(ROUTE_COMMAND) {
                        val activeLabel = activeAiProfile?.let { "${it.name} (${it.modelName})" }
                            ?: "Gemini 3.8 Flash"
                        CommandScreen(
                            uiState = uiState,
                            activeAiLabel = activeLabel,
                            unresolvedErrorCount = unresolvedErrorCount,
                            voiceState = voiceState,
                            settings = settings,
                            messages = messages,
                            onVoiceOrbClick = ::triggerVoiceOrb,
                            onSendPrompt = viewModel::sendCommand,
                            onToggleAutoSpeak = viewModel::toggleAutoSpeak,
                            onToggleLocale = {
                                val next = if (settings.speechLocale.startsWith("ms")) "en-US" else "ms-MY"
                                viewModel.setSpeechLocale(next)
                            },
                            onToggleTorch = viewModel::toggleTorch,
                            onOpenCamera = { navController.navigate(ROUTE_CAMERA) },
                            onPickFile = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                                navController.navigate(ROUTE_DEVICE) { launchSingleTop = true }
                            },
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onOpenAiModels = {
                                navController.navigate(ROUTE_MODELS) { launchSingleTop = true }
                            },
                            onOpenLunoTrading = {
                                navController.navigate(ROUTE_LUNO) { launchSingleTop = true }
                            },
                            onOpenApkBuilder = {
                                navController.navigate(ROUTE_BUILDER) { launchSingleTop = true }
                            },
                            onOpenAutopilot = {
                                navController.navigate(ROUTE_AUTOPILOT) { launchSingleTop = true }
                            },
                            onOpenTermux = {
                                navController.navigate(ROUTE_TERMUX) { launchSingleTop = true }
                            },
                            onOpenGameArena = {
                                navController.navigate(ROUTE_ARENA) { launchSingleTop = true }
                            },
                            onOpenRepairCenter = {
                                navController.navigate(ROUTE_REPAIR) { launchSingleTop = true }
                            },
                            onClearChat = viewModel::clearChatHistory,
                            onClearVoiceError = viewModel::clearVoiceError
                        )
                    }

                    composable(ROUTE_BUILDER) {
                        ApkBuilderScreen(
                            builderOpState = uiState.builderOpState,
                            projects = generatedProjects,
                            selectedProjectId = uiState.selectedProjectId,
                            termuxInstalled = uiState.termuxInstalled,
                            termuxBanner = uiState.termuxStatusBanner,
                            onGenerateProject = viewModel::generateApkProjectFromIdea,
                            onSelectProject = viewModel::selectGeneratedProject,
                            onDeleteProject = viewModel::deleteGeneratedProject,
                            onCompileInTermux = viewModel::compileGeneratedProjectInTermux,
                            onCopyBuildScript = viewModel::copyGeneratedBuildScript,
                            onExportBuildSh = { project ->
                                pendingExportProject = project
                                val slug = project.packageId.substringAfterLast('.').ifBlank { "novacore_app" }
                                exportBuildScriptLauncher.launch("${slug}_build.sh")
                            }
                        )
                    }

                    composable(ROUTE_AUTOPILOT) {
                        AutopilotScreen(
                            autopilotState = autopilotState,
                            accessibilityReady = uiState.accessibilityReady,
                            installedApps = uiState.installedApps,
                            onUpdateConfig = viewModel::updateAutopilotConfig,
                            onStartAutopilot = viewModel::startCrossAppAutopilot,
                            onStopAutopilot = viewModel::stopCrossAppAutopilot,
                            onOpenAccessibility = viewModel::openAccessibilitySettings,
                            onOpenAppInfo = viewModel::openAppInfoSettings
                        )
                    }

                    composable(ROUTE_LUNO) {
                        LunoTradingScreen(
                            lunoState = uiState.luno,
                            savedKeyId = settings.lunoKeyId,
                            savedKeySecret = settings.lunoKeySecret,
                            onLoadMarket = { viewModel.loadLunoMarket() },
                            onSelectPair = viewModel::selectLunoPair,
                            onUpdateCapitalAndRisk = viewModel::updateLunoCapitalAndRisk,
                            onRunVeteranAi = viewModel::runVeteranLunoAiAnalysis,
                            onFetchBalance = viewModel::saveAndFetchLunoBalance,
                            onLaunchLunoApp = viewModel::launchLunoApp,
                            onLaunchLunoAutopilot = viewModel::prepareAndLaunchLunoAutopilot
                        )
                    }

                    composable(ROUTE_TERMUX) {
                        TermuxScreen(
                            uiState = uiState,
                            savedScripts = savedScripts,
                            onUpdateEditor = viewModel::updateTermuxEditor,
                            onGenerateCode = viewModel::generateTermuxScript,
                            onRunInTermux = viewModel::runCurrentTermuxScript,
                            onSaveScript = viewModel::saveCurrentTermuxScript,
                            onCopyCode = viewModel::copyTermuxCode,
                            onExportScript = {
                                exportScriptLauncher.launch(uiState.termuxScriptName.ifBlank { "novacore_script.sh" })
                            },
                            onOpenTermux = viewModel::openTermuxApp,
                            onCopySetupCommand = viewModel::copyTermuxSetupCommand,
                            onRequestTermuxPermission = viewModel::requestTermuxPermission,
                            onLoadScript = viewModel::loadSavedScript,
                            onDeleteScript = viewModel::deleteSavedScript
                        )
                    }

                    composable(ROUTE_DEVICE) {
                        DeviceHubScreen(
                            uiState = uiState,
                            unresolvedErrorCount = unresolvedErrorCount,
                            liveSensors = liveSensors,
                            onRefreshTelemetry = viewModel::refreshDeviceSnapshot,
                            onToggleTorch = viewModel::toggleTorch,
                            onOpenSettingsPanel = viewModel::openSystemSettings,
                            onOpenAccessibilitySettings = viewModel::openAccessibilitySettings,
                            onOpenWriteSettings = viewModel::openWriteSettingsScreen,
                            onOpenAppInfo = viewModel::openAppInfoSettings,
                            onOpenRepairCenter = { navController.navigate(ROUTE_REPAIR) { launchSingleTop = true } },
                            onOpenAiModels = { navController.navigate(ROUTE_MODELS) { launchSingleTop = true } },
                            onOpenTermux = { navController.navigate(ROUTE_TERMUX) { launchSingleTop = true } },
                            onOpenGameArena = { navController.navigate(ROUTE_ARENA) { launchSingleTop = true } },
                            onLockScreen = viewModel::triggerLockScreen,
                            onPowerMenu = viewModel::triggerPowerDialog,
                            onScreenshot = viewModel::triggerScreenshot,
                            onGlobalHome = viewModel::triggerGlobalHome,
                            onVolumeAdjust = viewModel::triggerVolumeAdjust,
                            onMediaPlayPause = { viewModel.triggerMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                            onBrightnessAdjust = viewModel::triggerBrightnessAdjust,
                            onPickFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onOpenCamera = { navController.navigate(ROUTE_CAMERA) },
                            onAnalyzeSelectedFile = viewModel::analyzeSelectedFile,
                            onLaunchApp = viewModel::launchInstalledApp
                        )
                    }

                    composable(ROUTE_ARENA) {
                        GameArenaScreen(
                            uiState = uiState,
                            onSelectGame = viewModel::selectGame,
                            onResetGame = viewModel::resetActiveGame,
                            onPlayTicTacToe = viewModel::playUserTicTacToe,
                            onPlayConnect4 = viewModel::playUserConnect4,
                            onPlay2048 = viewModel::playUser2048,
                            onTriggerAiMove = viewModel::triggerAiTurnForCurrentGame,
                            onToggleAiAutoPlay = viewModel::toggleAiAutoPlay,
                            onLaunchExternalGame = viewModel::launchExternalGameAndPrepare,
                            onStartExternalMacro = viewModel::startExternalGameMacro,
                            onStopExternalMacro = { viewModel.stopExternalGameMacro() },
                            onOpenAccessibility = viewModel::openAccessibilitySettings
                        )
                    }

                    composable(ROUTE_MODELS) {
                        AiModelsScreen(
                            profiles = aiProfiles,
                            activeProfile = activeAiProfile,
                            connectionTestState = uiState.connectionTestState,
                            onToggleProfileSwitch = viewModel::toggleAiProfileSwitch,
                            onSaveProfile = viewModel::saveAndActivateAiProfile,
                            onDeleteProfile = viewModel::deleteAiProfile,
                            onTestConnection = viewModel::testAiProfileConnection,
                            onRestoreFreePresets = viewModel::restoreFreeAiPresets,
                            onOpenUrl = viewModel::openUrlExternal,
                            onClearConnectionTest = viewModel::clearConnectionTest
                        )
                    }

                    composable(ROUTE_REPAIR) {
                        RepairScreen(
                            healthCheck = uiState.healthCheck,
                            errors = systemErrors,
                            diagnosingErrorId = uiState.diagnosingErrorId,
                            onRunDiagnostics = viewModel::runFullSystemDiagnostics,
                            onRunAutoHealAll = viewModel::runAutoHealAll,
                            onRunAiRepairIssue = viewModel::runAiSelfRepairForIssue,
                            onApplyQuickFixKey = viewModel::applyQuickFixKey,
                            onApplyAutoFix = viewModel::applyAutoFix,
                            onAskAiDiagnose = viewModel::askAiToDiagnoseError,
                            onResolveError = viewModel::resolveSystemError,
                            onClearResolved = viewModel::clearResolvedSystemErrors,
                            onClearAll = viewModel::clearAllSystemErrors,
                            onOpenAccessibility = viewModel::openAccessibilitySettings,
                            onOpenAiModels = { navController.navigate(ROUTE_MODELS) { launchSingleTop = true } },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ROUTE_CAMERA) {
                        CameraStudioScreen(
                            uiState = uiState,
                            onAnalyzeBitmap = viewModel::analyzeCameraBitmap,
                            onClearResult = viewModel::clearVisionResult,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
