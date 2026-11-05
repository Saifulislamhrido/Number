package com.example.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelProgressEntity
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.levels.DifficultyCategory
import com.example.levels.LevelManager
import com.example.sound.SoundManager
import com.example.ui.components.GameTopBar
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow

@Composable
fun LevelSelectScreen(
    coins: Int,
    completedLevels: List<LevelProgressEntity>,
    maxUnlockedLevel: Int,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(DifficultyCategory.EASY) }
    val categories = DifficultyCategory.entries

    val categoryLevels = remember(selectedCategory) {
        LevelManager.getLevelsForCategory(selectedCategory)
    }

    val progressMap = remember(completedLevels) {
        completedLevels.associateBy { it.levelNumber }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        GameTopBar(
            title = Strings.get("level_select", lang),
            coins = coins,
            lang = lang,
            onBackClick = onBack
        )

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            containerColor = DarkPurpleSurface,
            contentColor = GoldYellow,
            edgePadding = 16.dp,
            divider = {}
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Tab(
                    selected = isSelected,
                    onClick = {
                        soundManager.playClick()
                        selectedCategory = cat
                    },
                    text = {
                        Text(
                            text = Strings.get(cat.labelKey, lang),
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) GoldYellow else Color(0xFFB0BEC5),
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of 25 Levels for the selected category
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
            ) {
                items(categoryLevels) { lvl ->
                    val isUnlocked = lvl.levelNumber <= maxUnlockedLevel
                    val progress = progressMap[lvl.levelNumber]
                    val stars = progress?.stars ?: 0

                    Box(
                        modifier = Modifier
                            .testTag("level_item_${lvl.levelNumber}")
                            .aspectRatio(1f)
                            .shadow(if (isUnlocked) 4.dp else 0.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    !isUnlocked -> Color(0xFF1E1738)
                                    stars == 3 -> Color(0xFF7C4DFF)
                                    progress?.isCompleted == true -> Color(0xFF00B0FF)
                                    else -> Color(0xFF00E676)
                                }
                            )
                            .border(
                                width = if (lvl.levelNumber == maxUnlockedLevel) 2.dp else 1.dp,
                                color = if (lvl.levelNumber == maxUnlockedLevel) GoldYellow else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = isUnlocked) {
                                soundManager.playClick()
                                onSelectLevel(lvl.levelNumber)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF616161),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = Strings.formatNumber(lvl.levelNumber, lang),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Stars row
                                Row {
                                    for (s in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= stars) GoldYellow else Color(0x44FFFFFF),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
