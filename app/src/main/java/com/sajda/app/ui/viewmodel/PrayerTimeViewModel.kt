package com.sajda.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerName
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.UserSettings
import com.sajda.app.service.AdzanScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrayerTimeUiState(
    val todayPrayerTime: PrayerTime? = null,
    val weeklyPrayerTimes: List<PrayerTime> = emptyList(),
    val monthlyPrayerTimes: List<PrayerTime> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val isRefreshing: Boolean = true
)

@HiltViewModel
class PrayerTimeViewModel @Inject constructor(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val adzanScheduler: AdzanScheduler
) : ViewModel() {

    companion object {
        private const val TAG = "PrayerTimeViewModel"
    }

    private val _uiState = MutableStateFlow(PrayerTimeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observePrayerTimes()
        observeSettings()
        refreshPrayerTimes()
    }

    private fun observePrayerTimes() {
        viewModelScope.launch {
            runCatching {
                prayerTimeRepository.observeTodayPrayerTime().collect { prayerTime ->
                    _uiState.update { it.copy(todayPrayerTime = prayerTime, isRefreshing = false) }
                }
            }.onFailure(::handlePrayerTimeError)
        }

        viewModelScope.launch {
            runCatching {
                prayerTimeRepository.observeWeeklyPrayerTimes().collect { prayerTimes ->
                    _uiState.update { it.copy(weeklyPrayerTimes = prayerTimes, isRefreshing = false) }
                }
            }.onFailure(::handlePrayerTimeError)
        }

        viewModelScope.launch {
            runCatching {
                prayerTimeRepository.observeMonthlyPrayerTimes().collect { prayerTimes ->
                    _uiState.update { it.copy(monthlyPrayerTimes = prayerTimes, isRefreshing = false) }
                }
            }.onFailure(::handlePrayerTimeError)
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            runCatching {
                preferencesDataStore.settingsFlow.collect { settings ->
                    _uiState.update { it.copy(settings = settings) }
                }
            }.onFailure(::handlePrayerTimeError)
        }
    }

    fun refreshPrayerTimes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            runCatching {
                val settings = preferencesDataStore.settingsFlow.first()
                val times = prayerTimeRepository.refreshPrayerTimes(settings)
                adzanScheduler.reschedule(times, settings)
            }.onFailure(::handlePrayerTimeError)
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

    private fun handlePrayerTimeError(error: Throwable) {
        Log.e(TAG, "Prayer time pipeline failed", error)
        _uiState.update { it.copy(isRefreshing = false) }
    }
}
