package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.FreeAiPresets
import com.example.data.GeneratedAppProjectEntity
import com.example.data.NovaDatabase
import com.example.data.SystemErrorEntity
import com.example.device.TermuxBridge
import com.example.ui.screens.RepairScreen
import com.example.ui.theme.NovaCoreTheme
import com.example.viewmodel.HealthCheckStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun readAppNameMatchesNovaCore() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NovaCore", appName)
    }

    @Test
    fun termuxBridgeSafelyHandlesMissingTermuxWithoutCrashing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bridge = TermuxBridge(context)
        assertFalse(bridge.hasRunCommandPermission())
        val dispatch = bridge.runInTermux("echo hello")
        assertFalse(dispatch.dispatchedViaService)
        assertFalse(dispatch.termuxInstalled)
        assertTrue(dispatch.message.contains("papan klip", ignoreCase = true))
    }

    @Test
    fun roomDatabaseV5StoresAiProfilesErrorsAndGeneratedApkProjects() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, NovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val aiDao = db.aiProfileDao()
            aiDao.insertAll(FreeAiPresets.defaultProfiles())
            val profiles = aiDao.getAllOnce()
            assertTrue(profiles.size >= 10)
            assertEquals(1, profiles.count { it.isActive })

            val errorDao = db.systemErrorDao()
            val errId = errorDao.insert(
                SystemErrorEntity(
                    module = "AI_API",
                    message = "App Check test error",
                    suggestedFixKey = "FIX_AUTO_FAILOVER_FREE_AI"
                )
            )
            assertTrue(errId > 0)

            val appDao = db.generatedAppDao()
            val projId = appDao.insert(
                GeneratedAppProjectEntity(
                    appName = "BudgetMini",
                    packageId = "com.novacore.generated.budgetmini",
                    prompt = "Budget tracker",
                    summary = "Simple budget app",
                    mainActivityKt = "class MainActivity",
                    manifestXml = "<manifest/>",
                    buildGradleKts = "plugins {}",
                    stringsXml = "<resources/>",
                    termuxBuildSh = "#!/bin/bash"
                )
            )
            assertTrue(projId > 0)
        } finally {
            db.close()
        }
    }

    @Test
    fun repairScreenRendersSelfHealingControlsAndHealthMatrix() {
        val fakeHealth = HealthCheckStatus(
            internetOk = true,
            aiEngineOk = false,
            aiEngineNote = "App Check token needed",
            accessibilityOk = false,
            termuxOk = true,
            termuxPermissionOk = false,
            voiceOk = true,
            roomDbOk = true,
            lunoApiOk = true,
            lunoApiNote = "OK (8 pasangan MYR aktif)",
            lastHealReport = "Siap diperbaiki automatik."
        )

        composeTestRule.setContent {
            NovaCoreTheme {
                RepairScreen(
                    healthCheck = fakeHealth,
                    errors = listOf(
                        SystemErrorEntity(
                            id = 1L,
                            module = "AI_API",
                            message = "Sambungan AI gagal",
                            suggestedFixKey = "FIX_AUTO_FAILOVER_FREE_AI"
                        )
                    ),
                    diagnosingErrorId = null,
                    onRunDiagnostics = {},
                    onRunAutoHealAll = {},
                    onRunAiRepairIssue = {},
                    onApplyQuickFixKey = {},
                    onApplyAutoFix = {},
                    onAskAiDiagnose = {},
                    onResolveError = {},
                    onClearResolved = {},
                    onClearAll = {},
                    onOpenAccessibility = {},
                    onOpenAiModels = {},
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ai_auto_heal_all_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("repair_issue_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_diagnose_repair_button").assertIsDisplayed()
    }
}
