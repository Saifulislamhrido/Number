package com.example.modes.sequence

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

data class SequencePuzzle(
    val terms: List<Int>,
    val nextTerm: Int,
    val options: List<Int>,
    val formulaDesc: String
)

@Composable
fun NumberSequenceScreen(
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
    var currentIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }
    val eliminatedOptions = remember { mutableStateListOf<Int>() }

    fun generatePuzzles(): List<SequencePuzzle> {
        val difficulty = (levelNumber - 1) / 25
        return (1..puzzleCount).map { idx ->
            val rand = Random(levelNumber * 300 + idx)
            val terms = mutableListOf<Int>()
            var nextTerm: Int
            var desc: String

            when ((idx + difficulty) % 4) {
                0 -> { // Arithmetic (+step)
                    val step = rand.nextInt(3 + difficulty, 8 + difficulty * 2)
                    val start = rand.nextInt(2, 20)
                    for (i in 0..4) terms.add(start + i * step)
                    nextTerm = start + 5 * step
                    desc = "Pattern: Add $step each time"
                }
                1 -> { // Geometric (*ratio)
                    val ratio = if (difficulty == 0) 2 else 3
                    val start = rand.nextInt(1, 4)
                    var cur = start
                    repeat(4) {
                        terms.add(cur)
                        cur *= ratio
                    }
                    nextTerm = cur
                    desc = "Pattern: Multiply by $ratio each time"
                }
                2 -> { // Triangular or n(n+1)/2 or Fibonacci
                    var a = rand.nextInt(1, 4)
                    var b = rand.nextInt(2, 5)
                    terms.add(a)
                    terms.add(b)
                    repeat(3) {
                        val c = a + b
                        terms.add(c)
                        a = b
                        b = c
                    }
                    nextTerm = a + b
                    desc = "Pattern: Fibonacci sum of previous two numbers"
                }
                else -> { // Differences increase by +1 (+1, +2, +3, +4, +5...)
                    val start = rand.nextInt(1, 10)
                    var cur = start
                    terms.add(cur)
                    for (i in 1..4) {
                        cur += i * (1 + difficulty)
                        terms.add(cur)
                    }
                    nextTerm = cur + 5 * (1 + difficulty)
                    desc = "Pattern: Differences grow by ${1 + difficulty}"
                }
            }

            val wrong = mutableSetOf<Int>()
            while (wrong.size < 3) {
                val delta = listOf(-4, -2, -1, 1, 2, 4, 6).random(rand)
                val c = nextTerm + delta
                if (c != nextTerm && c > 0) wrong.add(c)
            }
            val options = (wrong.toList() + nextTerm).shuffled(rand)

            SequencePuzzle(terms, nextTerm, options, desc)
        }
    }

    val puzzles = remember(levelNumber) { generatePuzzles() }
    val currentPuzzle = puzzles[currentIdx.coerceIn(0, puzzles.lastIndex)]

    LaunchedEffect(isPaused, isWon, isGameOver) {
        while (!isPaused && !isWon && !isGameOver) {
            delay(1000)
            elapsedSeconds++
        }
    }

    fun restartGame() {
        currentIdx = 0
        score = 0
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        eliminatedOptions.clear()
        isHintRevealed = false
    }

    fun onOptionSelected(opt: Int) {
        if (isPaused || isWon || isGameOver) return

        if (opt == currentPuzzle.nextTerm) {
            score += 150
            soundManager.playCorrect()

            if (currentIdx + 1 < puzzles.size) {
                currentIdx++
                eliminatedOptions.clear()
                isHintRevealed = false
            } else {
                isWon = true
                soundManager.playLevelComplete()
                onWin(3, score, elapsedSeconds)
            }
        } else {
            soundManager.playWrong()
            if (currentIdx + 1 < puzzles.size) {
                currentIdx++
                eliminatedOptions.clear()
            } else {
                isGameOver = true
            }
        }
    }

    fun triggerHint() {
        val wrong = currentPuzzle.options.filter { it != currentPuzzle.nextTerm }
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
                title = "${Strings.get("mode_sequence_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                            Text(text = "Series", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "${Strings.formatNumber(currentIdx + 1, lang)}/${Strings.formatNumber(puzzles.size, lang)}",
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

                // Sequence Card
                Game3DCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkPurpleSurface,
                    borderColor = Color(0xFF7C4DFF)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "What is the NEXT number?",
                            color = Color(0xFFB0BEC5),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Terms
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentPuzzle.terms.forEach { term ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = term.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            // Next placeholder
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "?",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        if (isHintRevealed) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = currentPuzzle.formulaDesc,
                                color = Color(0xFF00E676),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Options
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
                                testTag = "seq_opt_$opt"
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
                    testTag = "seq_hint_btn"
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
                    hintText = currentPuzzle.formulaDesc,
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
