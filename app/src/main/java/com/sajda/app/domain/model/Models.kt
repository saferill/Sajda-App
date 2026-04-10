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
    val downloadedAt: Long? = null,
    val downloadedReciterId: String? = null,
    val downloadedReciterIds: Set<String> = emptySet(),
    val downloadedAudioBytes: Long = 0L
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

enum class AdhanStyle(
    val id: String,
    val title: String,
    val regularResName: String,
    val subuhResName: String
) {
    DEFAULT("system_default", "Default Android", "adzan_regular", "adzan_subuh"),
    MAKKAH("makkah", "Makkah", "adzan_makkah", "adzan_subuh"),
    MADINAH("madinah", "Madinah", "adzan_madinah", "adzan_subuh"),
    MISHARY("mishary", "Mishary Rashid Alafasy", "adzan_mishary", "adzan_subuh"),
    ABDULBASET("abdulbaset", "Abdul Baset Abdussamad", "adzan_abdulbasit", "adzan_subuh");

    companion object {
        fun fromId(id: String): AdhanStyle = entries.find { it.id == id } ?: DEFAULT
    }
}

enum class QuranReciter(
    val id: String,
    val title: String
) {
    ABDULLAH_AL_JUHANY("01", "Abdullah Al-Juhany"),
    ABDUL_MUHSIN_AL_QASIM("02", "Abdul Muhsin Al-Qasim"),
    ABDURRAHMAN_AS_SUDAIS("03", "Abdurrahman as-Sudais"),
    IBRAHIM_AL_DOSSARI("04", "Ibrahim Al-Dossari"),
    MISYARI_RASYID_AL_AFASI("05", "Misyari Rasyid Al-Afasi"),
    YASSER_AL_DOSARI("06", "Yasser Al-Dosari");

    companion object {
        fun fromId(id: String): QuranReciter = entries.find { it.id == id } ?: MISYARI_RASYID_AL_AFASI
    }
}

enum class AudioDownloadMode {
    SELECTED_RECITER_ONLY,
    ALL_RECITERS
}

enum class CalendarDisplayMode {
    HIJRI,
    GREGORIAN
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
    val locationPermissionPrompted: Boolean = false,
    val locationName: String = "",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val adzanSound: AdhanStyle = AdhanStyle.DEFAULT,
    val fajrAdzanSound: AdhanStyle = AdhanStyle.DEFAULT,
    val selectedQuranReciter: QuranReciter = QuranReciter.MISYARI_RASYID_AL_AFASI,
    val audioDownloadMode: AudioDownloadMode = AudioDownloadMode.ALL_RECITERS,
    val wifiOnlyAudioDownloads: Boolean = false,
    val calendarDisplayMode: CalendarDisplayMode = CalendarDisplayMode.HIJRI,
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
    val autoUpdateCheckEnabled: Boolean = false,
    val lastUpdateCheckAt: String = "",
    val adhanSnoozeMinutes: Int = 10,
    val fajrAdzanEnabled: Boolean = true,
    val dhuhrAdzanEnabled: Boolean = true,
    val asrAdzanEnabled: Boolean = true,
    val maghribAdzanEnabled: Boolean = true,
    val ishaAdzanEnabled: Boolean = true,
    val lastBackupAt: String = "",
    val lastRestoreAt: String = ""
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
    ENGLISH,
    ARABIC,
    SPANISH,
    GERMAN,
    PORTUGUESE,
    CHINESE,
    JAPANESE,
    KOREAN,
    ITALIAN,
    POLISH,
    UKRAINIAN,
    SWAHILI,
    TAGALOG,
    TURKISH,
    URDU,
    FRENCH,
    MALAY,
    HINDI;

    val languageTag: String
        get() = when (this) {
            INDONESIAN -> "id"
            ENGLISH -> "en"
            ARABIC -> "ar"
            SPANISH -> "es"
            GERMAN -> "de"
            PORTUGUESE -> "pt"
            CHINESE -> "zh"
            JAPANESE -> "ja"
            KOREAN -> "ko"
            ITALIAN -> "it"
            POLISH -> "pl"
            UKRAINIAN -> "uk"
            SWAHILI -> "sw"
            TAGALOG -> "tl"
            TURKISH -> "tr"
            URDU -> "ur"
            FRENCH -> "fr"
            MALAY -> "ms"
            HINDI -> "hi"
        }
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

enum class HadithBook(
    val apiId: String,
    val title: String
) {
    BUKHARI("bukhari", "Shahih Bukhari"),
    MUSLIM("muslim", "Shahih Muslim"),
    TIRMIDZI("tirmidzi", "Sunan Tirmidzi"),
    NASAI("nasai", "Sunan Nasai"),
    ABU_DAUD("abu-daud", "Sunan Abu Daud"),
    IBNU_MAJAH("ibnu-majah", "Sunan Ibnu Majah"),
    AHMAD("ahmad", "Musnad Ahmad"),
    DARIMI("darimi", "Sunan Darimi"),
    MALIK("malik", "Muwatha Malik");
}

data class TafsirEntry(
    val surahNumber: Int,
    val ayatNumber: Int,
    val sourceName: String,
    val sourceDescription: String = "",
    val text: String
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
