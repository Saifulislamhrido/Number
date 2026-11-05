package com.example.i18n

enum class Language(val code: String, val displayName: String) {
    EN("en", "English"),
    BN("bn", "বাংলা")
}

object Strings {
    fun get(key: String, lang: Language): String {
        val entry = dictionary[key] ?: return key
        return when (lang) {
            Language.EN -> entry.first
            Language.BN -> entry.second
        }
    }

    // Convert digits 0-9 to Bengali numerals if Bengali selected, or keep English
    fun formatNumber(number: Int, lang: Language): String {
        val str = number.toString()
        if (lang == Language.EN) return str
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return str.map { ch ->
            if (ch in '0'..'9') bengaliDigits[ch - '0'] else ch
        }.joinToString("")
    }

    private val dictionary = mapOf(
        // General / App
        "app_name" to Pair("Number Master 3D", "নাম্বার মাস্টার ৩ডি"),
        "subtitle" to Pair("Number Brain Challenge", "নাম্বার ব্রেইন চ্যালেঞ্জ"),
        "play" to Pair("PLAY QUEST", "খেলা শুরু করুন"),
        "game_modes" to Pair("GAME MODES", "গেম মোডসমূহ"),
        "level_select" to Pair("LEVEL SELECT", "লেভেল নির্বাচন"),
        "daily_challenge" to Pair("DAILY CHALLENGE", "দৈনিক চ্যালেঞ্জ"),
        "stats_achievements" to Pair("STATS & AWARDS", "পরিসংখ্যান ও অর্জন"),
        "settings" to Pair("SETTINGS", "সেটিংস"),
        "coins" to Pair("Coins", "কয়েন"),
        "stars" to Pair("Stars", "স্টার"),
        "best_score" to Pair("Best Score", "সেরা স্কোর"),
        "score" to Pair("Score", "স্কোর"),
        "moves" to Pair("Moves", "চাল"),
        "time" to Pair("Time", "সময়"),
        "target" to Pair("Target", "লক্ষ্য"),
        "level" to Pair("Level", "লেভেল"),
        "pause" to Pair("PAUSED", "সাময়িক স্থগিত"),
        "resume" to Pair("Resume", "চালিয়ে যান"),
        "restart" to Pair("Restart", "পুনরায় শুরু"),
        "undo" to Pair("Undo", "পূর্বাবস্থায়"),
        "hint" to Pair("Hint", "ইঙ্গিত"),
        "back" to Pair("Back", "পেছনে"),
        "home" to Pair("Home", "হোম"),
        "next_level" to Pair("Next Level", "পরবর্তী লেভেল"),
        "claim" to Pair("Claim", "গ্রহণ করুন"),
        "claimed" to Pair("Claimed", "গ্রহণ করা হয়েছে"),
        "confirm" to Pair("Confirm", "নিশ্চিত করুন"),
        "cancel" to Pair("Cancel", "বাতিল"),
        "close" to Pair("Close", "বন্ধ"),

        // Dialogs & Results
        "victory" to Pair("VICTORY!", "অভিনন্দন! বিজয়!"),
        "game_over" to Pair("GAME OVER", "খেলা শেষ"),
        "level_completed" to Pair("Level Completed!", "লেভেল সম্পন্ন হয়েছে!"),
        "earned_coins" to Pair("Coins Earned:", "অর্জিত কয়েন:"),
        "perfect_bonus" to Pair("Perfect Bonus!", "নিখুঁত বোনাস!"),
        "try_again" to Pair("Try Again", "আবার চেষ্টা করুন"),
        "use_hint_title" to Pair("Need a Hint?", "ইঙ্গিত প্রয়োজন?"),
        "use_hint_cost" to Pair("Unlock this hint for 20 coins?", "২০ কয়েন দিয়ে এই ইঙ্গিতটি দেখতে চান?"),
        "free_hint" to Pair("Use Free Hint", "বিনামূল্যে ইঙ্গিত ব্যবহার করুন"),
        "not_enough_coins" to Pair("Not enough coins! Complete levels to earn more.", "পর্যাপ্ত কয়েন নেই! আরো অর্জন করতে লেভেল খেলুন।"),
        "reset_progress_title" to Pair("Reset Game Progress?", "গেম অগ্রগতি রিসেট করবেন?"),
        "reset_progress_desc" to Pair("This will reset all completed levels, stars, and coins. This action cannot be undone!", "এটি আপনার সমস্ত সম্পূর্ণ লেভেল, স্টার এবং কয়েন রিসেট করবে। এটি পূর্বাবস্থায় ফেরানো যাবে না!"),

        // Categories
        "category_easy" to Pair("Easy (1-25)", "সহজ (১-২৫)"),
        "category_medium" to Pair("Medium (26-50)", "মাঝারি (২৬-৫০)"),
        "category_hard" to Pair("Hard (51-75)", "কঠিন (৫১-৭৫)"),
        "category_expert" to Pair("Expert (76-100)", "বিশেষজ্ঞ (৭৬-১০০)"),

        // Game Modes
        "mode_match_title" to Pair("Number Match", "নাম্বার ম্যাচ"),
        "mode_match_desc" to Pair("Find and match identical number pairs to clear the board.", "বোর্ড পরিষ্কার করতে একই সংখ্যার জোড়া মেলান।"),
        "mode_merge_title" to Pair("Number Merge", "নাম্বার মার্জ"),
        "mode_merge_desc" to Pair("Merge identical numbers to reach the ultimate target.", "চূড়ান্ত লক্ষ্যে পৌঁছাতে একই সংখ্যা সংযুক্ত করুন (২+২=৪, ৪+৪=৮)।"),
        "mode_2048_title" to Pair("2048 Puzzle", "২০৪৮ পাজল"),
        "mode_2048_desc" to Pair("Swipe tiles across the 4x4 grid to merge numbers and reach 2048!", "৪x৪ গ্রিডে টাইলস সোয়াইপ করে সংখ্যা মার্জ করুন এবং ২০৪৮ এ পৌঁছান!"),
        "mode_block_title" to Pair("Block Puzzle", "ব্লক পাজল"),
        "mode_block_desc" to Pair("Place number block pieces to fill rows and columns for mega combos.", "সারি ও কলাম পূরণ করে মেগা কম্বো পেতে ব্লক বসান।"),
        "mode_math_title" to Pair("Math Challenge", "গণিত চ্যালেঞ্জ"),
        "mode_math_desc" to Pair("Fast-paced mental math with +, -, ×, and ÷ under time pressure.", "সময়ের সাথে সাথে দ্রুত যোগ, বিয়োগ, গুণ ও ভাগ সমাধান করুন।"),
        "mode_missing_title" to Pair("Missing Number", "অনুপস্থিত সংখ্যা"),
        "mode_missing_desc" to Pair("Identify the hidden number in smart logic progressions.", "যৌক্তিক ধারার লুকানো সংখ্যাটি চিহ্নিত করুন।"),
        "mode_sequence_title" to Pair("Number Sequence", "সংখ্যার ধারা"),
        "mode_sequence_desc" to Pair("Discover the sequence rule and calculate the next number.", "ধারার নিয়ম আবিষ্কার করুন এবং পরবর্তী সংখ্যাটি বের করুন।"),
        "mode_memory_title" to Pair("Number Memory", "নাম্বার মেমোরি"),
        "mode_memory_desc" to Pair("Memorize the flashed numbers and tap them in correct order.", "উঁকি দেওয়া সংখ্যাগুলি মুখস্থ করুন এবং সঠিক ক্রমানুসারে ট্যাপ করুন।"),

        // Settings
        "settings_sound" to Pair("Sound Effects", "শব্দ প্রভাব"),
        "settings_music" to Pair("Background Music", "পটভূমির সঙ্গীত"),
        "settings_vibration" to Pair("Haptic Vibration", "ভাইব্রেশন"),
        "settings_language" to Pair("Language / ভাষা", "ভাষা / Language"),
        "settings_dark_mode" to Pair("Dark Theme", "ডার্ক থিম"),
        "settings_reset" to Pair("Reset All Progress", "সমস্ত অগ্রগতি রিসেট করুন"),
        "settings_about" to Pair("About Number Master 3D", "নাম্বার মাস্টার ৩ডি সম্পর্কে"),
        "about_text" to Pair("Number Master 3D is a 100% offline premium number brain game with 8 puzzle modes, 100 curated levels, tactile 3D visuals, and rewards.", "নাম্বার মাস্টার ৩ডি একটি ১০০% অফলাইন প্রিমিয়াম নাম্বার ব্রেইন গেম যাতে রয়েছে ৮টি পাজল মোড, ১০০টি লেভেল, ৩ডি ভিজ্যুয়াল এবং পুরস্কার।"),

        // Daily Challenge & Stats
        "streak_days" to Pair("Day Streak", "দিনের ধারাবাহিকতা"),
        "daily_ready" to Pair("Today's Brain Puzzle is Ready!", "আজকের ব্রেইন পাজল প্রস্তুত!"),
        "daily_reward_claimed" to Pair("Daily Reward Claimed Today!", "আজকের দৈনিক পুরস্কার গ্রহণ করা হয়েছে!"),
        "stats_games_played" to Pair("Total Games Played", "মোট খেলা হয়েছে"),
        "stats_levels_cleared" to Pair("Levels Completed", "সম্পন্ন লেভেল"),
        "stats_stars_collected" to Pair("Stars Collected", "অর্জিত স্টার"),
        "stats_coins_earned" to Pair("Total Coins Earned", "মোট অর্জিত কয়েন"),
        "achievements_title" to Pair("Achievements", "অর্জিত সাফল্য"),

        // In-game instructions & hints
        "correct" to Pair("Correct!", "সঠিক!"),
        "wrong" to Pair("Wrong!", "ভুল!"),
        "memorize_time" to Pair("Memorize the numbers!", "সংখ্যাগুলি মনে রাখুন!"),
        "tap_in_order" to Pair("Tap numbers in ascending order (1, 2, 3...)", "ছোট থেকে বড় ক্রমানুসারে সংখ্যা ট্যাপ করুন")
    )
}
