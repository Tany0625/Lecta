package com.tany.lecta

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

fun greetingFor(hour: Int): String = when {
    hour < 12 -> "Good morning"
    hour < 17 -> "Good afternoon"
    else -> "Good evening"
}

fun dueText(days: Long): String = when {
    days < 0 -> "overdue"
    days == 0L -> "today"
    days == 1L -> "tomorrow"
    else -> "in $days days"
}

fun progressColor(fraction: Float): Color = when {
    fraction >= 1f -> Color(0xFF34C759)
    fraction >= 0.75f -> Color(0xFF9ACD32)
    fraction >= 0.5f -> Color(0xFFFFD60A)
    fraction >= 0.25f -> Color(0xFFFF9500)
    else -> Color(0xFFFF3B30)
}

@Composable
fun GreetingSection(
    tasks: List<LectaTask>,
    modifier: Modifier = Modifier,
    userName: String = ProfileStore.DEFAULT_NAME,
    showSummary: Boolean = true
) {
    val today = LocalDate.now()
    val total = tasks.size
    val doneCount = tasks.count { it.done }
    val pending = tasks.filter { !it.done }
    val criticals = pending.filter { it.priority == Priority.Critical }
    val nextCritical = criticals.minByOrNull { it.startDate }

    val summary = when {
        total == 0 -> "No tasks yet - tap + to add one"
        pending.isEmpty() -> "All tasks done - nice work!"
        else -> {
            val base = "${pending.size} ${if (pending.size == 1) "task" else "tasks"} pending"
            if (nextCritical != null) {
                val days = ChronoUnit.DAYS.between(today, nextCritical.startDate)
                base + " · ${criticals.size} critical, next due ${dueText(days)}"
            } else base
        }
    }

    val fraction = if (total == 0) 0f else doneCount.toFloat() / total
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progressFraction"
    )
    val barColor by animateColorAsState(
        targetValue = progressColor(fraction),
        animationSpec = tween(600),
        label = "progressColor"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${greetingFor(LocalTime.now().hour)}, $userName",
            fontSize = 19.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            color = lectaColors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (showSummary) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = lectaColors.text.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$doneCount of $total done · ${(fraction * 100).toInt()}%",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = lectaColors.text.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(lectaColors.surface)
                .border(0.5.dp, lectaColors.text.copy(alpha = 0.35f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}
