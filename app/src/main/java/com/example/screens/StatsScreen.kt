package com.example.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import com.example.data.model.AchievementEntity
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

@Composable
fun StatsScreen(
    coins: Int,
    completedLevels: Int,
    totalStars: Int,
    achievements: List<AchievementEntity>,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onClaimAchievement: (AchievementEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = Strings.get("stats_achievements", lang),
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
                    .widthIn(max = 540.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Stats Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Game3DCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = DarkPurpleSurface
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Levels Done", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                                Text(
                                    text = "${Strings.formatNumber(completedLevels, lang)}/100",
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Game3DCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = DarkPurpleSurface
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Total Stars", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Strings.formatNumber(totalStars, lang),
                                        color = GoldYellow,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🏆 ${Strings.get("achievements_title", lang)}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Achievements List
                items(achievements) { ach ->
                    val isCompleted = completedLevels >= ach.targetValue || ach.isUnlocked
                    val isClaimed = ach.isClaimed

                    Game3DCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkPurpleSurface,
                        borderColor = if (isCompleted && !isClaimed) GoldYellow else Color(0x33FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompleted) GoldYellow else Color(0x22FFFFFF)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isClaimed) Icons.Default.Check else Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (isCompleted) Color.Black else Color(0xFF757575),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.titleKey,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ach.descKey,
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (isClaimed) {
                                Text(
                                    text = "✓ Claimed",
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            } else if (isCompleted) {
                                Game3DButton(
                                    text = "Claim +${ach.rewardCoins}",
                                    fontSize = 12,
                                    height = 40.dp,
                                    gradient = GreenPlayGradient,
                                    onClick = {
                                        soundManager.playCoin()
                                        onClaimAchievement(ach.copy(isUnlocked = true))
                                    },
                                    testTag = "claim_ach_${ach.id}"
                                )
                            } else {
                                Text(
                                    text = "+${ach.rewardCoins} $",
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
