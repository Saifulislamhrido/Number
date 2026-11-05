package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.ui.theme.BlueActionGradient
import com.example.ui.theme.DarkPurpleSurfaceElevated
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GreenPlayGradient
import com.example.ui.theme.OrangeActionGradient
import com.example.ui.theme.PurpleGameGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PauseDialog(
    lang: Language,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onResume) {
        Game3DCard(
            modifier = Modifier
                .testTag("pause_dialog")
                .fillMaxWidth(0.9f),
            backgroundColor = DarkPurpleSurfaceElevated
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = Strings.get("pause", lang),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(24.dp))

                Game3DButton(
                    text = Strings.get("resume", lang),
                    gradient = GreenPlayGradient,
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_resume_btn"
                )
                Spacer(modifier = Modifier.height(14.dp))

                Game3DButton(
                    text = Strings.get("restart", lang),
                    gradient = OrangeActionGradient,
                    shadowColor = Color(0xFFBF360C),
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_restart_btn"
                )
                Spacer(modifier = Modifier.height(14.dp))

                Game3DButton(
                    text = Strings.get("home", lang),
                    gradient = BlueActionGradient,
                    shadowColor = Color(0xFF0D47A1),
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_home_btn"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelCompleteDialog(
    stars: Int,
    coinsEarned: Int,
    score: Int,
    lang: Language,
    onNextLevel: () -> Unit,
    onHome: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onNextLevel) {
        Game3DCard(
            modifier = Modifier
                .testTag("level_complete_dialog")
                .fillMaxWidth(0.92f),
            backgroundColor = DarkPurpleSurfaceElevated,
            borderColor = GoldYellow
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = Strings.get("victory", lang),
                    color = GoldYellow,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = Strings.get("level_completed", lang),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3D Star rating row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isStarAchieved = i <= stars
                        val scale by animateFloatAsState(
                            targetValue = if (isStarAchieved) 1.15f else 0.85f,
                            animationSpec = spring(dampingRatio = 0.5f),
                            label = "star_scale_$i"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .scale(scale)
                                .size(48.dp)
                                .background(
                                    if (isStarAchieved) GoldYellow else Color(0xFF424242),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $i",
                                tint = if (isStarAchieved) Color.White else Color(0xFF757575),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coins earned banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x3300E676), CircleShape)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${Strings.get("earned_coins", lang)} +${Strings.formatNumber(coinsEarned, lang)}",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(GoldYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }

                if (stars == 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⭐ ${Strings.get("perfect_bonus", lang)} ⭐",
                        color = GoldYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Game3DButton(
                    text = Strings.get("next_level", lang),
                    gradient = GreenPlayGradient,
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "victory_next_level_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Game3DButton(
                    text = Strings.get("home", lang),
                    gradient = BlueActionGradient,
                    shadowColor = Color(0xFF0D47A1),
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "victory_home_btn"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOverDialog(
    score: Int,
    lang: Language,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onRetry) {
        Game3DCard(
            modifier = Modifier
                .testTag("game_over_dialog")
                .fillMaxWidth(0.9f),
            backgroundColor = DarkPurpleSurfaceElevated,
            borderColor = Color(0xFFFF5252)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = Strings.get("game_over", lang),
                    color = Color(0xFFFF5252),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${Strings.get("score", lang)}: ${Strings.formatNumber(score, lang)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Game3DButton(
                    text = Strings.get("try_again", lang),
                    gradient = OrangeActionGradient,
                    shadowColor = Color(0xFFBF360C),
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "game_over_retry_btn"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Game3DButton(
                    text = Strings.get("home", lang),
                    gradient = BlueActionGradient,
                    shadowColor = Color(0xFF0D47A1),
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "game_over_home_btn"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HintDialog(
    hintText: String,
    hintCost: Int,
    userCoins: Int,
    freeHintsRemaining: Int,
    lang: Language,
    isRevealed: Boolean,
    onUseFreeHint: () -> Unit,
    onBuyHintWithCoins: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Game3DCard(
            modifier = Modifier
                .testTag("hint_dialog")
                .fillMaxWidth(0.92f),
            backgroundColor = DarkPurpleSurfaceElevated,
            borderColor = Color(0xFF00E5FF)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 ${Strings.get("hint", lang)}",
                    color = Color(0xFF00E5FF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isRevealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x2200E5FF), CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hintText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Game3DButton(
                        text = Strings.get("close", lang),
                        gradient = BlueActionGradient,
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = Strings.get("use_hint_cost", lang),
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (freeHintsRemaining > 0) {
                        Game3DButton(
                            text = "${Strings.get("free_hint", lang)} ($freeHintsRemaining)",
                            gradient = GreenPlayGradient,
                            onClick = onUseFreeHint,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Game3DButton(
                        text = "Unlock (20 Coins)",
                        gradient = if (userCoins >= hintCost) OrangeActionGradient else BlueActionGradient,
                        enabled = userCoins >= hintCost,
                        onClick = onBuyHintWithCoins,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (userCoins < hintCost && freeHintsRemaining <= 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Strings.get("not_enough_coins", lang),
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Game3DButton(
                        text = Strings.get("cancel", lang),
                        gradient = Brush.verticalGradient(listOf(Color(0xFF616161), Color(0xFF424242))),
                        shadowColor = Color(0xFF212121),
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
