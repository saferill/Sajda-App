package com.sajda.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajda.app.data.local.PreferencesDataStore
import com.sajda.app.data.repository.SpiritualContentBundle
import com.sajda.app.data.repository.SpiritualContentRepository
import com.sajda.app.domain.model.AppLanguage
import com.sajda.app.domain.model.DailyDua
import com.sajda.app.domain.model.HadithEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SpiritualContentUiState(
    val duas: List<DailyDua> = emptyList(),
    val hadithOfDay: HadithEntry? = null,
    val hadithCategories: Map<String, List<HadithEntry>> = emptyMap(),
    val isLoading: Boolean = true,
    val isRemote: Boolean = false,
    val sourceLabel: String = "",
    val errorMessage: String? = null
)

class SpiritualContentViewModel(
    private val repository: SpiritualContentRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    val settings = preferencesDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = com.sajda.app.domain.model.UserSettings()
    )

    private val _uiState = MutableStateFlow(SpiritualContentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(forceLanguage: AppLanguage? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val language = forceLanguage ?: preferencesDataStore.settingsFlow.first().appLanguage
            runCatching {
                repository.load(language)
            }.onSuccess { bundle ->
                _uiState.value = bundle.toUiState()
            }.onFailure { error ->
                _uiState.value = SpiritualContentUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to load spiritual content."
                )
            }
        }
    }

    private fun SpiritualContentBundle.toUiState(): SpiritualContentUiState {
        return SpiritualContentUiState(
            duas = duas,
            hadithOfDay = hadithOfDay,
            hadithCategories = hadithCategories,
            isLoading = false,
            isRemote = isRemote,
            sourceLabel = sourceLabel
        )
    }
}
