package com.tinybakery.rush.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.theme.Indigo

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Image(painterResource(R.drawable.icon_home), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.back))
        }
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = Indigo)
    }
}
