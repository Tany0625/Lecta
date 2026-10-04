package com.tany.lecta

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.ZoneId

object ReminderScheduler {
    const val CHANNEL_ID = "lecta_reminders"
    const val DEADLINE_HOUR = 21
    val OFFSET_HOURS = listOf(48, 24, 12, 3)
    private const val PREFS = "lecta_prefs"
    private const val KEY_SCHEDULED = "reminder_ids"
    private const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders before your task deadlines"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun deadlineMillis(task: LectaTask): Long {
        return task.endDate
            .atTime(DEADLINE_HOUR, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun requestCode(taskId: Int, index: Int): Int = taskId * 10 + index

    private fun alarmIntent(context: Context, taskId: Int, index: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("hours", OFFSET_HOURS[index])
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(taskId, index),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelTask(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        OFFSET_HOURS.indices.forEach { index ->
            val pending = alarmIntent(context, taskId, index)
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    private fun scheduleTask(context: Context, task: LectaTask) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val deadline = deadlineMillis(task)
        val now = System.currentTimeMillis()
        OFFSET_HOURS.forEachIndexed { index, hours ->
            val fireAt = deadline - hours * 3_600_000L
            val pending = alarmIntent(context, task.id, index)
            if (fireAt > now) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pending)
            } else {
                alarmManager.cancel(pending)
            }
        }
    }

    fun sync(context: Context, tasks: List<LectaTask>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = prefs.getStringSet(KEY_SCHEDULED, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

        val active = if (AppSettings.reminders) tasks.filter { !it.done } else emptyList()
        val activeIds = active.map { it.id }.toSet()

        (previous - activeIds).forEach { cancelTask(context, it) }
        active.forEach { scheduleTask(context, it) }

        prefs.edit()
            .putStringSet(KEY_SCHEDULED, activeIds.map { it.toString() }.toSet())
            .apply()
    }

    fun show(context: Context, task: LectaTask, hours: Int) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, NOTIFICATION_PERMISSION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val index = OFFSET_HOURS.indexOf(hours).coerceAtLeast(0)
        val body = "${task.title}\n${task.priority.label} priority"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Due in $hours hours")
            .setContentText(task.title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(requestCode(task.id, index), notification)
        } catch (e: SecurityException) {
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppSettings.load(context)
        if (!AppSettings.reminders) return

        val taskId = intent.getIntExtra("task_id", -1)
        val hours = intent.getIntExtra("hours", 0)

        val task = TaskStore.load(context).firstOrNull { it.id == taskId } ?: return
        if (task.done) return

        ReminderScheduler.show(context, task, hours)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AppSettings.load(context)
            ReminderScheduler.ensureChannel(context)
            ReminderScheduler.sync(context, TaskStore.load(context))
        }
    }
}
