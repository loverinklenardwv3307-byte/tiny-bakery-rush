package com.tinybakery.rush.presentation.recipebook

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*
import com.tinybakery.rush.domain.model.RecipeId

@Composable
fun RecipeBookRoute(onBack: () -> Unit) {
    val vm: RecipeBookViewModel = viewModel(factory = ServiceLocator.factory())
    val state by vm.uiState.collectAsStateWithLifecycle()
    RecipeBookScreen(state, onBack, vm::toggle, vm::retry)
}

@Composable
fun RecipeBookScreen(state: RecipeBookUiState, onBack: () -> Unit, onDetails: (RecipeId) -> Unit, onRetry: () -> Unit) {
    BakeryBackdrop(Backdrop.BOOK) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ScreenHeader(stringResource(R.string.recipe_book_title), onBack)
            Text(stringResource(R.string.recipe_hint), style = MaterialTheme.typography.headlineMedium, color = Indigo)
            Text(stringResource(R.string.recipes_ready, state.unlockedCount), style = MaterialTheme.typography.bodyLarge)
            if (state.loading) InlineStatus(stringResource(R.string.loading), loading = true)
            if (state.error) InlineStatus(stringResource(R.string.load_error), onRetry)
            state.cards.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { card ->
                        RecipeCard(card, state.expanded == card.recipe.id, { onDetails(card.recipe.id) }, Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecipeCard(card: RecipeCardUi, expanded: Boolean, onDetails: () -> Unit, modifier: Modifier) {
    val description = stringResource(if (card.unlocked) R.string.recipe_unlocked else R.string.recipe_locked, card.recipe.fromDay - 1)
    BakeryPanel(modifier.animateContentSize(tween(200)).semantics { stateDescription = description }) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            RecipeArt(card.recipe.id, 96.dp, Modifier.alpha(if (card.unlocked) 1f else 0.45f))
        }
        Text(stringResource(card.recipe.id.nameRes()), style = MaterialTheme.typography.titleMedium, color = Indigo)
        Text(description, style = MaterialTheme.typography.labelSmall)
        if (expanded) Text(stringResource(R.string.recipe_details, card.recipe.bakeMs / 1000, card.recipe.coins), style = MaterialTheme.typography.bodyMedium)
        SecondaryButton(if (expanded) R.string.close else R.string.details, onDetails, Modifier.fillMaxWidth())
    }
}
