package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.domain.model.AsrMadhhab
import com.sajda.app.domain.model.CityPreset
import com.sajda.app.domain.model.PrayerCalculationMethod
import com.sajda.app.service.AdzanScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationManagerViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = com.sajda.app.domain.model.UserSettings()
    )

    fun setAutoLocation(enabled: Boolean, refreshSchedule: Boolean = true) {
        viewModelScope.launch {
            preferencesDataStore.setAutoLocation(enabled)
            if (refreshSchedule) {
                reschedulePrayerAlarms()
            }
        }
    }

    fun setLocationPermissionPrompted(prompted: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setLocationPermissionPrompted(prompted)
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

    private suspend fun reschedulePrayerAlarms() {
        val latestSettings = preferencesDataStore.settingsFlow.first()
        val prayerTimes = prayerTimeRepository.refreshPrayerTimes(latestSettings)
        adzanScheduler.reschedule(prayerTimes, latestSettings)
    }
}
