package com.example.focuslock

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager

class BlockerService : AccessibilityService() {
    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!Store.isLocked(this)) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || isSafe(pkg)) return
        val wait = Store.allowed(this)[pkg]
        if (wait != null && (wait == 0 || Store.hasPass(this, pkg))) return

        // Not allowed: collapse notification shade / settings, then show the lock screen.
        if (pkg == "com.android.systemui") performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(Intent(this, LockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }

    // Keyboards and the default dialer (emergency calls) are always permitted.
    private fun isSafe(pkg: String): Boolean {
        val ime = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .enabledInputMethodList.any { it.packageName == pkg }
        val dialer = (getSystemService(TELECOM_SERVICE) as TelecomManager).defaultDialerPackage == pkg
        return ime || dialer
    }

    override fun onInterrupt() {}
}
