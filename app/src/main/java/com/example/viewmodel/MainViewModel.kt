package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AchievementEntity
import com.example.data.model.LevelProgressEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.GameRepository
import com.example.i18n.Language
import com.example.levels.GameModeType
import com.example.levels.LevelConfig
import com.example.levels.LevelManager
import com.example.sound.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Home : ScreenDestination()
    object GameModes : ScreenDestination()
    object LevelSelect : ScreenDestination()
    object DailyChallenge : ScreenDestination()
    object Stats : ScreenDestination()
    object Settings : ScreenDestination()
    data class PlayLevel(val levelNumber: Int) : ScreenDestination()
    data class PlayMode(val mode: GameModeType) : ScreenDestination()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val prefs = PreferencesManager(application)
    val repository = GameRepository(database.gameDao(), prefs)
    val soundManager = SoundManager(application)

    private val screenStack = mutableListOf<ScreenDestination>(ScreenDestination.Home)
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Home)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    val coins: StateFlow<Int> = prefs.coins
    val language: StateFlow<Language> = prefs.language
    val isSoundEnabled: StateFlow<Boolean> = prefs.isSoundEnabled
    val isMusicEnabled: StateFlow<Boolean> = prefs.isMusicEnabled
    val isVibrationEnabled: StateFlow<Boolean> = prefs.isVibrationEnabled
    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
    val dailyStreak: StateFlow<Int> = prefs.dailyStreak
    val freeHints: StateFlow<Int> = prefs.freeHints

    val completedLevels: StateFlow<List<LevelProgressEntity>> = repository.allLevels.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val completedCount: StateFlow<Int> = repository.completedCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalStars: StateFlow<Int?> = repository.totalStars.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        soundManager.isSoundEnabled = prefs.isSoundEnabled.value
        soundManager.isMusicEnabled = prefs.isMusicEnabled.value
        soundManager.isVibrationEnabled = prefs.isVibrationEnabled.value
        if (soundManager.isMusicEnabled) {
            soundManager.startMusic()
        }

        viewModelScope.launch {
            repository.initDefaultAchievements()
        }
    }

    fun navigateTo(dest: ScreenDestination) {
        screenStack.add(dest)
        _currentScreen.value = dest
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
            _currentScreen.value = screenStack.last()
            return true
        } else if (_currentScreen.value !is ScreenDestination.Home) {
            _currentScreen.value = ScreenDestination.Home
            screenStack.clear()
            screenStack.add(ScreenDestination.Home)
            return true
        }
        return false
    }

    fun getMaxUnlockedLevel(completed: List<LevelProgressEntity>): Int {
        val completedNumbers = completed.filter { it.isCompleted }.map { it.levelNumber }.toSet()
        for (i in 1..100) {
            if (!completedNumbers.contains(i)) return i
        }
        return 100
    }

    fun onLevelWon(levelNumber: Int, stars: Int, score: Int, time: Int, modeId: String) {
        viewModelScope.launch {
            repository.saveLevelCompletion(levelNumber, stars, score, time, modeId)
            repository.recordModeScore(modeId, score, true)
        }
    }

    fun spendCoinsForHint(cost: Int = 20): Boolean {
        return prefs.spendCoins(cost)
    }

    fun useFreeHint(): Boolean {
        return prefs.useFreeHint()
    }

    fun claimDailyReward(): Int {
        return prefs.claimDailyReward()
    }

    fun canClaimDailyReward(): Boolean {
        return prefs.canClaimDailyReward()
    }

    fun claimAchievement(achievement: AchievementEntity) {
        viewModelScope.launch {
            repository.claimAchievement(achievement)
        }
    }

    fun setLanguage(lang: Language) {
        prefs.setLanguage(lang)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.setSoundEnabled(enabled)
        soundManager.isSoundEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.setMusicEnabled(enabled)
        soundManager.isMusicEnabled = enabled
        if (enabled) {
            soundManager.startMusic()
        } else {
            soundManager.stopMusic()
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.setVibrationEnabled(enabled)
        soundManager.isVibrationEnabled = enabled
    }

    fun setDarkMode(dark: Boolean) {
        prefs.setDarkMode(dark)
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopMusic()
    }
}
