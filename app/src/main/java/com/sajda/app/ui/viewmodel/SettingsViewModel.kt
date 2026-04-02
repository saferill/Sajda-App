package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

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

    fun setAdzanSound(sound: String) {
        viewModelScope.launch { preferencesDataStore.setAdzanSound(sound) }
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
                locationName = cityPreset.name,
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
