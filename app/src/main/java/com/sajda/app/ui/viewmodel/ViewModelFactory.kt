package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AppUpdateRepository
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.service.AdzanScheduler

class HomeViewModelFactory(
    private val quranRepository: QuranRepository,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(quranRepository, prayerTimeRepository, preferencesDataStore) as T
    }
}

class QuranViewModelFactory(
    private val quranRepository: QuranRepository,
    private val audioRepository: AudioRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QuranViewModel(quranRepository, audioRepository, preferencesDataStore) as T
    }
}

class PrayerTimeViewModelFactory(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore,
    private val adzanScheduler: AdzanScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PrayerTimeViewModel(prayerTimeRepository, preferencesDataStore, adzanScheduler) as T
    }
}

class SettingsViewModelFactory(
    private val preferencesDataStore: PreferencesDataStore,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val adzanScheduler: AdzanScheduler,
    private val appUpdateRepository: AppUpdateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            preferencesDataStore,
            prayerTimeRepository,
            adzanScheduler,
            appUpdateRepository
        ) as T
    }
}
