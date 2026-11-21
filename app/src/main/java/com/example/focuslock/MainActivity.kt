package com.example.focuslock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.*

class MainActivity : Activity() {
    private lateinit var a11y: Button
    private val waits = intArrayOf(0, 10, 30, 60)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val allowed = Store.allowed(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 60, 40, 40) }
        root.addView(TextView(this).apply { text = "Focus Lock"; textSize = 28f })

        a11y = Button(this).apply {
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        root.addView(a11y)

        val minutes = EditText(this).apply {
            hint = "Minutes (e.g. 30)"; setText("30"); inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(minutes)
        root.addView(Button(this).apply {
            text = "Start focus session"
            setOnClickListener {
                val m = minutes.text.toString().toIntOrNull() ?: 0
                when {
                    !enabled() -> toast("Enable the Focus Lock accessibility service first")
                    m !in 1..480 -> toast("Choose 1 to 480 minutes")
                    else -> { Store.start(this@MainActivity, m); startActivity(Intent(this@MainActivity, LockActivity::class.java)) }
                }
            }
        })
        root.addView(TextView(this).apply {
            text = "Allowed apps (tap the wait button to set a delay before opening):"; setPadding(0, 32, 0, 8)
        })

        val apps = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
            .filter { it.first != packageName }.distinctBy { it.first }.sortedBy { it.second.lowercase() }
        for ((pkg, label) in apps) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val wait = Button(this).apply { text = "${allowed[pkg] ?: 0}s" }
            val cb = CheckBox(this).apply {
                text = label; isChecked = allowed.containsKey(pkg)
                setOnCheckedChangeListener { _, on ->
                    if (on) allowed[pkg] = allowed[pkg] ?: 0 else allowed.remove(pkg)
                    Store.saveAllowed(this@MainActivity, allowed)
                }
            }
            wait.setOnClickListener {
                val next = waits[(waits.indexOf(allowed[pkg] ?: 0) + 1) % waits.size]
                wait.text = "${next}s"
                if (cb.isChecked) { allowed[pkg] = next; Store.saveAllowed(this, allowed) }
                else { allowed[pkg] = next; cb.isChecked = true }
            }
            row.addView(cb, LinearLayout.LayoutParams(0, -2, 1f)); row.addView(wait)
            root.addView(row)
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        if (Store.isLocked(this)) { startActivity(Intent(this, LockActivity::class.java)); return }
        a11y.text = if (enabled()) "Accessibility service: ON" else "Step 1: Enable accessibility service"
    }

    private fun enabled() = (Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
        .contains("$packageName/")
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
