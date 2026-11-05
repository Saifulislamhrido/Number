package com.example.modes.math

import androidx.compose.animation.core.animateFloatAsState
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

data class MathQuestion(
    val equationText: String,
    val correctAnswer: Int,
    val options: List<Int>
)

@Composable
fun MathChallengeScreen(
    levelNumber: Int,
    questionCount: Int = 5,
    timeLimitSeconds: Int = 20,
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
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(timeLimitSeconds) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var isPaused by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var isHintRevealed by remember { mutableStateOf(false) }
    val eliminatedOptions = remember { mutableStateListOf<Int>() }

    fun generateQuestions(): List<MathQuestion> {
        val difficulty = (levelNumber - 1) / 25 // 0..3
        return (1..questionCount).map { qIndex ->
            val rand = Random(levelNumber * 100 + qIndex)
            val operation = when (difficulty) {
                0 -> if (rand.nextBoolean()) "+" else "-"
                1 -> listOf("+", "-", "×").random(rand)
                else -> listOf("+", "-", "×", "÷").random(rand)
            }

            var num1: Int
            var num2: Int
            var answer: Int
            var text: String

            when (operation) {
                "+" -> {
                    num1 = rand.nextInt(5 + difficulty * 10, 20 + difficulty * 25)
                    num2 = rand.nextInt(3 + difficulty * 8, 15 + difficulty * 20)
                    answer = num1 + num2
                    text = "$num1 + $num2 = ?"
                }
                "-" -> {
                    num1 = rand.nextInt(15 + difficulty * 15, 30 + difficulty * 30)
                    num2 = rand.nextInt(3 + difficulty * 5, num1 - 1)
                    answer = num1 - num2
                    text = "$num1 - $num2 = ?"
                }
                "×" -> {
                    num1 = rand.nextInt(2 + difficulty, 9 + difficulty * 2)
                    num2 = rand.nextInt(3 + difficulty, 10 + difficulty * 2)
                    answer = num1 * num2
                    text = "$num1 × $num2 = ?"
                }
                else -> { // "÷"
                    num2 = rand.nextInt(2, 6 + difficulty)
                    val quotient = rand.nextInt(2, 10 + difficulty * 2)
                    num1 = num2 * quotient
                    answer = quotient
                    text = "$num1 ÷ $num2 = ?"
                }
            }

            val wrongOptions = mutableSetOf<Int>()
            while (wrongOptions.size < 3) {
                val delta = listOf(-3, -2, -1, 1, 2, 3, 4, -4).random(rand)
                val candidate = answer + delta
                if (candidate != answer && candidate >= 0) {
                    wrongOptions.add(candidate)
                }
            }

            val allOptions = (wrongOptions.toList() + answer).shuffled(rand)
            MathQuestion(text, answer, allOptions)
        }
    }

    val questions = remember(levelNumber) { generateQuestions() }
    val currentQuestion = questions[currentQuestionIdx.coerceIn(0, questions.lastIndex)]

    // Timer countdown
    LaunchedEffect(currentQuestionIdx, isPaused, isWon, isGameOver) {
        timeLeft = timeLimitSeconds
        while (timeLeft > 0 && !isPaused && !isWon && !isGameOver) {
            delay(1000)
            timeLeft--
            elapsedSeconds++
        }
        if (timeLeft <= 0 && !isWon && !isPaused && !isGameOver) {
            isGameOver = true
            soundManager.playWrong()
        }
    }

    fun restartGame() {
        currentQuestionIdx = 0
        score = 0
        streak = 0
        timeLeft = timeLimitSeconds
        elapsedSeconds = 0
        isWon = false
        isGameOver = false
        isPaused = false
        eliminatedOptions.clear()
        isHintRevealed = false
    }

    fun onOptionSelected(selected: Int) {
        if (isPaused || isWon || isGameOver) return

        if (selected == currentQuestion.correctAnswer) {
            streak++
            val timeBonus = timeLeft * 5
            val streakBonus = streak * 10
            score += 100 + timeBonus + streakBonus
            soundManager.playCorrect()

            if (currentQuestionIdx + 1 < questions.size) {
                currentQuestionIdx++
                eliminatedOptions.clear()
                isHintRevealed = false
            } else {
                isWon = true
                soundManager.playLevelComplete()
                val stars = if (streak == questions.size) 3 else 2
                onWin(stars, score, elapsedSeconds)
            }
        } else {
            streak = 0
            soundManager.playWrong()
            if (currentQuestionIdx + 1 < questions.size) {
                currentQuestionIdx++
                eliminatedOptions.clear()
                isHintRevealed = false
            } else {
                isGameOver = true
            }
        }
    }

    fun triggerHint() {
        // Eliminate 2 wrong choices
        val wrongOnes = currentQuestion.options.filter { it != currentQuestion.correctAnswer }
        eliminatedOptions.clear()
        eliminatedOptions.addAll(wrongOnes.take(2))
        isHintRevealed = true
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground),
        topBar = {
            GameTopBar(
                title = "${Strings.get("mode_math_title", lang)} ${Strings.formatNumber(levelNumber, lang)}",
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
                // HUD: Question count, Streak, Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Game3DCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = DarkPurpleSurface
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Q", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "${Strings.formatNumber(currentQuestionIdx + 1, lang)}/${Strings.formatNumber(questions.size, lang)}",
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
                            Text(text = "Streak", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                            Text(
                                text = "🔥 ${Strings.formatNumber(streak, lang)}",
                                color = Color(0xFFFF9100),
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

                // Time progress bar
                val progress = timeLeft.toFloat() / timeLimitSeconds.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (timeLeft <= 4) Color(0xFFFF5252) else Color(0xFF00E5FF),
                    trackColor = Color(0x33FFFFFF)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Question Box
                Game3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    backgroundColor = DarkPurpleSurface,
                    borderColor = Color(0xFF7C4DFF)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentQuestion.equationText,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4 Multiple Choice Option Buttons
                val buttonGradients = listOf(
                    GreenPlayGradient,
                    BlueActionGradient,
                    OrangeActionGradient,
                    VioletActionGradient
                )

                currentQuestion.options.chunked(2).forEachIndexed { rowIdx, rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEachIndexed { colIdx, opt ->
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
                                testTag = "math_option_$opt"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Hint button
                Game3DButton(
                    text = "💡 ${Strings.get("hint", lang)} (Eliminate 2 choices)",
                    gradient = OrangeActionGradient,
                    shadowColor = Color(0xFFBF360C),
                    onClick = { showHintDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "math_hint_btn"
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
