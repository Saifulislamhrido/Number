package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 3D Game Palette - Vibrant & Punchy
val DeepSpaceBackground = Color(0xFF0F0C20)
val DarkPurpleSurface = Color(0xFF1E1738)
val DarkPurpleSurfaceElevated = Color(0xFF2B2150)
val PurpleAccent = Color(0xFF7C4DFF)
val BrightCyan = Color(0xFF00E5FF)
val BrightEmerald = Color(0xFF00E676)
val BrightAmber = Color(0xFFFFD600)
val BrightOrange = Color(0xFFFF6D00)
val BrightCoral = Color(0xFFFF3D00)
val BrightPink = Color(0xFFFF4081)
val GoldYellow = Color(0xFFFFC107)
val GoldDark = Color(0xFFC79100)

// Gradients
val PurpleGameGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2A1B54), Color(0xFF130924))
)

val GoldenCoinGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFEE58), Color(0xFFFFA000))
)

val GreenPlayGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00A152))
)

val BlueActionGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF00B0FF), Color(0xFF2979FF))
)

val OrangeActionGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00))
)

val VioletActionGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFB388FF), Color(0xFF7C4DFF))
)

// Dynamic 3D Number Tile Colors for Powers of 2 & Grid elements
fun getTileColor(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(Color(0xFF261D42), Color(0xFF1C1533)) // Empty
        2 -> Pair(Color(0xFF00E5FF), Color(0xFF00B0FF))
        4 -> Pair(Color(0xFF00E676), Color(0xFF00C853))
        8 -> Pair(Color(0xFFFFD600), Color(0xFFFFAB00))
        16 -> Pair(Color(0xFFFF9100), Color(0xFFFF6D00))
        32 -> Pair(Color(0xFFFF5252), Color(0xFFFF1744))
        64 -> Pair(Color(0xFFFF4081), Color(0xFFF50057))
        128 -> Pair(Color(0xFFE040FB), Color(0xFFD500F9))
        256 -> Pair(Color(0xFF7C4DFF), Color(0xFF651FFF))
        512 -> Pair(Color(0xFF536DFE), Color(0xFF3D5AFE))
        1024 -> Pair(Color(0xFFFFAB00), Color(0xFFFF6D00))
        2048 -> Pair(Color(0xFFFFD700), Color(0xFFFF8F00))
        4096 -> Pair(Color(0xFF00BFA5), Color(0xFF00897B))
        8192 -> Pair(Color(0xFFFF1744), Color(0xFFD50000))
        else -> Pair(Color(0xFF7C4DFF), Color(0xFF512DA8))
    }
}
