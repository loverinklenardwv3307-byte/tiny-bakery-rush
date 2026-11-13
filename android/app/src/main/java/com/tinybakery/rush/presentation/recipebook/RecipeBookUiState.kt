package com.tinybakery.rush.presentation.recipebook

import com.tinybakery.rush.domain.model.*

data class RecipeCardUi(val recipe: Recipe, val unlocked: Boolean)
data class RecipeBookUiState(val loading: Boolean = true, val error: Boolean = false, val cards: List<RecipeCardUi> = emptyList(), val unlockedCount: Int = 1, val expanded: RecipeId? = null)
