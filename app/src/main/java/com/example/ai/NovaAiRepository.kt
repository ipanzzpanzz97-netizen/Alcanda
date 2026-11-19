package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.AiProviderProfileEntity
import com.example.data.AiProviderType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "NovaAiRepository"
private const val DEFAULT_GEMINI_MODEL = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

enum class DeviceAction {
    NONE,
    OPEN_CAMERA,
    OPEN_FILE_PICKER,
    OPEN_PHOTO_PICKER,
    TOGGLE_FLASHLIGHT_ON,
    TOGGLE_FLASHLIGHT_OFF,
    SHOW_TELEMETRY,
    RUN_TERMUX_SCRIPT,
    OPEN_TERMUX_STUDIO,
    PLAY_GAME_TICTACTOE,
    PLAY_GAME_CONNECT4,
    PLAY_GAME_2048,
    LAUNCH_APP,
    OPEN_WIFI_SETTINGS,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_BATTERY_SETTINGS,
    // Out-of-app system & full APK controls (Poco X7 Pro / Android Accessibility + System APIs)
    LOCK_SCREEN,
    POWER_OFF_DEVICE,
    SYSTEM_HOME,
    SYSTEM_BACK,
    SYSTEM_RECENTS,
    SYSTEM_NOTIFICATIONS,
    SYSTEM_QUICK_SETTINGS,
    TAKE_SCREENSHOT,
    CLICK_SCREEN_TEXT,
    TAP_SCREEN_COORDS,
    SWIPE_DIRECTION,
    TYPE_SCREEN_TEXT,
    READ_OUTSIDE_SCREEN,
    SCROLL_DOWN,
    SCROLL_UP,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    MEDIA_PLAY_PAUSE,
    MEDIA_NEXT,
    MEDIA_PREVIOUS,
    BRIGHTNESS_UP,
    BRIGHTNESS_DOWN,
    SET_TIMER,
    DIAL_NUMBER,
    WEB_SEARCH,
    OPEN_LUNO_TRADING,
    OPEN_REPAIR_CENTER,
    OPEN_APK_BUILDER,
    OPEN_AUTOPILOT,
    START_LUNO_AUTOPILOT,
    STOP_AUTOPILOT
}

data class AiCommandResult(
    val reply: String,
    val action: DeviceAction,
    val actionParam: String = "",
    val termuxScriptName: String = "",
    val termuxCode: String = ""
)

data class AiGameMoveResult(
    val move: String,
    val comment: String
)

data class LunoVeteranAiReport(
    val actionSignal: String, // e.g., "BELI LIMIT", "TUNGGU RETRACE", "AMBIL UNTUNG"
    val confidenceLabel: String,
    val entryPlanMyr: String,
    val stopLossPlanMyr: String,
    val takeProfitPlanMyr: String,
    val strategySummary: String,
    val smallCapitalAdvice: String,
    val riskWarning: String
)

enum class AutopilotActionType {
    CLICK_TEXT,
    TAP_COORDS,
    TYPE_TEXT,
    SWIPE,
    SCROLL_DOWN,
    SCROLL_UP,
    PRESS_BACK,
    PRESS_HOME,
    WAIT,
    PAUSE_FOR_CONFIRM,
    FINISH
}

data class AutopilotDecision(
    val thought: String,
    val action: AutopilotActionType,
    val param: String,
    val speakUpdate: String
)

data class GeneratedApkBlueprint(
    val appName: String,
    val packageId: String,
    val summary: String,
    val mainActivityKt: String,
    val manifestXml: String,
    val buildGradleKts: String,
    val stringsXml: String,
    val termuxBuildSh: String
)

data class AiSelfRepairPlan(
    val diagnosis: String,
    val fixKey: String,
    val repairSteps: List<String>
)

class NovaAiRepository {

    private val externalClient = ExternalAiClient()

