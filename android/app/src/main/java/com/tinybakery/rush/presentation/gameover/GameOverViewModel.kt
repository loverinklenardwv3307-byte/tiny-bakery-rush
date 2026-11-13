package com.tinybakery.rush.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameOverViewModel(private val resultId: Long, private val repository: BakeryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(GameOverUiState())
    val uiState = mutableState.asStateFlow()
    private val eventChannel = Channel<Long>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            val result = store.progress?.results?.firstOrNull { it.sessionId == resultId }
            mutableState.update { it.copy(result = result, loading = store.loading, error = store.error || (!store.loading && result == null)) }
        } }
    }
    fun replay() {
        val result = uiState.value.result ?: return
        if (uiState.value.busy) return
        mutableState.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            try { eventChannel.send(repository.start(result.day, replace = true).id) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(error = true) } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }
    fun retry() { if (uiState.value.result != null) replay() else viewModelScope.launch { repository.load() } }
}
