package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.getTileColor

@Composable
fun Game3DNumberTile(
    value: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isMatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "number_tile_$value"
) {
    val (primaryColor, shadowColor) = getTileColor(value)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else if (isMatched) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "tile_scale"
    )

    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .testTag(testTag)
            .aspectRatio(1f)
            .scale(scale)
            .then(
                if (onClick != null && value != 0) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value == 0) {
            // Empty placeholder tile
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(shape)
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, Color(0x11FFFFFF), shape)
            )
        } else {
            // 3D Bevel Bottom Shadow Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = 4.dp)
                    .clip(shape)
                    .background(shadowColor)
            )

            // 3D Tile Face
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(if (isSelected) 8.dp else 4.dp, shape)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0x66FFFFFF),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Top Gleam Highlight
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x33FFFFFF),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = when {
                        value >= 1000 -> 16.sp
                        value >= 100 -> 20.sp
                        else -> 24.sp
                    },
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
