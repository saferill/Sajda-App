package com.sajda.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdhanPlaybackState(
    val isActive: Boolean = false,
    val prayerName: String = "",
    val prayerKey: String = ""
)

object AdhanPlaybackStore {
    private val _state = MutableStateFlow(AdhanPlaybackState())
    val state: StateFlow<AdhanPlaybackState> = _state.asStateFlow()

    fun markActive(prayerName: String, prayerKey: String) {
        _state.value = AdhanPlaybackState(
            isActive = true,
            prayerName = prayerName,
            prayerKey = prayerKey
        )
    }

    fun clear() {
        _state.value = AdhanPlaybackState()
    }
}
