package com.sajda.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.Surah
import com.sajda.app.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentTime: String = DateTimeUtils.currentTimeString(),
    val todayPrayerTime: PrayerTime? = null,
    val nextPrayerLabel: String = "Subuh",
    val nextPrayerTime: String = "--:--",
    val countdown: String = "--",
    val lastReadSurah: Surah? = null,
    val lastReadAyat: Ayat? = null,
    val dailyAyat: Ayat? = null,
    val dailyAyatDate: String = LocalDate.now().toString(),
    val quickPlaySurah: Surah? = null,
    val dailyAyatRead: Int = 0,
    val streakCount: Int = 0,
    val locationName: String = "",
    val appLanguage: AppLanguage = AppLanguage.INDONESIAN,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeHomeData()
        startClock()
        loadDailyAyat(LocalDate.now())
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            runCatching {
                quranRepository.observeLastRead().collect { lastRead ->
                    runCatching {
                        val surah = lastRead?.let { quranRepository.getSurahByNumber(it.surahNumber) }
                        val ayat = lastRead?.let { quranRepository.getAyat(it.surahNumber, it.ayatNumber) }
                        _uiState.update {
                            it.copy(
                                lastReadSurah = surah,
                                lastReadAyat = ayat,
                                isLoading = false
                            )
                        }
                    }.onFailure { error ->
                        handleHomeError("observeLastRead:emit", error)
                    }
                }
            }.onFailure { error ->
                handleHomeError("observeLastRead", error)
            }
        }

        viewModelScope.launch {
            runCatching {
                prayerTimeRepository.observeTodayPrayerTime().collect { prayerTime ->
                    runCatching {
                        _uiState.update {
                            val nextPrayer = prayerTime?.let(DateTimeUtils::nextPrayer)
                            it.copy(
                                todayPrayerTime = prayerTime,
                                locationName = prayerTime?.locationName.orEmpty(),
                                nextPrayerLabel = nextPrayer?.first?.label ?: it.nextPrayerLabel,
                                nextPrayerTime = nextPrayer?.second ?: it.nextPrayerTime,
                                countdown = prayerTime?.let(DateTimeUtils::countdownClockToNextPrayer) ?: "--:--:--",
                                isLoading = false
                            )
                        }
                    }.onFailure { error ->
                        handleHomeError("observeTodayPrayerTime:emit", error)
                    }
                }
            }.onFailure { error ->
                handleHomeError("observeTodayPrayerTime", error)
            }
        }

        viewModelScope.launch {
            runCatching {
                preferencesDataStore.settingsFlow.collect { settings ->
                    runCatching {
                        val quickPlay = when {
                            settings.lastPlayedSurah > 0 -> quranRepository.getSurahByNumber(settings.lastPlayedSurah)
                            else -> quranRepository.getLastDownloadedSurah()
                        }
                        _uiState.update {
                            it.copy(
                                quickPlaySurah = quickPlay,
                                dailyAyatRead = settings.dailyAyatRead,
                                streakCount = settings.streakCount,
                                appLanguage = settings.appLanguage,
                                isLoading = false
                            )
                        }
                    }.onFailure { error ->
                        handleHomeError("settingsFlow:emit", error)
                    }
                }
            }.onFailure { error ->
                handleHomeError("settingsFlow", error)
            }
        }
    }

    private fun loadDailyAyat(date: LocalDate) {
        viewModelScope.launch {
            runCatching {
                val ayat = quranRepository.getDailyAyat(date)
                _uiState.update {
                    it.copy(
                        dailyAyat = ayat,
                        dailyAyatDate = date.toString(),
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                handleHomeError("loadDailyAyat", error)
            }
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                runCatching {
                    val today = LocalDate.now()
                    if (_uiState.value.dailyAyatDate != today.toString()) {
                        loadDailyAyat(today)
                    }
                    _uiState.update { current ->
                        val prayerTime = current.todayPrayerTime
                        if (prayerTime == null) {
                            current.copy(
                                currentTime = DateTimeUtils.currentTimeString(),
                                isLoading = false
                            )
                        } else {
                            val nextPrayer = DateTimeUtils.nextPrayer(prayerTime)
                            current.copy(
                                currentTime = DateTimeUtils.currentTimeString(),
                                nextPrayerLabel = nextPrayer.first.label,
                                nextPrayerTime = nextPrayer.second,
                                countdown = DateTimeUtils.countdownClockToNextPrayer(prayerTime),
                                isLoading = false
                            )
                        }
                    }
                }.onFailure { error ->
                    handleHomeError("startClock", error)
                }
                delay(1_000)
            }
        }
    }

    private fun handleHomeError(step: String, error: Throwable) {
        Log.e(TAG, "Home pipeline failed at $step", error)
        _uiState.update {
            it.copy(
                currentTime = DateTimeUtils.currentTimeString(),
                isLoading = false
            )
        }
    }
}
