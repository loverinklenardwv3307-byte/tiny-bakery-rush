package com.tinybakery.rush.presentation.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.domain.repository.BakeryAudio
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioUiState(val enabled: Boolean = false)

class AudioViewModel(private val repository: BakeryRepository, private val audio: BakeryAudio) : ViewModel() {
    private val mutableState = MutableStateFlow(AudioUiState())
    val uiState = mutableState.asStateFlow()
    private var foreground = false
    private var allowed = false
    private var musicJob: Job? = null

    init {
        viewModelScope.launch { repository.state.collect { refresh() } }
    }

    fun foreground(value: Boolean) { foreground = value; refresh() }
    fun allowed(value: Boolean) { allowed = value; refresh() }

    private fun refresh() {
        val settings = repository.state.value.progress?.settings
        val active = foreground && allowed && settings != null && (settings.soundEnabled || settings.musicEnabled)
        if (!active) {
            musicJob?.cancel(); musicJob = null; audio.deactivate()
        } else {
            if (!uiState.value.enabled) audio.activate()
            if (settings?.musicEnabled == true) {
                if (musicJob?.isActive != true) musicJob = viewModelScope.launch { audio.music() }
            } else { musicJob?.cancel(); musicJob = null }
        }
        mutableState.update { it.copy(enabled = active) }
    }

    override fun onCleared() { audio.deactivate() }
}
