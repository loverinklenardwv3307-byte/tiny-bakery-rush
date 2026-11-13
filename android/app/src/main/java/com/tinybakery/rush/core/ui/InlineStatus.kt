package com.tinybakery.rush.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.theme.*

@Composable
fun InlineStatus(message: String, onRetry: (() -> Unit)? = null, loading: Boolean = false) {
    BakeryPanel(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Indigo, trackColor = Lavender)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = if (onRetry != null) ActionRed else Ink)
        if (onRetry != null) SecondaryButton(R.string.retry, onRetry)
    }
}
