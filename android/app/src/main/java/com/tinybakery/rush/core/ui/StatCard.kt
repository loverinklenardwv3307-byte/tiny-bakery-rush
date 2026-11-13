package com.tinybakery.rush.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.core.theme.Indigo

data class StatValue(val label: String, val value: String, val positive: Boolean = true)

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    BakeryPanel(modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Indigo)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StatRow(values: List<StatValue>, modifier: Modifier = Modifier) {
    val visible = values.filter { it.positive }
    if (visible.size >= 2) {
        Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            visible.forEach { StatCard(it.label, it.value, Modifier.weight(1f)) }
        }
    }
}
