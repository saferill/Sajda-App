package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.AudioRepository
import com.sajda.app.data.repository.QuranRepository
import com.sajda.app.domain.model.AudioDownloadState
import com.sajda.app.domain.model.Ayat
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.Bookmark
import com.sajda.app.domain.model.QuranReadingMode
import com.sajda.app.domain.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranUiState(
    val surahList: List<Surah> = emptyList(),
    val selectedSurah: Surah? = null,
    val ayatList: List<Ayat> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val downloadStates: Map<Int, AudioDownloadState> = emptyMap(),
    val focusMode: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.INDONESIAN,
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

class QuranViewModel(
    private val quranRepository: QuranRepository,
    private val audioRepository: AudioRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

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
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            quranRepository.observeBookmarks().collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }

    private fun observeDownloadStates() {
        viewModelScope.launch {
            audioRepository.downloadStates.collect { states ->
                _uiState.update { it.copy(downloadStates = states) }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            preferencesDataStore.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        focusMode = settings.focusMode,
                        appLanguage = settings.appLanguage,
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
        }
    }

    fun openSurah(surah: Surah) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedSurah = surah) }
            val ayats = quranRepository.getAyatBySurah(surah.number)
            _uiState.update { it.copy(ayatList = ayats, isLoading = false) }
        }
    }

    fun closeSurah() {
        _uiState.update { it.copy(selectedSurah = null, ayatList = emptyList(), errorMessage = null) }
    }

    fun downloadAudio(surah: Surah) {
        viewModelScope.launch {
            runCatching { audioRepository.downloadSurah(surah) }
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

    fun toggleBookmark(ayat: Ayat) {
        viewModelScope.launch {
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
        }
    }

    fun saveBookmarkReflection(ayat: Ayat, folderName: String, note: String, highlightColor: String) {
        viewModelScope.launch {
            val selectedSurah = uiState.value.selectedSurah ?: return@launch
            quranRepository.saveBookmarkReflection(
                surahNumber = selectedSurah.number,
                ayatNumber = ayat.ayatNumber,
                surahName = selectedSurah.transliteration,
                folderName = folderName,
                note = note.trim(),
                highlightColor = highlightColor
            )
        }
    }

    fun recordLastRead(ayat: Ayat) {
        viewModelScope.launch {
            quranRepository.updateLastRead(ayat.surahNumber, ayat.ayatNumber)
            preferencesDataStore.recordAyatRead()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
