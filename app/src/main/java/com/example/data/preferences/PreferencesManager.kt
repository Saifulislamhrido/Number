package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.i18n.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("number_master_prefs", Context.MODE_PRIVATE)

    private val _coins = MutableStateFlow(prefs.getInt(KEY_COINS, 100))
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _language = MutableStateFlow(
        if (prefs.getString(KEY_LANGUAGE, "en") == "bn") Language.BN else Language.EN
    )
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isMusicEnabled = MutableStateFlow(prefs.getBoolean(KEY_MUSIC, true))
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _dailyStreak = MutableStateFlow(prefs.getInt(KEY_STREAK, 1))
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    private val _freeHints = MutableStateFlow(prefs.getInt(KEY_FREE_HINTS, 3))
    val freeHints: StateFlow<Int> = _freeHints.asStateFlow()

    fun addCoins(amount: Int) {
        val newCoins = (_coins.value + amount).coerceAtLeast(0)
        _coins.value = newCoins
        prefs.edit().putInt(KEY_COINS, newCoins).apply()
    }

    fun spendCoins(amount: Int): Boolean {
        if (_coins.value < amount) return false
        val newCoins = _coins.value - amount
        _coins.value = newCoins
        prefs.edit().putInt(KEY_COINS, newCoins).apply()
        return true
    }

    fun useFreeHint(): Boolean {
        if (_freeHints.value > 0) {
            val updated = _freeHints.value - 1
            _freeHints.value = updated
            prefs.edit().putInt(KEY_FREE_HINTS, updated).apply()
            return true
        }
        return false
    }

    fun addFreeHints(count: Int) {
        val updated = _freeHints.value + count
        _freeHints.value = updated
        prefs.edit().putInt(KEY_FREE_HINTS, updated).apply()
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun setMusicEnabled(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
    }

    fun canClaimDailyReward(): Boolean {
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val lastClaim = prefs.getString(KEY_LAST_CLAIM_DATE, "")
        return today != lastClaim
    }

    fun claimDailyReward(): Int {
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val streak = _dailyStreak.value
        val rewardAmount = (50 + (streak.coerceAtMost(7) - 1) * 25)
        addCoins(rewardAmount)

        // Update streak
        val newStreak = (streak % 7) + 1
        _dailyStreak.value = newStreak

        prefs.edit()
            .putString(KEY_LAST_CLAIM_DATE, today)
            .putInt(KEY_STREAK, newStreak)
            .apply()

        return rewardAmount
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _coins.value = 100
        _dailyStreak.value = 1
        _freeHints.value = 3
        _language.value = Language.EN
        _isSoundEnabled.value = true
        _isMusicEnabled.value = true
        _isVibrationEnabled.value = true
        _isDarkMode.value = true
    }

    companion object {
        private const val KEY_COINS = "coins"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_SOUND = "sound"
        private const val KEY_MUSIC = "music"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LAST_CLAIM_DATE = "last_claim_date"
        private const val KEY_STREAK = "daily_streak"
        private const val KEY_FREE_HINTS = "free_hints"
    }
}
