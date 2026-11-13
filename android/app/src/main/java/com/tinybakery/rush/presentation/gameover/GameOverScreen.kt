package com.tinybakery.rush.presentation.gameover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*
import com.tinybakery.rush.domain.model.RecipeId

@Composable
fun GameOverRoute(id: Long, onReplay: (Long) -> Unit, onMenu: () -> Unit) {
    val vm: GameOverViewModel = viewModel(key = "result_$id", factory = ServiceLocator.factory(id))
    val state by vm.uiState.collectAsStateWithLifecycle()
    val replay by rememberUpdatedState(onReplay)
    LaunchedEffect(Unit) { vm.events.collect { replay(it) } }
    GameOverScreen(state, vm::replay, onMenu, vm::retry)
}

@Composable
fun GameOverScreen(state: GameOverUiState, onReplay: () -> Unit, onMenu: () -> Unit, onRetry: () -> Unit) {
    val scale by animateFloatAsState(if (state.result != null) 1f else 0.97f, tween(220), label = "summaryEntry")
    BakeryBackdrop(Backdrop.SUMMARY) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically)) {
            Text(stringResource(R.string.summary_eyebrow), style = MaterialTheme.typography.labelSmall, color = Indigo, textAlign = TextAlign.Center)
            if (state.loading) InlineStatus(stringResource(R.string.saving), loading = true)
            if (state.error) InlineStatus(stringResource(R.string.save_error), onRetry)
            val result = state.result
            if (result != null) {
                BakeryPanel(Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { RecipeArt(RecipeId.BUN, 104.dp, decorative = true) }
                    Text(stringResource(when {
                        result.complete && result.day == 60 -> R.string.bakery_complete
                        result.complete -> R.string.day_complete
                        else -> R.string.practice_title
                    }, result.day), Modifier.fillMaxWidth().semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = Indigo, textAlign = TextAlign.Center)
                    Text(stringResource(result.reason.messageRes()), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    if (result.served == 0) Text(stringResource(R.string.no_deliveries), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                StatRow(listOf(
                    StatValue(stringResource(R.string.orders_label), stringResource(R.string.number, result.served), result.served > 0),
                    StatValue(stringResource(R.string.earned_label), stringResource(R.string.number, result.earned), result.earned > 0),
                    StatValue(stringResource(R.string.combo_label), stringResource(R.string.number, result.bestCombo), result.bestCombo > 0)
                ))
                BakeryPanel(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
                    if (result.deliveryCoins > 0) CoinLine(stringResource(R.string.delivery_coins), result.deliveryCoins)
                    if (result.tips > 0) CoinLine(stringResource(R.string.tips), result.tips)
                    if (result.bonus > 0) CoinLine(stringResource(R.string.completion_bonus), result.bonus)
                    if (result.earned == 0) Text(stringResource(R.string.wallet_unchanged), style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(color = Indigo.copy(alpha = 0.12f))
                    CoinLine(stringResource(R.string.wallet_total), result.walletAfter)
                    if (result.tips > 0) TipJar(result.tips)
                }
                result.newRecipe?.let { recipe ->
                    BakeryPanel(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RecipeArt(recipe, 64.dp)
                            Column {
                                Text(stringResource(R.string.new_recipe), style = MaterialTheme.typography.bodyMedium, color = Indigo)
                                Text(stringResource(recipe.nameRes()), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                if (result.complete && result.day < 60) Text(stringResource(R.string.next_day_unlocked, minOf(result.day + 1, result.unlockedDay)), style = MaterialTheme.typography.bodyMedium, color = Indigo)
                if (result.complete && result.day in listOf(5, 10, 15, 25, 35)) Text(stringResource(R.string.new_upgrade), style = MaterialTheme.typography.bodyMedium, color = Indigo)
                Text(stringResource(R.string.summary_hint), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                PrimaryButton(R.string.play_again, onReplay, enabled = !state.busy && !state.loading)
                SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth(), enabled = !state.busy && !state.loading)
            }
        }
    }
}

@Composable
private fun CoinLine(label: String, amount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.coins, amount), style = MaterialTheme.typography.titleMedium, color = Indigo)
    }
}
