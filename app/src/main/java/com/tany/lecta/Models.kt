package com.tany.lecta

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

enum class Priority(
    val label: String,
    val color: Color,
    val textColor: Color
) {
    Critical("Critical", Color(0xFFFF2C2C), Color.White),
    High("High", Color(0xFFFF9500), Color.White),
    Medium("Medium", Color(0xFFFFD60A), Color.Black),
    Low("Low", Color(0xFF34C759), Color.White)
}

data class LectaTask(
    val id: Int,
    val title: String,
    val source: String,
    val priority: Priority,
    val startDate: LocalDate,
    val endDate: LocalDate = startDate,
    val confidence: Int,
    val extracted: String,
    val done: Boolean = false,
    val manual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

const val TASK_TITLE_MAX = 60

const val NAME_MAX = 24

const val NOTICE_TEXT_MAX = 150

const val HOLD_TO_DELETE_MS = 1000L

data class LectaNotice(
    val id: Int,
    val text: String,
    val date: LocalDate
)
