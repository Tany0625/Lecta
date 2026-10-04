package com.tany.lecta

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppSettings {
    private const val PREFS = "lecta_prefs"

    var themeId by mutableStateOf("classic")
    var cleanupHours by mutableStateOf(24)
    var haptics by mutableStateOf(true)
    var sundayStart by mutableStateOf(false)
    var reminders by mutableStateOf(true)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        themeId = prefs.getString("theme", "classic") ?: "classic"
        cleanupHours = prefs.getInt("cleanup_hours", 24)
        haptics = prefs.getBoolean("haptics", true)
        sundayStart = prefs.getBoolean("sunday_start", false)
        reminders = prefs.getBoolean("reminders", true)
        applyCleanup()
    }

    private fun save(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("theme", themeId)
            .putInt("cleanup_hours", cleanupHours)
            .putBoolean("haptics", haptics)
            .putBoolean("sunday_start", sundayStart)
            .putBoolean("reminders", reminders)
            .apply()
    }

    private fun applyCleanup() {
        TaskStore.expiryMs = if (cleanupHours <= 0) Long.MAX_VALUE else cleanupHours * 3_600_000L
    }

    fun setTheme(context: Context, id: String) {
        themeId = id
        save(context)
    }

    fun setCleanupHours(context: Context, hours: Int) {
        cleanupHours = hours
        applyCleanup()
        save(context)
    }

    fun setHaptics(context: Context, enabled: Boolean) {
        haptics = enabled
        save(context)
    }

    fun setReminders(context: Context, enabled: Boolean) {
        reminders = enabled
        save(context)
        ReminderScheduler.sync(context, TaskStore.load(context))
    }

    fun setSundayStart(context: Context, enabled: Boolean) {
        sundayStart = enabled
        save(context)
    }
}
