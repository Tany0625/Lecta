package com.tany.lecta

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tany.lecta.ui.theme.LectaBackground
import com.tany.lecta.ui.theme.LectaTaskBox
import com.tany.lecta.ui.theme.LectaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
const val NOTICE_TEXT_MAX = 150
const val HOLD_TO_DELETE_MS = 1000L

data class LectaNotice(
    val id: Int,
    val text: String,
    val date: LocalDate
)

object NoticeStore {
    private const val PREFS = "lecta_prefs"
    private const val KEY = "notices"

    fun load(context: Context): List<LectaNotice> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map {
                val o = array.getJSONObject(it)
                LectaNotice(
                    id = o.getInt("id"),
                    text = o.getString("text"),
                    date = LocalDate.parse(o.getString("date"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, notices: List<LectaNotice>) {
        val array = JSONArray()
        notices.forEach {
            array.put(
                JSONObject().apply {
                    put("id", it.id)
                    put("text", it.text)
                    put("date", it.date.toString())
                }
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}

object TaskStore {
    private const val PREFS = "lecta_prefs"
    private const val KEY = "tasks"
    const val EXPIRY_MS = 24L * 60 * 60 * 1000

    fun isExpired(task: LectaTask, now: Long = System.currentTimeMillis()): Boolean {
        val completed = task.completedAt ?: return false
        return task.done && now - completed >= EXPIRY_MS
    }

    fun load(context: Context): List<LectaTask> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length())
                .map { toTask(array.getJSONObject(it)) }
                .filter { !isExpired(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, tasks: List<LectaTask>) {
        val array = JSONArray()
        tasks.forEach { array.put(toJson(it)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    private fun toJson(task: LectaTask): JSONObject = JSONObject().apply {
        put("id", task.id)
        put("title", task.title)
        put("source", task.source)
        put("priority", task.priority.name)
        put("startDate", task.startDate.toString())
        put("endDate", task.endDate.toString())
        put("confidence", task.confidence)
        put("extracted", task.extracted)
        put("done", task.done)
        put("manual", task.manual)
        put("createdAt", task.createdAt)
        put("completedAt", task.completedAt ?: JSONObject.NULL)
    }

    private fun toTask(o: JSONObject): LectaTask = LectaTask(
        id = o.getInt("id"),
        title = o.getString("title"),
        source = o.getString("source"),
        priority = Priority.valueOf(o.getString("priority")),
        startDate = LocalDate.parse(o.getString("startDate")),
        endDate = LocalDate.parse(o.getString("endDate")),
        confidence = o.getInt("confidence"),
        extracted = o.getString("extracted"),
        done = o.getBoolean("done"),
        manual = o.getBoolean("manual"),
        createdAt = o.getLong("createdAt"),
        completedAt = if (o.isNull("completedAt")) null else o.getLong("completedAt")
    )
}

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

@Composable
fun TaskBox(
    task: LectaTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showDelete by remember { mutableStateOf(false) }
    var holdTick by remember { mutableStateOf(0) }
    LaunchedEffect(holdTick) {
        if (holdTick > 0) {
            delay(5000)
            showDelete = false
        }
    }

    val pop = remember { Animatable(0f) }
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(task.done) {
        if (firstRun) {
            firstRun = false
            return@LaunchedEffect
        }
        pop.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        pop.animateTo(0f, tween(520, easing = FastOutSlowInEasing))
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
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    var heldLongEnough = true
                    withTimeoutOrNull(HOLD_TO_DELETE_MS) {
                        waitForUpOrCancellation()
                        heldLongEnough = false
                    }
                    if (heldLongEnough) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDelete = true
                        holdTick++
                    }
                }
            }
            .fillMaxWidth()
            .height(200.dp)
            .background(LectaTaskBox, RoundedCornerShape(14.dp))
            .border(0.2.dp, Color.Black, RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

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

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD3D3D3))
            )

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
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
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

                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedVisibility(
                        visible = showDelete,
                        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                        exit = fadeOut(tween(200)) + scaleOut(tween(200))
                    ) {
                        Button(
                            onClick = {
                                showDelete = false
                                onDelete()
                            },
                            modifier = Modifier.height(26.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF3B30),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Delete task",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

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
                            text = if (task.manual) "NA" else "${task.confidence}%",
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
                        text = if (task.manual) "Entered ${agoText(task.createdAt)}" else task.extracted,
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
    notices: List<LectaNotice>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val first = notices.firstOrNull()

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
                if (first == null) {
                    Text(
                        text = "No notices",
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        lineHeight = 11.sp
                    )
                } else {
                    Text(
                        text = "•",
                        color = Color.Black,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = first.text,
                            color = Color.Black,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = first.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                            color = Color(0xFF682E08),
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onViewAll,
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
fun NoticeBoardFull(
    notices: List<LectaNotice>,
    onAdd: () -> Unit,
    onDelete: (LectaNotice) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFFFD6B9), RoundedCornerShape(22.dp))
            .border(0.2.dp, Color.Black, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notice Board",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close notice board",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onClose() }
                        .padding(8.dp),
                    tint = Color(0xFF682E08)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 70.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notices.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notices yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                items(notices, key = { it.id }) { notice ->
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(14.dp))
                            .border(0.2.dp, Color.Black, RoundedCornerShape(14.dp))
                            .padding(start = 14.dp, top = 10.dp, bottom = 12.dp, end = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = notice.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF682E08)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notice.text,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                color = Color.Black
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete notice",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onDelete(notice) }
                                .padding(7.dp),
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .height(34.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF682E08),
                contentColor = Color(0xFFE2CFAE)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add notice",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showPicker by remember { mutableStateOf(false) }
    val brown = Color(0xFF8A4A25)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(LectaBackground, RoundedCornerShape(22.dp))
                .border(0.2.dp, Color.Black, RoundedCornerShape(22.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "New Notice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Add a notice by hand",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Notice")
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(NOTICE_TEXT_MAX) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What is the notice?", fontSize = 14.sp) },
                supportingText = {
                    Text(
                        text = "${text.length}/$NOTICE_TEXT_MAX",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = brown,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.4f),
                    cursorColor = brown,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            DateField(
                label = "Date",
                date = date,
                onClick = { showPicker = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brown),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brown)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onConfirm(text.trim(), date) },
                    enabled = text.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brown,
                        contentColor = Color.White,
                        disabledContainerColor = brown.copy(alpha = 0.35f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Confirm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPicker) {
        DatePickerSheet(
            initial = date,
            onPick = { picked ->
                date = picked
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    image: Painter? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF9AA0A6))
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
fun LectaHeader(onMenuClick: () -> Unit = {}) {
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
                .padding(start = 8.dp)
                .clip(CircleShape)
                .clickable { onMenuClick() }
                .padding(8.dp),
            tint = Color.Black
        )

        ProfilePicture(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onClick = {
            }
        )
    }
}

private fun greetingFor(hour: Int): String = when {
    hour < 12 -> "Good morning"
    hour < 17 -> "Good afternoon"
    else -> "Good evening"
}

private fun dueText(days: Long): String = when {
    days < 0 -> "overdue"
    days == 0L -> "today"
    days == 1L -> "tomorrow"
    else -> "in $days days"
}

private fun progressColor(fraction: Float): Color = when {
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
    userName: String = "Tany",
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
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (showSummary) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = summary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = Color.Black.copy(alpha = 0.6f),
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
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$doneCount of $total done · ${(fraction * 100).toInt()}%",
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .border(0.5.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
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

fun agoText(createdAt: Long): String {
    val minutes = (System.currentTimeMillis() - createdAt) / 60000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> {
            val hours = minutes / 60
            "$hours${if (hours == 1L) "hr" else "hrs"} ago"
        }
        else -> "${minutes / 1440}d ago"
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun PriorityDot(priority: Priority) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(priority.color, CircleShape)
    )
}

@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        FieldLabel(label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initial: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        onPick(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("OK", color = Color(0xFF8A4A25))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8A4A25))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Priority, LocalDate, LocalDate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.Medium) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var priorityMenu by remember { mutableStateOf(false) }
    var pickerTarget by remember { mutableStateOf(0) }

    val brown = Color(0xFF8A4A25)
    val isSingleDay = startDate == endDate
    val dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(LectaBackground, RoundedCornerShape(22.dp))
                .border(0.2.dp, Color.Black, RoundedCornerShape(22.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "New Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Add a task by hand",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Task")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(TASK_TITLE_MAX) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What needs to be done?", fontSize = 14.sp) },
                supportingText = {
                    Text(
                        text = "${title.length}/$TASK_TITLE_MAX",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp
                    )
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = brown,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.4f),
                    cursorColor = brown,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Priority")
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { priorityMenu = true }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityDot(priority)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = priority.label,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Choose priority",
                        tint = Color.Black
                    )
                }

                DropdownMenu(
                    expanded = priorityMenu,
                    onDismissRequest = { priorityMenu = false },
                    containerColor = Color.White
                ) {
                    Priority.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PriorityDot(option)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(option.label, fontSize = 14.sp, color = Color.Black)
                                }
                            },
                            onClick = {
                                priority = option
                                priorityMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateField(
                    label = "Start date",
                    date = startDate,
                    onClick = { pickerTarget = 1 },
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    label = "End date",
                    date = endDate,
                    onClick = { pickerTarget = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFFFD6B9), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSingleDay) "Single-day task" else "Period task · $dayCount days",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF682E08)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brown),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brown)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onConfirm(title.trim(), priority, startDate, endDate) },
                    enabled = title.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brown,
                        contentColor = Color.White,
                        disabledContainerColor = brown.copy(alpha = 0.35f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Confirm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (pickerTarget == 1) {
        DatePickerSheet(
            initial = startDate,
            onPick = { picked ->
                startDate = picked
                if (endDate.isBefore(picked)) endDate = picked
                pickerTarget = 0
            },
            onDismiss = { pickerTarget = 0 }
        )
    }

    if (pickerTarget == 2) {
        DatePickerSheet(
            initial = endDate,
            onPick = { picked ->
                endDate = if (picked.isBefore(startDate)) startDate else picked
                pickerTarget = 0
            },
            onDismiss = { pickerTarget = 0 }
        )
    }
}

private fun fileNameOf(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment ?: "Selected file"
}

@Composable
fun AiTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Uri, String) -> Unit
) {
    val context = LocalContext.current
    val brown = Color(0xFF8A4A25)
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedName by remember { mutableStateOf("") }
    var pickedType by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri) ?: ""
            if (type == "application/pdf" || type.startsWith("image/")) {
                pickedUri = uri
                pickedName = fileNameOf(context, uri)
                pickedType = if (type == "application/pdf") "PDF" else "IMG"
                error = null
            } else {
                error = "Only PDF or image files are allowed"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(LectaBackground, RoundedCornerShape(22.dp))
                .border(0.2.dp, Color.Black, RoundedCornerShape(22.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Let AI add a task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Upload a document and AI will find the task",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Upload")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.5.dp, brown.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clickable { launcher.launch(arrayOf("application/pdf", "image/*")) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFFD6B9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pickedUri == null) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Upload",
                                tint = Color(0xFF682E08)
                            )
                        } else {
                            Text(
                                text = pickedType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF682E08)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pickedUri == null) {
                        Text(
                            text = "Tap to upload",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "PDF or image",
                            fontSize = 11.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    } else {
                        Text(
                            text = pickedName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap to change",
                            fontSize = 11.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFFFF3B30)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brown),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brown)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val uri = pickedUri
                        if (uri != null) onConfirm(uri, pickedName)
                    },
                    enabled = pickedUri != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brown,
                        contentColor = Color.White,
                        disabledContainerColor = brown.copy(alpha = 0.35f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text("Confirm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF682E08)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
fun SideMenu(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(
                LectaBackground,
                RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Tany",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Lecta",
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.Black.copy(alpha = 0.25f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        DrawerItem(Icons.Default.Person, "Account", onClose)
        DrawerItem(Icons.Default.Info, "About the app", onClose)
        DrawerItem(Icons.Default.Settings, "Settings", onClose)
        DrawerItem(Icons.Default.Email, "Help & feedback", onClose)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Lecta v1.0",
            fontSize = 11.sp,
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    question: String,
    preview: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val brown = Color(0xFF8A4A25)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(LectaBackground, RoundedCornerShape(22.dp))
                .border(0.2.dp, Color.Black, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = preview,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Color.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(0.2.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brown),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brown)
                ) {
                    Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF3B30),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LectaHome() {

    val context = LocalContext.current

    val tasks = remember {
        mutableStateListOf<LectaTask>().apply { addAll(TaskStore.load(context)) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { tasks.toList() }.collect { TaskStore.save(context, it) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            tasks.removeAll { TaskStore.isExpired(it, now) }
            delay(60_000L)
        }
    }

    val notices = remember {
        mutableStateListOf<LectaNotice>().apply { addAll(NoticeStore.load(context)) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { notices.toList() }.collect { NoticeStore.save(context, it) }
    }

    val todayDate = LocalDate.now()
    val sortedNotices = notices.sortedWith(
        compareBy<LectaNotice> { it.date.isBefore(todayDate) }
            .thenBy { if (it.date.isBefore(todayDate)) -it.date.toEpochDay() else it.date.toEpochDay() }
    )

    var noticeExpanded by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<LectaNotice?>(null) }

    BackHandler(enabled = noticeExpanded) { noticeExpanded = false }

    val listState = rememberLazyListState()

    var fabExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = menuOpen) { menuOpen = false }
    val plusRotation by animateFloatAsState(
        targetValue = if (fabExpanded) 90f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "plusRotation"
    )

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
        val noticeTop = taskAreaTop - 30.dp - boxHeight - noticeBoardBottomGap

        val greetingHeight = maxOf(noticeTop - 52.dp - 8.dp, 0.dp)

        Box(modifier = Modifier.fillMaxSize()) {

            GreetingSection(
                tasks = tasks,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 52.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(greetingHeight),
                showSummary = greetingHeight >= 80.dp
            )

            AnimatedVisibility(
                visible = !noticeExpanded,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(250))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = noticeTop),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NoticeBoard(
                            notices = sortedNotices,
                            onViewAll = {
                                fabExpanded = false
                                noticeExpanded = true
                            },
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
                            if (sortedTasks.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No task left",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            items(sortedTasks, key = { it.id }) { task ->
                                TaskBox(
                                    task = task,
                                    onToggle = {
                                        val firstIndex = listState.firstVisibleItemIndex
                                        val firstOffset = listState.firstVisibleItemScrollOffset

                                        val index = tasks.indexOfFirst { it.id == task.id }
                                        if (index >= 0) {
                                            val current = tasks[index]
                                            tasks[index] = current.copy(
                                                done = !current.done,
                                                completedAt = if (current.done) null else System.currentTimeMillis()
                                            )
                                        }

                                        listState.requestScrollToItem(firstIndex, firstOffset)
                                    },
                                    onDelete = { tasks.removeAll { it.id == task.id } },
                                    modifier = Modifier.animateItem(
                                        placementSpec = spring(
                                            dampingRatio = 0.65f,
                                            stiffness = Spring.StiffnessLow,
                                            visibilityThreshold = IntOffset.VisibilityThreshold
                                        )
                                    )
                                )
                            }
                        }
                    }

                    if (fabExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { fabExpanded = false }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AnimatedVisibility(
                            visible = fabExpanded,
                            enter = fadeIn(tween(200, delayMillis = 60)) +
                                    scaleIn(tween(200, delayMillis = 60)) +
                                    slideInVertically(tween(250, delayMillis = 60)) { it / 2 },
                            exit = fadeOut(tween(150)) +
                                    scaleOut(tween(150)) +
                                    slideOutVertically(tween(200)) { it / 2 }
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    showAddDialog = true
                                },
                                modifier = Modifier.padding(bottom = 12.dp),
                                shape = CircleShape,
                                containerColor = Color(0xFFFFD6B9),
                                contentColor = Color(0xFF682E08)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Add task manually",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = fabExpanded,
                            enter = fadeIn(tween(200)) +
                                    scaleIn(tween(200)) +
                                    slideInVertically(tween(250)) { it / 2 },
                            exit = fadeOut(tween(150, delayMillis = 40)) +
                                    scaleOut(tween(150, delayMillis = 40)) +
                                    slideOutVertically(tween(200, delayMillis = 40)) { it / 2 }
                        ) {
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    showAiDialog = true
                                },
                                modifier = Modifier.padding(bottom = 12.dp),
                                shape = CircleShape,
                                containerColor = Color(0xFFFFD6B9),
                                contentColor = Color(0xFF682E08)
                            ) {
                                Text(
                                    text = "AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = { fabExpanded = !fabExpanded },
                            shape = CircleShape,
                            containerColor = Color(0xFF8A4A25)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add task",
                                modifier = Modifier.rotate(plusRotation),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = noticeExpanded,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = noticeTop),
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 10 },
                exit = fadeOut(tween(200))
            ) {
                NoticeBoardFull(
                    notices = sortedNotices,
                    onAdd = { showNoticeDialog = true },
                    onDelete = { notice -> noticeToDelete = notice },
                    onClose = { noticeExpanded = false },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(this@BoxWithConstraints.maxHeight - noticeTop - 16.dp)
                )
            }

            LectaHeader(onMenuClick = { menuOpen = true })

            AnimatedVisibility(
                visible = menuOpen,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { menuOpen = false }
                )
            }

            AnimatedVisibility(
                visible = menuOpen,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it },
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { -it }
            ) {
                SideMenu(onClose = { menuOpen = false })
            }

