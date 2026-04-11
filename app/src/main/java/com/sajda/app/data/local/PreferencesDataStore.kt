package com.sajda.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sajda.app.domain.model.AdhanLogEntry
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.AdhanStyle
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.util.DateTimeUtils
import com.sajda.app.util.LocationConstants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sajda_preferences")

class PreferencesDataStore(private val context: Context) {

    private val gson = Gson()

    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val NIGHT_MODE = booleanPreferencesKey("night_mode")
        private val FOCUS_MODE = booleanPreferencesKey("focus_mode")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")
        private val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        private val ARABIC_ONLY = booleanPreferencesKey("arabic_only")
        private val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        private val QURAN_READING_MODE = stringPreferencesKey("quran_reading_mode")
        private val ARABIC_FONT_SIZE = intPreferencesKey("arabic_font_size")
        private val TRANSLATION_FONT_SIZE = intPreferencesKey("translation_font_size")
        private val VERSE_SPACING = intPreferencesKey("verse_spacing")
        private val ADZAN_ENABLED = booleanPreferencesKey("adzan_enabled")
        private val OVERRIDE_SILENT_MODE = booleanPreferencesKey("override_silent_mode")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val AUTO_LOCATION = booleanPreferencesKey("auto_location")
        private val LOCATION_PERMISSION_PROMPTED = booleanPreferencesKey("location_permission_prompted")
        private val LATITUDE = doublePreferencesKey("latitude")
        private val LONGITUDE = doublePreferencesKey("longitude")
        private val LOCATION = stringPreferencesKey("location")
        private val ADZAN_SOUND = stringPreferencesKey("adzan_sound")
        private val FAJR_ADZAN_SOUND = stringPreferencesKey("fajr_adzan_sound")
        private val QURAN_RECITER = stringPreferencesKey("quran_reciter")
        private val AUDIO_DOWNLOAD_MODE = stringPreferencesKey("audio_download_mode")
        private val AUDIO_DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("audio_download_wifi_only")
        private val CALENDAR_DISPLAY_MODE = stringPreferencesKey("calendar_display_mode")
        private val PRAYER_CALCULATION_METHOD = stringPreferencesKey("prayer_calculation_method")
        private val ASR_MADHHAB = stringPreferencesKey("asr_madhhab")
        private val LAST_PLAYED_SURAH = intPreferencesKey("last_played_surah")
        private val DAILY_AYAT_READ = intPreferencesKey("daily_ayat_read")
        private val STREAK_COUNT = intPreferencesKey("streak_count")
        private val ACTIVITY_DATE = stringPreferencesKey("activity_date")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val PERMISSION_SETUP_COMPLETED = booleanPreferencesKey("permission_setup_completed")
        private val FAVORITE_LOCATIONS = stringSetPreferencesKey("favorite_locations")
        private val QURAN_REMINDER_ENABLED = booleanPreferencesKey("quran_reminder_enabled")
        private val QURAN_REMINDER_TIME = stringPreferencesKey("quran_reminder_time")
        private val MORNING_DZIKIR_ENABLED = booleanPreferencesKey("morning_dzikir_enabled")
        private val MORNING_DZIKIR_TIME = stringPreferencesKey("morning_dzikir_time")
        private val EVENING_DZIKIR_ENABLED = booleanPreferencesKey("evening_dzikir_enabled")
        private val EVENING_DZIKIR_TIME = stringPreferencesKey("evening_dzikir_time")
        private val DUA_BOOKMARKS = stringSetPreferencesKey("dua_bookmarks")
        private val LAST_ADHAN_PRAYER = stringPreferencesKey("last_adhan_prayer")
        private val LAST_ADHAN_STATUS = stringPreferencesKey("last_adhan_status")
        private val LAST_ADHAN_AT = stringPreferencesKey("last_adhan_at")
        private val ADHAN_HISTORY_JSON = stringPreferencesKey("adhan_history_json")
        private val NEXT_SCHEDULED_PRAYER = stringPreferencesKey("next_scheduled_prayer")
        private val NEXT_SCHEDULED_AT = stringPreferencesKey("next_scheduled_at")
        private val AUTO_UPDATE_CHECK_ENABLED = booleanPreferencesKey("auto_update_check_enabled")
        private val LAST_UPDATE_CHECK_AT = stringPreferencesKey("last_update_check_at")
        private val LAST_NOTIFIED_UPDATE_VERSION = stringPreferencesKey("last_notified_update_version")
        private val ADHAN_SNOOZE_MINUTES = intPreferencesKey("adhan_snooze_minutes")
        private val LAST_UPDATE_DOWNLOAD_ID = longPreferencesKey("last_update_download_id")
        private val LAST_BACKUP_AT = stringPreferencesKey("last_backup_at")
        private val LAST_RESTORE_AT = stringPreferencesKey("last_restore_at")
        private val FAJR_ENABLED = booleanPreferencesKey("fajr_enabled")
        private val DHUHR_ENABLED = booleanPreferencesKey("dhuhr_enabled")
        private val ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        private val MAGHRIB_ENABLED = booleanPreferencesKey("maghrib_enabled")
        private val ISHA_ENABLED = booleanPreferencesKey("isha_enabled")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            darkMode = preferences[DARK_MODE] ?: false,
            nightMode = preferences[NIGHT_MODE] ?: false,
            focusMode = preferences[FOCUS_MODE] ?: false,
            appLanguage = preferences[APP_LANGUAGE]?.let {
                runCatching { AppLanguage.valueOf(it) }.getOrDefault(AppLanguage.INDONESIAN)
            } ?: AppLanguage.INDONESIAN,
            showTranslation = preferences[SHOW_TRANSLATION] ?: true,
            arabicOnly = preferences[ARABIC_ONLY] ?: false,
            showTransliteration = preferences[SHOW_TRANSLITERATION] ?: false,
            quranReadingMode = preferences[QURAN_READING_MODE]?.let {
                runCatching { QuranReadingMode.valueOf(it) }.getOrDefault(QuranReadingMode.ARABIC_INDONESIAN)
            } ?: QuranReadingMode.ARABIC_INDONESIAN,
            arabicFontSize = (preferences[ARABIC_FONT_SIZE] ?: 30).coerceIn(24, 40),
            translationFontSize = (preferences[TRANSLATION_FONT_SIZE] ?: 16).coerceIn(12, 22),
            verseSpacing = (preferences[VERSE_SPACING] ?: 18).coerceIn(12, 28),
            adzanEnabled = preferences[ADZAN_ENABLED] ?: true,
            overrideSilentMode = preferences[OVERRIDE_SILENT_MODE] ?: false,
            vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
            autoLocation = preferences[AUTO_LOCATION] ?: false,
            locationPermissionPrompted = preferences[LOCATION_PERMISSION_PROMPTED] ?: false,
            locationName = preferences[LOCATION] ?: "",
            latitude = preferences[LATITUDE] ?: LocationConstants.DEFAULT_LATITUDE,
            longitude = preferences[LONGITUDE] ?: LocationConstants.DEFAULT_LONGITUDE,
            adzanSound = preferences[ADZAN_SOUND]?.let { AdhanStyle.fromId(it) } ?: AdhanStyle.DEFAULT,
            fajrAdzanSound = preferences[FAJR_ADZAN_SOUND]?.let { AdhanStyle.fromId(it) } ?: AdhanStyle.DEFAULT,
            selectedQuranReciter = preferences[QURAN_RECITER]?.let { QuranReciter.fromId(it) }
                ?: QuranReciter.MISYARI_RASYID_AL_AFASI,
            audioDownloadMode = preferences[AUDIO_DOWNLOAD_MODE]?.let {
                runCatching { AudioDownloadMode.valueOf(it) }.getOrDefault(AudioDownloadMode.ALL_RECITERS)
            } ?: AudioDownloadMode.ALL_RECITERS,
            wifiOnlyAudioDownloads = preferences[AUDIO_DOWNLOAD_WIFI_ONLY] ?: false,
            calendarDisplayMode = preferences[CALENDAR_DISPLAY_MODE]?.let {
                runCatching { CalendarDisplayMode.valueOf(it) }.getOrDefault(CalendarDisplayMode.HIJRI)
            } ?: CalendarDisplayMode.HIJRI,
            prayerCalculationMethod = preferences[PRAYER_CALCULATION_METHOD]?.let {
                runCatching { PrayerCalculationMethod.valueOf(it) }.getOrDefault(PrayerCalculationMethod.KEMENAG)
            } ?: PrayerCalculationMethod.KEMENAG,
            asrMadhhab = preferences[ASR_MADHHAB]?.let {
                runCatching { AsrMadhhab.valueOf(it) }.getOrDefault(AsrMadhhab.SHAFII)
            } ?: AsrMadhhab.SHAFII,
            lastPlayedSurah = preferences[LAST_PLAYED_SURAH] ?: 0,
            dailyAyatRead = preferences[DAILY_AYAT_READ] ?: 0,
            streakCount = preferences[STREAK_COUNT] ?: 0,
            activityDate = preferences[ACTIVITY_DATE] ?: "",
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            permissionSetupCompleted = preferences[PERMISSION_SETUP_COMPLETED] ?: false,
            favoriteLocationNames = preferences[FAVORITE_LOCATIONS].orEmpty(),
            quranReminderEnabled = preferences[QURAN_REMINDER_ENABLED] ?: false,
            quranReminderTime = preferences[QURAN_REMINDER_TIME] ?: "20:00",
            morningDzikirReminderEnabled = preferences[MORNING_DZIKIR_ENABLED] ?: false,
            morningDzikirReminderTime = preferences[MORNING_DZIKIR_TIME] ?: "05:30",
            eveningDzikirReminderEnabled = preferences[EVENING_DZIKIR_ENABLED] ?: false,
            eveningDzikirReminderTime = preferences[EVENING_DZIKIR_TIME] ?: "18:00",
            lastAdhanPrayer = preferences[LAST_ADHAN_PRAYER] ?: "",
            lastAdhanStatus = preferences[LAST_ADHAN_STATUS] ?: "",
            lastAdhanAt = preferences[LAST_ADHAN_AT] ?: "",
            adhanHistory = parseAdhanHistory(preferences[ADHAN_HISTORY_JSON]),
            nextScheduledPrayer = preferences[NEXT_SCHEDULED_PRAYER] ?: "",
            nextScheduledAt = preferences[NEXT_SCHEDULED_AT] ?: "",
            autoUpdateCheckEnabled = preferences[AUTO_UPDATE_CHECK_ENABLED] ?: false,
            lastUpdateCheckAt = preferences[LAST_UPDATE_CHECK_AT] ?: "",
            adhanSnoozeMinutes = preferences[ADHAN_SNOOZE_MINUTES] ?: 10,
            fajrAdzanEnabled = preferences[FAJR_ENABLED] ?: true,
            dhuhrAdzanEnabled = preferences[DHUHR_ENABLED] ?: true,
            asrAdzanEnabled = preferences[ASR_ENABLED] ?: true,
            maghribAdzanEnabled = preferences[MAGHRIB_ENABLED] ?: true,
            ishaAdzanEnabled = preferences[ISHA_ENABLED] ?: true,
            lastBackupAt = preferences[LAST_BACKUP_AT] ?: "",
            lastRestoreAt = preferences[LAST_RESTORE_AT] ?: ""
        )
    }

    suspend fun setDarkMode(enabled: Boolean) = context.dataStore.edit { it[DARK_MODE] = enabled }

    suspend fun setNightMode(enabled: Boolean) = context.dataStore.edit { it[NIGHT_MODE] = enabled }

    suspend fun setFocusMode(enabled: Boolean) = context.dataStore.edit { it[FOCUS_MODE] = enabled }

    suspend fun setAppLanguage(language: AppLanguage) = context.dataStore.edit {
        it[APP_LANGUAGE] = language.name
    }

    suspend fun setShowTranslation(enabled: Boolean) = context.dataStore.edit {
        it[SHOW_TRANSLATION] = enabled
    }

    suspend fun setArabicOnly(enabled: Boolean) = context.dataStore.edit {
        it[ARABIC_ONLY] = enabled
    }

    suspend fun setShowTransliteration(enabled: Boolean) = context.dataStore.edit {
        it[SHOW_TRANSLITERATION] = enabled
    }

    suspend fun setQuranReadingMode(mode: QuranReadingMode) = context.dataStore.edit {
        it[QURAN_READING_MODE] = mode.name
        when (mode) {
            QuranReadingMode.ARABIC_ONLY -> {
                it[ARABIC_ONLY] = true
                it[SHOW_TRANSLATION] = false
            }

            QuranReadingMode.ARABIC_INDONESIAN,
            QuranReadingMode.ARABIC_ENGLISH -> {
                it[ARABIC_ONLY] = false
                it[SHOW_TRANSLATION] = true
            }

            QuranReadingMode.ALL -> {
                it[ARABIC_ONLY] = false
                it[SHOW_TRANSLATION] = true
                it[SHOW_TRANSLITERATION] = true
            }
        }
    }

    suspend fun setArabicFontSize(size: Int) = context.dataStore.edit {
        it[ARABIC_FONT_SIZE] = size.coerceIn(24, 40)
    }

    suspend fun setTranslationFontSize(size: Int) = context.dataStore.edit {
        it[TRANSLATION_FONT_SIZE] = size.coerceIn(12, 22)
    }

    suspend fun setVerseSpacing(spacing: Int) = context.dataStore.edit {
        it[VERSE_SPACING] = spacing.coerceIn(12, 28)
    }

    suspend fun setAdzanEnabled(enabled: Boolean) = context.dataStore.edit { it[ADZAN_ENABLED] = enabled }

    suspend fun setOverrideSilentMode(enabled: Boolean) = context.dataStore.edit {
        it[OVERRIDE_SILENT_MODE] = enabled
    }

    suspend fun setVibrationEnabled(enabled: Boolean) = context.dataStore.edit {
        it[VIBRATION_ENABLED] = enabled
    }

    suspend fun setAutoLocation(enabled: Boolean) = context.dataStore.edit { it[AUTO_LOCATION] = enabled }

    suspend fun setLocationPermissionPrompted(prompted: Boolean) = context.dataStore.edit {
        it[LOCATION_PERMISSION_PROMPTED] = prompted
    }

    suspend fun updateLocation(locationName: String, latitude: Double, longitude: Double, automatic: Boolean) {
        context.dataStore.edit {
            it[LOCATION] = locationName
            it[LATITUDE] = latitude
            it[LONGITUDE] = longitude
            it[AUTO_LOCATION] = automatic
        }
    }

    suspend fun setAdzanSound(style: AdhanStyle) = context.dataStore.edit { it[ADZAN_SOUND] = style.id }

    suspend fun setFajrAdzanSound(style: AdhanStyle) = context.dataStore.edit { it[FAJR_ADZAN_SOUND] = style.id }

    suspend fun setSelectedQuranReciter(reciter: QuranReciter) = context.dataStore.edit {
        it[QURAN_RECITER] = reciter.id
    }

    suspend fun setAudioDownloadMode(mode: AudioDownloadMode) = context.dataStore.edit {
        it[AUDIO_DOWNLOAD_MODE] = mode.name
    }

    suspend fun setWifiOnlyAudioDownloads(enabled: Boolean) = context.dataStore.edit {
        it[AUDIO_DOWNLOAD_WIFI_ONLY] = enabled
    }

    suspend fun setCalendarDisplayMode(mode: CalendarDisplayMode) = context.dataStore.edit {
        it[CALENDAR_DISPLAY_MODE] = mode.name
    }

    suspend fun setPrayerCalculationMethod(method: PrayerCalculationMethod) = context.dataStore.edit {
        it[PRAYER_CALCULATION_METHOD] = method.name
    }

    suspend fun setAsrMadhhab(madhhab: AsrMadhhab) = context.dataStore.edit {
        it[ASR_MADHHAB] = madhhab.name
    }

    suspend fun setLastPlayedSurah(surahNumber: Int) = context.dataStore.edit {
        it[LAST_PLAYED_SURAH] = surahNumber
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = context.dataStore.edit {
        it[ONBOARDING_COMPLETED] = completed
    }

    suspend fun setPermissionSetupCompleted(completed: Boolean) = context.dataStore.edit {
        it[PERMISSION_SETUP_COMPLETED] = completed
    }

    suspend fun toggleFavoriteLocation(locationName: String) = context.dataStore.edit { preferences ->
        val current = preferences[FAVORITE_LOCATIONS].orEmpty().toMutableSet()
        if (!current.add(locationName)) {
            current.remove(locationName)
        }
        preferences[FAVORITE_LOCATIONS] = current
    }

    suspend fun setQuranReminder(enabled: Boolean, time: String? = null) = context.dataStore.edit {
        it[QURAN_REMINDER_ENABLED] = enabled
        time?.let { selected -> it[QURAN_REMINDER_TIME] = selected }
    }

    suspend fun setMorningDzikirReminder(enabled: Boolean, time: String? = null) = context.dataStore.edit {
        it[MORNING_DZIKIR_ENABLED] = enabled
        time?.let { selected -> it[MORNING_DZIKIR_TIME] = selected }
    }

    suspend fun setEveningDzikirReminder(enabled: Boolean, time: String? = null) = context.dataStore.edit {
        it[EVENING_DZIKIR_ENABLED] = enabled
        time?.let { selected -> it[EVENING_DZIKIR_TIME] = selected }
    }

    suspend fun toggleDuaBookmark(duaId: String) = context.dataStore.edit { preferences ->
        val current = preferences[DUA_BOOKMARKS].orEmpty().toMutableSet()
        if (!current.add(duaId)) {
            current.remove(duaId)
        }
        preferences[DUA_BOOKMARKS] = current
    }

    val duaBookmarksFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[DUA_BOOKMARKS].orEmpty()
    }

    suspend fun setPrayerEnabled(prayerName: PrayerName, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            when (prayerName) {
                PrayerName.FAJR -> preferences[FAJR_ENABLED] = enabled
                PrayerName.DHUHR -> preferences[DHUHR_ENABLED] = enabled
                PrayerName.ASR -> preferences[ASR_ENABLED] = enabled
                PrayerName.MAGHRIB -> preferences[MAGHRIB_ENABLED] = enabled
                PrayerName.ISHA -> preferences[ISHA_ENABLED] = enabled
            }
        }
    }

    suspend fun updateAdhanLastEvent(
        prayerName: String,
        status: String,
        occurredAt: String = DateTimeUtils.dateTimeString(),
        occurredAtEpochMillis: Long = System.currentTimeMillis(),
        details: String = ""
    ) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ADHAN_PRAYER] = prayerName
            preferences[LAST_ADHAN_STATUS] = status
            preferences[LAST_ADHAN_AT] = occurredAt
            preferences[ADHAN_HISTORY_JSON] = updatedAdhanHistoryJson(
                currentRaw = preferences[ADHAN_HISTORY_JSON],
                prayerName = prayerName,
                status = status,
                occurredAt = occurredAt,
                occurredAtEpochMillis = occurredAtEpochMillis,
                details = details
            )
        }
    }

    suspend fun appendAdhanLog(
        prayerName: String,
        status: String,
        occurredAt: String = DateTimeUtils.dateTimeString(),
        occurredAtEpochMillis: Long = System.currentTimeMillis(),
        details: String = ""
    ) {
        context.dataStore.edit { preferences ->
            preferences[ADHAN_HISTORY_JSON] = updatedAdhanHistoryJson(
                currentRaw = preferences[ADHAN_HISTORY_JSON],
                prayerName = prayerName,
                status = status,
                occurredAt = occurredAt,
                occurredAtEpochMillis = occurredAtEpochMillis,
                details = details
            )
        }
    }

    suspend fun updateNextScheduledPrayer(
        prayerName: String,
        scheduledAt: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[NEXT_SCHEDULED_PRAYER] = prayerName
            preferences[NEXT_SCHEDULED_AT] = scheduledAt
        }
    }

    suspend fun setAdhanSnoozeMinutes(minutes: Int) = context.dataStore.edit {
        it[ADHAN_SNOOZE_MINUTES] = minutes.coerceIn(5, 30)
    }

    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) = context.dataStore.edit {
        it[AUTO_UPDATE_CHECK_ENABLED] = enabled
    }

    suspend fun setLastUpdateCheckAt(checkedAt: String = DateTimeUtils.dateTimeString()) = context.dataStore.edit {
        it[LAST_UPDATE_CHECK_AT] = checkedAt
    }

    suspend fun setLastNotifiedUpdateVersion(versionName: String) = context.dataStore.edit {
        it[LAST_NOTIFIED_UPDATE_VERSION] = versionName
    }

    suspend fun getLastNotifiedUpdateVersion(): String {
        return context.dataStore.data.first()[LAST_NOTIFIED_UPDATE_VERSION] ?: ""
    }

    suspend fun setLastUpdateDownloadId(downloadId: Long) = context.dataStore.edit {
        it[LAST_UPDATE_DOWNLOAD_ID] = downloadId
    }

    suspend fun setLastBackupAt(value: String = DateTimeUtils.dateTimeString()) = context.dataStore.edit {
        it[LAST_BACKUP_AT] = value
    }

    suspend fun setLastRestoreAt(value: String = DateTimeUtils.dateTimeString()) = context.dataStore.edit {
        it[LAST_RESTORE_AT] = value
    }

    suspend fun restoreSettings(snapshot: UserSettings) = context.dataStore.edit { preferences ->
        preferences[DARK_MODE] = snapshot.darkMode
        preferences[NIGHT_MODE] = snapshot.nightMode
        preferences[FOCUS_MODE] = snapshot.focusMode
        preferences[APP_LANGUAGE] = snapshot.appLanguage.name
        preferences[SHOW_TRANSLATION] = snapshot.showTranslation
        preferences[ARABIC_ONLY] = snapshot.arabicOnly
        preferences[SHOW_TRANSLITERATION] = snapshot.showTransliteration
        preferences[QURAN_READING_MODE] = snapshot.quranReadingMode.name
        preferences[ARABIC_FONT_SIZE] = snapshot.arabicFontSize
        preferences[TRANSLATION_FONT_SIZE] = snapshot.translationFontSize
        preferences[VERSE_SPACING] = snapshot.verseSpacing
        preferences[ADZAN_ENABLED] = snapshot.adzanEnabled
        preferences[OVERRIDE_SILENT_MODE] = snapshot.overrideSilentMode
        preferences[VIBRATION_ENABLED] = snapshot.vibrationEnabled
        preferences[AUTO_LOCATION] = snapshot.autoLocation
        preferences[LOCATION_PERMISSION_PROMPTED] = snapshot.locationPermissionPrompted
        preferences[LOCATION] = snapshot.locationName
        preferences[LATITUDE] = snapshot.latitude
        preferences[LONGITUDE] = snapshot.longitude
        preferences[ADZAN_SOUND] = snapshot.adzanSound.id
        preferences[FAJR_ADZAN_SOUND] = snapshot.fajrAdzanSound.id
        preferences[QURAN_RECITER] = snapshot.selectedQuranReciter.id
        preferences[AUDIO_DOWNLOAD_MODE] = snapshot.audioDownloadMode.name
        preferences[AUDIO_DOWNLOAD_WIFI_ONLY] = snapshot.wifiOnlyAudioDownloads
        preferences[CALENDAR_DISPLAY_MODE] = snapshot.calendarDisplayMode.name
        preferences[PRAYER_CALCULATION_METHOD] = snapshot.prayerCalculationMethod.name
        preferences[ASR_MADHHAB] = snapshot.asrMadhhab.name
        preferences[QURAN_REMINDER_ENABLED] = snapshot.quranReminderEnabled
        preferences[QURAN_REMINDER_TIME] = snapshot.quranReminderTime
        preferences[MORNING_DZIKIR_ENABLED] = snapshot.morningDzikirReminderEnabled
        preferences[MORNING_DZIKIR_TIME] = snapshot.morningDzikirReminderTime
        preferences[EVENING_DZIKIR_ENABLED] = snapshot.eveningDzikirReminderEnabled
        preferences[EVENING_DZIKIR_TIME] = snapshot.eveningDzikirReminderTime
        preferences[ONBOARDING_COMPLETED] = snapshot.onboardingCompleted
        preferences[PERMISSION_SETUP_COMPLETED] = snapshot.permissionSetupCompleted
        preferences[ADHAN_SNOOZE_MINUTES] = snapshot.adhanSnoozeMinutes
        preferences[FAJR_ENABLED] = snapshot.fajrAdzanEnabled
        preferences[DHUHR_ENABLED] = snapshot.dhuhrAdzanEnabled
        preferences[ASR_ENABLED] = snapshot.asrAdzanEnabled
        preferences[MAGHRIB_ENABLED] = snapshot.maghribAdzanEnabled
        preferences[ISHA_ENABLED] = snapshot.ishaAdzanEnabled
    }

    suspend fun getLastUpdateDownloadId(): Long? {
        return context.dataStore.data.first()[LAST_UPDATE_DOWNLOAD_ID]
    }

    suspend fun recordAyatRead() {
        val today = DateTimeUtils.todayString()
        val yesterday = LocalDate.now().minusDays(1).toString()

        context.dataStore.edit { preferences ->
            val previousDate = preferences[ACTIVITY_DATE] ?: ""
            val previousStreak = preferences[STREAK_COUNT] ?: 0

            when (previousDate) {
                today -> {
                    preferences[DAILY_AYAT_READ] = (preferences[DAILY_AYAT_READ] ?: 0) + 1
                }

                yesterday -> {
                    preferences[DAILY_AYAT_READ] = 1
                    preferences[STREAK_COUNT] = previousStreak + 1
                    preferences[ACTIVITY_DATE] = today
                }

                else -> {
                    preferences[DAILY_AYAT_READ] = 1
                    preferences[STREAK_COUNT] = 1
                    preferences[ACTIVITY_DATE] = today
                }
            }
        }
    }

    suspend fun resetDailyCounterIfNeeded() {
        val today = DateTimeUtils.todayString()
        context.dataStore.edit { preferences ->
            val activityDate = preferences[ACTIVITY_DATE] ?: ""
            if (activityDate.isNotEmpty() && activityDate != today) {
                preferences[DAILY_AYAT_READ] = 0
            }
        }
    }

    private fun parseAdhanHistory(raw: String?): List<AdhanLogEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val listType = object : TypeToken<List<AdhanLogEntry>>() {}.type
            gson.fromJson<List<AdhanLogEntry>>(raw, listType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun updatedAdhanHistoryJson(
        currentRaw: String?,
        prayerName: String,
        status: String,
        occurredAt: String,
        occurredAtEpochMillis: Long,
        details: String
    ): String {
        val history = parseAdhanHistory(currentRaw).toMutableList()
        history.add(
            0,
            AdhanLogEntry(
                prayerName = prayerName,
                status = status,
                occurredAt = occurredAt,
                occurredAtEpochMillis = occurredAtEpochMillis,
                details = details
            )
        )
        val sevenDaysAgo = System.currentTimeMillis() - (7L * 24L * 60L * 60L * 1000L)
        return gson.toJson(
            history
                .filter { it.occurredAtEpochMillis >= sevenDaysAgo }
                .take(80)
        )
    }
}
