package com.example.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.sound.SoundManager
import com.example.ui.components.CoinBadge
import com.example.ui.components.Game3DButton
import com.example.ui.components.Game3DCard
import com.example.ui.theme.BlueActionGradient
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GreenPlayGradient
import com.example.ui.theme.OrangeActionGradient
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.VioletActionGradient

@Composable
fun HomeScreen(
    coins: Int,
    completedLevelsCount: Int,
    totalStars: Int,
    currentLevel: Int,
    lang: Language,
    soundManager: SoundManager,
    onPlayCurrentLevel: () -> Unit,
    onNavigateGameModes: () -> Unit,
    onNavigateLevelSelect: () -> Unit,
    onNavigateDailyChallenge: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateSettings: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Breathing Mascot Animation
    val infiniteTransition = rememberInfiniteTransition(label = "home_anim")
    val mascotFloatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepSpaceBackground),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Language toggle, Sound toggle, Coin Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Switch Pill
                    Box(
                        modifier = Modifier
                            .testTag("lang_toggle_btn")
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp))
                            .clickable {
                                soundManager.playClick()
                                onToggleLanguage()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lang == Language.EN) "🇺🇸 EN" else "🇧🇩 বাংলা",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sound toggle
                        Box(
                            modifier = Modifier
                                .testTag("home_sound_toggle")
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .clickable {
                                    soundManager.playClick()
                                    onToggleSound()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (soundManager.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Sound",
                                tint = if (soundManager.isSoundEnabled) Color(0xFF00E5FF) else Color(0xFFB0BEC5),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        CoinBadge(coins = coins, lang = lang)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3D Animated Number Mascot & Logo
                Box(
                    modifier = Modifier
                        .offset(y = mascotFloatY.dp)
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glowing background ring
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(16.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF7C4DFF), Color(0xFF1E1738))
                                )
                            )
                            .border(2.dp, Color(0xFF00E5FF), CircleShape)
                    )

                    // 3D Number Character "8" with playful eyes
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.offset(y = 4.dp)
                        ) {
                            // Eyes
                            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                        }
                        Text(
                            text = "3D",
                            color = GoldYellow,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Strings.get("app_name", lang),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = Strings.get("subtitle", lang),
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Player Progress Summary Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${Strings.get("level", lang)}: ${Strings.formatNumber(currentLevel, lang)}/100",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Stars",
                                    tint = GoldYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.formatNumber(totalStars, lang),
                                    color = GoldYellow,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (completedLevelsCount.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = Color(0xFF00E676),
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Big Primary Action: PLAY CURRENT LEVEL
                Game3DButton(
                    text = "▶ ${Strings.get("play", lang)} (Lv ${Strings.formatNumber(currentLevel, lang)})",
                    fontSize = 20,
                    height = 64.dp,
                    gradient = GreenPlayGradient,
                    shadowColor = Color(0xFF00701A),
                    onClick = {
                        soundManager.playClick()
                        onPlayCurrentLevel()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_play_btn"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // GAME MODES button (Browse all 8 modes)
                Game3DButton(
                    text = "🎮 ${Strings.get("game_modes", lang)}",
                    gradient = BlueActionGradient,
                    shadowColor = Color(0xFF0D47A1),
                    onClick = {
                        soundManager.playClick()
                        onNavigateGameModes()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "home_game_modes_btn"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // LEVEL SELECT & DAILY CHALLENGE (2 buttons in a row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Game3DButton(
                        text = "🗺️ ${Strings.get("level_select", lang)}",
                        fontSize = 15,
                        gradient = VioletActionGradient,
                        shadowColor = Color(0xFF512DA8),
                        onClick = {
                            soundManager.playClick()
                            onNavigateLevelSelect()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "home_level_select_btn"
                    )

                    Game3DButton(
                        text = "🎁 ${Strings.get("daily_challenge", lang)}",
                        fontSize = 15,
                        gradient = OrangeActionGradient,
                        shadowColor = Color(0xFFBF360C),
                        onClick = {
                            soundManager.playClick()
                            onNavigateDailyChallenge()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "home_daily_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STATS & SETTINGS row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Game3DButton(
                        text = "🏆 ${Strings.get("stats_achievements", lang)}",
                        fontSize = 14,
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFF00BFA5), Color(0xFF00796B))
                        ),
                        shadowColor = Color(0xFF004D40),
                        onClick = {
                            soundManager.playClick()
                            onNavigateStats()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "home_stats_btn"
                    )

                    Game3DButton(
                        text = "⚙️ ${Strings.get("settings", lang)}",
                        fontSize = 14,
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFF546E7A), Color(0xFF37474F))
                        ),
                        shadowColor = Color(0xFF263238),
                        onClick = {
                            soundManager.playClick()
                            onNavigateSettings()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "home_settings_btn"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
