package com.tany.lecta

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

enum class AppStage { Splash, Onboarding, Home }

@Composable
fun AppRoot() {
    val context = LocalContext.current
    var stage by rememberSaveable { mutableStateOf(AppStage.Splash) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lectaColors.background)
    ) {
        Crossfade(
            targetState = stage,
            animationSpec = tween(400),
            label = "appStage"
        ) { current ->
            when (current) {
                AppStage.Splash -> SplashScreen(
                    onFinished = {
                        stage = if (ProfileStore.isOnboarded(context)) {
                            AppStage.Home
                        } else {
                            AppStage.Onboarding
                        }
                    }
                )
                AppStage.Onboarding -> OnboardingScreen(
                    onFinished = { stage = AppStage.Home }
                )
                AppStage.Home -> LectaHome()
            }
        }
    }
}