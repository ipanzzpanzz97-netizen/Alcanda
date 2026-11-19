package com.example.device

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ComponentName
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NovaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
        _isConnected.value = false
    }

    fun lockPhoneScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    fun openPowerMenu(): Boolean = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)

    fun goHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun goBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun openRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    fun openQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

    fun takeSystemScreenshot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    fun getActiveWindowPackage(): String =
        rootInActiveWindow?.packageName?.toString().orEmpty()

    fun clickNodeByText(query: String): Boolean {
        val target = query.trim()
        if (target.isEmpty()) return false
        val root = rootInActiveWindow ?: return false
        val matches = root.findAccessibilityNodeInfosByText(target)
        if (!matches.isNullOrEmpty()) {
            for (node in matches) {
                if (clickNodeOrParent(node)) return true
            }
            // Gesture fallback on first matched node bounds (important for custom/Flutter views like Luno)
            val rect = android.graphics.Rect()
            matches.first().getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                return dispatchTapAtPixels(rect.centerX().toFloat(), rect.centerY().toFloat())
            }
        }
        return findAndClickByDescription(root, target)
    }

    fun dispatchTapAtPixels(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x.coerceAtLeast(1f), y.coerceAtLeast(1f)) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 60L))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun scrollActiveWindow(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        if (tryScrollRecursive(root, forward)) return true
        // Gesture fallback
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val startY = if (forward) dm.heightPixels * 0.72f else dm.heightPixels * 0.30f
        val endY = if (forward) dm.heightPixels * 0.28f else dm.heightPixels * 0.75f
        return dispatchSwipe(x, startY, x, endY, 280L)
    }

    fun dispatchTapAtPercent(xPercent: Float, yPercent: Float): Boolean {
        val dm = resources.displayMetrics
        val x = (dm.widthPixels * xPercent.coerceIn(0.05f, 0.95f))
        val y = (dm.heightPixels * yPercent.coerceIn(0.05f, 0.95f))
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 60L))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun dispatchSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMs))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun readVisibleScreenSummary(maxItems: Int = 25): String {
        val root = rootInActiveWindow ?: return ""
        val collected = LinkedHashSet<String>()
        collectVisibleText(root, collected, maxItems)
        return collected.joinToString(" | ")
    }

    fun readDetailedScreenNodes(maxNodes: Int = 30): String {
        val root = rootInActiveWindow ?: return "Tiada tetingkap aktif dikesan."
        val pkg = root.packageName?.toString().orEmpty()
        val dm = resources.displayMetrics
        val width = dm.widthPixels.coerceAtLeast(1).toFloat()
        val height = dm.heightPixels.coerceAtLeast(1).toFloat()
        val items = ArrayList<String>()
        collectDetailedNodes(root, items, maxNodes, width, height)
        return buildString {
            appendLine("ActivePackage: $pkg")
            if (items.isEmpty()) {
                append("Tiada elemen interaktif ditemui.")
            } else {
                items.forEach { appendLine(it) }
            }
        }
    }

    fun typeTextIntoFocusedOrFirstInput(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            return setNodeText(focused, text)
        }
        val editable = findFirstEditableNode(root)
        return if (editable != null) {
            setNodeText(editable, text)
        } else {
            false
        }
    }

    fun swipeDirection(direction: String): Boolean {
        val dm = resources.displayMetrics
        val w = dm.widthPixels.toFloat()
        val h = dm.heightPixels.toFloat()
        return when (direction.trim().uppercase()) {
            "UP" -> dispatchSwipe(w * 0.5f, h * 0.72f, w * 0.5f, h * 0.28f, 260L)
            "DOWN" -> dispatchSwipe(w * 0.5f, h * 0.28f, w * 0.5f, h * 0.72f, 260L)
            "LEFT" -> dispatchSwipe(w * 0.80f, h * 0.5f, w * 0.20f, h * 0.5f, 260L)
            "RIGHT" -> dispatchSwipe(w * 0.20f, h * 0.5f, w * 0.80f, h * 0.5f, 260L)
            else -> false
        }
    }

    private fun setNodeText(node: AccessibilityNodeInfo, text: String): Boolean {
        val args = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun findFirstEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable && node.isEnabled) return node
        for (i in 0 until node.childCount) {
            val found = findFirstEditableNode(node.getChild(i))
            if (found != null) return found
        }
        return null
    }

    private fun collectDetailedNodes(
        node: AccessibilityNodeInfo?,
        out: MutableList<String>,
        limit: Int,
        screenW: Float,
        screenH: Float
    ) {
        if (node == null || out.size >= limit) return
        val txt = node.text?.toString()?.trim().orEmpty()
        val desc = node.contentDescription?.toString()?.trim().orEmpty()
        val label = txt.ifEmpty { desc }
        if (label.isNotEmpty() || node.isClickable || node.isEditable) {
            val rect = android.graphics.Rect()
            node.getBoundsInScreen(rect)
            val xPct = (((rect.centerX()) / screenW) * 100f).toInt().coerceIn(0, 100)
            val yPct = (((rect.centerY()) / screenH) * 100f).toInt().coerceIn(0, 100)
            val flags = buildList {
                if (node.isClickable) add("CLICK")
                if (node.isEditable) add("INPUT")
                if (node.isScrollable) add("SCROLL")
            }.joinToString(",")
            val display = label.take(40).ifEmpty { node.className?.toString()?.substringAfterLast('.').orEmpty() }
            out.add("[$display] (${xPct}%,${yPct}%) $flags".trim())
        }
        for (i in 0 until node.childCount) {
            collectDetailedNodes(node.getChild(i), out, limit, screenW, screenH)
            if (out.size >= limit) break
        }
    }

    private fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var curr = node
        var depth = 0
        while (curr != null && depth < 6) {
            if (curr.isClickable && curr.isEnabled) {
                return curr.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            curr = curr.parent
            depth++
        }
        return false
    }

    private fun findAndClickByDescription(node: AccessibilityNodeInfo?, query: String): Boolean {
        if (node == null) return false
        val text = node.text?.toString().orEmpty()
        val desc = node.contentDescription?.toString().orEmpty()
        if (text.contains(query, ignoreCase = true) || desc.contains(query, ignoreCase = true)) {
            if (clickNodeOrParent(node)) return true
        }
        for (i in 0 until node.childCount) {
            if (findAndClickByDescription(node.getChild(i), query)) return true
        }
        return false
    }

    private fun tryScrollRecursive(node: AccessibilityNodeInfo?, forward: Boolean): Boolean {
        if (node == null) return false
        if (node.isScrollable) {
            val action = if (forward) {
                AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            } else {
                AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            }
            if (node.performAction(action)) return true
        }
        for (i in 0 until node.childCount) {
            if (tryScrollRecursive(node.getChild(i), forward)) return true
        }
        return false
    }

    private fun collectVisibleText(
        node: AccessibilityNodeInfo?,
        out: MutableSet<String>,
        limit: Int
    ) {
        if (node == null || out.size >= limit) return
        val txt = node.text?.toString()?.trim().orEmpty()
        val desc = node.contentDescription?.toString()?.trim().orEmpty()
        if (txt.isNotEmpty()) out.add(txt.take(60))
        else if (desc.isNotEmpty()) out.add(desc.take(60))
        for (i in 0 until node.childCount) {
            collectVisibleText(node.getChild(i), out, limit)
            if (out.size >= limit) break
        }
    }

    companion object {
        @Volatile
        var instance: NovaAccessibilityService? = null
            private set

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        fun isEnabledInSettings(context: Context): Boolean {
            val expected = ComponentName(context, NovaAccessibilityService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).orEmpty()
            return enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
        }
    }
}
