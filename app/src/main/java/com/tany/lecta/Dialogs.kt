package com.tany.lecta

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tany.lecta.ui.theme.LectaBackground
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun PriorityDot(priority: Priority) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(priority.color, CircleShape)
    )
}

@Composable
fun DateField(
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
fun DatePickerSheet(
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

fun fileNameOf(context: Context, uri: Uri): String {
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
