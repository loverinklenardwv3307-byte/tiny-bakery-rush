package com.tinybakery.rush.domain.repository

import com.tinybakery.rush.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface BakeryRepository {
    val state: StateFlow<BakeryStoreState>
    suspend fun load()
    suspend fun select(day: Int? = null, chapter: Int? = null)
    suspend fun start(day: Int, replace: Boolean = false): DaySession
    suspend fun checkpoint(session: DaySession)
    suspend fun finalize(session: DaySession): DayResult
    suspend fun purchase(track: UpgradeTrack)
    suspend fun saveSettings(settings: BakerySettings)
}
