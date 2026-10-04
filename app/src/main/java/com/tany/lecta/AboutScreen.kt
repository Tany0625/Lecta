package com.tany.lecta

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(lectaColors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            PageHeader(title = "About the app", onBack = onBack)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(R.drawable.lecta_logo),
            contentDescription = "Lecta logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = AppInfo.APP_NAME,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = lectaColors.text
        )
        Text(
            text = "Version ${AppInfo.VERSION}",
            fontSize = 12.sp,
            color = lectaColors.text.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lecta keeps your tasks, deadlines and notices in one place, so nothing important slips past you.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = lectaColors.text.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(lectaColors.card, RoundedCornerShape(18.dp))
                .border(0.2.dp, lectaColors.text, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "About the developer",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(lectaColors.accentDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppInfo.DEVELOPER_NAME.first().toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = lectaColors.onAccentDark
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = AppInfo.DEVELOPER_NAME,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = lectaColors.text
                    )
                    Text(
                        text = AppInfo.DEVELOPER_ROLE,
                        fontSize = 12.sp,
                        color = lectaColors.text.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = AppInfo.DEVELOPER_BIO,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = lectaColors.text
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "© 2026 ${AppInfo.DEVELOPER_NAME}",
            fontSize = 11.sp,
            color = lectaColors.text.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
