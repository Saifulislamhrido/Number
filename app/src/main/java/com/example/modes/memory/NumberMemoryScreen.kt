package com.example.modes.memory

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameTopBar
import com.example.ui.components.HintDialog
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.PauseDialog
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.OrangeActionGradient
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val value: Int,
    val isRevealed: Boolean,
    val isTappedCorrectly: Boolean
)

@Composable
fun NumberMemoryScreen(
    levelNumber: Int,
    memoryCount: Int = 4,
    memorizeTimeSeconds: Int = 4,
    coins: Int,
    freeHints: Int,
    hintText: String,
    lang: Language,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onWin: (stars: Int, score: Int, time: Int) -> Unit,
    onSpendCoinsForHint: () -> Boolean,
    onUseFreeHint: () -> Boolean,
    modifier: Modifier = Modifier
) {
    var score by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var memorizeCountdown by remember { mutableIntStateOf(memorizeTimeSeconds) }
    var isMemorizingPhase by remember { mutableStateOf(true) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }

    val cards = remember(levelNumber) {
        val rand = Random(levelNumber * 400)
        val distinctNumbers = mutableSetOf<Int>()
        while (distinctNumbers.size < memoryCount) {
            distinctNumbers.add(rand.nextInt(1, 50))
        }
        val sortedList = distinctNumbers.toList().sorted()
        val shuffledList = sortedList.shuffled(rand)
        mutableStateListOf<MemoryCard>().apply {
            shuffledList.forEachIndexed { i, num ->
                add(MemoryCard(id = i, value = num, isRevealed = true, isTappedCorrectly = false))
            }
        }
    }

    // Next required value to tap (ascending order)
    val sortedTargetValues = remember(cards) {
        cards.map { it.value }.sorted()
    }
    var currentTargetIdx by remember { mutableIntStateOf(0) }

    // Memorize phase countdown
    LaunchedEffect(isMemorizingPhase, isPaused, isWon, isGameOver) {
        if (isMemorizingPhase) {
            memorizeCountdown = memorizeTimeSeconds
            while (memorizeCountdown > 0 && !isPaused) {
                delay(1000)
                memorizeCountdown--
            }
            if (memorizeCountdown <= 0) {
                isMemorizingPhase = false
                // Hide all cards
                cards.indices.forEach { i ->
                    cards[i] = cards[i].copy(isRevealed = false)
                }
                soundManager.playMove()
            }
        }
    }

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun restartGame() {
        score = 0
        elapsedSeconds = 0
        currentTargetIdx = 0
        memorizeCountdown = memorizeTimeSeconds
        isMemorizingPhase = true
        isWon = false
        isGameOver = false
        isPaused = false
        isHintRevealed = false

        val rand = Random(System.currentTimeMillis())
        val distinctNumbers = mutableSetOf<Int>()
        while (distinctNumbers.size < memoryCount) {
            distinctNumbers.add(rand.nextInt(1, 50))
        }
        val shuffled = distinctNumbers.toList().shuffled(rand)
        cards.clear()
        shuffled.forEachIndexed { i, num ->
            cards.add(MemoryCard(id = i, value = num, isRevealed = true, isTappedCorrectly = false))
        }
    }

    fun onCardClick(index: Int) {
        if (isPaused || isWon || isGameOver || isMemorizingPhase) return
        val card = cards[index]
        if (card.isTappedCorrectly) return

        val expectedValue = sortedTargetValues[currentTargetIdx]
        if (card.value == expectedValue) {
            // Correct tap!
            cards[index] = card.copy(isRevealed = true, isTappedCorrectly = true)
            currentTargetIdx++
            score += 150
            soundManager.playCorrect()

            if (currentTargetIdx >= sortedTargetValues.size) {
                isWon = true
                soundManager.playLevelComplete()
                onWin(3, score, elapsedSeconds)
            }
        } else {
            // Wrong tap!
            soundManager.playWrong()
            isGameOver = true
        }
    }

    fun triggerHint() {
        // Peek the next required card
        val expected = sortedTargetValues[currentTargetIdx]
        val targetIdx = cards.indexOfFirst { it.value == expected }
        if (targetIdx != -1) {
            cards[targetIdx] = cards[targetIdx].copy(isRevealed = true)
        }
        isHintRevealed = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_memory_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
                coins = coins,
                lang = lang,
                onBackClick = onBack,
                onPauseClick = { isPaused = true },
                isSoundOn = soundManager.isSoundEnabled,
                onToggleSound = { soundManager.isSoundEnabled = !soundManager.isSoundEnabled }
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
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Game3DCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkPurpleSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Target", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "${Strings.formatNumber(currentTargetIdx, lang)}/${Strings.formatNumber(memoryCount, lang)}",
                                color = GoldYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Game3DCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkPurpleSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = Strings.get("score", lang), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = Strings.formatNumber(score, lang),
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phase Instruction & Timer
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface,
                    borderColor = if (isMemorizingPhase) GoldYellow else Color(0xFF00E5FF)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = if (isMemorizingPhase) Strings.get("memorize_time", lang) else Strings.get("tap_in_order", lang),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (isMemorizingPhase) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${Strings.formatNumber(memorizeCountdown, lang)}s",
                                color = GoldYellow,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { memorizeCountdown.toFloat() / memorizeTimeSeconds.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = GoldYellow,
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Memory Cards Grid
                val gridCols = if (cards.size <= 4) 2 else 3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridCols),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(cards) { index, card ->
                        val showFace = card.isRevealed || isMemorizingPhase
                        Box(
                            modifier = Modifier
                                .testTag("memory_card_$index")
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        card.isTappedCorrectly -> Color(0xFF00E676)
                                        showFace -> Color(0xFF7C4DFF)
                                        else -> Color(0xFF261D42)
                                    }
                                )
                                .border(
                                    2.dp,
                                    if (card.isTappedCorrectly) Color.White else Color(0x44FFFFFF),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onCardClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (showFace) {
                                Text(
                                    text = card.value.toString(),
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                Text(
                                    text = "?",
                                    color = Color(0x66FFFFFF),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hint Button
                Game3DButton(
                    text = "💡 ${Strings.get("hint", lang)} (Peek next number)",
                    gradient = OrangeActionGradient,
                    shadowColor = Color(0xFFBF360C),
                    enabled = !isMemorizingPhase,
                    onClick = { showHintDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "memory_hint_btn"
                )
            }

            if (isPaused) {
                PauseDialog(
                    lang = lang,
                    onResume = { isPaused = false },
                    onRestart = { restartGame() },
                    onHome = onBack
                )
            }

            if (isWon) {
                LevelCompleteDialog(
                    stars = 3,
                    coinsEarned = 30,
                    score = score,
                    lang = lang,
                    onNextLevel = onBack,
                    onHome = onBack
                )
            }

            if (isGameOver) {
                GameOverDialog(
                    score = score,
                    lang = lang,
                    onRetry = { restartGame() },
                    onHome = onBack
                )
            }

            if (showHintDialog) {
                HintDialog(
                    hintText = "Next smallest number revealed!",
                    hintCost = 20,
                    userCoins = coins,
                    freeHintsRemaining = freeHints,
                    lang = lang,
                    isRevealed = isHintRevealed,
                    onUseFreeHint = {
                        if (onUseFreeHint()) triggerHint()
                    },
                    onBuyHintWithCoins = {
                        if (onSpendCoinsForHint()) triggerHint()
                    },
                    onDismiss = { showHintDialog = false }
                )
            }
        }
    }
}
