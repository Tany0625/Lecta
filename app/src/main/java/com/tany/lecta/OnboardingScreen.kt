package com.tany.lecta

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<ImageBitmap?>(null) }
    val painter = remember(photo) { photo?.let { BitmapPainter(it) } }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { ProfileStore.saveImage(context, uri) }
                if (saved != null) photo = saved
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lectaColors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome to ${AppInfo.APP_NAME}",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = lectaColors.text,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Let's set up your profile",
            fontSize = 14.sp,
            color = lectaColors.text.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Box(modifier = Modifier.size(128.dp)) {
            ProfilePicture(
                modifier = Modifier.size(128.dp),
                image = painter,
                onClick = { launcher.launch(arrayOf("image/*")) }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(38.dp)
                    .background(lectaColors.accent, CircleShape)
                    .border(2.dp, lectaColors.background, CircleShape)
                    .clip(CircleShape)
                    .clickable { launcher.launch(arrayOf("image/*")) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Add photo",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (photo == null) "Add a photo (optional)" else "Looking good!",
            fontSize = 12.sp,
            color = lectaColors.text.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            FieldLabel("Your name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(NAME_MAX) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(ProfileStore.DEFAULT_NAME, fontSize = 14.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = lectaColors.surface,
                    unfocusedContainerColor = lectaColors.surface,
                    focusedBorderColor = lectaColors.accent,
                    unfocusedBorderColor = lectaColors.text.copy(alpha = 0.4f),
                    cursorColor = lectaColors.accent,
                    focusedTextColor = lectaColors.text,
                    unfocusedTextColor = lectaColors.text
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) ProfileStore.saveName(context, trimmed)
                ProfileStore.setOnboarded(context)
                onFinished()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = lectaColors.accent,
                contentColor = Color.White
            )
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
            onClick = {
                focusManager.clearFocus()
                ProfileStore.deleteImage(context)
                ProfileStore.setOnboarded(context)
                onFinished()
            }
        ) {
            Text(
                text = "Skip for now",
                fontSize = 14.sp,
                color = lectaColors.accent
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "You can change these anytime in Account",
            fontSize = 11.sp,
            color = lectaColors.text.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
