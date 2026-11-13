package com.tinybakery.rush.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.core.navigation.Screen
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(private val repository: BakeryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MenuUiState())
    val uiState = mutableState.asStateFlow()
    private val eventChannel = Channel<Screen>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var retryAction: (suspend () -> Unit)? = null

    init {
        viewModelScope.launch {
            repository.state.collect { store ->
                val p = store.progress
                mutableState.update { old ->
                    if (p == null) old.copy(loading = store.loading, error = store.error)
                    else {
                        val session = p.session?.takeIf { s -> p.results.none { it.sessionId == s.id } }
                        old.copy(loading = false, error = store.error, selectedDay = p.selectedDay, currentDay = p.currentDay,
                            chapter = p.chapter, wallet = p.wallet, completed = p.completedDays,
                            goal = 3 + (p.selectedDay - 1) / 10,
                            resumable = session?.day == p.selectedDay, pendingResult = session?.finishReason != null)
                    }
                }
            }
        }
        viewModelScope.launch { repository.load() }
    }

    private fun perform(action: suspend () -> Unit) {
        if (uiState.value.busy) return
        retryAction = action
        mutableState.update { it.copy(busy = true, error = false) }
        viewModelScope.launch {
            try { action(); retryAction = null }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(error = true) } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }

    fun retry() { perform(retryAction ?: { repository.load() }) }
    fun chapter(value: Int) { perform { repository.select(chapter = value) } }
    fun select(day: Int) {
        val p = repository.state.value.progress ?: return
        val active = p.session?.takeIf { s -> p.results.none { it.sessionId == s.id } }
        if (active != null && active.day != day) mutableState.update { it.copy(changeDay = day) }
        else perform { repository.select(day = day) }
    }

    fun keepDay() { mutableState.update { it.copy(changeDay = null) } }
    fun startSelected() {
        val day = uiState.value.changeDay ?: return
        mutableState.update { it.copy(changeDay = null) }
        perform { eventChannel.send(Screen.Game(repository.start(day, replace = true).id)) }
    }
    fun play() { perform { eventChannel.send(Screen.Game(repository.start(uiState.value.selectedDay).id)) } }
}
