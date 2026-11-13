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
fun TipJar(coins: Int, modifier: Modifier = Modifier) {
    val fill = (coins / 18f).coerceIn(0f, 1f)
    val description = stringResource(R.string.tip_jar_state, coins, (fill * 100).toInt())
    Column(modifier.semantics { stateDescription = description }, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.tip_jar), style = MaterialTheme.typography.labelSmall, color = Indigo)
            Text(stringResource(R.string.coins, coins), style = MaterialTheme.typography.labelSmall, color = Indigo)
        }
        LinearProgressIndicator(progress = { fill }, modifier = Modifier.fillMaxWidth().height(6.dp), color = ActionRed, trackColor = Lilac.copy(alpha = 0.4f))
    }
}
