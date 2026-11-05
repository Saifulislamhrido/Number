package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val bestTimeSeconds: Int = 0,
    val modeId: String = ""
)

@Entity(tableName = "mode_high_scores")
data class ModeHighScoreEntity(
    @PrimaryKey val modeId: String,
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    val wins: Int = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val titleKey: String,
    val descKey: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 50
)
