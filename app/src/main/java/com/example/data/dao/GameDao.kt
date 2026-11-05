package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AchievementEntity
import com.example.data.model.LevelProgressEntity
import com.example.data.model.ModeHighScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Level Progress
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllLevels(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevel(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLevel(level: LevelProgressEntity)

    @Query("SELECT COUNT(*) FROM level_progress WHERE isCompleted = 1")
    fun getCompletedLevelCount(): Flow<Int>

    @Query("SELECT SUM(stars) FROM level_progress")
    fun getTotalStars(): Flow<Int?>

    @Query("DELETE FROM level_progress")
    suspend fun clearLevelProgress()

    // Mode High Scores
    @Query("SELECT * FROM mode_high_scores")
    fun getAllHighScores(): Flow<List<ModeHighScoreEntity>>

    @Query("SELECT * FROM mode_high_scores WHERE modeId = :modeId LIMIT 1")
    suspend fun getHighScore(modeId: String): ModeHighScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHighScore(score: ModeHighScoreEntity)

    @Query("DELETE FROM mode_high_scores")
    suspend fun clearHighScores()

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultAchievements(achievements: List<AchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()
}
