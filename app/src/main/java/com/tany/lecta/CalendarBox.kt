package com.tany.lecta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalTextApi::class)
val centeredTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

@Composable
fun CalendarBox(
    tasks: List<LectaTask>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val month = YearMonth.from(today)

    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
    val daysInMonth = month.lengthOfMonth()

    val previousMonth = month.minusMonths(1)
    val previousMonthDays = previousMonth.lengthOfMonth()

    val weeks = (firstDayOffset + daysInMonth + 6) / 7

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(0.2.dp, Color.Black, RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = month.month.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = Color.Black,
                style = centeredTextStyle
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 7.sp,
                        lineHeight = 8.sp,
                        color = Color.Black,
                        style = centeredTextStyle
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (row in 0 until weeks) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        for (column in 0 until 7) {
                            val dayNumber = row * 7 + column - firstDayOffset + 1

                            val date: LocalDate
                            val isCurrentMonth: Boolean

                            if (dayNumber < 1) {
                                date = previousMonth.atDay(previousMonthDays + dayNumber)
                                isCurrentMonth = false
                            } else if (dayNumber > daysInMonth) {
                                date = month.plusMonths(1).atDay(dayNumber - daysInMonth)
                                isCurrentMonth = false
                            } else {
                                date = month.atDay(dayNumber)
                                isCurrentMonth = true
                            }

                            val isToday = date == today

                            val task = tasks
                                .filter { date >= it.startDate && date <= it.endDate }
                                .minByOrNull { it.priority.ordinal }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (task != null) {
                                    if (task.startDate == task.endDate) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight(0.9f)
                                                .aspectRatio(1f)
                                                .background(task.priority.color, CircleShape)
                                        )
                                    } else {
                                        val roundLeft = date == task.startDate || column == 0
                                        val roundRight = date == task.endDate || column == 6
                                        val shape = RoundedCornerShape(
                                            topStartPercent = if (roundLeft) 50 else 0,
                                            bottomStartPercent = if (roundLeft) 50 else 0,
                                            topEndPercent = if (roundRight) 50 else 0,
                                            bottomEndPercent = if (roundRight) 50 else 0
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(0.9f)
                                                .background(task.priority.color, shape)
                                        )
                                    }
                                }

                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(0.9f)
                                            .aspectRatio(1f)
                                            .background(Color(0xFF007AFF), CircleShape)
                                    )
                                }

                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 8.sp,
                                    lineHeight = 8.sp,
                                    style = centeredTextStyle,
                                    color = when {
                                        isToday -> Color.White
                                        task != null -> task.priority.textColor
                                        isCurrentMonth -> Color.Black
                                        else -> Color.Black.copy(alpha = 0.3f)
                                    },
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
