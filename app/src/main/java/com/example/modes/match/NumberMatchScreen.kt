package com.example.modes.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.Language
import com.example.i18n.Strings
import com.example.sound.SoundManager
import com.example.ui.components.Game3DButton
import com.example.ui.components.Game3DCard
import com.example.ui.components.Game3DNumberTile
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

@Composable
fun NumberMatchScreen(
    levelNumber: Int,
    targetPairs: Int,
    initialMoves: Int,
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
    var movesLeft by remember { mutableIntStateOf(initialMoves) }
    var pairsMatched by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Dialog states
    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }
    var highlightedIndex1 by remember { mutableIntStateOf(-1) }
    var highlightedIndex2 by remember { mutableIntStateOf(-1) }

    // Sound toggle state
    var isSoundOn by remember { mutableStateOf(soundManager.isSoundEnabled) }

    // Generate tiles for targetPairs (each pair duplicated)
    val totalTiles = targetPairs * 2
    val gridCols = if (totalTiles <= 12) 3 else 4

    val tileValues = remember(levelNumber) {
        val numbers = (1..targetPairs).map { it * 2 } // e.g. 2, 4, 6, 8...
        (numbers + numbers).shuffled()
    }

    val matchedIndices = remember { mutableStateListOf<Int>() }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Timer
    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun restartGame() {
        movesLeft = initialMoves
        pairsMatched = 0
        score = 0
        elapsedSeconds = 0
        matchedIndices.clear()
        selectedIndex = -1
        isWon = false
        isGameOver = false
        isPaused = false
        highlightedIndex1 = -1
        highlightedIndex2 = -1
        isHintRevealed = false
    }

    fun onTileClick(index: Int) {
        if (isPaused || isWon || isGameOver) return
        if (matchedIndices.contains(index)) return
        if (selectedIndex == index) return

        soundManager.playClick()

        if (selectedIndex == -1) {
            selectedIndex = index
        } else {
            movesLeft--
            val firstIndex = selectedIndex
            selectedIndex = -1

            if (tileValues[firstIndex] == tileValues[index]) {
                // Match!
                matchedIndices.add(firstIndex)
                matchedIndices.add(index)
                pairsMatched++
                score += 100 + (movesLeft * 10)
                soundManager.playMerge()

                if (pairsMatched >= targetPairs) {
                    isWon = true
                    soundManager.playLevelComplete()
                    val stars = if (movesLeft >= initialMoves / 2) 3 else if (movesLeft >= 2) 2 else 1
                    onWin(stars, score, elapsedSeconds)
                }
            } else {
                soundManager.playWrong()
                if (movesLeft <= 0) {
                    isGameOver = true
                }
            }
        }
    }

    fun triggerHint() {
        // Find an unmatched pair
        val unmatched = (0 until totalTiles).filter { !matchedIndices.contains(it) }
        val pair = unmatched.groupBy { tileValues[it] }.values.firstOrNull { it.size >= 2 }
        if (pair != null) {
            highlightedIndex1 = pair[0]
            highlightedIndex2 = pair[1]
        }
        isHintRevealed = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_match_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
                coins = coins,
                lang = lang,
                onBackClick = onBack,
                onPauseClick = { isPaused = true },
                isSoundOn = isSoundOn,
                onToggleSound = {
                    isSoundOn = !isSoundOn
                    soundManager.isSoundEnabled = isSoundOn
                }
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
                // HUD Cards (Target, Moves Left, Score)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Game3DCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkPurpleSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = Strings.get("target", lang), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "${Strings.formatNumber(pairsMatched, lang)}/${Strings.formatNumber(targetPairs, lang)}",
                                color = GoldYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Game3DCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkPurpleSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = Strings.get("moves", lang), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = Strings.formatNumber(movesLeft, lang),
                                color = if (movesLeft <= 3) Color(0xFFFF5252) else Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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

                // Board of Tiles
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkPurpleSurface)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridCols),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(tileValues) { index, value ->
                            val isMatched = matchedIndices.contains(index)
                            val isSelected = selectedIndex == index || index == highlightedIndex1 || index == highlightedIndex2
                            Game3DNumberTile(
                                value = if (isMatched) 0 else value,
                                isSelected = isSelected,
                                isMatched = isMatched,
                                onClick = { onTileClick(index) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Controls: Hint & Restart
                Row(modifier = Modifier.fillMaxWidth()) {
                    Game3DButton(
                        text = "💡 ${Strings.get("hint", lang)}",
                        gradient = OrangeActionGradient,
                        shadowColor = Color(0xFFBF360C),
                        onClick = { showHintDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "match_hint_btn"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Game3DButton(
                        text = Strings.get("restart", lang),
                        onClick = { restartGame() },
                        modifier = Modifier.weight(1f),
                        testTag = "match_restart_btn"
                    )
                }
            }

            // Dialogs
            if (isPaused) {
                PauseDialog(
                    lang = lang,
                    onResume = { isPaused = false },
                    onRestart = { restartGame() },
                    onHome = onBack
                )
            }

            if (isWon) {
                val stars = if (movesLeft >= initialMoves / 2) 3 else if (movesLeft >= 2) 2 else 1
                LevelCompleteDialog(
                    stars = stars,
                    coinsEarned = 25 + (stars * 10),
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
                    hintText = hintText,
                    hintCost = 20,
                    userCoins = coins,
                    freeHintsRemaining = freeHints,
                    lang = lang,
                    isRevealed = isHintRevealed,
                    onUseFreeHint = {
                        if (onUseFreeHint()) {
                            triggerHint()
                        }
                    },
                    onBuyHintWithCoins = {
                        if (onSpendCoinsForHint()) {
                            triggerHint()
                        }
                    },
                    onDismiss = { showHintDialog = false }
                )
            }
        }
    }
}
