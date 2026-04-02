package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.PrayerTimeRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.PrayerTime
import com.sajda.app.domain.model.Surah
import com.sajda.app.util.DateTimeUtils
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val quranRepository: QuranRepository,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeHomeData()
        startClock()
        loadDailyAyat(LocalDate.now())
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            quranRepository.observeLastRead().collect { lastRead ->
                val surah = lastRead?.let { quranRepository.getSurahByNumber(it.surahNumber) }
                val ayat = lastRead?.let { quranRepository.getAyat(it.surahNumber, it.ayatNumber) }
                _uiState.update {
                    it.copy(
                        lastReadSurah = surah,
                        lastReadAyat = ayat,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            prayerTimeRepository.observeTodayPrayerTime().collect { prayerTime ->
                _uiState.update {
                    val nextPrayer = prayerTime?.let { current ->
                        DateTimeUtils.nextPrayer(current)
                    }
                    it.copy(
                        todayPrayerTime = prayerTime,
                        locationName = prayerTime?.locationName.orEmpty(),
                        nextPrayerLabel = nextPrayer?.first?.label ?: it.nextPrayerLabel,
                        nextPrayerTime = nextPrayer?.second ?: it.nextPrayerTime,
                        countdown = prayerTime?.let(DateTimeUtils::countdownClockToNextPrayer) ?: "--:--:--",
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            preferencesDataStore.settingsFlow.collect { settings ->
                val quickPlay = when {
                    settings.lastPlayedSurah > 0 -> quranRepository.getSurahByNumber(settings.lastPlayedSurah)
                    else -> quranRepository.getLastDownloadedSurah()
                }
                _uiState.update {
                    it.copy(
                        quickPlaySurah = quickPlay,
                        dailyAyatRead = settings.dailyAyatRead,
                        streakCount = settings.streakCount,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadDailyAyat(date: LocalDate) {
        viewModelScope.launch {
            val ayat = quranRepository.getDailyAyat(date)
            _uiState.update {
                it.copy(
                    dailyAyat = ayat,
                    dailyAyatDate = date.toString()
                )
            }
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val today = LocalDate.now()
                if (_uiState.value.dailyAyatDate != today.toString()) {
                    loadDailyAyat(today)
                }
                _uiState.update { current ->
                    val prayerTime = current.todayPrayerTime
                    if (prayerTime == null) {
                        current.copy(currentTime = DateTimeUtils.currentTimeString())
                    } else {
                        val nextPrayer = DateTimeUtils.nextPrayer(prayerTime)
                        current.copy(
                            currentTime = DateTimeUtils.currentTimeString(),
                            nextPrayerLabel = nextPrayer.first.label,
                            nextPrayerTime = nextPrayer.second,
                            countdown = DateTimeUtils.countdownClockToNextPrayer(prayerTime)
                        )
                    }
                }
                delay(1_000)
            }
        }
    }
}
