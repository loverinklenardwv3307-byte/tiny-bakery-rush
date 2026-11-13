package com.tinybakery.rush.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.core.theme.*

enum class Backdrop { SPLASH, MENU, GAME, SUMMARY, BOOK, SETTINGS }

@Composable
fun BakeryBackdrop(kind: Backdrop, content: @Composable BoxScope.() -> Unit) {
    val colors = when (kind) {
        Backdrop.SPLASH -> listOf(Indigo.copy(alpha = 0.96f), Indigo.copy(alpha = 0.87f), Lilac.copy(alpha = 0.93f))
        Backdrop.MENU -> listOf(Lavender.copy(alpha = 0.30f), Lavender.copy(alpha = 0.80f), Lavender.copy(alpha = 0.98f))
        Backdrop.GAME -> listOf(Lavender.copy(alpha = 0.70f), Lavender.copy(alpha = 0.88f), Lilac.copy(alpha = 0.95f))
        Backdrop.SUMMARY -> listOf(Lilac.copy(alpha = 0.85f), Lavender.copy(alpha = 0.95f), Blush.copy(alpha = 0.98f))
        Backdrop.BOOK -> listOf(Blush.copy(alpha = 0.94f), Lavender.copy(alpha = 0.92f), Cream.copy(alpha = 0.97f))
        Backdrop.SETTINGS -> listOf(Lavender.copy(alpha = 0.94f), Lilac.copy(alpha = 0.90f), Blush.copy(alpha = 0.97f))
    }
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.bg_game), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        Canvas(Modifier.fillMaxSize()) {
            listOf(0.08f to 0.12f, 0.9f to 0.21f, 0.06f to 0.72f, 0.95f to 0.9f).forEachIndexed { index, point ->
                drawCircle(Cream.copy(alpha = 0.10f), (22 + index * 9).dp.toPx(), Offset(size.width * point.first, size.height * point.second))
            }
        }
        content()
    }
}
