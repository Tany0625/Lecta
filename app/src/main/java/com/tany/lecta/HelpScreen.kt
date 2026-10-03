package com.tany.lecta

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

val ratingLabels = listOf(
    "Not up to expectations",
    "Could be better",
    "It's okay",
    "Really good",
    "Loved it!"
)

fun sendFeedbackEmail(
    context: Context,
    fromId: String,
    rating: Int,
    ratingLabel: String,
    message: String
) {
    val body = buildString {
        append("From: $fromId\n")
        append("Rating: $rating/5 ($ratingLabel)\n\n")
        append(if (message.isBlank()) "(no message)" else message)
        append("\n\n---\n")
        append("${AppInfo.APP_NAME} v${AppInfo.VERSION}\n")
        append("${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}")
    }

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(AppInfo.FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "${AppInfo.APP_NAME} feedback: $rating/5 from $fromId")
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found on this phone", Toast.LENGTH_LONG).show()
    }
}

suspend fun submitFeedback(
    fromId: String,
    rating: Int,
    ratingLabel: String,
    message: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val payload = JSONObject().apply {
            put("access_key", AppInfo.WEB3FORMS_KEY)
            put("subject", "${AppInfo.APP_NAME} feedback: $rating/5 from $fromId")
            put("from_name", "${AppInfo.APP_NAME} app user")
            put("email", fromId)
            put("rating", "$rating/5 ($ratingLabel)")
            put("message", if (message.isBlank()) "(no message)" else message)
            put("app", "${AppInfo.APP_NAME} v${AppInfo.VERSION}")
            put("device", "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}")
        }

        val connection = (URL("https://api.web3forms.com/submit").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        connection.disconnect()

        code in 200..299 && JSONObject(text).optBoolean("success", false)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    var open by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = !open }
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(if (open) 180f else 0f),
                tint = lectaColors.accentDark
            )
        }

        AnimatedVisibility(visible = open) {
            Text(
                text = answer,
                modifier = Modifier.padding(top = 6.dp, end = 24.dp),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = lectaColors.text.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val brown = lectaColors.accent

    var email by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val emailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val canSend = emailValid && rating > 0

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = lectaColors.surface,
        unfocusedContainerColor = lectaColors.surface,
        focusedBorderColor = brown,
        unfocusedBorderColor = lectaColors.text.copy(alpha = 0.4f),
        cursorColor = brown,
        focusedTextColor = lectaColors.text,
        unfocusedTextColor = lectaColors.text
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(lectaColors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader(title = "Help & feedback", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(lectaColors.card, RoundedCornerShape(18.dp))
                .border(0.2.dp, lectaColors.text, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Quick help",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            FaqItem(
                "How do I add a task?",
                "Tap the + button, then choose the pen icon to add a task by hand."
            )
            FaqItem(
                "How do I delete a task?",
                "Press and hold the task card for a second, then tap Delete task."
            )
            FaqItem(
                "What happens to completed tasks?",
                "They move to the bottom of the list and are removed automatically after the time set in Settings (24 hours by default)."
            )
            FaqItem(
                "How do I add a notice?",
                "Tap View all on the Notice Board, then tap Add notice at the bottom."
            )
            FaqItem(
                "How do I change my name or photo?",
                "Open the menu or tap your picture at the top right, then choose Account."
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(lectaColors.card, RoundedCornerShape(18.dp))
                .border(0.2.dp, lectaColors.text, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .clickable {
                    if (AppInfo.DOCS_URL.isBlank()) {
                        Toast.makeText(context, "Documentation is coming soon", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(AppInfo.DOCS_URL))
                            )
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No browser found on this phone", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = lectaColors.accentDark
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Documentation",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = lectaColors.text
                )
                Text(
                    text = if (AppInfo.DOCS_URL.isBlank()) "Coming soon" else "Opens the GitHub page",
                    fontSize = 12.sp,
                    color = lectaColors.text.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Send feedback",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = lectaColors.text
        )
        Text(
            text = "Tell me what you think of ${AppInfo.APP_NAME}",
            fontSize = 12.sp,
            color = lectaColors.text.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FieldLabel("Your email")
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("name@example.com", fontSize = 14.sp) },
            isError = email.isNotEmpty() && !emailValid,
            supportingText = if (email.isNotEmpty() && !emailValid) {
                { Text("Enter a valid email address", fontSize = 11.sp) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("How much do you like ${AppInfo.APP_NAME}?")
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (star in 1..5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "$star stars",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { rating = star }
                        .padding(6.dp),
                    tint = if (star <= rating) Color(0xFFFFB800) else lectaColors.text.copy(alpha = 0.2f)
                )
            }
        }
        Text(
            text = if (rating == 0) "Tap a star to rate" else ratingLabels[rating - 1],
            fontSize = 12.sp,
            fontWeight = if (rating == 0) FontWeight.Normal else FontWeight.Bold,
            color = if (rating == 0) lectaColors.text.copy(alpha = 0.6f) else lectaColors.accentDark
        )

        Spacer(modifier = Modifier.height(14.dp))

        FieldLabel("How was your experience?")
        OutlinedTextField(
            value = message,
            onValueChange = { message = it.take(AppInfo.FEEDBACK_MAX) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Share your thoughts, ideas or problems", fontSize = 14.sp) },
            supportingText = {
                Text(
                    text = "${message.length}/${AppInfo.FEEDBACK_MAX}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 11.sp
                )
            },
            minLines = 4,
            maxLines = 8,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                sending = true
                failed = false
                status = null
                val label = ratingLabels[rating - 1]
                scope.launch {
                    val ok = submitFeedback(email.trim(), rating, label, message.trim())
                    sending = false
                    if (ok) {
                        status = "Thanks! Your feedback has been sent."
                        rating = 0
                        message = ""
                    } else {
                        failed = true
                        status = "Couldn't send your feedback. Check your internet connection and try again."
                    }
                }
            },
            enabled = canSend && !sending,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = brown,
                contentColor = Color.White,
                disabledContainerColor = brown.copy(alpha = 0.35f),
                disabledContentColor = Color.White
            )
        ) {
            Text(
                text = if (sending) "Sending..." else "Send feedback",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (status != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status ?: "",
                fontSize = 12.sp,
                color = if (failed) Color(0xFFFF3B30) else Color(0xFF4CAF50)
            )
        }

        if (failed) {
            TextButton(
                onClick = {
                    sendFeedbackEmail(
                        context = context,
                        fromId = email.trim(),
                        rating = rating,
                        ratingLabel = ratingLabels[rating - 1],
                        message = message.trim()
                    )
                }
            ) {
                Text("Send with email app instead", fontSize = 12.sp, color = brown)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
