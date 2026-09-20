package com.tany.lecta.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    background = LectaBackground,
    surface = LectaTaskBox,
    onBackground = LectaBlack,
    onSurface = LectaBlack
)

@Composable
fun LectaTheme(
    content: @Composable () -> Unit
){
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}