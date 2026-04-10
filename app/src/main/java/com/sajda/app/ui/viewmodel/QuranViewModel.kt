package com.sajda.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.AudioDownloadState
import com.sajda.app.domain.model.AudioDownloadMode
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.QuranReciter
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.Surah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranUiState(
    val surahList: List<Surah> = emptyList(),
    val selectedSurah: Surah? = null,
    val ayatList: List<Ayat> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val downloadStates: Map<Int, AudioDownloadState> = emptyMap(),
    val focusMode: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.INDONESIAN,
    val selectedQuranReciter: QuranReciter = QuranReciter.MISYARI_RASYID_AL_AFASI,
    val audioDownloadMode: AudioDownloadMode = AudioDownloadMode.ALL_RECITERS,
    val wifiOnlyAudioDownloads: Boolean = false,
    val quranReadingMode: QuranReadingMode = QuranReadingMode.ARABIC_INDONESIAN,
    val showTranslation: Boolean = true,
    val arabicOnly: Boolean = false,
    val showTransliteration: Boolean = false,
    val arabicFontSize: Int = 30,
    val translationFontSize: Int = 16,
    val verseSpacing: Int = 18,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val audioRepository: AudioRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    companion object {
        private const val TAG = "QuranViewModel"
    }

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSurahList()
        observeBookmarks()
        observeDownloadStates()
        observeSettings()
    }

    private fun observeSurahList() {
        viewModelScope.launch {
            runCatching {
                quranRepository.observeAllSurah().collect { surahList ->
                    _uiState.update { current ->
                        current.copy(
                            surahList = surahList,
                            selectedSurah = current.selectedSurah?.let { selected ->
                                surahList.firstOrNull { it.number == selected.number }
                            },
                            isLoading = false
                        )
                    }
                }
            }.onFailure { error ->
                handleQuranError("observeSurahList", error)
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            runCatching {
                quranRepository.observeBookmarks().collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
            }.onFailure { error ->
                handleQuranError("observeBookmarks", error)
            }
        }
    }

    private fun observeDownloadStates() {
        viewModelScope.launch {
            runCatching {
                audioRepository.downloadStates.collect { states ->
                    _uiState.update { it.copy(downloadStates = states) }
                }
            }.onFailure { error ->
                handleQuranError("observeDownloadStates", error)
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            runCatching {
                preferencesDataStore.settingsFlow.collect { settings ->
                    _uiState.update {
                        it.copy(
                            focusMode = settings.focusMode,
                            appLanguage = settings.appLanguage,
                            selectedQuranReciter = settings.selectedQuranReciter,
                            audioDownloadMode = settings.audioDownloadMode,
                            wifiOnlyAudioDownloads = settings.wifiOnlyAudioDownloads,
                            quranReadingMode = settings.quranReadingMode,
                            showTranslation = settings.showTranslation,
                            arabicOnly = settings.arabicOnly,
                            showTransliteration = settings.showTransliteration,
                            arabicFontSize = settings.arabicFontSize,
                            translationFontSize = settings.translationFontSize,
                            verseSpacing = settings.verseSpacing
                        )
                    }
                }
            }.onFailure { error ->
                handleQuranError("observeSettings", error)
            }
        }
    }

    fun openSurah(surah: Surah) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedSurah = surah) }
            runCatching {
                quranRepository.getAyatBySurah(surah.number)
            }.onSuccess { ayats ->
                _uiState.update { it.copy(ayatList = ayats, isLoading = false) }
            }.onFailure { error ->
                handleQuranError("openSurah", error)
            }
        }
    }

    fun closeSurah() {
        _uiState.update { it.copy(selectedSurah = null, ayatList = emptyList(), errorMessage = null) }
    }

    fun downloadAudio(
        surah: Surah,
        modeOverride: AudioDownloadMode? = null,
        wifiOnlyOverride: Boolean? = null
    ) {
        viewModelScope.launch {
            runCatching { audioRepository.downloadSurah(surah, modeOverride, wifiOnlyOverride) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Gagal mengunduh audio") }
                }
        }
    }

    fun deleteAudio(surahNumber: Int) {
        viewModelScope.launch {
            runCatching { audioRepository.deleteSurahAudio(surahNumber) }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Gagal menghapus audio") }
                }
        }
    }

    fun deleteAllAudio() {
        viewModelScope.launch {
            runCatching { audioRepository.deleteAllDownloadedAudio() }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message ?: "Gagal menghapus semua audio") }
                }
        }
    }

    fun toggleBookmark(ayat: Ayat) {
        viewModelScope.launch {
            runCatching {
                val selectedSurah = uiState.value.selectedSurah ?: return@launch
                val existing = quranRepository.getBookmark(selectedSurah.number, ayat.ayatNumber)
                if (existing != null) {
                    quranRepository.removeBookmark(selectedSurah.number, ayat.ayatNumber)
                } else {
                    quranRepository.addBookmark(
                        Bookmark(
                            surahNumber = selectedSurah.number,
                            ayatNumber = ayat.ayatNumber,
                            surahName = selectedSurah.transliteration,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }.onFailure { error -> handleQuranError("toggleBookmark", error) }
        }
    }

    fun saveBookmarkReflection(ayat: Ayat, folderName: String, note: String, highlightColor: String) {
        viewModelScope.launch {
            runCatching {
                val selectedSurah = uiState.value.selectedSurah ?: return@launch
                quranRepository.saveBookmarkReflection(
                    surahNumber = selectedSurah.number,
                    ayatNumber = ayat.ayatNumber,
                    surahName = selectedSurah.transliteration,
                    folderName = folderName,
                    note = note.trim(),
                    highlightColor = highlightColor
                )
            }.onFailure { error -> handleQuranError("saveBookmarkReflection", error) }
        }
    }

    fun recordLastRead(ayat: Ayat) {
        viewModelScope.launch {
            runCatching {
                quranRepository.updateLastRead(ayat.surahNumber, ayat.ayatNumber)
                preferencesDataStore.recordAyatRead()
            }.onFailure { error -> handleQuranError("recordLastRead", error) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun handleQuranError(step: String, error: Throwable) {
        Log.e(TAG, "Qur'an pipeline failed at $step", error)
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = error.message ?: "Gagal memuat data Qur'an"
            )
        }
    }
}