    private val commandSystemInstruction = """
        You are NovaCore, an intelligent AI assistant embedded directly inside the user's Xiaomi Poco X7 Pro smartphone.
        You speak fluent Bahasa Melayu (natural, friendly, helpful) and English, matching whichever language the user uses.
        You have real hardware, full installed-APK access, and out-of-app OS integrations inside this APK:
        - Camera & Vision (`OPEN_CAMERA`, `OPEN_PHOTO_PICKER`)
        - Direct Storage Access Framework Files (`OPEN_FILE_PICKER`)
        - Flashlight (`TOGGLE_FLASHLIGHT_ON`, `TOGGLE_FLASHLIGHT_OFF`)
        - Hardware Telemetry & Sensors (`SHOW_TELEMETRY`)
        - Termux Script Coding & Execution (`OPEN_TERMUX_STUDIO`, `RUN_TERMUX_SCRIPT`)
        - AI APK Builder Studio (`OPEN_APK_BUILDER` when user asks to create/build a new Android app/APK from an idea, with `actionParam` set to the app description)
        - Cross-App AI Autopilot Agent (`OPEN_AUTOPILOT`, `START_LUNO_AUTOPILOT`, `STOP_AUTOPILOT` when user asks AI to autopilot outside apps or operate inside Luno Ori `co.bitx.android.wallet` automatically, with `actionParam` set to the goal)
        - Interactive AI Game Arena & External Game Co-Pilot (`PLAY_GAME_TICTACTOE`, `PLAY_GAME_CONNECT4`, `PLAY_GAME_2048`, or `LAUNCH_APP` for any installed online/offline game)
        - Full Installed APK Launcher (`LAUNCH_APP` with `actionParam` set to any app or game name from `[ALL INSTALLED APKS]`, e.g., "Luno", "WhatsApp", "Termux", "Mobile Legends", "PUBG", "TikTok", etc.)
        - Veteran Luno Malaysia Trading Desk (`OPEN_LUNO_TRADING` when user asks about Luno trading, crypto MYR prices, chart/RSI setup, or modal RM10-RM500)
        - Error Diagnostic & Auto-Repair Center (`OPEN_REPAIR_CENTER` when user reports an error or asks to check/fix system issues)
        - System Settings (`OPEN_WIFI_SETTINGS`, `OPEN_BLUETOOTH_SETTINGS`, `OPEN_BATTERY_SETTINGS`)
        - Out-of-App System & Screen Controls via NovaAccessibilityService:
          * `LOCK_SCREEN` (kunci / matikan skrin telefon)
          * `POWER_OFF_DEVICE` (buka menu kuasa / matikan telefon Poco X7 Pro)
          * `SYSTEM_HOME` / `SYSTEM_BACK` / `SYSTEM_RECENTS` / `SYSTEM_NOTIFICATIONS` / `SYSTEM_QUICK_SETTINGS`
          * `TAKE_SCREENSHOT` (tangkap skrin / screenshot sekarang)
          * `READ_OUTSIDE_SCREEN` (baca teks & koordinat butang pada aplikasi luar yang sedang dibuka)
          * `CLICK_SCREEN_TEXT` (ketuk butang pada skrin luar dengan teks `actionParam`)
          * `TAP_SCREEN_COORDS` (ketuk koordinat peratus skrin luar, `actionParam` format `"50,75"` iaitu xPct,yPct)
          * `SWIPE_DIRECTION` (leret skrin ke arah `UP`, `DOWN`, `LEFT`, atau `RIGHT` dalam `actionParam`)
          * `TYPE_SCREEN_TEXT` (taip teks `actionParam` ke dalam kotak input aplikasi luar)
          * `SCROLL_DOWN` / `SCROLL_UP`
          * `VOLUME_UP` / `VOLUME_DOWN` / `VOLUME_MUTE`
          * `MEDIA_PLAY_PAUSE` / `MEDIA_NEXT` / `MEDIA_PREVIOUS`
          * `BRIGHTNESS_UP` / `BRIGHTNESS_DOWN`
          * `SET_TIMER` (`actionParam` = bilangan saat)
          * `DIAL_NUMBER` (`actionParam` = nombor telefon)
          * `WEB_SEARCH` (`actionParam` = kata carian)

        When the user speaks or types a command, pick the exact matching `action` so the phone obeys immediately, and write a concise `reply` suitable for Text-to-Speech.
        Never invent capabilities you don't have; use the exact actions above.
    """.trimIndent()

    private val commandSchema = Schema.obj(
        properties = mapOf(
            "reply" to Schema.string(
                description = "Friendly, concise natural response to the user in the user's language (Malay or English)."
            ),
            "action" to Schema.enumeration(
                values = DeviceAction.entries.map { it.name },
                description = "The exact phone action to execute immediately on the device."
            ),
            "actionParam" to Schema.string(
                description = "Optional parameter such as app name, button text, xPct,yPct coords, swipe direction, typed text, timer seconds, phone number, or search query."
            ),
            "termuxScriptName" to Schema.string(
                description = "Short script filename if action is RUN_TERMUX_SCRIPT or OPEN_TERMUX_STUDIO, e.g. scan_net.sh"
            ),
            "termuxCode" to Schema.string(
                description = "Complete executable Termux bash/python command if user asked to code or run something in Termux."
            )
        ),
        optionalProperties = listOf("actionParam", "termuxScriptName", "termuxCode")
    )

    private val gameMoveSchema = Schema.obj(
        properties = mapOf(
            "move" to Schema.string(
                description = "Exact chosen move token from the validMoves list."
            ),
            "comment" to Schema.string(
                description = "Short witty 1-sentence game commentary in Bahasa Melayu or English."
            )
        )
    )

    private val lunoVeteranSchema = Schema.obj(
        properties = mapOf(
            "actionSignal" to Schema.enumeration(
                values = listOf("BELI LIMIT (ACCUMULATE)", "TUNGGU RETRACE (WAIT)", "AMBIL UNTUNG SEBAHAGIAN", "ELAK ENTRY (OVERBOUGHT/RISIKO)"),
                description = "Clear disciplined tactical stance for Luno Exchange."
            ),
            "confidenceLabel" to Schema.string(
                description = "Confluence score e.g. 'Tinggi (3/3 Indikator)' or 'Sederhana (2/3)'."
            ),
            "entryPlanMyr" to Schema.string(
                description = "Specific Limit Order entry zone in RM."
            ),
            "stopLossPlanMyr" to Schema.string(
                description = "Specific invalidation Stop-Loss level in RM."
            ),
            "takeProfitPlanMyr" to Schema.string(
                description = "Specific TP1 and TP2 target prices in RM."
            ),
            "strategySummary" to Schema.string(
                description = "Veteran technical breakdown referencing RSI(14), EMA 9/21, Orderbook Spread, and Support/Resistance in Bahasa Melayu."
            ),
            "smallCapitalAdvice" to Schema.string(
                description = "Practical Luno Malaysia fee-saving & position sizing advice for user's MYR capital (emphasizing Luno Exchange Limit Order 0.25% vs Instant Buy ~2%)."
            ),
            "riskWarning" to Schema.string(
                description = "Honest 1-sentence risk disclaimer noting crypto volatility and no guaranteed profit."
            )
        )
    )

