package com.sajda.app.domain.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val transliteration: String,
    val translation: String,
    val englishTranslation: String = "",
    val revelationPlace: String,
    val totalVerses: Int,
    val audioUrl: String,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val downloadedAt: Long? = null
)

data class Ayat(
    val id: Int,
    val surahNumber: Int,
    val ayatNumber: Int,
    val textArabic: String,
    val translation: String,
    val englishTranslation: String = "",
    val transliteration: String
)

data class Bookmark(
    val id: Int = 0,
    val surahNumber: Int,
    val ayatNumber: Int,
    val surahName: String,
    val folderName: String = "Favorites",
    val note: String = "",
    val highlightColor: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class LastRead(
    val surahNumber: Int,
    val ayatNumber: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

data class PrayerTime(
    val date: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val qiblaDirection: Double
)

enum class PrayerName(val label: String, val key: String) {
    FAJR("Subuh", "fajr"),
    DHUHR("Dzuhur", "dhuhr"),
    ASR("Ashar", "asr"),
    MAGHRIB("Maghrib", "maghrib"),
    ISHA("Isya", "isha");
}

enum class PrayerCalculationMethod(
    val label: String,
    val description: String,
    val fajrAngle: Double,
    val ishaAngle: Double? = null,
    val ishaIntervalMinutes: Int? = null
) {
    KEMENAG(
        label = "Kemenag",
        description = "Profil Indonesia dengan Subuh lebih awal dan Isya konservatif.",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    MUSLIM_WORLD_LEAGUE(
        label = "Muslim World League",
        description = "Metode global yang umum dipakai aplikasi Muslim modern.",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    UMM_AL_QURA(
        label = "Umm al-Qura",
        description = "Fajar berbasis sudut, Isya memakai interval tetap setelah Maghrib.",
        fajrAngle = 18.5,
        ishaIntervalMinutes = 90
    ),
    ISNA(
        label = "ISNA",
        description = "Metode ringan dengan sudut 15° untuk Fajar dan Isya.",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    )
}

enum class AsrMadhhab(
    val label: String,
    val description: String,
    val shadowFactor: Double
) {
    SHAFII(
        label = "Syafi'i",
        description = "Bayangan 1x tinggi benda. Umum dipakai di Indonesia.",
        shadowFactor = 1.0
    ),
    HANAFI(
        label = "Hanafi",
        description = "Bayangan 2x tinggi benda.",
        shadowFactor = 2.0
    )
}

data class UserSettings(
    val darkMode: Boolean = false,
    val nightMode: Boolean = false,
    val focusMode: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.INDONESIAN,
    val showTranslation: Boolean = true,
    val arabicOnly: Boolean = false,
    val showTransliteration: Boolean = false,
    val quranReadingMode: QuranReadingMode = QuranReadingMode.ARABIC_INDONESIAN,
    val arabicFontSize: Int = 30,
    val translationFontSize: Int = 16,
    val verseSpacing: Int = 18,
    val adzanEnabled: Boolean = true,
    val overrideSilentMode: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val autoLocation: Boolean = false,
    val locationName: String = "Jakarta",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val adzanSound: String = "system_default",
    val prayerCalculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.KEMENAG,
    val asrMadhhab: AsrMadhhab = AsrMadhhab.SHAFII,
    val lastPlayedSurah: Int = 0,
    val dailyAyatRead: Int = 0,
    val streakCount: Int = 0,
    val activityDate: String = "",
    val onboardingCompleted: Boolean = false,
    val favoriteLocationNames: Set<String> = emptySet(),
    val quranReminderEnabled: Boolean = false,
    val quranReminderTime: String = "20:00",
    val morningDzikirReminderEnabled: Boolean = false,
    val morningDzikirReminderTime: String = "05:30",
    val eveningDzikirReminderEnabled: Boolean = false,
    val eveningDzikirReminderTime: String = "18:00",
    val lastAdhanPrayer: String = "",
    val lastAdhanStatus: String = "",
    val lastAdhanAt: String = "",
    val adhanHistory: List<AdhanLogEntry> = emptyList(),
    val nextScheduledPrayer: String = "",
    val nextScheduledAt: String = "",
    val autoUpdateCheckEnabled: Boolean = true,
    val lastUpdateCheckAt: String = "",
    val adhanSnoozeMinutes: Int = 10,
    val fajrAdzanEnabled: Boolean = true,
    val dhuhrAdzanEnabled: Boolean = true,
    val asrAdzanEnabled: Boolean = true,
    val maghribAdzanEnabled: Boolean = true,
    val ishaAdzanEnabled: Boolean = true
)

data class AppUpdateInfo(
    val versionName: String,
    val releaseName: String,
    val notes: String,
    val downloadUrl: String,
    val releasePageUrl: String,
    val publishedAt: String
)

data class AudioDownloadState(
    val surahNumber: Int,
    val progress: Int = 0,
    val isDownloading: Boolean = false,
    val isFailed: Boolean = false
)

data class CityPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val areaType: String = "Kota",
    val province: String = "",
    val aliases: List<String> = emptyList()
) {
    val displayName: String
        get() = when {
            areaType.equals("Provinsi", ignoreCase = true) -> "Provinsi $name"
            province.isBlank() -> "$areaType $name"
            else -> "$areaType $name, $province"
        }

    val subtitle: String
        get() = when {
            areaType.equals("Provinsi", ignoreCase = true) -> "Provinsi"
            province.isBlank() -> areaType
            else -> "$areaType - $province"
        }

    fun matches(query: String): Boolean {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return true
        return searchableText.contains(normalizedQuery)
    }

    fun matchScore(query: String): Int {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) return 99
        return when {
            displayName.lowercase() == normalizedQuery -> 0
            name.lowercase() == normalizedQuery -> 1
            displayName.lowercase().startsWith(normalizedQuery) -> 2
            name.lowercase().startsWith(normalizedQuery) -> 3
            province.lowercase().startsWith(normalizedQuery) -> 4
            aliases.any { it.lowercase().startsWith(normalizedQuery) } -> 5
            searchableText.contains(normalizedQuery) -> 6
            else -> 99
        }
    }

    private val searchableText: String
        get() = buildString {
            append(name.lowercase())
            append(' ')
            append(displayName.lowercase())
            append(' ')
            append(areaType.lowercase())
            append(' ')
            append(province.lowercase())
            aliases.forEach { alias ->
                append(' ')
                append(alias.lowercase())
            }
        }
}

enum class AppLanguage {
    INDONESIAN,
    ENGLISH
}

enum class QuranReadingMode {
    ARABIC_ONLY,
    ARABIC_INDONESIAN,
    ARABIC_ENGLISH,
    ALL
}

data class DailyDua(
    val id: String,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val sourceLabel: String = ""
)

data class HadithEntry(
    val id: String,
    val category: String,
    val title: String,
    val collection: String,
    val reference: String,
    val narrator: String,
    val text: String,
    val arabicText: String = "",
    val sourceLabel: String = ""
)

data class AdhanLogEntry(
    val id: Long = System.currentTimeMillis(),
    val prayerName: String,
    val status: String,
    val occurredAt: String,
    val occurredAtEpochMillis: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class QuranSearchResult(
    val type: SearchResultType,
    val title: String,
    val subtitle: String,
    val surahNumber: Int,
    val ayatNumber: Int? = null
)

enum class SearchResultType {
    SURAH,
    AYAT
}

data class AudioPlaybackState(
    val title: String = "",
    val surahNumber: Int = 0,
    val audioPath: String? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val elapsedLabel: String = "00:00",
    val durationLabel: String = "00:00"
) {
    val isActive: Boolean
        get() = audioPath != null
}
