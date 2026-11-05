package com.example.modes.game2048

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.BlueActionGradient
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.OrangeActionGradient
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

enum class MoveDirection { UP, DOWN, LEFT, RIGHT }

@Composable
fun Game2048Screen(
    levelNumber: Int,
    targetTile: Int,
    maxMoves: Int = 0,
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
    val size = 4
    var score by remember { mutableIntStateOf(0) }
    var movesCount by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }

    val grid = remember {
        mutableStateListOf<Int>().apply {
            repeat(16) { add(0) }
        }
    }

    val undoStack = remember { mutableStateListOf<Pair<List<Int>, Int>>() }

    fun spawnTile() {
        val emptyIndices = grid.indices.filter { grid[it] == 0 }
        if (emptyIndices.isNotEmpty()) {
            val randomPos = emptyIndices.random()
            grid[randomPos] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun restartGame() {
        grid.fill(0)
        score = 0
        movesCount = 0
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        undoStack.clear()
        spawnTile()
        spawnTile()
    }

    // Initialize board on start
    LaunchedEffect(levelNumber) {
        if (grid.all { it == 0 }) {
            spawnTile()
            spawnTile()
        }
    }

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun checkGameOver(): Boolean {
        if (grid.any { it == 0 }) return false
        // Check horizontal and vertical merges
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val current = grid[r * 4 + c]
                if (c < 3 && current == grid[r * 4 + (c + 1)]) return false
                if (r < 3 && current == grid[(r + 1) * 4 + c]) return false
            }
        }
        return true
    }

    fun move(direction: MoveDirection) {
        if (isPaused || isWon || isGameOver) return

        val prevGrid = grid.toList()
        var moved = false
        var gainedScore = 0

        fun processLine(line: List<Int>): Pair<List<Int>, Int> {
            val nonZeros = line.filter { it != 0 }.toMutableList()
            var lineScore = 0
            val mergedLine = mutableListOf<Int>()
            var i = 0
            while (i < nonZeros.size) {
                if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i + 1]) {
                    val mergedVal = nonZeros[i] * 2
                    mergedLine.add(mergedVal)
                    lineScore += mergedVal
                    i += 2
                } else {
                    mergedLine.add(nonZeros[i])
                    i++
                }
            }
            while (mergedLine.size < 4) {
                mergedLine.add(0)
            }
            return Pair(mergedLine, lineScore)
        }

        when (direction) {
            MoveDirection.LEFT -> {
                for (r in 0 until 4) {
                    val row = (0 until 4).map { grid[r * 4 + it] }
                    val (newRow, pts) = processLine(row)
                    gainedScore += pts
                    for (c in 0 until 4) {
                        grid[r * 4 + c] = newRow[c]
                    }
                }
            }
            MoveDirection.RIGHT -> {
                for (r in 0 until 4) {
                    val row = (3 downTo 0).map { grid[r * 4 + it] }
                    val (newRow, pts) = processLine(row)
                    gainedScore += pts
                    for (c in 0 until 4) {
                        grid[r * 4 + (3 - c)] = newRow[c]
                    }
                }
            }
            MoveDirection.UP -> {
                for (c in 0 until 4) {
                    val col = (0 until 4).map { grid[it * 4 + c] }
                    val (newCol, pts) = processLine(col)
                    gainedScore += pts
                    for (r in 0 until 4) {
                        grid[r * 4 + c] = newCol[r]
                    }
                }
            }
            MoveDirection.DOWN -> {
                for (c in 0 until 4) {
                    val col = (3 downTo 0).map { grid[it * 4 + c] }
                    val (newCol, pts) = processLine(col)
                    gainedScore += pts
                    for (r in 0 until 4) {
                        grid[(3 - r) * 4 + c] = newCol[r]
                    }
                }
            }
        }

        if (grid != prevGrid) {
            moved = true
            undoStack.add(Pair(prevGrid, score))
            if (undoStack.size > 8) undoStack.removeAt(0)
            score += gainedScore
            movesCount++
            spawnTile()

            if (gainedScore > 0) {
                soundManager.playMerge()
            } else {
                soundManager.playMove()
            }

            if (grid.any { it >= targetTile }) {
                isWon = true
                soundManager.playLevelComplete()
                val stars = if (maxMoves == 0 || movesCount <= maxMoves) 3 else 2
                onWin(stars, score, elapsedSeconds)
            } else if (checkGameOver()) {
                isGameOver = true
                soundManager.playWrong()
            }
        }
    }

    fun performUndo() {
        if (undoStack.isNotEmpty()) {
            val snapshot = undoStack.removeAt(undoStack.lastIndex)
            for (i in 0 until 16) {
                grid[i] = snapshot.first[i]
            }
            score = snapshot.second
            movesCount = (movesCount - 1).coerceAtLeast(0)
            soundManager.playMove()
        }
    }

    var totalDragX = 0f
    var totalDragY = 0f

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_2048_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD: Target, Score, Moves
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
                                text = Strings.formatNumber(targetTile, lang),
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
                            Text(text = Strings.get("score", lang), color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = Strings.formatNumber(score, lang),
                                color = Color(0xFF00E676),
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
                                text = Strings.formatNumber(movesCount, lang),
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Swipeable 4x4 Grid Board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkPurpleSurface)
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    totalDragX = 0f
                                    totalDragY = 0f
                                },
                                onDrag = { _, dragAmount ->
                                    totalDragX += dragAmount.x
                                    totalDragY += dragAmount.y
                                },
                                onDragEnd = {
                                    val threshold = 40f
                                    if (abs(totalDragX) > abs(totalDragY)) {
                                        if (totalDragX > threshold) move(MoveDirection.RIGHT)
                                        else if (totalDragX < -threshold) move(MoveDirection.LEFT)
                                    } else {
                                        if (totalDragY > threshold) move(MoveDirection.DOWN)
                                        else if (totalDragY < -threshold) move(MoveDirection.UP)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(grid) { _, value ->
                            Game3DNumberTile(value = value)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tactile D-Pad Controls for precision/accessibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { move(MoveDirection.LEFT) },
                        modifier = Modifier
                            .testTag("dpad_left")
                            .size(46.dp)
                            .background(Color(0x33FFFFFF), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Swipe Left", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { move(MoveDirection.UP) },
                            modifier = Modifier
                                .testTag("dpad_up")
                                .size(46.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Swipe Up", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        IconButton(
                            onClick = { move(MoveDirection.DOWN) },
                            modifier = Modifier
                                .testTag("dpad_down")
                                .size(46.dp)
                                .background(Color(0x33FFFFFF), CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Swipe Down", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { move(MoveDirection.RIGHT) },
                        modifier = Modifier
                            .testTag("dpad_right")
                            .size(46.dp)
                            .background(Color(0x33FFFFFF), CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Swipe Right", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action buttons: Undo, Hint, Restart
                Row(modifier = Modifier.fillMaxWidth()) {
                    Game3DButton(
                        text = "⤺ ${Strings.get("undo", lang)}",
                        gradient = BlueActionGradient,
                        shadowColor = Color(0xFF0D47A1),
                        enabled = undoStack.isNotEmpty(),
                        onClick = { performUndo() },
                        modifier = Modifier.weight(1f),
                        testTag = "g2048_undo_btn"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Game3DButton(
                        text = "💡 ${Strings.get("hint", lang)}",
                        gradient = OrangeActionGradient,
                        shadowColor = Color(0xFFBF360C),
                        onClick = { showHintDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "g2048_hint_btn"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Game3DButton(
                        text = Strings.get("restart", lang),
                        onClick = { restartGame() },
                        modifier = Modifier.weight(1f),
                        testTag = "g2048_restart_btn"
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
                LevelCompleteDialog(
                    stars = 3,
                    coinsEarned = 35,
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
                        if (onUseFreeHint()) isHintRevealed = true
                    },
                    onBuyHintWithCoins = {
                        if (onSpendCoinsForHint()) isHintRevealed = true
                    },
                    onDismiss = { showHintDialog = false }
                )
            }
        }
    }
}
