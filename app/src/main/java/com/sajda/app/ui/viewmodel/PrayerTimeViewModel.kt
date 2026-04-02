package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerTimeUiState(
    val todayPrayerTime: PrayerTime? = null,
    val weeklyPrayerTimes: List<PrayerTime> = emptyList(),
    val monthlyPrayerTimes: List<PrayerTime> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val isRefreshing: Boolean = true
)

class PrayerTimeViewModel(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val adzanScheduler: AdzanScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerTimeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observePrayerTimes()
        observeSettings()
        refreshPrayerTimes()
    }

    private fun observePrayerTimes() {
        viewModelScope.launch {
            prayerTimeRepository.observeTodayPrayerTime().collect { prayerTime ->
                _uiState.update { it.copy(todayPrayerTime = prayerTime, isRefreshing = false) }
            }
        }

        viewModelScope.launch {
            prayerTimeRepository.observeWeeklyPrayerTimes().collect { prayerTimes ->
                _uiState.update { it.copy(weeklyPrayerTimes = prayerTimes, isRefreshing = false) }
            }
        }

        viewModelScope.launch {
            prayerTimeRepository.observeMonthlyPrayerTimes().collect { prayerTimes ->
                _uiState.update { it.copy(monthlyPrayerTimes = prayerTimes, isRefreshing = false) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            preferencesDataStore.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    fun refreshPrayerTimes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val settings = preferencesDataStore.settingsFlow.first()
            val times = prayerTimeRepository.refreshPrayerTimes(settings)
            adzanScheduler.reschedule(times, settings)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleAdzan(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setAdzanEnabled(enabled)
            refreshPrayerTimes()
        }
    }

    fun setLocation(cityPreset: CityPreset) {
        viewModelScope.launch {
            preferencesDataStore.updateLocation(
                cityPreset.displayName,
                cityPreset.latitude,
                cityPreset.longitude,
                automatic = false
            )
            refreshPrayerTimes()
        }
    }

    fun togglePrayer(prayerName: PrayerName, enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerEnabled(prayerName, enabled)
            refreshPrayerTimes()
        }
    }
}
