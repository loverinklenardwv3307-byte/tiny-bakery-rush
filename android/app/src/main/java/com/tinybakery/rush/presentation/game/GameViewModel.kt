package com.tinybakery.rush.presentation.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.R
import com.tinybakery.rush.core.config.GameConfig
import com.tinybakery.rush.domain.model.*
import com.tinybakery.rush.domain.repository.*
import com.tinybakery.rush.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class GameViewModel(
    private val sessionId: Long, private val repository: BakeryRepository,
    private val advance: AdvanceSession, private val apply: ApplyBakeryAction,
    private val recipes: List<Recipe>, private val audio: BakeryAudio
) : ViewModel() {
    private val mutableState = MutableStateFlow(GameUiState())
    val uiState = mutableState.asStateFlow()
    private val eventChannel = Channel<Long>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()
    private var loop: Job? = null
    private var settlement: Job? = null
    private var lastCheckpoint = 0L

    init { open() }

    private fun open() {
        viewModelScope.launch {
            mutableState.update { it.copy(phase = GamePhase.LOADING, error = false) }
            if (repository.state.value.progress == null) repository.load()
            val p = repository.state.value.progress
            val session = p?.session?.takeIf { it.id == sessionId }
            if (session == null) mutableState.update { it.copy(phase = GamePhase.ERROR, error = true) }
            else {
                render(session, GamePhase.PAUSED)
                when {
                    p.results.any { it.sessionId == sessionId } -> render(session, GamePhase.FINISHED)
                    session.finishReason != null -> finish(session)
                    else -> resume()
                }
            }
        }
    }

    fun enter() {
        if (uiState.value.phase == GamePhase.PAUSED && !uiState.value.pauseDialog) resume()
    }

    fun leave() {
        pause()
        mutableState.update { it.copy(pauseDialog = false) }
    }

    private fun render(session: DaySession, phase: GamePhase = uiState.value.phase) {
        val playing = phase == GamePhase.PLAYING
        val selected = session.tray.firstOrNull { it.id == session.selectedTrayId }
        val serve = session.orders.any { it.recipe == selected?.recipe }
        val bake = session.shapeSteps == 3 && session.ovens.any { it == null }
        val mainAction = when { serve -> MainAction.SERVE; session.shapeSteps == 3 -> MainAction.BAKE; else -> MainAction.SHAPE }
        val hint = when {
            session.tray.size >= session.trayCapacity -> R.string.tray_full_hint
            serve -> R.string.serve_hint
            session.shapeSteps == 3 -> R.string.bake_hint
            session.ovens.any { it != null } -> R.string.collect_hint
            else -> R.string.dough_hint
        }
        val unlockedDay = repository.state.value.progress?.currentDay ?: 1
        mutableState.update { it.copy(
            session = session, phase = phase, recipes = recipes.filter { r -> r.fromDay <= unlockedDay }.map { r -> r.id },
            orders = session.orders.map { order ->
                val remaining = (order.patienceMs - session.elapsedMs + order.arrivedMs).coerceAtLeast(0L)
                val hearts = when { remaining == 0L -> 0; remaining * 3 > order.patienceMs * 2 -> 3; remaining * 3 > order.patienceMs -> 2; else -> 1 }
                OrderUi(order, hearts, playing && order.recipe == selected?.recipe)
            },
            ovens = session.ovens.mapIndexed { index, load ->
                val label = when { load == null -> R.string.oven_empty; load.ageMs >= load.bakeMs + 4000L -> R.string.oven_overbaked; load.golden -> R.string.oven_golden; else -> R.string.oven_baking }
                OvenUi(index, load, label, if (load == null) 0 else ((load.bakeMs - load.ageMs).coerceAtLeast(0L) + 999L).div(1000L).toInt(),
                    if (load == null) 0f else (load.ageMs.toFloat() / load.bakeMs).coerceIn(0f, 1f), playing && load != null && session.tray.size < session.trayCapacity)
            }, hintRes = hint, mainAction = mainAction, canAct = playing && when (mainAction) {
                MainAction.SHAPE -> session.shapeSteps < 3; MainAction.BAKE -> bake; MainAction.SERVE -> serve
            }, canShape = playing && session.shapeSteps < 3, canBake = playing && bake
        ) }
    }

    fun resume() {
        val session = uiState.value.session ?: return
        if (session.finishReason != null || uiState.value.error || loop?.isActive == true) return
        mutableState.update { it.copy(pauseDialog = false) }
        render(session, GamePhase.PLAYING)
        loop = viewModelScope.launch {
            var previous = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(GameConfig.TICK_MS)
                val now = SystemClock.elapsedRealtime()
                val current = uiState.value.session ?: break
                val next = advance(current, now - previous)
                previous = now
                render(next)
                if (next.finishReason != null) { finish(next); break }
                if (next.elapsedMs - lastCheckpoint >= 1000L) {
                    lastCheckpoint = next.elapsedMs
                    if (!save(next)) break
                }
            }
        }
    }

    fun action(action: BakeryAction) {
        if (uiState.value.phase != GamePhase.PLAYING) return
        val current = uiState.value.session ?: return
        val next = apply(current, action, repository.state.value.progress?.currentDay ?: 1)
        if (next == current) return
        render(next)
        if (next.served > current.served) mutableState.update { it.copy(statusServed = next.served) }
        if (next.finishReason != null) finish(next)
        else {
            viewModelScope.launch { save(next) }
            val tone = when (action) { BakeryAction.Shape -> BakeryTone.SHAPE; is BakeryAction.Collect -> BakeryTone.COLLECT; is BakeryAction.Serve -> BakeryTone.SERVE; else -> null }
            if (tone != null && repository.state.value.progress?.settings?.soundEnabled == true) viewModelScope.launch { audio.effect(tone) }
        }
    }

    fun mainAction() { action(when (uiState.value.mainAction) { MainAction.SHAPE -> BakeryAction.Shape; MainAction.BAKE -> BakeryAction.Bake(); MainAction.SERVE -> BakeryAction.Serve() }) }

    fun pause(showDialog: Boolean = false) {
        loop?.cancel(); loop = null
        if (uiState.value.phase == GamePhase.PLAYING) {
            val session = uiState.value.session ?: return
            render(session, GamePhase.PAUSED)
            mutableState.update { it.copy(pauseDialog = showDialog) }
            viewModelScope.launch { save(session) }
        } else if (showDialog && uiState.value.phase == GamePhase.PAUSED) mutableState.update { it.copy(pauseDialog = true) }
    }

    private suspend fun save(session: DaySession): Boolean {
        return try { repository.checkpoint(session); true }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) {
            loop?.cancel(); loop = null
            render(uiState.value.session ?: session, GamePhase.ERROR)
            mutableState.update { it.copy(error = true, pauseDialog = false) }
            false
        }
    }

    private fun finish(session: DaySession) {
        if (settlement?.isActive == true) return
        loop?.cancel(); loop = null
        render(session, GamePhase.FINISHING)
        mutableState.update { it.copy(error = false, pauseDialog = false) }
        settlement = viewModelScope.launch {
            try {
                repository.checkpoint(session)
                if (session.finishReason == FinishReason.EARLY_COLLECTION) delay(GameConfig.EARLY_FEEDBACK_MS)
                val result = repository.finalize(session)
                render(session, GamePhase.FINISHED)
                eventChannel.send(result.sessionId)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                render(session, GamePhase.ERROR)
                mutableState.update { it.copy(error = true) }
            }
        }
    }

    fun retry() {
        val session = uiState.value.session
        if (session == null) { open(); return }
        if (session.finishReason != null) finish(session)
        else viewModelScope.launch {
            if (save(session)) {
                mutableState.update { it.copy(error = false) }
                render(session, GamePhase.PAUSED)
            }
        }
    }

    fun summary() { viewModelScope.launch { eventChannel.send(sessionId) } }
}
