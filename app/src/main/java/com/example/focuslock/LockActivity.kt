package com.example.focuslock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*

class LockActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var timer: TextView
    private lateinit var status: TextView
    private var pendingPkg: String? = null
    private var pendingEnd = 0L

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(48, 96, 48, 48)
            setBackgroundColor(0xFF111827.toInt()); gravity = Gravity.CENTER_HORIZONTAL
        }
        fun tv(size: Float) = TextView(this).apply { textSize = size; setTextColor(0xFFFFFFFF.toInt()); gravity = Gravity.CENTER }
        root.addView(tv(18f).apply { text = "Stay focused" })
        timer = tv(48f); root.addView(timer)
        status = tv(16f).apply { setPadding(0, 24, 0, 24) }; root.addView(status)

        val pm = packageManager
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val allowed = Store.allowed(this)
        if (allowed.isEmpty()) list.addView(tv(16f).apply { text = "No apps allowed this session." })
        for ((pkg, wait) in allowed) {
            val label = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch (e: Exception) { continue }
            list.addView(Button(this).apply {
                text = if (wait > 0) "$label  (wait ${wait}s)" else label
                setOnClickListener { open(pkg, label, wait) }
            })
        }
        root.addView(ScrollView(this).apply { addView(list) })
        setContentView(root)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun open(pkg: String, label: String, wait: Int) {
        if (wait == 0 || Store.hasPass(this, pkg)) { launch(pkg); return }
        pendingPkg = pkg; pendingEnd = System.currentTimeMillis() + wait * 1000L
        status.text = "Opening $label in ${wait}s..."
    }

    private fun launch(pkg: String) {
        pendingPkg = null; status.text = ""
        packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!Store.isLocked(this@LockActivity)) { finish(); return }
            val left = (Store.endTime(this@LockActivity) - System.currentTimeMillis()) / 1000
            timer.text = "%02d:%02d:%02d".format(left / 3600, left % 3600 / 60, left % 60)
            pendingPkg?.let {
                val s = (pendingEnd - System.currentTimeMillis() + 999) / 1000
                if (s <= 0) { Store.grantPass(this@LockActivity, it); launch(it) }
                else status.text = "Opening in ${s}s... put the phone down."
            }
            h.postDelayed(this, 500)
        }
    }

    override fun onResume() { super.onResume(); h.post(tick) }
    override fun onPause() { super.onPause(); h.removeCallbacks(tick) }
    @Deprecated("locked") override fun onBackPressed() { if (!Store.isLocked(this)) super.onBackPressed() }
}
