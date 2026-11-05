package com.example.levels

object LevelManager {
    private val modes = listOf(
        GameModeType.NUMBER_MATCH,
        GameModeType.MATH_CHALLENGE,
        GameModeType.MISSING_NUMBER,
        GameModeType.NUMBER_MERGE,
        GameModeType.NUMBER_SEQUENCE,
        GameModeType.NUMBER_MEMORY,
        GameModeType.BLOCK_PUZZLE,
        GameModeType.GAME_2048
    )

    private val levelsList: List<LevelConfig> by lazy {
        (1..100).map { levelNum ->
            createLevel(levelNum)
        }
    }

    fun getAllLevels(): List<LevelConfig> = levelsList

    fun getLevel(levelNumber: Int): LevelConfig {
        val index = (levelNumber - 1).coerceIn(0, 99)
        return levelsList[index]
    }

    fun getLevelsForCategory(category: DifficultyCategory): List<LevelConfig> {
        return levelsList.filter { it.category == category }
    }

    private fun createLevel(level: Int): LevelConfig {
        val category = DifficultyCategory.fromLevel(level)
        val modeIndex = (level - 1) % modes.size
        val mode = modes[modeIndex]

        return when (category) {
            DifficultyCategory.EASY -> generateEasyLevel(level, mode)
            DifficultyCategory.MEDIUM -> generateMediumLevel(level, mode)
            DifficultyCategory.HARD -> generateHardLevel(level, mode)
            DifficultyCategory.EXPERT -> generateExpertLevel(level, mode)
        }
    }

