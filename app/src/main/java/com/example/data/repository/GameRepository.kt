package com.example.data.repository

import com.example.data.dao.GameDao
import com.example.data.model.AchievementEntity
import com.example.data.model.LevelProgressEntity
import com.example.data.model.ModeHighScoreEntity
import com.example.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameDao: GameDao,
    val prefs: PreferencesManager
) {
    val allLevels: Flow<List<LevelProgressEntity>> = gameDao.getAllLevels()
    val completedCount: Flow<Int> = gameDao.getCompletedLevelCount()
    val totalStars: Flow<Int?> = gameDao.getTotalStars()
    val allHighScores: Flow<List<ModeHighScoreEntity>> = gameDao.getAllHighScores()
    val achievements: Flow<List<AchievementEntity>> = gameDao.getAllAchievements()

    suspend fun initDefaultAchievements() {
        val defaults = listOf(
            AchievementEntity("first_win", "First Step", "Complete your first level", 1, 0, rewardCoins = 50),
            AchievementEntity("level_10", "Level Climber", "Complete 10 levels", 10, 0, rewardCoins = 100),
            AchievementEntity("level_25", "Easy Master", "Complete all 25 Easy levels", 25, 0, rewardCoins = 200),
            AchievementEntity("level_50", "Medium Victor", "Complete 50 levels", 50, 0, rewardCoins = 300),
            AchievementEntity("level_100", "Puzzle Legend", "Complete all 100 levels", 100, 0, rewardCoins = 1000),
            AchievementEntity("merge_master", "Merge Wizard", "Merge 50 number tiles", 50, 0, rewardCoins = 150),
            AchievementEntity("math_prodigy", "Math Genius", "Answer 30 math questions correctly", 30, 0, rewardCoins = 150),
            AchievementEntity("star_collector", "Star Hunter", "Collect 50 stars across levels", 50, 0, rewardCoins = 250),
            AchievementEntity("coin_hoarder", "Treasure Keeper", "Accumulate 500 total coins", 500, 0, rewardCoins = 200)
        )
        gameDao.insertDefaultAchievements(defaults)
    }

    suspend fun saveLevelCompletion(levelNumber: Int, stars: Int, score: Int, timeSeconds: Int, modeId: String): Int {
        val current = gameDao.getLevel(levelNumber)
        val bestStars = maxOf(stars, current?.stars ?: 0)
        val bestScore = maxOf(score, current?.bestScore ?: 0)
        val bestTime = if (current?.bestTimeSeconds ?: 0 > 0) {
            minOf(timeSeconds, current?.bestTimeSeconds ?: timeSeconds)
        } else {
            timeSeconds
        }

        val updated = LevelProgressEntity(
            levelNumber = levelNumber,
            stars = bestStars,
            isCompleted = true,
            bestScore = bestScore,
            bestTimeSeconds = bestTime,
            modeId = modeId
        )
        gameDao.insertOrUpdateLevel(updated)

        // Calculate coins earned:
        // Base: 25 coins, +15 for 3 stars, +20 for first completion
        var coinsEarned = 25
        if (stars == 3) coinsEarned += 15
        if (current == null || !current.isCompleted) coinsEarned += 20

        prefs.addCoins(coinsEarned)
        return coinsEarned
    }

    suspend fun recordModeScore(modeId: String, score: Int, isWin: Boolean) {
        val existing = gameDao.getHighScore(modeId)
        val newHigh = maxOf(score, existing?.highScore ?: 0)
        val games = (existing?.gamesPlayed ?: 0) + 1
        val wins = (existing?.wins ?: 0) + (if (isWin) 1 else 0)

        gameDao.insertOrUpdateHighScore(
            ModeHighScoreEntity(
                modeId = modeId,
                highScore = newHigh,
                gamesPlayed = games,
                wins = wins
            )
        )
    }

    suspend fun claimAchievement(achievement: AchievementEntity) {
        if (achievement.isUnlocked && !achievement.isClaimed) {
            prefs.addCoins(achievement.rewardCoins)
            gameDao.updateAchievement(achievement.copy(isClaimed = true))
        }
    }

    suspend fun resetAllProgress() {
        gameDao.clearLevelProgress()
        gameDao.clearHighScores()
        gameDao.clearAchievements()
        prefs.resetAll()
        initDefaultAchievements()
    }
}
