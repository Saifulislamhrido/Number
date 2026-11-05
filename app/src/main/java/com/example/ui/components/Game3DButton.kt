package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenPlayGradient

@Composable
fun Game3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    gradient: Brush = GreenPlayGradient,
    shadowColor: Color = Color(0xFF00701A),
    textColor: Color = Color.White,
    fontSize: Int = 18,
    height: Dp = 56.dp,
    enabled: Boolean = true,
    testTag: String = "game_3d_button",
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val depth = 6.dp
    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) depth - 2.dp else 0.dp,
        label = "btn_press"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(height + depth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Bottom 3D Extrusion Shadow Layer
        Box(
            modifier = Modifier
                .offset(y = depth)
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(18.dp))
                .background(if (enabled) shadowColor else Color(0xFF333333))
        )

        // Top Tactile Surface
        Box(
            modifier = Modifier
                .offset(y = animatedOffset)
                .fillMaxWidth()
                .height(height)
                .shadow(
                    elevation = if (isPressed) 2.dp else 6.dp,
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (enabled) gradient else Brush.verticalGradient(
                        listOf(Color(0xFF666666), Color(0xFF444444))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (content != null) {
                    content()
                } else {
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
