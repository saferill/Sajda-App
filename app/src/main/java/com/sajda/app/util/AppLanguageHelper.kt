package com.sajda.app.util

import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.UserSettings

fun AppLanguage.isEnglish(): Boolean = this != AppLanguage.INDONESIAN

fun AppLanguage.displayName(): String {
    return when (this) {
        AppLanguage.INDONESIAN -> "Indonesia"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.ARABIC -> "Arabic"
        AppLanguage.TURKISH -> "Turkish"
        AppLanguage.URDU -> "Urdu"
        AppLanguage.FRENCH -> "French"
        AppLanguage.MALAY -> "Malay"
        AppLanguage.HINDI -> "Hindi"
    }
}

fun AppLanguage.pick(indonesian: String, english: String): String {
    return if (this == AppLanguage.INDONESIAN) indonesian else english
}

fun UserSettings.pick(indonesian: String, english: String): String = appLanguage.pick(indonesian, english)

fun PrayerName.displayName(language: AppLanguage): String {
    return when (this) {
        PrayerName.FAJR -> language.pick("Subuh", "Fajr")
        PrayerName.DHUHR -> language.pick("Dzuhur", "Dhuhr")
        PrayerName.ASR -> language.pick("Ashar", "Asr")
        PrayerName.MAGHRIB -> language.pick("Maghrib", "Maghrib")
        PrayerName.ISHA -> language.pick("Isya", "Isha")
    }
}

fun localizedPrayerName(label: String, language: AppLanguage): String {
    return when (label.trim().lowercase()) {
        "subuh", "fajr" -> PrayerName.FAJR.displayName(language)
        "dzuhur", "dhuhr" -> PrayerName.DHUHR.displayName(language)
        "ashar", "asr" -> PrayerName.ASR.displayName(language)
        "maghrib" -> PrayerName.MAGHRIB.displayName(language)
        "isya", "isha" -> PrayerName.ISHA.displayName(language)
        else -> label
    }
}

fun QuranReadingMode.displayLabel(language: AppLanguage): String {
    return when (this) {
        QuranReadingMode.ARABIC_ONLY -> language.pick("Arab saja", "Arabic only")
        QuranReadingMode.ARABIC_INDONESIAN -> language.pick("Arab + Indonesia", "Arabic + Indonesian")
        QuranReadingMode.ARABIC_ENGLISH -> language.pick("Arab + English", "Arabic + English")
        QuranReadingMode.ALL -> language.pick("Semua", "All")
    }
}

fun PrayerCalculationMethod.localizedDescription(language: AppLanguage): String {
    return when (this) {
        PrayerCalculationMethod.KEMENAG -> language.pick(
            "Profil Indonesia dengan Subuh lebih awal dan Isya yang cenderung konservatif.",
            "Indonesian profile with an earlier Fajr and a more conservative Isha setting."
        )
        PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE -> language.pick(
            "Metode global yang umum dipakai banyak aplikasi Muslim modern.",
            "A global method commonly used by modern Muslim apps."
        )
        PrayerCalculationMethod.UMM_AL_QURA -> language.pick(
            "Fajar berbasis sudut, sedangkan Isya dihitung dengan interval tetap setelah Maghrib.",
            "Fajr uses an angle while Isha is calculated using a fixed interval after Maghrib."
        )
        PrayerCalculationMethod.ISNA -> language.pick(
            "Metode ringan dengan sudut 15 derajat untuk Fajar dan Isya.",
            "A lighter method using 15-degree angles for Fajr and Isha."
        )
    }
}

fun AsrMadhhab.localizedDescription(language: AppLanguage): String {
    return when (this) {
        AsrMadhhab.SHAFII -> language.pick(
            "Bayangan 1x tinggi benda. Ini yang paling umum dipakai di Indonesia.",
            "Shadow length equals 1x the object's height. This is the most common method in Indonesia."
        )
        AsrMadhhab.HANAFI -> language.pick(
            "Bayangan 2x tinggi benda.",
            "Shadow length equals 2x the object's height."
        )
    }
}
