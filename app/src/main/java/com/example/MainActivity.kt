package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ai.configureAppCheck
import com.example.ui.NovaApp
import com.example.ui.theme.NovaCoreTheme

private const val ACTION_VOICE_COMMAND = "com.aistudio.novacore.ACTION_VOICE_COMMAND"

class MainActivity : ComponentActivity() {

    private var launchVoiceOnStart by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check before any Compose UI, ViewModel, or Firebase call.
        configureAppCheck(this, intent)

        launchVoiceOnStart = intent?.action == ACTION_VOICE_COMMAND

        enableEdgeToEdge()
        setContent {
            NovaCoreTheme {
                NovaApp(autoTriggerVoice = launchVoiceOnStart)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_VOICE_COMMAND) {
            launchVoiceOnStart = true
        }
    }
}
