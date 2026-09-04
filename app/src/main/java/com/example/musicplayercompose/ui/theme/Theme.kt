package com.example.musicplayercompose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MusicDarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = AccentPurpleLight,
    onSecondary = Color.White,
    tertiary = AccentPink,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MusicPlayerComposeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MusicDarkColorScheme,
        typography = Typography,
        content = content
    )
}