package com.example.focuslock

import android.content.Context

object Store {
    const val PASS_MS = 10 * 60 * 1000L // how long a wait-unlocked app stays open
    private fun p(c: Context) = c.getSharedPreferences("focus", Context.MODE_PRIVATE)

    fun endTime(c: Context) = p(c).getLong("end", 0)
    fun isLocked(c: Context) = System.currentTimeMillis() < endTime(c)
    fun start(c: Context, minutes: Int) =
        p(c).edit().putLong("end", System.currentTimeMillis() + minutes * 60_000L).apply()

    /** package -> seconds the user must wait before the app opens (0 = instant) */
    fun allowed(c: Context): MutableMap<String, Int> =
        (p(c).getStringSet("allowed", emptySet()) ?: emptySet()).mapNotNull {
            val i = it.lastIndexOf(':'); if (i < 0) null else it.substring(0, i) to (it.substring(i + 1).toIntOrNull() ?: 0)
        }.toMap().toMutableMap()

    fun saveAllowed(c: Context, m: Map<String, Int>) =
        p(c).edit().putStringSet("allowed", m.map { "${it.key}:${it.value}" }.toSet()).apply()

    fun grantPass(c: Context, pkg: String) =
        p(c).edit().putLong("pass_$pkg", System.currentTimeMillis() + PASS_MS).apply()
    fun hasPass(c: Context, pkg: String) = System.currentTimeMillis() < p(c).getLong("pass_$pkg", 0)
}
