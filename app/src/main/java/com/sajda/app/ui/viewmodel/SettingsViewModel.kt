package com.sajda.app.ui.viewmodel

import android.app.DownloadManager
import com.sajda.app.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.data.repository.BackupRepository
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.AudioDownloadMode
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject



data class BackupUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val lastFilePath: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler,
    private val backupRepository: BackupRepository
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState = _backupState.asStateFlow()

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

    fun setAudioDownloadMode(mode: AudioDownloadMode) {
        viewModelScope.launch { preferencesDataStore.setAudioDownloadMode(mode) }
    }

    fun setWifiOnlyAudioDownloads(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setWifiOnlyAudioDownloads(enabled) }
    }

    fun setCalendarDisplayMode(mode: CalendarDisplayMode) {
        viewModelScope.launch { preferencesDataStore.setCalendarDisplayMode(mode) }
    }    fun setPrayerCalculationMethod(method: PrayerCalculationMethod) {
        viewModelScope.launch { preferencesDataStore.setPrayerCalculationMethod(method) }
    }

    fun setAsrMadhhab(madhhab: AsrMadhhab) {
        viewModelScope.launch { preferencesDataStore.setAsrMadhhab(madhhab) }
    }




    fun completeOnboarding() {
        viewModelScope.launch { preferencesDataStore.setOnboardingCompleted(true) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { preferencesDataStore.setOnboardingCompleted(completed) }
    }

    fun completePermissionSetup() {
        viewModelScope.launch { preferencesDataStore.setPermissionSetupCompleted(true) }
    }

    fun setPermissionSetupCompleted(completed: Boolean) {
        viewModelScope.launch { preferencesDataStore.setPermissionSetupCompleted(completed) }
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



    fun exportBackup() {
        viewModelScope.launch {
            _backupState.value = BackupUiState(isBusy = true)
            val result = backupRepository.exportToDefaultFile()
            _backupState.value = result.fold(
                onSuccess = { file ->
                    BackupUiState(
                        isBusy = false,
                        message = "Backup tersimpan di ${file.name}",
                        lastFilePath = file.absolutePath
                    )
                },
                onFailure = { error ->
                    BackupUiState(
                        isBusy = false,
                        message = error.message ?: "Backup gagal dibuat"
                    )
                }
            )
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            _backupState.value = BackupUiState(isBusy = true)
            val result = backupRepository.restoreFromDefaultFile()
            _backupState.value = result.fold(
                onSuccess = { file ->
                    BackupUiState(
                        isBusy = false,
                        message = "Backup ${file.name} berhasil dipulihkan",
                        lastFilePath = file.absolutePath
                    )
                },
                onFailure = { error ->
                    BackupUiState(
                        isBusy = false,
                        message = error.message ?: "Restore backup gagal"
                    )
                }
            )
            reschedulePrayerAlarms()
        }
    }

    private suspend fun reschedulePrayerAlarms() {
        val latestSettings = preferencesDataStore.settingsFlow.first()
        val prayerTimes = prayerTimeRepository.refreshPrayerTimes(latestSettings)
        adzanScheduler.reschedule(prayerTimes, latestSettings)
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

    fun updateLocation(locationName: String, latitude: Double, longitude: Double, automatic: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(locationName, latitude, longitude, automatic)
        }
    }

    fun setAutoLocation(auto: Boolean, refreshSchedule: Boolean = false) {
        viewModelScope.launch { 
            preferencesDataStore.setAutoLocation(auto) 
            if (refreshSchedule) reschedulePrayerAlarms()
        }
    }

    fun setLocationPermissionPrompted(prompted: Boolean) {
        viewModelScope.launch { preferencesDataStore.setLocationPermissionPrompted(prompted) }
    }
}
