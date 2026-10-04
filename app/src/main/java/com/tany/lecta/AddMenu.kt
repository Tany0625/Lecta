package com.tany.lecta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onManual: () -> Unit,
    onAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "plusRotation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200, delayMillis = 60)) +
                        scaleIn(tween(200, delayMillis = 60)) +
                        slideInVertically(tween(250, delayMillis = 60)) { it / 2 },
                exit = fadeOut(tween(150)) +
                        scaleOut(tween(150)) +
                        slideOutVertically(tween(200)) { it / 2 }
            ) {
                SmallFloatingActionButton(
                    onClick = onManual,
                    modifier = Modifier.padding(bottom = 12.dp),
                    shape = CircleShape,
                    containerColor = lectaColors.card,
                    contentColor = lectaColors.accentDark
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Add task manually",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) +
                        scaleIn(tween(200)) +
                        slideInVertically(tween(250)) { it / 2 },
                exit = fadeOut(tween(150, delayMillis = 40)) +
                        scaleOut(tween(150, delayMillis = 40)) +
                        slideOutVertically(tween(200, delayMillis = 40)) { it / 2 }
            ) {
                SmallFloatingActionButton(
                    onClick = onAi,
                    modifier = Modifier.padding(bottom = 12.dp),
                    shape = CircleShape,
                    containerColor = lectaColors.card,
                    contentColor = lectaColors.accentDark
                ) {
                    Text(
                        text = "AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FloatingActionButton(
                onClick = onToggle,
                shape = CircleShape,
                containerColor = lectaColors.accent
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.rotate(rotation),
                    tint = Color.White
                )
            }
        }
    }
}
