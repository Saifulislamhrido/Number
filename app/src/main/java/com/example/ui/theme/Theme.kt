package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameDarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = Color.White,
    primaryContainer = DarkPurpleSurfaceElevated,
    onPrimaryContainer = Color.White,
    secondary = BrightCyan,
    onSecondary = Color.Black,
    tertiary = BrightAmber,
    onTertiary = Color.Black,
    background = DeepSpaceBackground,
    onBackground = Color.White,
    surface = DarkPurpleSurface,
    onSurface = Color.White,
    surfaceVariant = DarkPurpleSurfaceElevated,
    onSurfaceVariant = Color(0xFFD1C4E9),
    error = BrightCoral,
    onError = Color.White
)

private val GameLightColorScheme = lightColorScheme(
    primary = PurpleAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF311B92),
    secondary = Color(0xFF0091EA),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF8F00),
    onTertiary = Color.White,
    background = Color(0xFFF3F0F9),
    onBackground = Color(0xFF1A103C),
    surface = Color.White,
    onSurface = Color(0xFF1A103C),
    surfaceVariant = Color(0xFFE8E0F2),
    onSurfaceVariant = Color(0xFF4A3E66),
    error = Color(0xFFD50000),
    onError = Color.White
)

@Composable
fun NumberMasterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GameDarkColorScheme else GameLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
