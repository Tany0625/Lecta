package com.tany.lecta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = lectaColors.text.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(lectaColors.card, RoundedCornerShape(18.dp))
            .border(0.2.dp, lectaColors.text, RoundedCornerShape(18.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 14.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(lectaColors.divider)
    )
}

@Composable
private fun <T> ChoiceChips(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) lectaColors.accent else lectaColors.surface)
                    .border(
                        1.dp,
                        if (isSelected) lectaColors.accent else lectaColors.text.copy(alpha = 0.3f),
                        RoundedCornerShape(50)
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else lectaColors.text,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    option: ThemeOption,
    selected: Boolean,
    systemDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preview = paletteFor(option.id, systemDark)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(96.dp)
            .clip(shape)
            .border(
                if (selected) 2.5.dp else 0.5.dp,
                if (selected) lectaColors.accent else lectaColors.text.copy(alpha = 0.3f),
                shape
            )
            .clickable { onClick() }
    ) {
        if (option.id == "system") {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(ClassicColors.background)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(DarkColors.background)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(preview.background)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(preview.card, preview.accent, preview.surface).forEach { dot ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(dot, CircleShape)
                            .border(0.5.dp, Color.Gray.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (option.id == "system") {
                Box(
                    modifier = Modifier
                        .background(Color(0xCC000000), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = option.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = option.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = preview.text
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .background(lectaColors.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    taskCount: Int,
    noticeCount: Int,
    onClearData: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(lectaColors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PageHeader(title = "Settings", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle("Appearance")

        themeOptions.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { option ->
                    ThemeCard(
                        option = option,
                        selected = AppSettings.themeId == option.id,
                        systemDark = systemDark,
                        onClick = { AppSettings.setTheme(context, option.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSectionTitle("Tasks")
        SettingsCard {
            Text(
                text = "Remove completed tasks after",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChoiceChips(
                options = listOf(24 to "24 hrs", 72 to "3 days", 168 to "7 days", 0 to "Never"),
                selected = AppSettings.cleanupHours,
                onSelect = { AppSettings.setCleanupHours(context, it) }
            )

            SettingsDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vibrate when holding a task",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = lectaColors.text
                    )
                    Text(
                        text = "A small buzz before the delete button appears",
                        fontSize = 12.sp,
                        color = lectaColors.text.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = AppSettings.haptics,
                    onCheckedChange = { AppSettings.setHaptics(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = lectaColors.accent,
                        uncheckedThumbColor = lectaColors.text.copy(alpha = 0.6f),
                        uncheckedTrackColor = lectaColors.surface,
                        uncheckedBorderColor = lectaColors.text.copy(alpha = 0.4f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle("Calendar")
        SettingsCard {
            Text(
                text = "Week starts on",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChoiceChips(
                options = listOf(false to "Monday", true to "Sunday"),
                selected = AppSettings.sundayStart,
                onSelect = { AppSettings.setSundayStart(context, it) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSectionTitle("Data")
        SettingsCard {
            Text(
                text = "Clear all data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )
            Text(
                text = "Removes every task and notice from this phone",
                fontSize = 12.sp,
                color = lectaColors.text.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { confirmClear = true },
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3B30)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                Text("Clear all data", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "${AppInfo.APP_NAME} v${AppInfo.VERSION}",
            fontSize = 11.sp,
            color = lectaColors.text.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (confirmClear) {
        ConfirmDeleteDialog(
            title = "Clear all data?",
            question = "This removes every task and notice. It can't be undone.",
            preview = "$taskCount tasks and $noticeCount notices",
            onConfirm = {
                onClearData()
                confirmClear = false
            },
            onDismiss = { confirmClear = false }
        )
    }
}
