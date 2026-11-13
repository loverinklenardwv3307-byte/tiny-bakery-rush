package com.tinybakery.rush.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.ui.SecondaryButton

@Composable
fun PauseDialog(onResume: () -> Unit, onMenu: () -> Unit) {
    AlertDialog(onDismissRequest = onResume, shape = RoundedCornerShape(22.dp),
        title = { Text(stringResource(R.string.pause_title)) },
        text = { Text(stringResource(R.string.pause_body)) },
        confirmButton = { SecondaryButton(R.string.resume, onResume) },
        dismissButton = { SecondaryButton(R.string.menu, onMenu) })
}
