package com.example.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.view.KeyEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class DeviceTelemetry(
    val deviceModel: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val abis: String,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryTempC: Float,
    val batteryVoltageMv: Int,
    val ramTotalMb: Long,
    val ramAvailableMb: Long,
    val storageTotalGb: Double,
    val storageFreeGb: Double,
    val networkTransport: String,
    val hasInternet: Boolean,
    val sensorCount: Int,
    val sensorNames: List<String>
) {
    fun toPromptSummary(): String = buildString {
        appendLine("Device: $manufacturer $deviceModel (Android $androidVersion, SDK $sdkInt, ABI: $abis)")
        appendLine("Battery: $batteryPercent% (${if (isCharging) "Charging" else "Discharging"}), Temp: ${batteryTempC}C, ${batteryVoltageMv}mV")
        appendLine("RAM: ${ramTotalMb - ramAvailableMb}MB used / ${ramTotalMb}MB total (${ramAvailableMb}MB free)")
        appendLine("Storage: ${"%.1f".format(storageTotalGb - storageFreeGb)}GB used / ${"%.1f".format(storageTotalGb)}GB total (${"%.1f".format(storageFreeGb)}GB free)")
        appendLine("Network: $networkTransport (Internet: $hasInternet)")
        appendLine("Hardware Sensors: $sensorCount detected")
    }
}

data class LiveSensorReading(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val lightLux: Float? = null
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val isGame: Boolean
)

class DeviceTelemetryManager(private val context: Context) {

    private var torchEnabled = false

    fun collectSnapshot(): DeviceTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024L * 1024L)
        val availRamMb = memInfo.availMem / (1024L * 1024L)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.totalBytes.toDouble()
        val freeBytes = statFs.availableBytes.toDouble()
        val gbDivisor = 1024.0 * 1024.0 * 1024.0

        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connManager.activeNetwork
        val caps = connManager.getNetworkCapabilities(activeNetwork)
        val transport = when {
            caps == null -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Connected"
        }
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        return DeviceTelemetry(
            deviceModel = Build.MODEL.orEmpty(),
            manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() },
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            abis = Build.SUPPORTED_ABIS.joinToString(", "),
            batteryPercent = pct,
            isCharging = isCharging,
            batteryTempC = tempTenths / 10f,
            batteryVoltageMv = voltage,
            ramTotalMb = totalRamMb,
            ramAvailableMb = availRamMb,
            storageTotalGb = totalBytes / gbDivisor,
            storageFreeGb = freeBytes / gbDivisor,
            networkTransport = transport,
            hasInternet = hasInternet,
            sensorCount = sensors.size,
            sensorNames = sensors.map { it.name }.distinct().take(12)
        )
    }

    fun observeLiveSensors(): Flow<LiveSensorReading> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        var current = LiveSensorReading()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        current = current.copy(
                            accelX = event.values.getOrElse(0) { 0f },
                            accelY = event.values.getOrElse(1) { 0f },
                            accelZ = event.values.getOrElse(2) { 0f }
                        )
                        trySend(current)
                    }
                    Sensor.TYPE_LIGHT -> {
                        current = current.copy(lightLux = event.values.getOrElse(0) { 0f })
                        trySend(current)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    fun setTorch(enable: Boolean): Result<Boolean> {
        return runCatching {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && lensFacing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: error("Tiada lampu suluh (flash) dikesan pada peranti ini.")

            cameraManager.setTorchMode(cameraId, enable)
            torchEnabled = enable
            enable
        }
    }

    fun isTorchEnabled(): Boolean = torchEnabled

    // --- System-Wide Hardware & OS Controls ---

    fun adjustMediaVolume(direction: Int) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun dispatchMediaKey(keyCode: Int) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    fun canWriteSystemSettings(): Boolean = Settings.System.canWrite(context)

    fun adjustBrightness(delta: Int): Result<Int> {
        if (!canWriteSystemSettings()) {
            return Result.failure(IllegalStateException("Kebenaran Ubah Tetapan Sistem diperlukan untuk kawal kecerahan."))
        }
        return runCatching {
            val current = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            val updated = (current + delta).coerceIn(15, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                updated
            )
            updated
        }
    }

    fun openWriteSettingsScreen() {
        runCatching {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openAccessibilitySettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openAppInfoSettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun setTimer(seconds: Int, label: String = "NovaCore Timer"): Boolean {
        return runCatching {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds.coerceAtLeast(1))
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun dialPhoneNumber(number: String): Boolean {
        val clean = number.trim()
        if (clean.isEmpty()) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$clean")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun webSearch(query: String): Boolean {
        val clean = query.trim()
        if (clean.isEmpty()) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", clean)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun getInstalledLauncherApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == context.packageName) return@mapNotNull null
            val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
            val appInfo = info.activityInfo?.applicationInfo
            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                    (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            } else {
                false
            }
            InstalledAppInfo(appName = label, packageName = pkg, isGame = isGame)
        }.distinctBy { it.packageName }
            .sortedWith(compareByDescending<InstalledAppInfo> { it.isGame }.thenBy { it.appName.lowercase() })
    }

    fun launchAppByQuery(query: String): Result<String> {
        val apps = getInstalledLauncherApps()
        val normalized = query.trim().lowercase()
        val match = apps.firstOrNull {
            it.appName.lowercase() == normalized || it.packageName.lowercase() == normalized
        } ?: apps.firstOrNull {
            it.appName.lowercase().contains(normalized) || it.packageName.lowercase().contains(normalized)
        } ?: return Result.failure(IllegalStateException("Aplikasi '$query' tidak ditemui pada peranti."))

        return launchPackage(match.packageName).map { match.appName }
    }

    fun launchPackage(packageName: String): Result<Unit> {
        return runCatching {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: error("Tidak dapat membuka pakej $packageName.")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }

    fun openSystemSettings(panel: String): Boolean {
        val action = when (panel) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "BATTERY" -> Intent.ACTION_POWER_USAGE_SUMMARY
            else -> Settings.ACTION_SETTINGS
        }
        return runCatching {
            val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
