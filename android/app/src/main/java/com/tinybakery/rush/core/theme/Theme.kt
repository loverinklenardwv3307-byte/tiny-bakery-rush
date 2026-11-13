package com.tinybakery.rush.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BakeryColors = lightColorScheme(
    primary = Indigo, onPrimary = Cream, primaryContainer = Lavender, onPrimaryContainer = Indigo,
    secondary = Lilac, onSecondary = Ink, secondaryContainer = Blush, onSecondaryContainer = Indigo,
    tertiary = ActionRed, onTertiary = Cream, background = Lavender, onBackground = Ink,
    surface = Cream, onSurface = Ink, surfaceVariant = Lavender, onSurfaceVariant = Ink,
    outline = Indigo.copy(alpha = 0.25f), error = ActionRed, onError = Cream
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BakeryColors, typography = BakeryTypography, content = content)
}
