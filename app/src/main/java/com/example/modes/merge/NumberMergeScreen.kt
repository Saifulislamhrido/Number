package com.example.modes.merge

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
import com.example.ui.theme.BlueActionGradient
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.OrangeActionGradient
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun NumberMergeScreen(
    levelNumber: Int,
    targetValue: Int,
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
    val gridSize = 4
    val totalCells = gridSize * gridSize

    var movesLeft by remember { mutableIntStateOf(initialMoves) }
    var score by remember { mutableIntStateOf(0) }
    var highestTile by remember { mutableIntStateOf(2) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }

    var firstSelectedIndex by remember { mutableIntStateOf(-1) }
    var hintIndex1 by remember { mutableIntStateOf(-1) }
    var hintIndex2 by remember { mutableIntStateOf(-1) }

    val board = remember {
        mutableStateListOf<Int>().apply {
            val starterNumbers = listOf(2, 4, 8, 2, 4, 2, 8, 4, 2, 4, 2, 8, 4, 2, 4, 2)
            addAll(starterNumbers.shuffled(Random(levelNumber * 31)))
        }
    }

    // Undo stack: stores snapshots of (board, score, movesLeft)
    val undoStack = remember { mutableStateListOf<Triple<List<Int>, Int, Int>>() }

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun restartGame() {
        movesLeft = initialMoves
        score = 0
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        firstSelectedIndex = -1
        hintIndex1 = -1
        hintIndex2 = -1
        isHintRevealed = false
        undoStack.clear()
        board.clear()
        val starterNumbers = listOf(2, 4, 8, 2, 4, 2, 8, 4, 2, 4, 2, 8, 4, 2, 4, 2)
        board.addAll(starterNumbers.shuffled(Random(levelNumber * 31)))
        highestTile = board.maxOrNull() ?: 2
    }

    fun isAdjacent(idx1: Int, idx2: Int): Boolean {
        val r1 = idx1 / gridSize
        val c1 = idx1 % gridSize
        val r2 = idx2 / gridSize
        val c2 = idx2 % gridSize
        return (kotlin.math.abs(r1 - r2) + kotlin.math.abs(c1 - c2)) == 1
    }

    fun onTileClick(index: Int) {
        if (isPaused || isWon || isGameOver) return
        val currentVal = board[index]
        if (currentVal == 0) return

        soundManager.playClick()

        if (firstSelectedIndex == -1) {
            firstSelectedIndex = index
        } else {
            val first = firstSelectedIndex
            firstSelectedIndex = -1

            if (first != index && isAdjacent(first, index) && board[first] == board[index]) {
                // Save undo snapshot
                undoStack.add(Triple(board.toList(), score, movesLeft))
                if (undoStack.size > 5) undoStack.removeAt(0)

                // Perform Merge!
                val mergedVal = board[first] * 2
                board[index] = mergedVal
                // Spawn a new tile in the emptied cell
                val spawnVal = if (Random.nextFloat() < 0.75f) 2 else 4
                board[first] = spawnVal

                movesLeft--
                score += mergedVal
                soundManager.playMerge()

                if (mergedVal > highestTile) {
                    highestTile = mergedVal
                }

                if (mergedVal >= targetValue) {
                    isWon = true
                    soundManager.playLevelComplete()
                    val stars = if (movesLeft >= initialMoves / 2) 3 else if (movesLeft >= 2) 2 else 1
                    onWin(stars, score, elapsedSeconds)
                } else if (movesLeft <= 0) {
                    isGameOver = true
                    soundManager.playWrong()
                }
            } else {
                // Not a match or not adjacent - switch selection
                firstSelectedIndex = index
            }
        }
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val snapshot = undoStack.removeAt(undoStack.lastIndex)
            board.clear()
            board.addAll(snapshot.first)
            score = snapshot.second
            movesLeft = snapshot.third
            soundManager.playMove()
            firstSelectedIndex = -1
        }
    }

    fun triggerHint() {
        // Find adjacent identical pair
        for (i in 0 until totalCells) {
            val r = i / gridSize
            val c = i % gridSize
            // check right
            if (c + 1 < gridSize && board[i] == board[i + 1]) {
                hintIndex1 = i
                hintIndex2 = i + 1
                break
            }
            // check down
            if (r + 1 < gridSize && board[i] == board[i + gridSize]) {
                hintIndex1 = i
                hintIndex2 = i + gridSize
                break
            }
        }
        isHintRevealed = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_merge_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                            Text(text = Strings.get("target", lang), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = Strings.formatNumber(targetValue, lang),
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

                Spacer(modifier = Modifier.height(14.dp))

                // Board of 4x4 tiles
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
                        columns = GridCells.Fixed(gridSize),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(board) { index, value ->
                            val isSelected = index == firstSelectedIndex || index == hintIndex1 || index == hintIndex2
                            Game3DNumberTile(
                                value = value,
                                isSelected = isSelected,
                                onClick = { onTileClick(index) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Control Actions: Undo, Hint, Restart
                Row(modifier = Modifier.fillMaxWidth()) {
                    Game3DButton(
                        text = "⤺ ${Strings.get("undo", lang)}",
                        gradient = BlueActionGradient,
                        shadowColor = Color(0xFF0D47A1),
                        enabled = undoStack.isNotEmpty(),
                        onClick = { performUndo() },
                        modifier = Modifier.weight(1f),
                        testTag = "merge_undo_btn"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Game3DButton(
                        text = "💡 ${Strings.get("hint", lang)}",
                        gradient = OrangeActionGradient,
                        shadowColor = Color(0xFFBF360C),
                        onClick = { showHintDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "merge_hint_btn"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Game3DButton(
                        text = Strings.get("restart", lang),
                        onClick = { restartGame() },
                        modifier = Modifier.weight(1f),
                        testTag = "merge_restart_btn"
                    )
                }
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
                val stars = if (movesLeft >= initialMoves / 2) 3 else if (movesLeft >= 2) 2 else 1
                LevelCompleteDialog(
                    stars = stars,
                    coinsEarned = 30 + (stars * 10),
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
