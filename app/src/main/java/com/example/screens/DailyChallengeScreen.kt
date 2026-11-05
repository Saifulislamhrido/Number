package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.Game3DButton
import com.example.ui.components.Game3DCard
import com.example.ui.components.GameTopBar
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GreenPlayGradient
import com.example.ui.theme.OrangeActionGradient

@Composable
fun DailyChallengeScreen(
    coins: Int,
    streak: Int,
    canClaim: Boolean,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onClaimReward: () -> Int,
    onPlayDailyPuzzle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var claimedBonus by remember { mutableStateOf<Int?>(null) }
    var hasClaimedLocally by remember { mutableStateOf(!canClaim) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = Strings.get("daily_challenge", lang),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Streak Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface,
                    borderColor = GoldYellow
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🔥 ${Strings.get("streak_days", lang)}",
                            color = Color(0xFFFF9100),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${Strings.formatNumber(streak, lang)} Days",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // 7-day bonus tracker row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (day in 1..7) {
                                val isPastOrCurrent = day <= streak
                                val isToday = day == streak
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isToday) GoldYellow else if (isPastOrCurrent) Color(0xFF00E676) else Color(0x33FFFFFF)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "D$day",
                                        color = if (isToday) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Daily Reward Claim Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎁 Daily Gift Chest",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hasClaimedLocally) Strings.get("daily_reward_claimed", lang) else "Claim your daily bonus coins!",
                            color = Color(0xFFB0BEC5),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        if (claimedBonus != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🎉 +${Strings.formatNumber(claimedBonus!!, lang)} ${Strings.get("coins", lang)}!",
                                color = Color(0xFF00E676),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Game3DButton(
                            text = if (hasClaimedLocally) "✓ ${Strings.get("claimed", lang)}" else "🌟 ${Strings.get("claim", lang)} (+${50 + (streak.coerceAtMost(7) - 1) * 25} Coins)",
                            gradient = if (hasClaimedLocally) Brush.verticalGradient(listOf(Color(0xFF616161), Color(0xFF424242))) else GreenPlayGradient,
                            enabled = !hasClaimedLocally,
                            onClick = {
                                val amount = onClaimReward()
                                claimedBonus = amount
                                hasClaimedLocally = true
                                soundManager.playCoin()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "daily_claim_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Daily Brain Puzzle Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface,
                    borderColor = Color(0xFF00E5FF)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🧠 Today's Special Challenge",
                            color = Color(0xFF00E5FF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Solve today's featured brain puzzle for a +100 bonus coin reward!",
                            color = Color(0xFFB0BEC5),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Game3DButton(
                            text = "⚡ START TODAY'S PUZZLE",
                            gradient = OrangeActionGradient,
                            shadowColor = Color(0xFFBF360C),
                            onClick = {
                                soundManager.playClick()
                                onPlayDailyPuzzle()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "start_daily_puzzle_btn"
                        )
                    }
                }
            }
        }
    }
}
