package com.tinybakery.rush.presentation.gameover

import com.tinybakery.rush.domain.model.DayResult

data class GameOverUiState(val result: DayResult? = null, val loading: Boolean = true, val busy: Boolean = false, val error: Boolean = false)
