package com.example.modes.block

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

data class BlockShape(
    val id: Int,
    val pattern: List<Pair<Int, Int>>, // relative (r, c) offsets
    val color: Color
)

@Composable
fun BlockPuzzleScreen(
    levelNumber: Int,
    targetScore: Int,
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
    val boardSize = 6
    var score by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }

    val board = remember {
        mutableStateListOf<Int>().apply {
            repeat(boardSize * boardSize) { add(0) }
        }
    }

    // Catalog of shapes
    val shapeCatalog = remember {
        listOf(
            BlockShape(1, listOf(Pair(0, 0)), Color(0xFF00E5FF)), // 1x1
            BlockShape(2, listOf(Pair(0, 0), Pair(0, 1)), Color(0xFF00E676)), // 1x2
            BlockShape(3, listOf(Pair(0, 0), Pair(1, 0)), Color(0xFFFFD600)), // 2x1
            BlockShape(4, listOf(Pair(0, 0), Pair(0, 1), Pair(1, 0), Pair(1, 1)), Color(0xFFFF4081)), // 2x2
            BlockShape(5, listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)), Color(0xFF7C4DFF)), // 1x3
            BlockShape(6, listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)), Color(0xFFFF9100)), // 3x1
            BlockShape(7, listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1)), Color(0xFF00B0FF)) // L-shape
        )
    }

    val availableShapes = remember {
        mutableStateListOf<BlockShape>().apply {
            repeat(3) { add(shapeCatalog.random()) }
        }
    }

    var selectedShapeIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun canPlaceShape(shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for (offset in shape.pattern) {
            val r = startRow + offset.first
            val c = startCol + offset.second
            if (r !in 0 until boardSize || c !in 0 until boardSize) return false
            if (board[r * boardSize + c] != 0) return false
        }
        return true
    }

    fun canAnyShapeBePlaced(): Boolean {
        for (shape in availableShapes) {
            for (r in 0 until boardSize) {
                for (c in 0 until boardSize) {
                    if (canPlaceShape(shape, r, c)) return true
                }
            }
        }
        return false
    }

    fun checkAndClearLines() {
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until boardSize) {
            if ((0 until boardSize).all { board[r * boardSize + it] != 0 }) {
                rowsToClear.add(r)
            }
        }
        for (c in 0 until boardSize) {
            if ((0 until boardSize).all { board[it * boardSize + c] != 0 }) {
                colsToClear.add(c)
            }
        }

        val totalLines = rowsToClear.size + colsToClear.size
        if (totalLines > 0) {
            for (r in rowsToClear) {
                for (c in 0 until boardSize) board[r * boardSize + c] = 0
            }
            for (c in colsToClear) {
                for (r in 0 until boardSize) board[r * boardSize + c] = 0
            }

            val comboMultiplier = if (totalLines > 1) totalLines else 1
            score += totalLines * 100 * comboMultiplier
            soundManager.playMerge()
        }
    }

    fun onCellClick(row: Int, col: Int) {
        if (isPaused || isWon || isGameOver) return
        if (selectedShapeIndex == -1) return

        val shape = availableShapes[selectedShapeIndex]
        if (canPlaceShape(shape, row, col)) {
            // Place shape
            for (offset in shape.pattern) {
                val r = row + offset.first
                val c = col + offset.second
                board[r * boardSize + c] = shape.id
            }

            score += shape.pattern.size * 10
            soundManager.playClick()

            availableShapes.removeAt(selectedShapeIndex)
            selectedShapeIndex = -1

            checkAndClearLines()

            if (availableShapes.isEmpty()) {
                repeat(3) { availableShapes.add(shapeCatalog.random()) }
            }

            if (score >= targetScore) {
                isWon = true
                soundManager.playLevelComplete()
                onWin(3, score, elapsedSeconds)
            } else if (!canAnyShapeBePlaced()) {
                isGameOver = true
                soundManager.playWrong()
            }
        } else {
            soundManager.playWrong()
        }
    }

    fun restartGame() {
        board.fill(0)
        score = 0
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        selectedShapeIndex = -1
        availableShapes.clear()
        repeat(3) { availableShapes.add(shapeCatalog.random()) }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_block_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                // HUD: Target & Score
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
                                text = Strings.formatNumber(targetScore, lang),
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

                Spacer(modifier = Modifier.height(10.dp))

                // 6x6 Grid Board
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkPurpleSurface)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(boardSize),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(board) { index, value ->
                            val r = index / boardSize
                            val c = index % boardSize
                            val isOccupied = value != 0
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isOccupied) Color(0xFF00E5FF) else Color(0x22FFFFFF)
                                    )
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable { onCellClick(r, c) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shape Palette to choose from
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableShapes.forEachIndexed { idx, shape ->
                        val isSelected = selectedShapeIndex == idx
                        Box(
                            modifier = Modifier
                                .testTag("shape_palette_$idx")
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x44FFFFFF) else Color(0x11FFFFFF))
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) GoldYellow else Color(0x33FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedShapeIndex = if (selectedShapeIndex == idx) -1 else idx
                                    soundManager.playClick()
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column {
                                for (r in 0..2) {
                                    Row {
                                        for (c in 0..2) {
                                            val filled = shape.pattern.contains(Pair(r, c))
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .padding(1.dp)
                                                    .background(
                                                        if (filled) shape.color else Color.Transparent,
                                                        RoundedCornerShape(3.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control Actions: Hint & Restart
                Row(modifier = Modifier.fillMaxWidth()) {
                    Game3DButton(
                        text = "💡 ${Strings.get("hint", lang)}",
                        gradient = OrangeActionGradient,
                        shadowColor = Color(0xFFBF360C),
                        onClick = { showHintDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "block_hint_btn"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Game3DButton(
                        text = Strings.get("restart", lang),
                        onClick = { restartGame() },
                        modifier = Modifier.weight(1f),
                        testTag = "block_restart_btn"
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
