package com.tany.lecta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tany.lecta.ui.theme.LectaBackground

@Composable
fun AccountScreen(
    userName: String,
    profileImage: Painter?,
    hasCustomPhoto: Boolean,
    pendingCount: Int,
    doneCount: Int,
    noticeCount: Int,
    onNameSave: (String) -> Unit,
    onUpload: () -> Unit,
    onRemovePhoto: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brown = Color(0xFF8A4A25)
    val focusManager = LocalFocusManager.current
    var draft by remember(userName) { mutableStateOf(userName) }
    val changed = draft.trim() != userName

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LectaBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .padding(6.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Account",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.size(120.dp)) {
            ProfilePicture(
                modifier = Modifier.size(120.dp),
                image = profileImage,
                onClick = onUpload
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(brown, CircleShape)
                    .border(2.dp, LectaBackground, CircleShape)
                    .clip(CircleShape)
                    .clickable { onUpload() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change photo",
                    modifier = Modifier.size(17.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onUpload,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF682E08),
                    contentColor = Color(0xFFE2CFAE)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Upload photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            if (hasCustomPhoto) {
                OutlinedButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, brown),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brown),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text("Remove photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            FieldLabel("Name")
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(NAME_MAX) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(ProfileStore.DEFAULT_NAME, fontSize = 14.sp) },
                supportingText = {
                    Text(
                        text = "${draft.length}/$NAME_MAX",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        fontSize = 11.sp
                    )
                },
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onNameSave(draft.trim())
                    focusManager.clearFocus()
                },
                enabled = changed,
                modifier = Modifier
                    .align(Alignment.End)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brown,
                    contentColor = Color.White,
                    disabledContainerColor = brown.copy(alpha = 0.35f),
                    disabledContentColor = Color.White
                )
            ) {
                Text("Save name", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFD6B9), RoundedCornerShape(18.dp))
                .border(0.2.dp, Color.Black, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Your activity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            StatRow("Pending tasks", pendingCount)
            StatRow("Completed tasks", doneCount)
            StatRow("Notices", noticeCount)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF682E08)
        )
    }
}
