package com.tany.lecta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tany.lecta.ui.theme.LectaBackground
import com.tany.lecta.ui.theme.LectaTaskBox
import com.tany.lecta.ui.theme.LectaTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


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
    val done: Boolean = false
)


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideStatusBar()

        setContent {
            LectaTheme {
                LectaHome()
            }
        }
    }

    private fun hideStatusBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBar()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }
}



fun addBreakPoints(text: String): String {
    return text.replace(
        Regex("""([\-_/\.])"""),
        "$1\u200B"
    )
}


@OptIn(ExperimentalTextApi::class)
private val centeredTextStyle = TextStyle(
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

            // Date
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

                            // If several tasks cover this date --
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

@Composable
fun TaskBox(
    task: LectaTask,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pop = remember { Animatable(0f) }
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(task.done) {
        if (firstRun) {
            firstRun = false
            return@LaunchedEffect
        }
        pop.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        pop.animateTo(0f, tween(800, easing = FastOutSlowInEasing))
    }

    val fmt = DateTimeFormatter.ofPattern("dd/MM/yy")
    val dateText =
        if (task.startDate == task.endDate) task.startDate.format(fmt)
        else "${task.startDate.format(fmt)}\nto\n${task.endDate.format(fmt)}"

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = pop.value * 28.dp.toPx()
                val scale = 1f + 0.04f * pop.value
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(200.dp)
            .background(LectaTaskBox, RoundedCornerShape(14.dp))
            .border(0.2.dp, Color.Black, RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // Sec1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.calender),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = dateText,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 4.dp)
                            .padding(horizontal = 2.dp),
                        fontSize = 9.sp,
                        lineHeight = 9.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Div1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD3D3D3))
            )

            // Sec2 - main
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Task: ")
                            }
                            withStyle(
                                SpanStyle(
                                    textDecoration =
                                        if (task.done) TextDecoration.LineThrough
                                        else TextDecoration.None,
                                    color =
                                        if (task.done) Color.Black.copy(alpha = 0.5f)
                                        else Color.Unspecified
                                )
                            ) {
                                append(addBreakPoints(task.title))
                            }
                        },
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Priority: ")
                                }
                                append(task.priority.label)
                            },
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .background(task.priority.color, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Source: ")
                            }
                            append(addBreakPoints(task.source))
                        },
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        maxLines = 3
                    )
                }
            }

            // Div2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD3D3D3))
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Confidence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 11.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${task.confidence}%",
                            fontSize = 11.sp,
                            lineHeight = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Checkbox(
                        checked = task.done,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Green,
                            checkmarkColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = task.extracted,
                        color = Color.Black.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun NoticeBoard(
    modifier: Modifier = Modifier
) {
    val notices by remember {
        mutableStateOf(
            listOf(
                "Robotics lab to be suspended till next week",
                "Global summit for environmental science on 12/11/2026"
            )
        )
    }

    Box(
        modifier = modifier
            .background(Color(0xFFFFD6B9), RoundedCornerShape(18.dp))
            .border(0.2.dp, Color.Black, RoundedCornerShape(18.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text(
                text = "Notice Board",
                color = Color.Black,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "•",
                    color = Color.Black,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = notices.firstOrNull() ?: "No notices",
                    color = Color.Black,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .height(26.dp),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF682E08),
                    contentColor = Color(0xFFE2CFAE)
                ),
                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "View all",
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    image: Painter? = null,          // pass a real photo latter
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF9AA0A6))   // default avatar
            .border(0.2.dp, Color.Black, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                painter = image,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile picture",
                modifier = Modifier.size(26.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun LectaHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(LectaBackground)
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            tint = Color.Black
        )

        ProfilePicture(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onClick = {
                // profile click karne ke baad action
            }
        )
    }
}

@Composable
fun LectaHome() {

    val today = LocalDate.now()
    val tasks = remember {
        mutableStateListOf(
            LectaTask(
                id = 1,
                title = "Pre Medical test for semester - 1",
                source = "Semester-wise-exam.pdf",
                priority = Priority.Critical,
                startDate = today.plusDays(3),
                endDate = today.plusDays(7),
                confidence = 93,
                extracted = "Extracted 3hrs ago"
            ),
            LectaTask(
                id = 2,
                title = "Submit lab record",
                source = "Lab-notice.pdf",
                priority = Priority.Medium,
                startDate = today.plusDays(10),
                confidence = 88,
                extracted = "Extracted 1d ago"
            ),
            LectaTask(
                id = 3,
                title = "Library book return",
                source = "Library-mail.png",
                priority = Priority.Low,
                startDate = today.plusDays(14),
                endDate = today.plusDays(16),
                confidence = 81,
                extracted = "Extracted 2d ago"
            )
        )
    }

    val listState = rememberLazyListState()
    val sortedTasks = tasks.sortedWith(
        compareBy<LectaTask> { it.done }
            .thenBy { it.priority.ordinal }
            .thenBy { it.startDate }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LectaBackground)
    ) {
        val noticeBoardBottomGap = 10.dp

        val taskAreaTop = (maxHeight / 2) + 15.dp
        val boxWidth = (maxWidth - 32.dp - 10.dp) / 2
        val boxHeight = boxWidth * (152f / 184f)

        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(
                        y = taskAreaTop - 30.dp - boxHeight - noticeBoardBottomGap
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoticeBoard(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(184f / 152f)
                )

                CalendarBox(
                    tasks = tasks,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(184f / 152f)
                )
            }

            Text(
                text = "Upcoming Tasks",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = taskAreaTop - 30.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(this@BoxWithConstraints.maxHeight - taskAreaTop)
                    .align(Alignment.TopStart)
                    .offset(y = taskAreaTop)
                    .clipToBounds()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 5.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sortedTasks, key = { it.id }) { task ->
                        TaskBox(
                            task = task,
                            onToggle = {
                                val firstIndex = listState.firstVisibleItemIndex
                                val firstOffset = listState.firstVisibleItemScrollOffset

                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) {
                                    tasks[index] = tasks[index].copy(done = !tasks[index].done)
                                }
                                listState.requestScrollToItem(firstIndex, firstOffset)
                            },
                            modifier = Modifier.animateItem(
                                placementSpec = spring(
                                    dampingRatio = 0.8f,
                                    stiffness = Spring.StiffnessVeryLow, // lower = slower
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )
                            )
                        )
                    }
                }
            }

            LectaHeader()

            FloatingActionButton(
                onClick = {
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                shape = CircleShape,
                containerColor = Color(0xFF8A4A25)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task",
                    tint = Color.White
                )
            }
        }
    }
}