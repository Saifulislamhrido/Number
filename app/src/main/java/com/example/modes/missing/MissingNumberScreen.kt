package com.example.modes.missing

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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.theme.BlueActionGradient
import com.example.ui.theme.DarkPurpleSurface
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.GoldYellow
import com.example.ui.theme.GreenPlayGradient
import com.example.ui.theme.OrangeActionGradient
import com.example.ui.theme.VioletActionGradient
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MissingPuzzle(
    val sequenceDisplay: List<String>,
    val missingIndex: Int,
    val correctAnswer: Int,
    val options: List<Int>,
    val explanation: String
)

@Composable
fun MissingNumberScreen(
    levelNumber: Int,
    puzzleCount: Int = 4,
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
    var currentPuzzleIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    val eliminatedOptions = remember { mutableStateListOf<Int>() }

    fun generatePuzzles(): List<MissingPuzzle> {
        val difficulty = (levelNumber - 1) / 25
        return (1..puzzleCount).map { idx ->
            val rand = Random(levelNumber * 200 + idx)
            val type = (idx + difficulty) % 4
            var numbers: List<Int>
            var explanation: String

            when (type) {
                0 -> { // Arithmetic (+step)
                    val step = rand.nextInt(2 + difficulty, 6 + difficulty * 2)
                    val start = rand.nextInt(1, 15)
                    numbers = (0..4).map { start + it * step }
                    explanation = "Rule: Add $step each step"
                }
                1 -> { // Double / Multiply
                    val mult = if (difficulty >= 2 && rand.nextBoolean()) 3 else 2
                    val start = rand.nextInt(1, 5)
                    numbers = (0..4).map {
                        var v = start
                        repeat(it) { v *= mult }
                        v
                    }
                    explanation = "Rule: Multiply by $mult each step"
                }
                2 -> { // Alternating (+a, -b or +a, +b)
                    val a = rand.nextInt(3, 7)
                    val b = rand.nextInt(1, 3)
                    val list = mutableListOf(rand.nextInt(10, 20))
                    for (i in 1..4) {
                        val prev = list.last()
                        list.add(if (i % 2 == 1) prev + a else prev - b)
                    }
                    numbers = list
                    explanation = "Rule: Alternating +$a and -$b"
                }
                else -> { // Squares (n^2) or Primes
                    val start = rand.nextInt(1, 4)
                    numbers = (start..(start + 4)).map { it * it }
                    explanation = "Rule: Consecutive squared numbers"
                }
            }

            val missingIdx = rand.nextInt(1, 4) // never the very first number
            val answer = numbers[missingIdx]

            val display = numbers.mapIndexed { i, n ->
                if (i == missingIdx) "?" else n.toString()
            }

            val wrongOptions = mutableSetOf<Int>()
            while (wrongOptions.size < 3) {
                val delta = listOf(-3, -2, -1, 1, 2, 3, 4, 5).random(rand)
                val cand = answer + delta
                if (cand != answer && cand > 0) wrongOptions.add(cand)
            }
            val options = (wrongOptions.toList() + answer).shuffled(rand)

            MissingPuzzle(display, missingIdx, answer, options, explanation)
        }
    }

    val puzzles = remember(levelNumber) { generatePuzzles() }
    val currentPuzzle = puzzles[currentPuzzleIdx.coerceIn(0, puzzles.lastIndex)]

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun restartGame() {
        currentPuzzleIdx = 0
        score = 0
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        showExplanation = false
        eliminatedOptions.clear()
        isHintRevealed = false
    }

    fun onOptionSelected(selected: Int) {
        if (isPaused || isWon || isGameOver) return

        if (selected == currentPuzzle.correctAnswer) {
            score += 150
            soundManager.playCorrect()

            if (currentPuzzleIdx + 1 < puzzles.size) {
                currentPuzzleIdx++
                showExplanation = false
                eliminatedOptions.clear()
                isHintRevealed = false
            } else {
                isWon = true
                soundManager.playLevelComplete()
                onWin(3, score, elapsedSeconds)
            }
        } else {
            soundManager.playWrong()
            showExplanation = true
            if (currentPuzzleIdx + 1 < puzzles.size) {
                currentPuzzleIdx++
                eliminatedOptions.clear()
            } else {
                isGameOver = true
            }
        }
    }

    fun triggerHint() {
        val wrong = currentPuzzle.options.filter { it != currentPuzzle.correctAnswer }
        eliminatedOptions.clear()
        eliminatedOptions.addAll(wrong.take(2))
        isHintRevealed = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_missing_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                            Text(text = "Puzzle", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "${Strings.formatNumber(currentPuzzleIdx + 1, lang)}/${Strings.formatNumber(puzzles.size, lang)}",
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

                Spacer(modifier = Modifier.height(24.dp))

                // Sequence Card Display
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
                            text = "Find the missing number:",
                            color = Color(0xFFB0BEC5),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Sequence items row
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentPuzzle.sequenceDisplay.forEach { item ->
                                val isMissing = item == "?"
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isMissing) 52.dp else 46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isMissing) GoldYellow else Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isMissing) "?" else item,
                                        color = if (isMissing) Color.Black else Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }

                        if (showExplanation || isHintRevealed) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentPuzzle.explanation,
                                color = Color(0xFF00E676),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 4 Options Grid
                val buttonGradients = listOf(GreenPlayGradient, BlueActionGradient, OrangeActionGradient, VioletActionGradient)
                currentPuzzle.options.chunked(2).forEachIndexed { rowIdx, rowOpts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOpts.forEachIndexed { colIdx, opt ->
                            val isEliminated = eliminatedOptions.contains(opt)
                            val grad = buttonGradients[(rowIdx * 2 + colIdx) % buttonGradients.size]
                            Game3DButton(
                                text = Strings.formatNumber(opt, lang),
                                fontSize = 22,
                                gradient = grad,
                                enabled = !isEliminated,
                                onClick = { onOptionSelected(opt) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                testTag = "missing_opt_$opt"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Game3DButton(
                    text = "💡 ${Strings.get("hint", lang)}",
                    gradient = OrangeActionGradient,
                    shadowColor = Color(0xFFBF360C),
                    onClick = { showHintDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "missing_hint_btn"
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
                    coinsEarned = 25,
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
                    hintText = "${currentPuzzle.explanation}. Hint: Answer is between ${currentPuzzle.correctAnswer - 5} and ${currentPuzzle.correctAnswer + 5}",
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
