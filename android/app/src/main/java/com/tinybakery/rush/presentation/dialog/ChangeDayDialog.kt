package com.tinybakery.rush.presentation.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.ui.SecondaryButton

@Composable
fun ChangeDayDialog(onKeep: () -> Unit, onStart: () -> Unit) {
    AlertDialog(onDismissRequest = onKeep, shape = RoundedCornerShape(22.dp),
        title = { Text(stringResource(R.string.change_day_title)) },
        text = { Text(stringResource(R.string.change_day_body)) },
        confirmButton = { SecondaryButton(R.string.start_selected, onStart) },
        dismissButton = { SecondaryButton(R.string.keep_day, onKeep) })
}
