package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SportsbookColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkGreenBg,
    secondary = NeonSuccess,
    onSecondary = DarkGreenBg,
    tertiary = AccentBlue,
    background = DarkGreenBg,
    surface = DarkGreenSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = NeonDanger,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SportsbookColorScheme,
        typography = Typography,
        content = content
    )
}
