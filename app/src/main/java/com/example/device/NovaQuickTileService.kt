package com.example.device

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.MainActivity

class NovaQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val running = AutopilotRuntime.state.value.isRunning
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = if (running) "Henti Autopilot" else "NovaCore AI"
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        if (AutopilotRuntime.state.value.isRunning) {
            AutopilotRuntime.requestStop()
            qsTile?.apply {
                state = Tile.STATE_ACTIVE
                label = "NovaCore AI"
                updateTile()
            }
            return
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "com.aistudio.novacore.ACTION_VOICE_COMMAND"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                this,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
