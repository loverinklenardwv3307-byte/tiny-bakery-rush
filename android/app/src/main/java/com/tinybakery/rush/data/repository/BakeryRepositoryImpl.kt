package com.tinybakery.rush.data.repository

import com.tinybakery.rush.data.local.BakeryPreferences
import com.tinybakery.rush.data.sample.SampleData
import com.tinybakery.rush.domain.model.*
import com.tinybakery.rush.domain.repository.BakeryRepository
import com.tinybakery.rush.domain.usecase.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BakeryRepositoryImpl(
    private val preferences: BakeryPreferences,
    private val create: CreateDaySession,
    private val settle: FinalizeDay,
    private val buy: PurchaseUpgrade
) : BakeryRepository {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(BakeryStoreState())
    override val state = mutableState.asStateFlow()

    override suspend fun load() = mutex.withLock {
        if (state.value.progress != null) {
            mutableState.update { it.copy(loading = false, error = false) }
        } else {
            mutableState.update { it.copy(loading = true, error = false) }
            try {
                val saved = preferences.read()
                val progress = saved ?: SampleData.initialProgress.also { preferences.write(it) }
                mutableState.update { it.copy(progress = progress, loading = false, error = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, error = true) }
            }
        }
    }

    private suspend fun mutate(transform: (BakeryProgress) -> BakeryProgress): BakeryProgress = mutex.withLock {
        val old = requireNotNull(state.value.progress)
        val next = transform(old)
        try {
            preferences.write(next)
            mutableState.update { it.copy(progress = next, error = false, loading = false) }
            next
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mutableState.update { it.copy(error = true) }
            throw e
        }
    }

    override suspend fun select(day: Int?, chapter: Int?) {
        mutate { p ->
            require(day == null || day in 1..p.currentDay)
            require(chapter == null || chapter in 0..5)
            p.copy(selectedDay = day ?: p.selectedDay, chapter = chapter ?: p.chapter)
        }
    }

    override suspend fun start(day: Int, replace: Boolean): DaySession {
        val progress = mutate { p ->
            val existing = p.session
            if (!replace && existing?.day == day && (existing.finishReason == null || p.results.none { it.sessionId == existing.id })) p
            else {
                val settled = if (existing?.finishReason != null && p.results.none { it.sessionId == existing.id }) settle(p, existing) else p
                val session = create(settled, day)
                settled.copy(session = session, selectedDay = day, nextSessionId = settled.nextSessionId + 1L)
            }
        }
        return requireNotNull(progress.session)
    }

    override suspend fun checkpoint(session: DaySession) {
        mutate { p ->
            val current = p.session
            if (current?.id != session.id || p.results.any { it.sessionId == session.id } ||
                current.elapsedMs > session.elapsedMs || (current.finishReason != null && session.finishReason == null)) p
            else p.copy(session = session)
        }
    }

    override suspend fun finalize(session: DaySession): DayResult {
        val progress = mutate { p ->
            require(p.session?.id == session.id || p.results.any { it.sessionId == session.id })
            settle(p, session)
        }
        return progress.results.first { it.sessionId == session.id }
    }

    override suspend fun purchase(track: UpgradeTrack) { mutate { buy(it, track) } }
    override suspend fun saveSettings(settings: BakerySettings) { mutate { it.copy(settings = settings) } }
}
