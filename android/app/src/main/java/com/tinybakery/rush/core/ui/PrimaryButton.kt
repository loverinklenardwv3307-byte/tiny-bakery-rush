package com.tinybakery.rush.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.core.theme.*

@Composable
fun PrimaryButton(textRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(dampingRatio = 0.75f, stiffness = 500f), label = "buttonPress")
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick, enabled = enabled, interactionSource = source,
        modifier = modifier.fillMaxWidth().heightIn(min = 60.dp).graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(if (enabled) 7.dp else 0.dp, shape, ambientColor = ActionRed, spotColor = ActionRed)
            .background(Brush.verticalGradient(if (enabled) listOf(ActionRed, ActionRed.copy(alpha = 0.88f)) else listOf(Lilac, Lavender)), shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Cream,
            disabledContainerColor = Color.Transparent, disabledContentColor = Ink.copy(alpha = 0.45f)),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) { Text(stringResource(textRes), style = MaterialTheme.typography.labelLarge) }
}