            noticeToDelete?.let { target ->
                ConfirmDeleteDialog(
                    title = "Delete notice?",
                    question = "Are you sure you want to delete this notice?",
                    preview = target.text,
                    onConfirm = {
                        notices.removeAll { it.id == target.id }
                        noticeToDelete = null
                    },
                    onDismiss = { noticeToDelete = null }
                )
            }

            if (showNoticeDialog) {
                AddNoticeDialog(
                    onDismiss = { showNoticeDialog = false },
                    onConfirm = { text, date ->
                        val nextId = (notices.maxOfOrNull { it.id } ?: 0) + 1
                        notices.add(LectaNotice(id = nextId, text = text, date = date))
                        showNoticeDialog = false
                    }
                )
            }

            if (showAiDialog) {
                AiTaskDialog(
                    onDismiss = { showAiDialog = false },
                    onConfirm = { _, _ -> showAiDialog = false }
                )
            }

            if (showAddDialog) {
                AddTaskDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, priority, start, end ->
                        val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                        tasks.add(
                            LectaTask(
                                id = nextId,
                                title = title,
                                source = "Manual",
                                priority = priority,
                                startDate = start,
                                endDate = end,
                                confidence = 0,
                                extracted = "",
                                manual = true
                            )
                        )
                        showAddDialog = false
                    }
                )
            }
        }
    }
}