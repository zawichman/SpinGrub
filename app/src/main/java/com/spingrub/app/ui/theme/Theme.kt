package com.spingrub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Fun, colorful palette
val Coral = Color(0xFFFF6B6B)
val Sunny = Color(0xFFFFD93D)
val Mint = Color(0xFF6BCB77)
val Sky = Color(0xFF4D96FF)
val Grape = Color(0xFF9B5DE5)
val Tangerine = Color(0xFFFF9F1C)
val Bubblegum = Color(0xFFF15BB5)
val Cream = Color(0xFFFFF8F0)
val Charcoal = Color(0xFF2B2D42)

// Vibrant segment palette reused across wheels
val SegmentColors = listOf(
    Coral, Sunny, Mint, Sky, Grape, Tangerine, Bubblegum,
    Color(0xFF00BBF9), Color(0xFF00F5D4), Color(0xFFFEE440)
)

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    secondary = Grape,
    onSecondary = Color.White,
    tertiary = Sky,
    background = Cream,
    onBackground = Charcoal,
    surface = Color.White,
    onSurface = Charcoal,
    primaryContainer = Sunny,
    onPrimaryContainer = Charcoal,
)

private val DarkColors = darkColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    secondary = Grape,
    tertiary = Sky,
    background = Color(0xFF1B1B2F),
    onBackground = Cream,
    surface = Color(0xFF252540),
    onSurface = Cream,
    primaryContainer = Grape,
    onPrimaryContainer = Color.White,
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp),
)

@Composable
fun SpinGrubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
