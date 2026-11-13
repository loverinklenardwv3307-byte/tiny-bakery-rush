package com.tinybakery.rush.presentation.upgrades

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*
import com.tinybakery.rush.domain.model.UpgradeTrack

@Composable
fun UpgradesRoute(onBack: () -> Unit) {
    val vm: UpgradesViewModel = viewModel(factory = ServiceLocator.factory())
    val state by vm.uiState.collectAsStateWithLifecycle()
    UpgradesScreen(state, onBack, vm::purchase, vm::retry)
}

@Composable
fun UpgradesScreen(state: UpgradesUiState, onBack: () -> Unit, onUpgrade: (UpgradeTrack) -> Unit, onRetry: () -> Unit) {
    BakeryBackdrop(Backdrop.SETTINGS) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ScreenHeader(stringResource(R.string.upgrades_title), onBack)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.logo), null, Modifier.size(100.dp), contentScale = ContentScale.Fit)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.equipment_hint), style = MaterialTheme.typography.titleMedium, color = Indigo)
                    Text(stringResource(R.string.coins, state.wallet), style = MaterialTheme.typography.titleLarge, color = Indigo)
                }
            }
            if (state.loading) InlineStatus(stringResource(R.string.loading), loading = true)
            if (state.error) InlineStatus(stringResource(R.string.save_error), onRetry)
            if (state.purchased) InlineStatus(stringResource(R.string.upgrade_saved))
            if (state.pausedSession) Text(stringResource(R.string.equipment_next_day), style = MaterialTheme.typography.bodyMedium)
            state.offers.forEach { item ->
                BakeryPanel(Modifier.fillMaxWidth()) {
                    Text(stringResource(if (item.track == UpgradeTrack.OVEN) R.string.oven_track else R.string.counter_track), style = MaterialTheme.typography.labelSmall, color = Indigo)
                    Text(stringResource(R.string.owned_level, stringResource(equipmentName(item.track, item.level))), style = MaterialTheme.typography.bodyMedium)
                    val offer = item.offer
                    if (offer == null) Text(stringResource(R.string.kitchen_complete), style = MaterialTheme.typography.titleLarge, color = Indigo)
                    else {
                        Text(stringResource(equipmentName(offer.track, offer.level)), style = MaterialTheme.typography.titleLarge, color = Indigo)
                        Text(stringResource(equipmentEffect(offer.track, offer.level)), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.coins, offer.cost), style = MaterialTheme.typography.titleMedium, color = Indigo)
                        if (state.currentDay < offer.unlockDay) Text(stringResource(R.string.unlock_day, offer.unlockDay), style = MaterialTheme.typography.bodyMedium)
                        else if (item.missingCoins > 0) Text(stringResource(R.string.need_coins, item.missingCoins), style = MaterialTheme.typography.bodyMedium)
                        PrimaryButton(R.string.upgrade, { onUpgrade(item.track) }, enabled = item.canBuy && !state.busy && !state.loading)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun equipmentName(track: UpgradeTrack, level: Int): Int = when (track) {
    UpgradeTrack.OVEN -> when (level) { 0 -> R.string.starter_oven; 1 -> R.string.quick_oven; 2 -> R.string.twin_oven; else -> R.string.master_oven }
    UpgradeTrack.COUNTER -> when (level) { 0 -> R.string.starter_counter; 1 -> R.string.roomy_counter; else -> R.string.cosy_counter }
}

private fun equipmentEffect(track: UpgradeTrack, level: Int): Int = when (track) {
    UpgradeTrack.OVEN -> when (level) { 1 -> R.string.oven_quick_effect; 2 -> R.string.oven_twin_effect; else -> R.string.oven_master_effect }
    UpgradeTrack.COUNTER -> if (level == 1) R.string.counter_roomy_effect else R.string.counter_cosy_effect
}
