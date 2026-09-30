package com.example.musicplayercompose.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayercompose.domain.model.ThemeMode

private val Violet = Color(0xFF7C5CFF)
private val VioletLight = Color(0xFFB6A6FF)
private val Pink = Color(0xFFFF5C9E)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B3FE0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6DEFF),
    onPrimaryContainer = Color(0xFF1B0A66),
    secondary = Color(0xFF625B71),
    secondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFC2185B),
    background = Color(0xFFFBF9FF),
    surface = Color(0xFFFBF9FF),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F2FC),
    surfaceContainer = Color(0xFFEFEBF8),
    surfaceContainerHigh = Color(0xFFE9E5F3),
    surfaceContainerHighest = Color(0xFFE3DFEE)
)

private val DarkColors = darkColorScheme(
    primary = VioletLight,
    onPrimary = Color(0xFF2B1580),
    primaryContainer = Color(0xFF4127B8),
    onPrimaryContainer = Color(0xFFE6DEFF),
    secondary = Color(0xFFCCC2DC),
    secondaryContainer = Color(0xFF4A4458),
    tertiary = Pink,
    background = Color(0xFF0F0E14),
    surface = Color(0xFF0F0E14),
    surfaceContainerLowest = Color(0xFF0A0910),
    surfaceContainerLow = Color(0xFF16151D),
    surfaceContainer = Color(0xFF1B1A23),
    surfaceContainerHigh = Color(0xFF25242E),
    surfaceContainerHighest = Color(0xFF302F3A)
)

private val AppTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp)
    )
}

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun MusicPlayerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val scheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, shapes = AppShapes, content = content)
}
