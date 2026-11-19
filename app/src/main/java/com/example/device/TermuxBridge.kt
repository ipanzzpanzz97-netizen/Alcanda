package com.example.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

data class TermuxDispatchResult(
    val dispatchedViaService: Boolean,
    val termuxInstalled: Boolean,
    val needsPermission: Boolean = false,
    val message: String
)

class TermuxBridge(private val context: Context) {

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val TERMUX_RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
        const val PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"
        const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
        const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
        const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        const val EXTRA_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"

        const val TERMUX_ALLOW_EXTERNAL_CMD =
            "mkdir -p ~/.termux && grep -qxF 'allow-external-apps=true' ~/.termux/termux.properties || echo 'allow-external-apps=true' >> ~/.termux/termux.properties && termux-reload-settings"
    }

    fun isTermuxInstalled(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    TERMUX_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            }
            true
        }.getOrDefault(false)
    }

    fun hasRunCommandPermission(): Boolean {
        if (!isTermuxInstalled()) return false
        return ContextCompat.checkSelfPermission(context, PERMISSION_RUN_COMMAND) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun openTermuxApp(): Boolean {
        return runCatching {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
                ?: return false
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    fun runInTermux(
        script: String,
        workdir: String = "/data/data/com.termux/files/home",
        runInBackground: Boolean = false
    ): TermuxDispatchResult {
        copyToClipboard("NovaCore Termux Script", script)

        if (!isTermuxInstalled()) {
            return TermuxDispatchResult(
                dispatchedViaService = false,
                termuxInstalled = false,
                needsPermission = false,
                message = "Termux tidak dipasang di peranti ini. Skrip telah disalin ke papan klip."
            )
        }

        if (!hasRunCommandPermission()) {
            openTermuxApp()
            return TermuxDispatchResult(
                dispatchedViaService = false,
                termuxInstalled = true,
                needsPermission = true,
                message = "Izin RUN_COMMAND Termux belum diberi. Skrip disalin ke papan klip & Termux dibuka — klik 'Minta Izin Termux' untuk kawalan terus."
            )
        }

        val intent = Intent(ACTION_RUN_COMMAND).apply {
            component = ComponentName(TERMUX_PACKAGE, TERMUX_RUN_COMMAND_SERVICE)
            putExtra(EXTRA_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash")
            putExtra(EXTRA_ARGUMENTS, arrayOf("-c", script))
            putExtra(EXTRA_WORKDIR, workdir)
            putExtra(EXTRA_BACKGROUND, runInBackground)
            putExtra(EXTRA_SESSION_ACTION, "0")
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            TermuxDispatchResult(
                dispatchedViaService = true,
                termuxInstalled = true,
                needsPermission = false,
                message = "Arahan dihantar terus ke Termux! (Skrip juga disalin ke papan klip sebagai sandaran)."
            )
        } catch (e: SecurityException) {
            openTermuxApp()
            TermuxDispatchResult(
                dispatchedViaService = false,
                termuxInstalled = true,
                needsPermission = false,
                message = "Kebenaran 'allow-external-apps=true' diperlukan dalam Termux. Skrip disalin & Termux dibuka — tampal untuk jalankan."
            )
        } catch (e: Exception) {
            openTermuxApp()
            TermuxDispatchResult(
                dispatchedViaService = false,
                termuxInstalled = true,
                needsPermission = false,
                message = "Termux dibuka & skrip disalin ke papan klip (${e.localizedMessage ?: "tampal terus di terminal"})."
            )
        }
    }
}
