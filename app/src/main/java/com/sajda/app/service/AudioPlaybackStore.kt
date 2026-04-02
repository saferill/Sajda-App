package com.sajda.app.service

import com.sajda.app.domain.model.AudioPlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioPlaybackStore {
    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun update(newState: AudioPlaybackState) {
        _state.value = newState
    }

    fun clear() {
        _state.value = AudioPlaybackState()
    }
}