    private fun generateEasyLevel(level: Int, mode: GameModeType): LevelConfig {
        val levelFactor = (level - 1) / 8 // 0..2
        return when (mode) {
            GameModeType.NUMBER_MATCH -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Match Beginner $level",
                targetDescription = "Match ${4 + levelFactor} pairs of numbers",
                targetValue = 4 + levelFactor,
                maxMoves = 14 - levelFactor,
                hintText = "Scan the corners and borders first to find duplicates.",
                gridRows = 4,
                gridCols = 3
            )
            GameModeType.MATH_CHALLENGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Math Warmup $level",
                targetDescription = "Solve 5 quick addition/subtraction questions",
                targetValue = 5,
                timeLimitSeconds = 25,
                hintText = "Break numbers into tens and ones to compute faster.",
                questionCount = 5
            )
            GameModeType.MISSING_NUMBER -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Missing Digit $level",
                targetDescription = "Find the missing number in 4 logic sequences",
                targetValue = 4,
                hintText = "Check the difference between consecutive numbers.",
                questionCount = 4
            )
            GameModeType.NUMBER_MERGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Merge Kickstart $level",
                targetDescription = "Merge tiles to reach ${32 * (1 shl levelFactor)}",
                targetValue = 32 * (1 shl levelFactor),
                maxMoves = 25,
                hintText = "Merge smaller numbers in a single corner.",
                gridRows = 4,
                gridCols = 4
            )
            GameModeType.NUMBER_SEQUENCE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Sequence Explorer $level",
                targetDescription = "Complete 4 pattern series",
                targetValue = 4,
                hintText = "Observe whether the sequence is adding or multiplying.",
                questionCount = 4
            )
            GameModeType.NUMBER_MEMORY -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Memory Flash $level",
                targetDescription = "Remember and recall 4 numbers in order",
                targetValue = 4,
                timeLimitSeconds = 4,
                hintText = "Say the digits aloud in rhythm before they vanish.",
                memoryCount = 4
            )
            GameModeType.BLOCK_PUZZLE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "Block Starter $level",
                targetDescription = "Reach ${300 + levelFactor * 100} points by clearing lines",
                targetValue = 300 + levelFactor * 100,
                hintText = "Try to leave the center open for larger shapes.",
                gridRows = 6,
                gridCols = 6
            )
            GameModeType.GAME_2048 -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EASY,
                title = "2048 Cadet $level",
                targetDescription = "Swipe and reach tile 64",
                targetValue = 64,
                maxMoves = 60,
                hintText = "Keep your highest tile in the bottom-right corner.",
                gridRows = 4,
                gridCols = 4
            )
        }
    }

    private fun generateMediumLevel(level: Int, mode: GameModeType): LevelConfig {
        val levelFactor = (level - 26) / 8 // 0..2
        return when (mode) {
            GameModeType.NUMBER_MATCH -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Match Adept $level",
                targetDescription = "Match ${6 + levelFactor} pairs of numbers",
                targetValue = 6 + levelFactor,
                maxMoves = 18 - levelFactor,
                hintText = "Prioritize tiles that unlock more board visibility.",
                gridRows = 4,
                gridCols = 4
            )
            GameModeType.MATH_CHALLENGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Mental Sprint $level",
                targetDescription = "Solve 6 mixed +, -, × questions",
                targetValue = 6,
                timeLimitSeconds = 20,
                hintText = "Multiplication takes precedence over addition.",
                questionCount = 6
            )
            GameModeType.MISSING_NUMBER -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Logic Gap $level",
                targetDescription = "Identify missing values in 5 progressive series",
                targetValue = 5,
                hintText = "Check if differences are increasing by a constant rate.",
                questionCount = 5
            )
            GameModeType.NUMBER_MERGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Merge Fusion $level",
                targetDescription = "Merge tiles to create ${128 * (1 shl levelFactor)}",
                targetValue = 128 * (1 shl levelFactor),
                maxMoves = 30,
                hintText = "Build an ascending snake pattern along the edges.",
                gridRows = 4,
                gridCols = 4
            )
            GameModeType.NUMBER_SEQUENCE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Pattern Weaver $level",
                targetDescription = "Solve 5 arithmetic & geometric sequences",
                targetValue = 5,
                hintText = "Look for alternating rules (+3, *2, +3, *2).",
                questionCount = 5
            )
            GameModeType.NUMBER_MEMORY -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Memory Matrix $level",
                targetDescription = "Remember and recall 5 numbers in 3.5 seconds",
                targetValue = 5,
                timeLimitSeconds = 3,
                hintText = "Group the digits into pairs like a telephone number.",
                memoryCount = 5
            )
            GameModeType.BLOCK_PUZZLE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "Block Strategist $level",
                targetDescription = "Reach ${600 + levelFactor * 150} points",
                targetValue = 600 + levelFactor * 150,
                hintText = "Clearing multiple lines at once earns double combo points.",
                gridRows = 6,
                gridCols = 6
            )
            GameModeType.GAME_2048 -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.MEDIUM,
                title = "2048 Ascendant $level",
                targetDescription = "Swipe to build tile 128",
                targetValue = 128,
                maxMoves = 90,
                hintText = "Avoid swiping upwards if your highest tile is anchored down.",
                gridRows = 4,
                gridCols = 4
            )
        }
    }

    private fun generateHardLevel(level: Int, mode: GameModeType): LevelConfig {
        val levelFactor = (level - 51) / 8 // 0..2
        return when (mode) {
            GameModeType.NUMBER_MATCH -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Match Master $level",
                targetDescription = "Match ${8 + levelFactor} pairs of numbers",
                targetValue = 8 + levelFactor,
                maxMoves = 22 - levelFactor,
                hintText = "Look for inverted pairs and identical color accents.",
                gridRows = 5,
                gridCols = 4
            )
            GameModeType.MATH_CHALLENGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Speed Calculus $level",
                targetDescription = "Solve 7 fast +, -, ×, ÷ arithmetic puzzles",
                targetValue = 7,
                timeLimitSeconds = 15,
                hintText = "Eliminate obvious wrong answer choices quickly.",
                questionCount = 7
            )
            GameModeType.MISSING_NUMBER -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Enigma Gaps $level",
                targetDescription = "Solve 6 tricky sequence progressions",
                targetValue = 6,
                hintText = "Examine Fibonacci sums: each term is the sum of previous two.",
                questionCount = 6
            )
            GameModeType.NUMBER_MERGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Merge Overlord $level",
                targetDescription = "Merge up to reach ${256 * (1 shl levelFactor)}",
                targetValue = 256 * (1 shl levelFactor),
                maxMoves = 35,
                hintText = "Keep high numbers shielded from fragment tiles.",
                gridRows = 4,
                gridCols = 4
            )
            GameModeType.NUMBER_SEQUENCE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Sequence Architect $level",
                targetDescription = "Decipher 6 complex polynomial patterns",
                targetValue = 6,
                hintText = "Consider squared numbers (n^2) and cubed values (n^3).",
                questionCount = 6
            )
            GameModeType.NUMBER_MEMORY -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Photographic Brain $level",
                targetDescription = "Remember and recall 6 numbers under 3s",
                targetValue = 6,
                timeLimitSeconds = 3,
                hintText = "Visualize the spatial layout of the numbers.",
                memoryCount = 6
            )
            GameModeType.BLOCK_PUZZLE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "Block Master $level",
                targetDescription = "Score ${1000 + levelFactor * 200} points",
                targetValue = 1000 + levelFactor * 200,
                hintText = "Always prepare a 3x3 open zone for large square blocks.",
                gridRows = 6,
                gridCols = 6
            )
            GameModeType.GAME_2048 -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.HARD,
                title = "2048 Grandmaster $level",
                targetDescription = "Combine tiles to create tile 256",
                targetValue = 256,
                maxMoves = 130,
                hintText = "Never collapse your full bottom anchor row.",
                gridRows = 4,
                gridCols = 4
            )
        }
    }

    private fun generateExpertLevel(level: Int, mode: GameModeType): LevelConfig {
        val levelFactor = (level - 76) / 8 // 0..2
        return when (mode) {
            GameModeType.NUMBER_MATCH -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Match Legend $level",
                targetDescription = "Match 10 pairs with tight move limits",
                targetValue = 10,
                maxMoves = 22,
                hintText = "Every single mismatched turn reduces your chance of 3 stars.",
                gridRows = 5,
                gridCols = 4
            )
            GameModeType.MATH_CHALLENGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Arithmetic God $level",
                targetDescription = "Solve 8 multi-operation equations under 12s",
                targetValue = 8,
                timeLimitSeconds = 12,
                hintText = "Watch out for negative numbers and division remainder rules.",
                questionCount = 8
            )
            GameModeType.MISSING_NUMBER -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Cryptic Digits $level",
                targetDescription = "Crack 7 intricate multi-rule number sequences",
                targetValue = 7,
                hintText = "Formulas may involve prime numbers or triangular sequences.",
                questionCount = 7
            )
            GameModeType.NUMBER_MERGE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Singularity Merge $level",
                targetDescription = "Merge into ultimate tile ${512 * (1 shl levelFactor)}",
                targetValue = 512 * (1 shl levelFactor),
                maxMoves = 45,
                hintText = "Plan chain reactions 3 moves ahead.",
                gridRows = 4,
                gridCols = 4
            )
            GameModeType.NUMBER_SEQUENCE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Quantum Sequence $level",
                targetDescription = "Solve 7 advanced exponential patterns",
                targetValue = 7,
                hintText = "Compare ratios between numbers rather than just differences.",
                questionCount = 7
            )
            GameModeType.NUMBER_MEMORY -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Memory Champion $level",
                targetDescription = "Recall 7 numbers flashed for only 2.5 seconds",
                targetValue = 7,
                timeLimitSeconds = 2,
                hintText = "Create an instant mental story connecting all digits.",
                memoryCount = 7
            )
            GameModeType.BLOCK_PUZZLE -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "Block Apex $level",
                targetDescription = "Reach ${1500 + levelFactor * 250} points without overflowing",
                targetValue = 1500 + levelFactor * 250,
                hintText = "Keep row and column clearance synchronized.",
                gridRows = 6,
                gridCols = 6
            )
            GameModeType.GAME_2048 -> LevelConfig(
                levelNumber = level,
                mode = mode,
                category = DifficultyCategory.EXPERT,
                title = "2048 Titan $level",
                targetDescription = "Achieve tile ${512 * (1 shl levelFactor)} or higher!",
                targetValue = 512 * (1 shl levelFactor),
                maxMoves = 180,
                hintText = "Execute the classic snake maneuver along outer walls.",
                gridRows = 4,
                gridCols = 4
            )
        }
    }
}
