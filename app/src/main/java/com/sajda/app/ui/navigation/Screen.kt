package com.sajda.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    // Bottom Navigation Routes
    @Serializable data object Home : Screen
    @Serializable data object Quran : Screen
    @Serializable data object Adhan : Screen
    @Serializable data object Hadith : Screen
    @Serializable data object Ramadan : Screen
    @Serializable data object Settings : Screen

    // Setup Routes
    @Serializable data object Onboarding : Screen
    @Serializable data object PermissionSetup : Screen

    // Secondary Overlays / Other screens
    @Serializable data object Search : Screen
    @Serializable data object Bookmarks : Screen
    @Serializable data object AudioManager : Screen
    @Serializable data object DailyDua : Screen
    @Serializable data object Calendar : Screen
    @Serializable data object RamadanPractices : Screen
    @Serializable data object RamadanDua : Screen
    @Serializable data object WeeklyPrayer : Screen
    @Serializable data object SmartReminder : Screen
    @Serializable data object Qibla : Screen
    @Serializable data object AdhanSettings : Screen
    @Serializable data object LocationSettings : Screen
    @Serializable data object LanguageSettings : Screen
    @Serializable data object UpdateCenter : Screen
    @Serializable data object FullPlayer : Screen
    
    @Serializable
    data class Tafsir(val surahNumber: Int, val ayatNumber: Int) : Screen
}
