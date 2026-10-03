package com.tany.lecta

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.tany.lecta.ui.theme.LectaBackground
import com.tany.lecta.ui.theme.LectaTaskBox

data class LectaColors(
    val isDark: Boolean,
    val background: Color,
    val taskBox: Color,
    val card: Color,
    val surface: Color,
    val text: Color,
    val accent: Color,
    val accentDark: Color,
    val onAccentDark: Color,
    val divider: Color
)

val ClassicColors = LectaColors(
    isDark = false,
    background = LectaBackground,
    taskBox = LectaTaskBox,
    card = Color(0xFFFFD6B9),
    surface = Color.White,
    text = Color.Black,
    accent = Color(0xFF8A4A25),
    accentDark = Color(0xFF682E08),
    onAccentDark = Color(0xFFE2CFAE),
    divider = Color(0xFFD3D3D3)
)

val DarkColors = LectaColors(
    isDark = true,
    background = Color(0xFF121214),
    taskBox = Color(0xFF26262B),
    card = Color(0xFF3A2D26),
    surface = Color(0xFF1E1E22),
    text = Color(0xFFECECEC),
    accent = Color(0xFFB86B35),
    accentDark = Color(0xFFE8B98A),
    onAccentDark = Color(0xFF2B1608),
    divider = Color(0xFF3A3A40)
)

val MidnightColors = LectaColors(
    isDark = true,
    background = Color(0xFF000000),
    taskBox = Color(0xFF14141A),
    card = Color(0xFF1B1D33),
    surface = Color(0xFF0E0E12),
    text = Color(0xFFF2F2F2),
    accent = Color(0xFF5B6CF0),
    accentDark = Color(0xFFB9C1FF),
    onAccentDark = Color(0xFF141A40),
    divider = Color(0xFF2A2A33)
)

val OceanColors = LectaColors(
    isDark = false,
    background = Color(0xFFE8F1F8),
    taskBox = Color(0xFFD3E6F5),
    card = Color(0xFFBFDDF2),
    surface = Color.White,
    text = Color(0xFF0D1B2A),
    accent = Color(0xFF1F6FA8),
    accentDark = Color(0xFF0B3C5D),
    onAccentDark = Color(0xFFD6EAF8),
    divider = Color(0xFFB0C4D4)
)

val ForestColors = LectaColors(
    isDark = false,
    background = Color(0xFFEEF3E8),
    taskBox = Color(0xFFDCE8D0),
    card = Color(0xFFC9DDB5),
    surface = Color.White,
    text = Color(0xFF1B2A1B),
    accent = Color(0xFF3F7D3A),
    accentDark = Color(0xFF234A20),
    onAccentDark = Color(0xFFDCEBCF),
    divider = Color(0xFFB5C4A8)
)

data class ThemeOption(val id: String, val label: String)

val themeOptions = listOf(
    ThemeOption("system", "Auto"),
    ThemeOption("classic", "Classic"),
    ThemeOption("dark", "Dark"),
    ThemeOption("midnight", "Midnight"),
    ThemeOption("ocean", "Ocean"),
    ThemeOption("forest", "Forest")
)

fun paletteFor(id: String, systemDark: Boolean): LectaColors = when (id) {
    "classic" -> ClassicColors
    "dark" -> DarkColors
    "midnight" -> MidnightColors
    "ocean" -> OceanColors
    "forest" -> ForestColors
    else -> if (systemDark) DarkColors else ClassicColors
}

val LocalLectaColors = compositionLocalOf { ClassicColors }

val lectaColors: LectaColors
    @Composable
    @ReadOnlyComposable
    get() = LocalLectaColors.current

@Composable
fun LectaAppTheme(content: @Composable () -> Unit) {
    val target = paletteFor(AppSettings.themeId, isSystemInDarkTheme())
    val spec = tween<Color>(350)

    val palette = LectaColors(
        isDark = target.isDark,
        background = animateColorAsState(target.background, spec, label = "background").value,
        taskBox = animateColorAsState(target.taskBox, spec, label = "taskBox").value,
        card = animateColorAsState(target.card, spec, label = "card").value,
        surface = animateColorAsState(target.surface, spec, label = "surface").value,
        text = animateColorAsState(target.text, spec, label = "text").value,
        accent = animateColorAsState(target.accent, spec, label = "accent").value,
        accentDark = animateColorAsState(target.accentDark, spec, label = "accentDark").value,
        onAccentDark = animateColorAsState(target.onAccentDark, spec, label = "onAccentDark").value,
        divider = animateColorAsState(target.divider, spec, label = "divider").value
    )

    val scheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.card,
            onSurfaceVariant = palette.text.copy(alpha = 0.7f),
            outline = palette.text.copy(alpha = 0.4f)
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            background = palette.background,
            onBackground = palette.text,
            surface = palette.surface,
            onSurface = palette.text,
            surfaceVariant = palette.card,
            onSurfaceVariant = palette.text.copy(alpha = 0.7f),
            outline = palette.text.copy(alpha = 0.4f)
        )
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightNavigationBars = !palette.isDark
            controller.isAppearanceLightStatusBars = !palette.isDark
        }
    }

    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(
            LocalContentColor provides palette.text,
            LocalLectaColors provides palette
        ) {
            content()
        }
    }
}
