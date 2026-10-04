package com.tany.lecta

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(400)) }
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
            )
        }
        delay(450)
        textAlpha.animateTo(1f, tween(500))
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lectaColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.lecta_logo),
                contentDescription = "Lecta logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = AppInfo.APP_NAME,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tasks. Deadlines. Notices.",
                fontSize = 13.sp,
                color = lectaColors.text.copy(alpha = 0.6f),
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )
        }
    }
}
