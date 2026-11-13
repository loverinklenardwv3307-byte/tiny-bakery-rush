package com.tinybakery.rush.presentation.menu

data class MenuUiState(
    val loading: Boolean = true, val error: Boolean = false, val busy: Boolean = false,
    val selectedDay: Int = 1, val currentDay: Int = 1, val chapter: Int = 0,
    val wallet: Int = 40, val completed: Set<Int> = emptySet(), val goal: Int = 3,
    val resumable: Boolean = false, val pendingResult: Boolean = false, val changeDay: Int? = null
)
