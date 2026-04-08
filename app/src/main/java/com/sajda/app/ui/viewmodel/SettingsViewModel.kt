package com.sajda.app.ui.viewmodel

import com.sajda.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AppUpdateInfo
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.CalendarDisplayMode
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUpdateUiState(
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val latestVersionName: String = "",
    val releaseName: String = "",
    val notes: String = "",
    val publishedAt: String = "",
    val releasePageUrl: String = "",
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null,
    val lastCheckedAt: String = ""
) {
    val hasUpdate: Boolean
        get() = latestVersionName.isNotBlank()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _appUpdateState = MutableStateFlow(AppUpdateUiState())
    val appUpdateState = _appUpdateState.asStateFlow()

    private var cachedUpdateInfo: AppUpdateInfo? = null

    init {
        viewModelScope.launch {
            settings.collect { latestSettings ->
                _appUpdateState.update { current ->
                    current.copy(lastCheckedAt = latestSettings.lastUpdateCheckAt)
                }
            }
        }
        checkForUpdates(silent = true)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setDarkMode(enabled) }
    }

    fun setNightMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setNightMode(enabled) }
    }

    fun setFocusMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setFocusMode(enabled) }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesDataStore.setAppLanguage(language) }
    }

    fun setShowTranslation(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShowTranslation(enabled) }
    }

    fun setArabicOnly(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setArabicOnly(enabled) }
    }

    fun setShowTransliteration(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShowTransliteration(enabled) }
    }

    fun setQuranReadingMode(mode: QuranReadingMode) {
        viewModelScope.launch { preferencesDataStore.setQuranReadingMode(mode) }
    }

    fun setArabicFontSize(size: Int) {
        viewModelScope.launch { preferencesDataStore.setArabicFontSize(size) }
    }

    fun setTranslationFontSize(size: Int) {
        viewModelScope.launch { preferencesDataStore.setTranslationFontSize(size) }
    }

    fun setVerseSpacing(spacing: Int) {
        viewModelScope.launch { preferencesDataStore.setVerseSpacing(spacing) }
    }

    fun setAdzanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAdzanEnabled(enabled)
            reschedulePrayerAlarms()
        }
    }

    fun setOverrideSilentMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setOverrideSilentMode(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setVibrationEnabled(enabled) }
    }

    fun setAdzanSound(style: com.sajda.app.domain.model.AdhanStyle) {
        viewModelScope.launch { preferencesDataStore.setAdzanSound(style) }
    }

    fun setFajrAdzanSound(style: com.sajda.app.domain.model.AdhanStyle) {
        viewModelScope.launch { preferencesDataStore.setFajrAdzanSound(style) }
    }

    fun setSelectedQuranReciter(reciter: QuranReciter) {
        viewModelScope.launch { preferencesDataStore.setSelectedQuranReciter(reciter) }
    }

    fun setCalendarDisplayMode(mode: CalendarDisplayMode) {
        viewModelScope.launch { preferencesDataStore.setCalendarDisplayMode(mode) }
    }

    fun setPrayerCalculationMethod(method: PrayerCalculationMethod) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerCalculationMethod(method)
            reschedulePrayerAlarms()
        }
    }

    fun setAsrMadhhab(madhhab: AsrMadhhab) {
        viewModelScope.launch {
            preferencesDataStore.setAsrMadhhab(madhhab)
            reschedulePrayerAlarms()
        }
    }

    fun setAutoLocation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAutoLocation(enabled)
            reschedulePrayerAlarms()
        }
    }

    fun setLocation(cityPreset: CityPreset) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(
                locationName = cityPreset.displayName,
                latitude = cityPreset.latitude,
                longitude = cityPreset.longitude,
                automatic = false
            )
            reschedulePrayerAlarms()
        }
    }

    fun setCurrentLocation(locationName: String, latitude: Double, longitude: Double, automatic: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                automatic = automatic
            )
            reschedulePrayerAlarms()
        }
    }

    fun toggleFavoriteLocation(locationName: String) {
        viewModelScope.launch {
            preferencesDataStore.toggleFavoriteLocation(locationName)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferencesDataStore.setOnboardingCompleted(true) }
    }

    fun setAdhanSnoozeMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesDataStore.setAdhanSnoozeMinutes(minutes)
        }
    }

    fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAutoUpdateCheckEnabled(enabled)
        }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            _appUpdateState.update { it.copy(isChecking = true, errorMessage = null) }
            val updateInfo = appUpdateRepository.fetchLatestRelease()
            preferencesDataStore.setLastUpdateCheckAt()
            cachedUpdateInfo = updateInfo
            _appUpdateState.update { current ->
                when (updateInfo) {
                    null -> current.copy(
                        latestVersionName = "",
                        releaseName = "",
                        notes = "",
                        publishedAt = "",
                        releasePageUrl = "",
                        isChecking = false,
                        isDownloading = false,
                        errorMessage = if (silent) null else "Belum ada versi publik yang lebih baru saat ini.",
                        lastCheckedAt = preferencesDataStore.settingsFlow.first().lastUpdateCheckAt
                    )

                    else -> current.copy(
                        latestVersionName = updateInfo.versionName,
                        releaseName = updateInfo.releaseName,
                        notes = updateInfo.notes,
                        publishedAt = updateInfo.publishedAt,
                        releasePageUrl = updateInfo.releasePageUrl,
                        isChecking = false,
                        isDownloading = false,
                        errorMessage = null,
                        lastCheckedAt = preferencesDataStore.settingsFlow.first().lastUpdateCheckAt
                    )
                }
            }
        }
    }

    fun downloadLatestUpdate() {
        val updateInfo = cachedUpdateInfo ?: return
        viewModelScope.launch {
            _appUpdateState.update {
                it.copy(
                    isDownloading = true,
                    errorMessage = "Unduhan update dimulai. Tunggu sampai notifikasi instalasi muncul."
                )
            }
            appUpdateRepository.enqueueUpdateDownload(updateInfo)
            _appUpdateState.update { it.copy(isDownloading = false) }
        }
    }

    fun setQuranReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setQuranReminder(enabled, time) }
    }

    fun setMorningDzikirReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setMorningDzikirReminder(enabled, time) }
    }

    fun setEveningDzikirReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { preferencesDataStore.setEveningDzikirReminder(enabled, time) }
    }

    fun setPrayerEnabled(prayerName: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerEnabled(prayerName, enabled)
            reschedulePrayerAlarms()
        }
    }

    fun refreshPrayerSchedule() {
        viewModelScope.launch {
            reschedulePrayerAlarms()
        }
    }

    private suspend fun reschedulePrayerAlarms() {
        val latestSettings = preferencesDataStore.settingsFlow.first()
        val prayerTimes = prayerTimeRepository.refreshPrayerTimes(latestSettings)
        adzanScheduler.reschedule(prayerTimes, latestSettings)
    }
}