    private fun firebaseCommandModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = commandSchema
            temperature = 0.4f
        },
        systemInstruction = content { text(commandSystemInstruction) }
    )

    private fun firebaseTextModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        systemInstruction = content {
            text(
                """
                You are NovaCore, an AI device assistant inside an Android APK.
                Respond clearly and concisely in Bahasa Melayu or English matching the user's prompt.
                For Termux coding requests, output clean, ready-to-run Termux bash/Python scripts with practical explanations.
                """.trimIndent()
            )
        }
    )

    private fun firebaseGameModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = gameMoveSchema
            temperature = 0.3f
        }
    )

    private val lunoSystemInstruction = """
        Anda ialah seorang Pedagang Kripto Veteran Luno Malaysia (10+ tahun pengalaman spot trading MYR pairs di Luno Exchange).
        Fokus utama anda:
        1. Lindungi modal pengguna dahulu (terutama modal mikro RM10–RM500). Jangan sekali-kali menjanjikan keuntungan pasti.
        2. Sentiasa bezakan yuran Luno Exchange Limit Order (Maker ~0.25%) berbanding Luno Instant Buy/Sell (~2% spread+fee) yang memakan modal kecil.
        3. Analisa secara teknikal berdasarkan data sebenar yang dibekalkan: RSI(14), EMA(9) vs EMA(21), Spread Bid-Ask, Bid Wall Pressure, dan paras Support/Resistance.
        4. Berikan pelan yang tepat, berdisiplin, dengan Entry Limit (RM), Stop-Loss (RM), TP1/TP2 (RM), serta amaran risiko yang jujur dalam Bahasa Melayu yang jelas.
    """.trimIndent()

    private fun firebaseLunoModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = lunoVeteranSchema
            temperature = 0.25f
        },
        systemInstruction = content { text(lunoSystemInstruction) }
    )

    private val autopilotSchema = Schema.obj(
        properties = mapOf(
            "thought" to Schema.string(description = "Brief reasoning about what is currently visible on the external screen."),
            "action" to Schema.enumeration(
                values = AutopilotActionType.entries.map { it.name },
                description = "Next single UI interaction to execute on the active external app."
            ),
            "param" to Schema.string(
                description = "Target button text for CLICK_TEXT, 'xPct,yPct' for TAP_COORDS, text for TYPE_TEXT, or UP/DOWN/LEFT/RIGHT for SWIPE."
            ),
            "speakUpdate" to Schema.string(
                description = "Short 1-sentence Bahasa Melayu status update for the user."
            )
        )
    )

    private fun firebaseAutopilotModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = autopilotSchema
            temperature = 0.2f
        },
        systemInstruction = content {
            text(
                """
                You are NovaCore Cross-App Autopilot operating outside this APK via Android Accessibility.
                Inspect the live active package and visible UI elements [label] (x%,y%) flags, then pick ONE safe, accurate next step toward the user's goal.
                When operating inside Luno (co.bitx.android.wallet), prefer the Exchange Limit Order tab over Instant Buy/Sell to minimize fees.
                CRITICAL SAFETY RULE: If financialSafeMode is TRUE and the next screen button executes a real-money transaction (e.g., Buy, Sell, Confirm, Place Order, Send, Withdraw, PIN), you MUST return action=PAUSE_FOR_CONFIRM so the human user can verify and tap confirm themselves.
                If goal is complete or no further UI action is required, return action=FINISH.
                """.trimIndent()
            )
        }
    )

    private val apkBlueprintSchema = Schema.obj(
        properties = mapOf(
            "appName" to Schema.string(description = "Short 1-2 word Android app title."),
            "packageSlug" to Schema.string(description = "Lowercase alphanumeric identifier e.g. expensehub."),
            "summary" to Schema.string(description = "Brief Bahasa Melayu description of what the generated APK does."),
            "mainActivityKt" to Schema.string(description = "Complete self-contained MainActivity.kt in Kotlin using Jetpack Compose Material 3.")
        )
    )

    private fun firebaseApkArchitectModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = apkBlueprintSchema
            temperature = 0.35f
        },
        systemInstruction = content {
            text(
                """
                You are an expert Android Jetpack Compose app architect.
                Generate a clean, complete, single-file MainActivity.kt using Jetpack Compose Material 3 based on the user's prompt.
                Use package com.novacore.generated.<packageSlug> or com.novacore.generated.app and include ComponentActivity, setContent, MaterialTheme, Scaffold, interactive state with remember, and polished UI.
                Keep code self-contained without third-party dependencies beyond AndroidX Activity Compose and Material3.
                """.trimIndent()
            )
        }
    )

    suspend fun testProviderConnection(profile: AiProviderProfileEntity): String {
        return if (profile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
            val response = withTimeout(30_000L) {
                firebaseTextModel(profile.modelName)
                    .generateContent("Reply with exactly: OK - Firebase AI Logic Ready")
            }
            response.text?.take(120) ?: "OK"
        } else {
            externalClient.testConnection(profile)
        }
    }

    suspend fun processCommand(
        activeProfile: AiProviderProfileEntity?,
        userPrompt: String,
        telemetrySummary: String,
        installedAppsSample: String,
        visibleScreenSummary: String,
        accessibilityReady: Boolean,
        recentHistory: List<Pair<String, String>>
    ): AiCommandResult {
        val historySummary = recentHistory.takeLast(6).joinToString("\n") { (role, msg) ->
            "$role: ${msg.take(200)}"
        }

        val fullPrompt = """
            [LIVE DEVICE TELEMETRY]
            $telemetrySummary
            Accessibility System Control Active: $accessibilityReady
            Visible Outside Screen Text: ${visibleScreenSummary.ifBlank { "In NovaCore app" }}
            
            [ALL INSTALLED APKS]
            $installedAppsSample
            
            [RECENT CONVERSATION]
            $historySummary
            
            [USER COMMAND]
            $userPrompt
        """.trimIndent()

        try {
            val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                val response = withTimeout(AI_TIMEOUT_MS) {
                    firebaseCommandModel(modelId).generateContent(fullPrompt)
                }
                response.text ?: throw IllegalStateException("Empty AI response.")
            } else {
                val externalSystemPrompt = """
                    $commandSystemInstruction
                    
                    IMPORTANT: Return ONLY a valid raw JSON object (no markdown fences) with these keys:
                    - "reply": string
                    - "action": one of [${DeviceAction.entries.joinToString(", ") { it.name }}]
                    - "actionParam": string (optional)
                    - "termuxScriptName": string (optional)
                    - "termuxCode": string (optional)
                """.trimIndent()
                externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = externalSystemPrompt,
                    userPrompt = fullPrompt,
                    expectJson = true
                )
            }

            val json = parseJsonLenient(rawJson)
            val reply = json.optString("reply", "Siap!")
            val actionRaw = json.optString("action", DeviceAction.NONE.name)
            val action = runCatching { DeviceAction.valueOf(actionRaw) }
                .getOrDefault(DeviceAction.NONE)
            val actionParam = json.optString("actionParam", "")
            val scriptName = json.optString("termuxScriptName", "")
            val termuxCode = json.optString("termuxCode", "")

            return AiCommandResult(
                reply = reply,
                action = action,
                actionParam = actionParam,
                termuxScriptName = scriptName,
                termuxCode = termuxCode
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Command processing timed out", e)
            throw IllegalStateException("AI request timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process command", e)
            throw e
        }
    }

    fun analyzeImageStream(
        activeProfile: AiProviderProfileEntity?,
        bitmap: Bitmap,
        prompt: String
    ): Flow<String> = flow {
        val accumulated = StringBuilder()
        try {
            if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                withTimeout(AI_TIMEOUT_MS) {
                    firebaseTextModel(modelId).generateContentStream(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    ).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            emit(accumulated.toString())
                        }
                    }
                }
            } else {
                val result = externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = "You are NovaCore Vision AI. Analyze the image and answer concisely in Bahasa Melayu or English.",
                    userPrompt = prompt,
                    expectJson = false,
                    bitmap = bitmap
                )
                accumulated.append(result)
                emit(result)
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model returned an empty visual analysis.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Vision stream timed out", e)
            throw IllegalStateException("Vision analysis timed out after 60s.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Vision analysis failed", e)
            throw e
        }
    }

    fun analyzeFileStream(
        activeProfile: AiProviderProfileEntity?,
        fileName: String,
        mimeType: String,
        fileContent: String,
        prompt: String
    ): Flow<String> = flow {
        val accumulated = StringBuilder()
        val combinedPrompt = """
            File Name: $fileName
            MIME Type: $mimeType
            User Request: $prompt
            
            File Content:
            ```
            ${fileContent.take(18_000)}
            ```
        """.trimIndent()

        try {
            if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                withTimeout(AI_TIMEOUT_MS) {
                    firebaseTextModel(modelId).generateContentStream(combinedPrompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            emit(accumulated.toString())
                        }
                    }
                }
            } else {
                val result = externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = "You are NovaCore File & Code Inspector. Respond in Bahasa Melayu or English.",
                    userPrompt = combinedPrompt,
                    expectJson = false
                )
                accumulated.append(result)
                emit(result)
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model returned an empty file analysis.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "File analysis timed out", e)
            throw IllegalStateException("File analysis timed out after 60s.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "File analysis failed", e)
            throw e
        }
    }

    fun generateTermuxCodeStream(
        activeProfile: AiProviderProfileEntity?,
        goal: String,
        currentCode: String
    ): Flow<String> = flow {
        val accumulated = StringBuilder()
        val prompt = """
            Generate a clean, ready-to-execute Termux script for Android Termux environment.
            Return ONLY the raw bash/python script content (with #!/data/data/com.termux/files/usr/bin/bash or python3 header and helpful # comments). Do NOT wrap in markdown code fences.
            Goal: $goal
            Existing code context (if editing):
            $currentCode
        """.trimIndent()

        try {
            if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                withTimeout(AI_TIMEOUT_MS) {
                    firebaseTextModel(modelId).generateContentStream(prompt).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            emit(accumulated.toString())
                        }
                    }
                }
            } else {
                val result = externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = "You are an expert Termux Bash & Python scripter. Output only executable script text without markdown fences.",
                    userPrompt = prompt,
                    expectJson = false
                )
                val cleaned = result
                    .removePrefix("```bash")
                    .removePrefix("```sh")
                    .removePrefix("```python")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                accumulated.append(cleaned)
                emit(cleaned)
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("Model returned an empty Termux script.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Termux code gen timed out", e)
            throw IllegalStateException("Script generation timed out after 60s.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Termux code gen failed", e)
            throw e
        }
    }

    suspend fun pickGameMove(
        activeProfile: AiProviderProfileEntity?,
        gameTitle: String,
        boardDescription: String,
        validMoves: List<String>
    ): AiGameMoveResult {
        val prompt = """
            You are playing $gameTitle!
            Current board state:
            $boardDescription
            
            Choose the strongest strategic move ONLY from validMoves: ${validMoves.joinToString(", ")}
            Return JSON with exact "move" string from validMoves and a short playful "comment" in Bahasa Melayu or English.
        """.trimIndent()

        try {
            val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                val response = withTimeout(30_000L) {
                    firebaseGameModel(modelId).generateContent(prompt)
                }
                response.text ?: throw IllegalStateException("Empty game move response.")
            } else {
                externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = "You are a competitive game AI. Return ONLY a raw JSON object with keys \"move\" and \"comment\".",
                    userPrompt = prompt,
                    expectJson = true
                )
            }

            val json = parseJsonLenient(rawJson)
            val chosen = json.optString("move", validMoves.first())
            val validChosen = if (validMoves.contains(chosen)) chosen else validMoves.first()
            val comment = json.optString("comment", "Giliran saya!")
            return AiGameMoveResult(move = validChosen, comment = comment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI game move failed", e)
            throw e
        }
    }

    suspend fun analyzeLunoMarketSetup(
        activeProfile: AiProviderProfileEntity?,
        analysis: com.example.trading.LunoMarketAnalysis,
        recentCandlesSummary: String,
        userQuestion: String = ""
    ): LunoVeteranAiReport {
        val prompt = """
            [LIVE LUNO MALAYSIA MARKET TELEMETRY]
            ${analysis.toPromptSummary()}
            
            [RECENT HOURLY CANDLES]
            $recentCandlesSummary
            
            [SOALAN / FOKUS PENGGUNA]
            ${userQuestion.ifBlank { "Beri analisa veteran penuh untuk modal RM ${"%.2f".format(analysis.microPlan.capitalMyr)} pada pasangan ${analysis.pair}." }}
        """.trimIndent()

        try {
            val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
                val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
                val response = withTimeout(AI_TIMEOUT_MS) {
                    firebaseLunoModel(modelId).generateContent(prompt)
                }
                response.text ?: throw IllegalStateException("Empty Luno AI response.")
            } else {
                val extSys = """
                    $lunoSystemInstruction
                    
                    IMPORTANT: Return ONLY a raw JSON object with these exact string keys:
                    "actionSignal", "confidenceLabel", "entryPlanMyr", "stopLossPlanMyr", "takeProfitPlanMyr", "strategySummary", "smallCapitalAdvice", "riskWarning".
                """.trimIndent()
                externalClient.generateText(
                    profile = activeProfile,
                    systemPrompt = extSys,
                    userPrompt = prompt,
                    expectJson = true
                )
            }

            val json = parseJsonLenient(rawJson)
            return LunoVeteranAiReport(
                actionSignal = json.optString("actionSignal", "TUNGGU RETRACE (WAIT)"),
                confidenceLabel = json.optString("confidenceLabel", "Sederhana"),
                entryPlanMyr = json.optString("entryPlanMyr", "RM ${com.example.trading.LunoMarketAnalysis.formatPrice(analysis.microPlan.suggestedEntryMyr)}"),
                stopLossPlanMyr = json.optString("stopLossPlanMyr", "RM ${com.example.trading.LunoMarketAnalysis.formatPrice(analysis.microPlan.suggestedStopLossMyr)}"),
                takeProfitPlanMyr = json.optString("takeProfitPlanMyr", "TP1 RM ${com.example.trading.LunoMarketAnalysis.formatPrice(analysis.microPlan.suggestedTp1Myr)} | TP2 RM ${com.example.trading.LunoMarketAnalysis.formatPrice(analysis.microPlan.suggestedTp2Myr)}"),
                strategySummary = json.optString("strategySummary", rawJson.take(400)),
                smallCapitalAdvice = json.optString("smallCapitalAdvice", "Gunakan Luno Exchange Limit Order (~0.25% fee) untuk elak kos tinggi Instant Buy (~2%)."),
                riskWarning = json.optString("riskWarning", "Kripto berisiko tinggi; tiada jaminan untung. Gunakan Stop-Loss & modal yang sanggup ditanggung.")
            )
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("Analisa Luno AI tamat masa (60s).")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Luno AI analysis failed", e)
            throw e
        }
    }

    private val selfRepairSchema = Schema.obj(
        properties = mapOf(
            "diagnosis" to Schema.string(description = "Punca masalah yang dikesan dalam Bahasa Melayu."),
            "fixKey" to Schema.enumeration(
                values = listOf(
                    "FIX_AUTO_FAILOVER_FREE_AI",
                    "FIX_SWITCH_GEMINI",
                    "FIX_ACCESSIBILITY",
                    "FIX_TERMUX_PERMISSION",
                    "FIX_TERMUX_SETUP",
                    "FIX_WRITE_SETTINGS",
                    "FIX_RESET_VOICE",
                    "FIX_RETRY_LUNO",
                    "FIX_STOP_AUTOPILOT",
                    "NONE"
                ),
                description = "Auto-repair action key to execute immediately on device."
            ),
            "repairSteps" to Schema.array(
                items = Schema.string(),
                description = "Concise numbered recovery steps in Bahasa Melayu."
            )
        )
    )

    private fun firebaseSelfRepairModel(modelName: String) = Firebase.ai.generativeModel(
        modelName = modelName.ifBlank { DEFAULT_GEMINI_MODEL },
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = selfRepairSchema
            temperature = 0.2f
        },
        systemInstruction = content {
            text(
                """
                You are the NovaCore APK Self-Healing Doctor AI running on Poco X7 Pro (HyperOS/Android).
                Given live module health and user bug report or error log, identify root cause and choose the exact automated `fixKey` so the APK heals itself right away:
                - FIX_AUTO_FAILOVER_FREE_AI: if Firebase AI / App Check or an API key fails
                - FIX_TERMUX_PERMISSION: if Termux RUN_COMMAND permission is missing
                - FIX_TERMUX_SETUP: if Termux needs allow-external-apps=true setup
                - FIX_ACCESSIBILITY: if outside app/screen control or Autopilot needs Accessibility enabled
                - FIX_RESET_VOICE: if mic / SpeechRecognizer / TTS is stuck or failing
                - FIX_RETRY_LUNO: if Luno market tickers or charts failed to load
                - FIX_WRITE_SETTINGS: if system brightness adjustment is blocked
                - FIX_STOP_AUTOPILOT: if cross-app autopilot loop is stuck
                - NONE: if only guidance is needed.
                Answer concisely in natural Bahasa Melayu.
                """.trimIndent()
            )
        }
    )

    suspend fun generateSelfRepairPlan(
        activeProfile: AiProviderProfileEntity?,
        userReportOrError: String,
        healthSummary: String
    ): AiSelfRepairPlan {
        val allowedKeys = setOf(
            "FIX_AUTO_FAILOVER_FREE_AI",
            "FIX_SWITCH_GEMINI",
            "FIX_ACCESSIBILITY",
            "FIX_TERMUX_PERMISSION",
            "FIX_TERMUX_SETUP",
            "FIX_WRITE_SETTINGS",
            "FIX_RESET_VOICE",
            "FIX_RETRY_LUNO",
            "FIX_STOP_AUTOPILOT",
            "NONE"
        )
        val prompt = """
            [LIVE APK HEALTH SNAPSHOT]
            $healthSummary

            [REPORTED BUG / ERROR]
            $userReportOrError
        """.trimIndent()

        val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
            val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
            withTimeout(30_000L) {
                firebaseSelfRepairModel(modelId).generateContent(prompt).text
                    ?: throw IllegalStateException("Empty self-repair response.")
            }
        } else {
            val extSys = """
                You are NovaCore APK Self-Healing Doctor AI. Return ONLY raw JSON with keys:
                "diagnosis" (string in Bahasa Melayu), "fixKey" (one of ${allowedKeys.joinToString(", ")}), "repairSteps" (array of short Bahasa Melayu strings).
            """.trimIndent()
            externalClient.generateText(
                profile = activeProfile,
                systemPrompt = extSys,
                userPrompt = prompt,
                expectJson = true
            )
        }

        val json = parseJsonLenient(rawJson)
        val diagnosis = json.optString("diagnosis", "Semakan diagnostik selesai.").ifBlank { rawJson.take(260) }
        val rawKey = json.optString("fixKey", "NONE").trim().uppercase()
        val safeKey = if (rawKey in allowedKeys) rawKey else "NONE"
        val stepsArr = json.optJSONArray("repairSteps")
        val steps = buildList {
            if (stepsArr != null) {
                for (i in 0 until stepsArr.length()) {
                    val s = stepsArr.optString(i).trim()
                    if (s.isNotBlank()) add(s)
                }
            }
        }
        return AiSelfRepairPlan(
            diagnosis = diagnosis,
            fixKey = safeKey,
            repairSteps = steps
        )
    }

    suspend fun diagnoseSystemError(
        activeProfile: AiProviderProfileEntity?,
        module: String,
        errorMessage: String,
        deviceSummary: String
    ): String {
        val prompt = """
            Diagnose this error in the NovaCore Android app (Poco X7 Pro / HyperOS) and give concise, practical steps to fix it in Bahasa Melayu:
            Module: $module
            Error: $errorMessage
            Device Context:
            $deviceSummary
        """.trimIndent()
        return if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
            val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
            withTimeout(40_000L) {
                firebaseTextModel(modelId).generateContent(prompt).text
                    ?: "Tiada cadangan pembaikan diterima."
            }
        } else {
            externalClient.generateText(
                profile = activeProfile,
                systemPrompt = "You are an Android & Termux Diagnostic Engineer. Reply concisely in Bahasa Melayu with exact fix steps.",
                userPrompt = prompt,
                expectJson = false
            )
        }
    }

    suspend fun decideNextAutopilotStep(
        activeProfile: AiProviderProfileEntity?,
        goal: String,
        targetPackage: String,
        stepNumber: Int,
        maxSteps: Int,
        financialSafeMode: Boolean,
        lunoMarketContext: String,
        screenNodesSummary: String,
        stepHistory: List<String>
    ): AutopilotDecision {
        val prompt = buildString {
            appendLine("Target Package: $targetPackage")
            appendLine("Step: $stepNumber / $maxSteps")
            appendLine("Financial Safe Mode (require PAUSE_FOR_CONFIRM before final money/order confirmation): $financialSafeMode")
            appendLine("User Goal: $goal")
            if (lunoMarketContext.isNotBlank()) {
                appendLine("\n[LUNO VETERAN CONTEXT]\n$lunoMarketContext")
            }
            appendLine("\n[PREVIOUS STEPS]\n${stepHistory.takeLast(5).joinToString(" -> ").ifBlank { "None" }}")
            appendLine("\n[LIVE EXTERNAL SCREEN TREE]\n$screenNodesSummary")
        }

        val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
            val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
            withTimeout(30_000L) {
                firebaseAutopilotModel(modelId).generateContent(prompt).text
                    ?: throw IllegalStateException("Empty autopilot response.")
            }
        } else {
            val extSys = """
                You are NovaCore Cross-App Autopilot. Return ONLY a raw JSON object with keys:
                "thought" (string), "action" (one of ${AutopilotActionType.entries.joinToString(", ") { it.name }}), "param" (string), "speakUpdate" (string in Bahasa Melayu).
                If financialSafeMode=true and next tap would confirm a real-money order/buy/sell/PIN, output action=PAUSE_FOR_CONFIRM.
            """.trimIndent()
            externalClient.generateText(
                profile = activeProfile,
                systemPrompt = extSys,
                userPrompt = prompt,
                expectJson = true
            )
        }

        val json = parseJsonLenient(rawJson)
        val thought = json.optString("thought", "Memeriksa skrin...")
        val actionRaw = json.optString("action", AutopilotActionType.WAIT.name)
        val action = runCatching { AutopilotActionType.valueOf(actionRaw) }.getOrDefault(AutopilotActionType.WAIT)
        val param = json.optString("param", "")
        val speakUpdate = json.optString("speakUpdate", thought)
        return AutopilotDecision(thought = thought, action = action, param = param, speakUpdate = speakUpdate)
    }

    suspend fun generateAndroidApkProject(
        activeProfile: AiProviderProfileEntity?,
        userIdea: String
    ): GeneratedApkBlueprint {
        val prompt = "Build a complete, functional Jetpack Compose Android app for this user request:\n$userIdea"
        val rawJson = if (activeProfile == null || activeProfile.typeEnum() == AiProviderType.FIREBASE_AI_LOGIC) {
            val modelId = activeProfile?.modelName ?: DEFAULT_GEMINI_MODEL
            withTimeout(AI_TIMEOUT_MS) {
                firebaseApkArchitectModel(modelId).generateContent(prompt).text
                    ?: throw IllegalStateException("AI gagal menjana kod projek APK.")
            }
        } else {
            val extSys = """
                Return ONLY a raw JSON object with keys: "appName", "packageSlug", "summary", "mainActivityKt".
                mainActivityKt must be a complete Kotlin Jetpack Compose Material 3 MainActivity using package com.novacore.generated.<packageSlug>.
            """.trimIndent()
            externalClient.generateText(
                profile = activeProfile,
                systemPrompt = extSys,
                userPrompt = prompt,
                expectJson = true
            )
        }

        val json = parseJsonLenient(rawJson)
        val rawAppName = json.optString("appName", "NovaCustomApp").trim().ifBlank { "NovaCustomApp" }
        val slug = json.optString("packageSlug", "customapp")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .ifBlank { "customapp" }
        val pkg = "com.novacore.generated.$slug"
        val summary = json.optString("summary", "Aplikasi Android terjana daripada arahan anda.")
        val rawKt = json.optString("mainActivityKt", "").trim()
        val finalKt = normalizeGeneratedKotlin(rawKt, pkg, rawAppName)

        val manifestXml = buildGeneratedManifest(rawAppName)
        val stringsXml = "<resources>\n    <string name=\"app_name\">$rawAppName</string>\n</resources>\n"
        val gradleKts = buildGeneratedGradle(pkg)
        val buildSh = buildTermuxApkScript(slug, pkg, rawAppName, finalKt, manifestXml, stringsXml, gradleKts)

        return GeneratedApkBlueprint(
            appName = rawAppName,
            packageId = pkg,
            summary = summary,
            mainActivityKt = finalKt,
            manifestXml = manifestXml,
            buildGradleKts = gradleKts,
            stringsXml = stringsXml,
            termuxBuildSh = buildSh
        )
    }

    private fun normalizeGeneratedKotlin(kt: String, pkg: String, appName: String): String {
        if (kt.isNotBlank() && kt.contains("class MainActivity")) {
            val lines = kt.lines().toMutableList()
            if (lines.firstOrNull()?.startsWith("package ") == true) {
                lines[0] = "package $pkg"
            } else {
                lines.add(0, "package $pkg")
            }
            return lines.joinToString("\n")
        }
        return """
            package $pkg

            import android.os.Bundle
            import androidx.activity.ComponentActivity
            import androidx.activity.compose.setContent
            import androidx.compose.foundation.layout.*
            import androidx.compose.material3.*
            import androidx.compose.runtime.*
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp

            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent {
                        MaterialTheme(colorScheme = darkColorScheme()) {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("$appName", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Dijana oleh NovaCore AI Builder")
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()
    }

    private fun buildGeneratedManifest(appName: String): String = """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <uses-permission android:name="android.permission.INTERNET" />
            <application
                android:label="$appName"
                android:theme="@android:style/Theme.Material.NoActionBar">
                <activity
                    android:name=".MainActivity"
                    android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        </manifest>
    """.trimIndent()

    private fun buildGeneratedGradle(pkg: String): String = """
        plugins {
            id("com.android.application") version "8.7.3"
            id("org.jetbrains.kotlin.android") version "2.0.21"
            id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
        }
        android {
            namespace = "$pkg"
            compileSdk = 35
            defaultConfig {
                applicationId = "$pkg"
                minSdk = 24
                targetSdk = 35
                versionCode = 1
                versionName = "1.0"
            }
            buildFeatures { compose = true }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            kotlinOptions { jvmTarget = "17" }
        }
        dependencies {
            implementation(platform("androidx.compose:compose-bom:2025.02.00"))
            implementation("androidx.activity:activity-compose:1.10.0")
            implementation("androidx.compose.material3:material3")
            implementation("androidx.compose.ui:ui")
        }
    """.trimIndent()

    private fun buildTermuxApkScript(
        slug: String,
        pkg: String,
        appName: String,
        mainActivityKt: String,
        manifestXml: String,
        stringsXml: String,
        gradleKts: String
    ): String {
        val pkgDir = pkg.replace('.', '/')
        return buildString {
            appendLine("#!/data/data/com.termux/files/usr/bin/bash")
            appendLine("set -e")
            appendLine("PROJ_DIR=\"\$HOME/novacore_builds/$slug\"")
            appendLine("mkdir -p \"\$PROJ_DIR/app/src/main/java/$pkgDir\"")
            appendLine("mkdir -p \"\$PROJ_DIR/app/src/main/res/values\"")
            appendLine()
            appendLine("cat > \"\$PROJ_DIR/settings.gradle.kts\" << 'EOF_SETTINGS'")
            appendLine("pluginManagement {")
            appendLine("    repositories { google(); mavenCentral(); gradlePluginPortal() }")
            appendLine("}")
            appendLine("dependencyResolutionManagement {")
            appendLine("    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)")
            appendLine("    repositories { google(); mavenCentral() }")
            appendLine("}")
            appendLine("rootProject.name = \"$appName\"")
            appendLine("include(\":app\")")
            appendLine("EOF_SETTINGS")
            appendLine()
            appendLine("cat > \"\$PROJ_DIR/app/build.gradle.kts\" << 'EOF_GRADLE'")
            appendLine(gradleKts.trimEnd())
            appendLine("EOF_GRADLE")
            appendLine()
            appendLine("cat > \"\$PROJ_DIR/app/src/main/AndroidManifest.xml\" << 'EOF_MANIFEST'")
            appendLine(manifestXml.trimEnd())
            appendLine("EOF_MANIFEST")
            appendLine()
            appendLine("cat > \"\$PROJ_DIR/app/src/main/res/values/strings.xml\" << 'EOF_STRINGS'")
            appendLine(stringsXml.trimEnd())
            appendLine("EOF_STRINGS")
            appendLine()
            appendLine("cat > \"\$PROJ_DIR/app/src/main/java/$pkgDir/MainActivity.kt\" << 'EOF_KT'")
            appendLine(mainActivityKt.trimEnd())
            appendLine("EOF_KT")
            appendLine()
            appendLine("echo \"Projek ditulis ke: \$PROJ_DIR\"")
            appendLine("if ! command -v gradle >/dev/null 2>&1; then")
            appendLine("    echo \"Memasang OpenJDK 17 & Gradle di Termux...\"")
            appendLine("    pkg update -y && pkg install -y openjdk-17 gradle wget unzip")
            appendLine("fi")
            appendLine("if [ -z \"\$ANDROID_HOME\" ] && [ -d \"\$HOME/android-sdk\" ]; then")
            appendLine("    export ANDROID_HOME=\"\$HOME/android-sdk\"")
            appendLine("fi")
            appendLine("if [ -z \"\$ANDROID_HOME\" ]; then")
            appendLine("    echo \"SILA TETAPKAN ANDROID_HOME dahulu di Termux (atau pasang android-sdk di ~/android-sdk), kemudian jalankan:\"")
            appendLine("    echo \"cd \$PROJ_DIR && gradle assembleDebug\"")
            appendLine("    exit 0")
            appendLine("fi")
            appendLine("cd \"\$PROJ_DIR\"")
            appendLine("gradle assembleDebug")
            appendLine("OUT_APK=\"\$PROJ_DIR/app/build/outputs/apk/debug/app-debug.apk\"")
            appendLine("if [ -f \"\$OUT_APK\" ]; then")
            appendLine("    mkdir -p /sdcard/Download")
            appendLine("    cp \"\$OUT_APK\" \"/sdcard/Download/$slug-debug.apk\"")
            appendLine("    echo \"BERJAYA! APK disimpan di /sdcard/Download/$slug-debug.apk\"")
            appendLine("fi")
        }
    }

    private fun parseJsonLenient(raw: String): JSONObject {
        val trimmed = raw.trim()
        return runCatching { JSONObject(trimmed) }.getOrElse {
            val start = trimmed.indexOf('{')
            val end = trimmed.lastIndexOf('}')
            if (start >= 0 && end > start) {
                runCatching { JSONObject(trimmed.substring(start, end + 1)) }
                    .getOrElse { JSONObject().put("reply", trimmed).put("action", DeviceAction.NONE.name) }
            } else {
                JSONObject().put("reply", trimmed).put("action", DeviceAction.NONE.name)
            }
        }
    }
}
