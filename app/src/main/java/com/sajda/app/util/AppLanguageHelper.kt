package com.sajda.app.util

import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.QuranReadingMode

fun AppLanguage.isEnglish(): Boolean = this != AppLanguage.INDONESIAN

fun AppLanguage.displayName(): String {
    return when (this) {
        AppLanguage.INDONESIAN -> "Indonesia"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.ARABIC -> "Arabic"
        AppLanguage.SPANISH -> "Spanish"
        AppLanguage.GERMAN -> "German"
        AppLanguage.PORTUGUESE -> "Portuguese"
        AppLanguage.CHINESE -> "Chinese"
        AppLanguage.JAPANESE -> "Japanese"
        AppLanguage.KOREAN -> "Korean"
        AppLanguage.ITALIAN -> "Italian"
        AppLanguage.POLISH -> "Polish"
        AppLanguage.UKRAINIAN -> "Ukrainian"
        AppLanguage.SWAHILI -> "Swahili"
        AppLanguage.TAGALOG -> "Tagalog"
        AppLanguage.TURKISH -> "Turkish"
        AppLanguage.URDU -> "Urdu"
        AppLanguage.FRENCH -> "French"
        AppLanguage.MALAY -> "Malay"
        AppLanguage.HINDI -> "Hindi"
    }
}

fun AppLanguage.displayNameRes(): Int {
    return when (this) {
        AppLanguage.INDONESIAN -> com.sajda.app.R.string.language_indonesian
        AppLanguage.ENGLISH -> com.sajda.app.R.string.language_english
        AppLanguage.ARABIC -> com.sajda.app.R.string.language_arabic
        AppLanguage.SPANISH -> com.sajda.app.R.string.language_spanish
        AppLanguage.GERMAN -> com.sajda.app.R.string.language_german
        AppLanguage.PORTUGUESE -> com.sajda.app.R.string.language_portuguese
        AppLanguage.CHINESE -> com.sajda.app.R.string.language_chinese
        AppLanguage.JAPANESE -> com.sajda.app.R.string.language_japanese
        AppLanguage.KOREAN -> com.sajda.app.R.string.language_korean
        AppLanguage.ITALIAN -> com.sajda.app.R.string.language_italian
        AppLanguage.POLISH -> com.sajda.app.R.string.language_polish
        AppLanguage.UKRAINIAN -> com.sajda.app.R.string.language_ukrainian
        AppLanguage.SWAHILI -> com.sajda.app.R.string.language_swahili
        AppLanguage.TAGALOG -> com.sajda.app.R.string.language_tagalog
        AppLanguage.TURKISH -> com.sajda.app.R.string.language_turkish
        AppLanguage.URDU -> com.sajda.app.R.string.language_urdu
        AppLanguage.FRENCH -> com.sajda.app.R.string.language_french
        AppLanguage.MALAY -> com.sajda.app.R.string.language_malay
        AppLanguage.HINDI -> com.sajda.app.R.string.language_hindi
    }
}

fun PrayerName.displayNameRes(): Int {
    return when (this) {
        PrayerName.FAJR -> com.sajda.app.R.string.fajr
        PrayerName.DHUHR -> com.sajda.app.R.string.dhuhr
        PrayerName.ASR -> com.sajda.app.R.string.asr_2
        PrayerName.MAGHRIB -> com.sajda.app.R.string.maghrib
        PrayerName.ISHA -> com.sajda.app.R.string.isha
    }
}

fun localizedPrayerNameRes(label: String): Int? {
    return when (label.trim().lowercase()) {
        "subuh", "fajr" -> PrayerName.FAJR.displayNameRes()
        "dzuhur", "dhuhr" -> PrayerName.DHUHR.displayNameRes()
        "ashar", "asr" -> PrayerName.ASR.displayNameRes()
        "maghrib" -> PrayerName.MAGHRIB.displayNameRes()
        "isya", "isha" -> PrayerName.ISHA.displayNameRes()
        else -> null
    }
}

fun QuranReadingMode.displayLabelRes(): Int {
    return when (this) {
        QuranReadingMode.ARABIC_ONLY -> com.sajda.app.R.string.arabic_only
        QuranReadingMode.ARABIC_INDONESIAN -> com.sajda.app.R.string.arabic_indonesian
        QuranReadingMode.ARABIC_ENGLISH -> com.sajda.app.R.string.arabic_english
        QuranReadingMode.ALL -> com.sajda.app.R.string.all
    }
}

fun PrayerCalculationMethod.localizedDescriptionRes(): Int {
    return when (this) {
        PrayerCalculationMethod.KEMENAG -> com.sajda.app.R.string.indonesian_profile_with_an_earlier_fajr
        PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE -> com.sajda.app.R.string.a_global_method_commonly_used_by_modern
        PrayerCalculationMethod.UMM_AL_QURA -> com.sajda.app.R.string.fajr_uses_an_angle_while_isha_is_calcula
        PrayerCalculationMethod.ISNA -> com.sajda.app.R.string.a_lighter_method_using_15_degree_angles
    }
}

fun AsrMadhhab.localizedDescriptionRes(): Int {
    return when (this) {
        AsrMadhhab.SHAFII -> com.sajda.app.R.string.shadow_length_equals_1x_the_object_s_hei
        AsrMadhhab.HANAFI -> com.sajda.app.R.string.shadow_length_equals_2x_the_object_s_hei
    }
}
