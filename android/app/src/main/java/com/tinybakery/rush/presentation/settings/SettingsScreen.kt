package com.tinybakery.rush.presentation.settings

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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = ServiceLocator.factory())
    val state by vm.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state, onBack, vm::sound, vm::music, vm::retry)
}

@Composable
fun SettingsScreen(state: SettingsUiState, onBack: () -> Unit, onSound: (Boolean) -> Unit, onMusic: (Boolean) -> Unit, onRetry: () -> Unit) {
    BakeryBackdrop(Backdrop.SETTINGS) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ScreenHeader(stringResource(R.string.settings_title), onBack)
            Image(painterResource(R.drawable.logo), null, Modifier.size(144.dp), contentScale = ContentScale.Fit)
            Text(stringResource(R.string.splash_subtitle), style = MaterialTheme.typography.titleMedium, color = Indigo, textAlign = TextAlign.Center)
            if (state.loading) InlineStatus(stringResource(R.string.loading), loading = true)
            if (state.error) InlineStatus(stringResource(R.string.save_error), onRetry)
            BakeryPanel(Modifier.fillMaxWidth()) {
                SettingRow(R.string.sound_effects, R.string.sound_description, state.soundEnabled, !state.busy && !state.loading, onSound)
                HorizontalDivider(color = Indigo.copy(alpha = 0.12f))
                SettingRow(R.string.music, R.string.music_description, state.musicEnabled, !state.busy && !state.loading, onMusic)
            }
            if (state.saved) Text(stringResource(R.string.settings_saved), Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium, color = Indigo)
            Text(stringResource(R.string.local_progress), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            PrimaryButton(R.string.done, onBack, enabled = !state.busy)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingRow(title: Int, description: Int, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val stateLabel = stringResource(if (checked) R.string.on else R.string.off)
    val titleLabel = stringResource(title)
    Row(Modifier.fillMaxWidth().heightIn(min = 80.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = Indigo)
            Text(stringResource(description), style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics { stateDescription = stateLabel; contentDescription = titleLabel },
            colors = SwitchDefaults.colors(checkedThumbColor = Cream, checkedTrackColor = Indigo, uncheckedTrackColor = Lavender))
    }
}
