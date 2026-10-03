package com.tany.lecta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tany.lecta.ui.theme.LectaBackground
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
