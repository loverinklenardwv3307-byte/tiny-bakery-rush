package com.tinybakery.rush.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.domain.model.BakerySettings
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: BakeryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val uiState = mutableState.asStateFlow()
    private var pending: BakerySettings? = null
    init {
        viewModelScope.launch { repository.state.collect { store ->
            mutableState.update { it.copy(loading = store.loading, error = store.error,
                soundEnabled = store.progress?.settings?.soundEnabled ?: it.soundEnabled,
                musicEnabled = store.progress?.settings?.musicEnabled ?: it.musicEnabled) }
        } }
        viewModelScope.launch { repository.load() }
    }
    fun sound(value: Boolean) { save(BakerySettings(value, uiState.value.musicEnabled)) }
    fun music(value: Boolean) { save(BakerySettings(uiState.value.soundEnabled, value)) }
    private fun save(settings: BakerySettings) {
        if (uiState.value.busy) return
        pending = settings
        mutableState.update { it.copy(busy = true, error = false, saved = false, soundEnabled = settings.soundEnabled, musicEnabled = settings.musicEnabled) }
        viewModelScope.launch {
            try { repository.saveSettings(settings); pending = null; mutableState.update { it.copy(saved = true) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                val old = repository.state.value.progress?.settings ?: BakerySettings()
                mutableState.update { it.copy(error = true, soundEnabled = old.soundEnabled, musicEnabled = old.musicEnabled) }
            } finally { mutableState.update { it.copy(busy = false) } }
        }
    }
    fun retry() { pending?.let(::save) ?: run { viewModelScope.launch { repository.load() } } }
}
