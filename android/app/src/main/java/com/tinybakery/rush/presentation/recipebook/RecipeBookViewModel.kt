package com.tinybakery.rush.presentation.recipebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.domain.model.*
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeBookViewModel(private val repository: BakeryRepository, private val recipes: List<Recipe>) : ViewModel() {
    private val mutableState = MutableStateFlow(RecipeBookUiState())
    val uiState = mutableState.asStateFlow()
    init {
        viewModelScope.launch { repository.state.collect { store ->
            val p = store.progress
            mutableState.update { it.copy(loading = store.loading, error = store.error,
                cards = recipes.map { recipe -> RecipeCardUi(recipe, p != null && recipe.fromDay <= p.currentDay) },
                unlockedCount = recipes.count { recipe -> p != null && recipe.fromDay <= p.currentDay }) }
        } }
        viewModelScope.launch { repository.load() }
    }
    fun toggle(id: RecipeId) { mutableState.update { it.copy(expanded = if (it.expanded == id) null else id) } }
    fun retry() { viewModelScope.launch { repository.load() } }
}
