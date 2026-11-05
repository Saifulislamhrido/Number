package com.example.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.levels.GameModeType
import com.example.levels.LevelManager
import com.example.modes.block.BlockPuzzleScreen
import com.example.modes.game2048.Game2048Screen
import com.example.modes.match.NumberMatchScreen
import com.example.modes.math.MathChallengeScreen
import com.example.modes.memory.NumberMemoryScreen
import com.example.modes.merge.NumberMergeScreen
import com.example.modes.missing.MissingNumberScreen
import com.example.modes.sequence.NumberSequenceScreen
import com.example.screens.DailyChallengeScreen
import com.example.screens.GameModesScreen
import com.example.screens.HomeScreen
import com.example.screens.LevelSelectScreen
import com.example.screens.SettingsScreen
import com.example.screens.StatsScreen
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun AppNavGraph(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val language by viewModel.language.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val completedLevels by viewModel.completedLevels.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalStars by viewModel.totalStars.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val dailyStreak by viewModel.dailyStreak.collectAsState()
    val freeHints by viewModel.freeHints.collectAsState()

    val maxUnlockedLevel = viewModel.getMaxUnlockedLevel(completedLevels)
    val currentCampaignLevel = maxUnlockedLevel.coerceIn(1, 100)

    when (val screen = currentScreen) {
        is ScreenDestination.Home -> {
            HomeScreen(
                coins = coins,
                completedLevelsCount = completedCount,
                totalStars = totalStars ?: 0,
                currentLevel = currentCampaignLevel,
                lang = language,
                soundManager = viewModel.soundManager,
                onPlayCurrentLevel = { viewModel.navigateTo(ScreenDestination.PlayLevel(currentCampaignLevel)) },
                onNavigateGameModes = { viewModel.navigateTo(ScreenDestination.GameModes) },
                onNavigateLevelSelect = { viewModel.navigateTo(ScreenDestination.LevelSelect) },
                onNavigateDailyChallenge = { viewModel.navigateTo(ScreenDestination.DailyChallenge) },
                onNavigateStats = { viewModel.navigateTo(ScreenDestination.Stats) },
                onNavigateSettings = { viewModel.navigateTo(ScreenDestination.Settings) },
                onToggleLanguage = {
                    val nextLang = if (language == com.example.i18n.Language.EN) com.example.i18n.Language.BN else com.example.i18n.Language.EN
                    viewModel.setLanguage(nextLang)
                },
                onToggleSound = { viewModel.setSoundEnabled(!isSoundEnabled) }
            )
        }

        is ScreenDestination.GameModes -> {
            BackHandler { viewModel.navigateBack() }
            GameModesScreen(
                coins = coins,
                lang = language,
                soundManager = viewModel.soundManager,
                onBack = { viewModel.navigateBack() },
                onSelectMode = { mode -> viewModel.navigateTo(ScreenDestination.PlayMode(mode)) }
            )
        }

        is ScreenDestination.LevelSelect -> {
            BackHandler { viewModel.navigateBack() }
            LevelSelectScreen(
                coins = coins,
                completedLevels = completedLevels,
                maxUnlockedLevel = maxUnlockedLevel,
                lang = language,
                soundManager = viewModel.soundManager,
                onBack = { viewModel.navigateBack() },
                onSelectLevel = { lvl -> viewModel.navigateTo(ScreenDestination.PlayLevel(lvl)) }
            )
        }

        is ScreenDestination.DailyChallenge -> {
            BackHandler { viewModel.navigateBack() }
            DailyChallengeScreen(
                coins = coins,
                streak = dailyStreak,
                canClaim = viewModel.canClaimDailyReward(),
                lang = language,
                soundManager = viewModel.soundManager,
                onBack = { viewModel.navigateBack() },
                onClaimReward = { viewModel.claimDailyReward() },
                onPlayDailyPuzzle = {
                    val dailyLevel = ((dailyStreak * 13) % 100) + 1
                    viewModel.navigateTo(ScreenDestination.PlayLevel(dailyLevel))
                }
            )
        }

        is ScreenDestination.Stats -> {
            BackHandler { viewModel.navigateBack() }
            StatsScreen(
                coins = coins,
                completedLevels = completedCount,
                totalStars = totalStars ?: 0,
                achievements = achievements,
                lang = language,
                soundManager = viewModel.soundManager,
                onBack = { viewModel.navigateBack() },
                onClaimAchievement = { ach -> viewModel.claimAchievement(ach) }
            )
        }

        is ScreenDestination.Settings -> {
            BackHandler { viewModel.navigateBack() }
            SettingsScreen(
                coins = coins,
                isSoundEnabled = isSoundEnabled,
                isMusicEnabled = isMusicEnabled,
                isVibrationEnabled = isVibrationEnabled,
                isDarkMode = isDarkMode,
                lang = language,
                soundManager = viewModel.soundManager,
                onBack = { viewModel.navigateBack() },
                onToggleSound = { viewModel.setSoundEnabled(it) },
                onToggleMusic = { viewModel.setMusicEnabled(it) },
                onToggleVibration = { viewModel.setVibrationEnabled(it) },
                onToggleDarkMode = { viewModel.setDarkMode(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onResetProgress = { viewModel.resetAllProgress() }
            )
        }

        is ScreenDestination.PlayLevel -> {
            BackHandler { viewModel.navigateBack() }
            val levelConfig = LevelManager.getLevel(screen.levelNumber)

            when (levelConfig.mode) {
                GameModeType.NUMBER_MATCH -> {
                    NumberMatchScreen(
                        levelNumber = levelConfig.levelNumber,
                        targetPairs = levelConfig.targetValue,
                        initialMoves = levelConfig.maxMoves,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_MERGE -> {
                    NumberMergeScreen(
                        levelNumber = levelConfig.levelNumber,
                        targetValue = levelConfig.targetValue,
                        initialMoves = levelConfig.maxMoves,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.GAME_2048 -> {
                    Game2048Screen(
                        levelNumber = levelConfig.levelNumber,
                        targetTile = levelConfig.targetValue,
                        maxMoves = levelConfig.maxMoves,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.BLOCK_PUZZLE -> {
                    BlockPuzzleScreen(
                        levelNumber = levelConfig.levelNumber,
                        targetScore = levelConfig.targetValue,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.MATH_CHALLENGE -> {
                    MathChallengeScreen(
                        levelNumber = levelConfig.levelNumber,
                        questionCount = levelConfig.questionCount,
                        timeLimitSeconds = if (levelConfig.timeLimitSeconds > 0) levelConfig.timeLimitSeconds else 20,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.MISSING_NUMBER -> {
                    MissingNumberScreen(
                        levelNumber = levelConfig.levelNumber,
                        puzzleCount = levelConfig.questionCount,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_SEQUENCE -> {
                    NumberSequenceScreen(
                        levelNumber = levelConfig.levelNumber,
                        puzzleCount = levelConfig.questionCount,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_MEMORY -> {
                    NumberMemoryScreen(
                        levelNumber = levelConfig.levelNumber,
                        memoryCount = levelConfig.memoryCount,
                        memorizeTimeSeconds = if (levelConfig.timeLimitSeconds > 0) levelConfig.timeLimitSeconds else 4,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(levelConfig.levelNumber, stars, score, time, levelConfig.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }
            }
        }

        is ScreenDestination.PlayMode -> {
            BackHandler { viewModel.navigateBack() }
            val representativeLevel = when (screen.mode) {
                GameModeType.NUMBER_MATCH -> 1
                GameModeType.MATH_CHALLENGE -> 2
                GameModeType.MISSING_NUMBER -> 3
                GameModeType.NUMBER_MERGE -> 4
                GameModeType.NUMBER_SEQUENCE -> 5
                GameModeType.NUMBER_MEMORY -> 6
                GameModeType.BLOCK_PUZZLE -> 7
                GameModeType.GAME_2048 -> 8
            }
            val levelConfig = LevelManager.getLevel(representativeLevel)

            when (screen.mode) {
                GameModeType.NUMBER_MATCH -> {
                    NumberMatchScreen(
                        levelNumber = representativeLevel,
                        targetPairs = 6,
                        initialMoves = 18,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_MERGE -> {
                    NumberMergeScreen(
                        levelNumber = representativeLevel,
                        targetValue = 256,
                        initialMoves = 35,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.GAME_2048 -> {
                    Game2048Screen(
                        levelNumber = representativeLevel,
                        targetTile = 2048,
                        maxMoves = 0,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.BLOCK_PUZZLE -> {
                    BlockPuzzleScreen(
                        levelNumber = representativeLevel,
                        targetScore = 800,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.MATH_CHALLENGE -> {
                    MathChallengeScreen(
                        levelNumber = representativeLevel,
                        questionCount = 7,
                        timeLimitSeconds = 18,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.MISSING_NUMBER -> {
                    MissingNumberScreen(
                        levelNumber = representativeLevel,
                        puzzleCount = 5,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_SEQUENCE -> {
                    NumberSequenceScreen(
                        levelNumber = representativeLevel,
                        puzzleCount = 5,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }

                GameModeType.NUMBER_MEMORY -> {
                    NumberMemoryScreen(
                        levelNumber = representativeLevel,
                        memoryCount = 5,
                        memorizeTimeSeconds = 3,
                        coins = coins,
                        freeHints = freeHints,
                        hintText = levelConfig.hintText,
                        lang = language,
                        soundManager = viewModel.soundManager,
                        onBack = { viewModel.navigateBack() },
                        onWin = { stars, score, time ->
                            viewModel.onLevelWon(representativeLevel, stars, score, time, screen.mode.id)
                        },
                        onSpendCoinsForHint = { viewModel.spendCoinsForHint(20) },
                        onUseFreeHint = { viewModel.useFreeHint() }
                    )
                }
            }
        }
    }
}
