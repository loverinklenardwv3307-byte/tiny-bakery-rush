package com.tinybakery.rush.domain.model

data class BakeryProgress(
    val wallet: Int = 40, val currentDay: Int = 1, val selectedDay: Int = 1, val chapter: Int = 0,
    val completedDays: Set<Int> = emptySet(), val ovenLevel: Int = 0, val counterLevel: Int = 0,
    val bestCombo: Int = 0, val nextSessionId: Long = 1L,
    val settings: BakerySettings = BakerySettings(), val session: DaySession? = null,
    val results: List<DayResult> = emptyList()
)
data class BakeryStoreState(val progress: BakeryProgress? = null, val loading: Boolean = true, val error: Boolean = false)
