package com.tany.lecta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

fun addBreakPoints(text: String): String {
    return text.replace(
        Regex("""([\-_/\.])"""),
        "$1\u200B"
    )
}

@Composable
fun TaskBox(
    task: LectaTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val doneTextColor = lectaColors.text.copy(alpha = 0.5f)
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
                        if (AppSettings.haptics) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        showDelete = true
                        holdTick++
                    }
                }
            }
            .fillMaxWidth()
            .height(200.dp)
            .background(lectaColors.taskBox, RoundedCornerShape(14.dp))
            .border(0.2.dp, lectaColors.text, RoundedCornerShape(14.dp))
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
                        color = lectaColors.text,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(lectaColors.divider)
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
                                        if (task.done) doneTextColor
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
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    showDelete = false
                                    onEdit()
                                },
                                modifier = Modifier.height(26.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = lectaColors.accent,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Delete",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(lectaColors.divider)
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
                        color = lectaColors.text.copy(alpha = 0.5f),
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
