package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.levels.GameModeType
import com.example.sound.SoundManager
import com.example.ui.components.Game3DButton
import com.example.ui.components.Game3DCard
import com.example.ui.components.GameTopBar
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GreenPlayGradient

data class ModeCardInfo(
    val mode: GameModeType,
    val iconEmoji: String,
    val accentColor: Color
)

@Composable
fun GameModesScreen(
    coins: Int,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onSelectMode: (GameModeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val modesList = listOf(
        ModeCardInfo(GameModeType.NUMBER_MATCH, "🎯", Color(0xFF00E5FF)),
        ModeCardInfo(GameModeType.NUMBER_MERGE, "🧬", Color(0xFF00E676)),
        ModeCardInfo(GameModeType.GAME_2048, "🎲", Color(0xFFFFD600)),
        ModeCardInfo(GameModeType.BLOCK_PUZZLE, "🧱", Color(0xFFFF9100)),
        ModeCardInfo(GameModeType.MATH_CHALLENGE, "⚡", Color(0xFFFF4081)),
        ModeCardInfo(GameModeType.MISSING_NUMBER, "🔍", Color(0xFF7C4DFF)),
        ModeCardInfo(GameModeType.NUMBER_SEQUENCE, "📈", Color(0xFF00B0FF)),
        ModeCardInfo(GameModeType.NUMBER_MEMORY, "🧠", Color(0xFFE040FB))
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = Strings.get("game_modes", lang),
                coins = coins,
                lang = lang,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepSpaceBackground),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(modesList) { info ->
                    Game3DCard(
                        modifier = Modifier
                            .testTag("mode_card_${info.mode.id}")
                            .fillMaxWidth()
                            .clickable {
                                soundManager.playClick()
                                onSelectMode(info.mode)
                            },
                        backgroundColor = DarkPurpleSurface,
                        borderColor = info.accentColor.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3D Mode Icon Badge
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(info.accentColor, info.accentColor.copy(alpha = 0.3f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = info.iconEmoji, fontSize = 28.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = Strings.get(info.mode.titleKey, lang),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Strings.get(info.mode.descKey, lang),
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Game3DButton(
                                text = "PLAY",
                                fontSize = 13,
                                height = 44.dp,
                                gradient = GreenPlayGradient,
                                onClick = {
                                    soundManager.playClick()
                                    onSelectMode(info.mode)
                                },
                                testTag = "play_${info.mode.id}"
                            )
                        }
                    }
                }
            }
        }
    }
}
