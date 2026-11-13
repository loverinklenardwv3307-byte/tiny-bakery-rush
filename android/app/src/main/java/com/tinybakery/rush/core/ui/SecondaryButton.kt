package com.tinybakery.rush.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.core.theme.*

@Composable
fun SecondaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Indigo.copy(alpha = if (enabled) 0.25f else 0.08f)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Cream.copy(alpha = 0.82f), contentColor = Indigo,
            disabledContainerColor = Lavender, disabledContentColor = Ink.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge)
    }
}
