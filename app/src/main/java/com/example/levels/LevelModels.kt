package com.example.levels

enum class DifficultyCategory(val labelKey: String, val range: IntRange) {
    EASY("category_easy", 1..25),
    MEDIUM("category_medium", 26..50),
    HARD("category_hard", 51..75),
    EXPERT("category_expert", 76..100);

    companion object {
        fun fromLevel(level: Int): DifficultyCategory {
            return when (level) {
                in 1..25 -> EASY
                in 26..50 -> MEDIUM
                in 51..75 -> HARD
                else -> EXPERT
            }
        }
    }
}

enum class GameModeType(val id: String, val titleKey: String, val descKey: String) {
    NUMBER_MATCH("match", "mode_match_title", "mode_match_desc"),
    NUMBER_MERGE("merge", "mode_merge_title", "mode_merge_desc"),
    GAME_2048("game_2048", "mode_2048_title", "mode_2048_desc"),
    BLOCK_PUZZLE("block", "mode_block_title", "mode_block_desc"),
    MATH_CHALLENGE("math", "mode_math_title", "mode_math_desc"),
    MISSING_NUMBER("missing", "mode_missing_title", "mode_missing_desc"),
    NUMBER_SEQUENCE("sequence", "mode_sequence_title", "mode_sequence_desc"),
    NUMBER_MEMORY("memory", "mode_memory_title", "mode_memory_desc")
}

data class LevelConfig(
    val levelNumber: Int,
    val mode: GameModeType,
    val category: DifficultyCategory,
    val title: String,
    val targetDescription: String,
    val targetValue: Int,
    val maxMoves: Int = 0,
    val timeLimitSeconds: Int = 0,
    val hintText: String,
    // Custom parameters for puzzle generation
    val gridRows: Int = 4,
    val gridCols: Int = 4,
    val questionCount: Int = 5,
    val memoryCount: Int = 4
)
