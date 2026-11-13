package com.tinybakery.rush.presentation.menu

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.navigation.Screen
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*
import com.tinybakery.rush.presentation.dialog.ChangeDayDialog

@Composable
fun MenuRoute(onNavigate: (Screen) -> Unit) {
    val vm: MenuViewModel = viewModel(factory = ServiceLocator.factory())
    val state by vm.uiState.collectAsStateWithLifecycle()
    val navigate by rememberUpdatedState(onNavigate)
    LaunchedEffect(Unit) { vm.events.collect { navigate(it) } }
    MenuScreen(state, vm::play, vm::select, vm::chapter, vm::retry,
        { onNavigate(Screen.Upgrades) }, { onNavigate(Screen.RecipeBook) }, { onNavigate(Screen.Settings) })
    if (state.changeDay != null) ChangeDayDialog(vm::keepDay, vm::startSelected)
}

@Composable
fun MenuScreen(state: MenuUiState, onPlay: () -> Unit, onDay: (Int) -> Unit, onChapter: (Int) -> Unit,
    onRetry: () -> Unit, onUpgrades: () -> Unit, onRecipes: () -> Unit, onSettings: () -> Unit) {
    BakeryBackdrop(Backdrop.MENU) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 600.dp
        Column(Modifier.fillMaxSize().safeDrawingPadding().then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.app_name), Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = Indigo)
                TextButton(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.settings), style = MaterialTheme.typography.labelSmall) }
            }
            Column(if (compact) Modifier else Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.neighbourhood), style = MaterialTheme.typography.labelSmall, color = Indigo)
                        Text(stringResource(R.string.menu_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
                        Text(stringResource(R.string.day_ready, state.selectedDay), style = MaterialTheme.typography.bodyMedium, color = Indigo)
                    }
                    Image(painterResource(R.drawable.logo), null, Modifier.size(128.dp), contentScale = ContentScale.Fit)
                }
                if (state.loading) InlineStatus(stringResource(R.string.loading), loading = true)
                if (state.error) InlineStatus(stringResource(R.string.load_error), onRetry)
                if (!state.loading) {
                    StatRow(listOf(StatValue(stringResource(R.string.current_day), stringResource(R.string.number, state.currentDay)),
                        StatValue(stringResource(R.string.kitchen_coins), stringResource(R.string.coins, state.wallet), state.wallet > 0)))
                    if (state.wallet == 0) Text(stringResource(R.string.coins, state.wallet), style = MaterialTheme.typography.bodyMedium)
                    DaySelector(state, onDay, onChapter)
                    Text(stringResource(R.string.completed_count, state.completed.size), style = MaterialTheme.typography.labelSmall, color = Indigo)
                    if (state.completed.size == 60) Text(stringResource(R.string.all_days_complete), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
            }
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(when { state.pendingResult -> R.string.pending_hint; state.resumable -> R.string.resume_hint; else -> R.string.day_goal }, state.goal),
                    style = MaterialTheme.typography.bodyMedium, color = Ink)
                PrimaryButton(R.string.play, onPlay, enabled = !state.loading && !state.busy && !state.error)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(R.string.upgrades, onUpgrades, Modifier.weight(1f))
                    SecondaryButton(R.string.recipe_book, onRecipes, Modifier.weight(1f))
                }
            }
        }
        }
    }
}

@Composable
private fun DaySelector(state: MenuUiState, onDay: (Int) -> Unit, onChapter: (Int) -> Unit) {
    BakeryPanel(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.choose_day), style = MaterialTheme.typography.titleMedium, color = Indigo)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items((0..5).toList()) { chapter ->
                val selected = state.chapter == chapter
                OutlinedButton(onClick = { onChapter(chapter) }, enabled = !state.busy,
                    modifier = Modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Indigo else Lavender, contentColor = if (selected) Cream else Indigo),
                    border = BorderStroke(1.dp, Indigo.copy(alpha = 0.16f)), contentPadding = PaddingValues(horizontal = 12.dp)) {
                    Text(stringResource(R.string.chapters, chapter * 10 + 1, chapter * 10 + 10), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        val days = (state.chapter * 10 + 1..state.chapter * 10 + 10).toList()
        days.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { day ->
                    val locked = day > state.currentDay
                    val selected = day == state.selectedDay
                    val completed = day in state.completed
                    val description = stringResource(when { locked -> R.string.day_locked; selected -> R.string.day_selected; completed -> R.string.day_completed; else -> R.string.day_available }, day)
                    OutlinedButton(onClick = { onDay(day) }, enabled = !locked && !state.busy,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { stateDescription = description },
                        shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(2.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Indigo else Indigo.copy(alpha = 0.12f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Indigo else if (completed) Blush else Cream,
                            contentColor = if (selected) Cream else Indigo, disabledContainerColor = Lavender, disabledContentColor = Ink.copy(alpha = 0.32f))) {
                        Text(stringResource(R.string.day_tile, day), fontWeight = FontWeight.Bold)
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
