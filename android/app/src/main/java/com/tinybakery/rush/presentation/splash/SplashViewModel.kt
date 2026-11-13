package com.tinybakery.rush.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.core.config.GameConfig
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(repository: BakeryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SplashUiState())
    val uiState = mutableState.asStateFlow()
    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()
    private var consumed = false

    init {
        viewModelScope.launch { repository.load() }
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _finished.value = true
            mutableState.update { it.copy(finished = true) }
        }
    }

    fun consumeFinished(): Boolean {
        if (!_finished.value || consumed) return false
        consumed = true
        return true
    }
}
