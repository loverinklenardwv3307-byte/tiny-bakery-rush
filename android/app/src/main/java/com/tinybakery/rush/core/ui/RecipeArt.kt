package com.tinybakery.rush.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tinybakery.rush.R
import com.tinybakery.rush.domain.model.*

fun RecipeId.nameRes(): Int = when (this) {
    RecipeId.BUN -> R.string.plain_bun
    RecipeId.CROISSANT -> R.string.butter_croissant
    RecipeId.DANISH -> R.string.berry_danish
    RecipeId.SWIRL -> R.string.cinnamon_swirl
}

fun CustomerId.nameRes(): Int = when (this) {
    CustomerId.MILA -> R.string.mila
    CustomerId.OMAR -> R.string.omar
    CustomerId.NORA -> R.string.nora
}

fun FinishReason.messageRes(): Int = when (this) {
    FinishReason.SUCCESS -> R.string.reason_success
    FinishReason.EARLY_COLLECTION -> R.string.reason_early
    FinishReason.OVERBAKED -> R.string.reason_overbaked
    FinishReason.PATIENCE -> R.string.reason_patience
    FinishReason.TIMEOUT -> R.string.reason_timeout
    FinishReason.CLOSED -> R.string.reason_closed
}

@Composable
fun RecipeArt(recipe: RecipeId, size: Dp = 64.dp, modifier: Modifier = Modifier, decorative: Boolean = false) {
    val painter = when (recipe) {
        RecipeId.BUN -> painterResource(R.drawable.sprite_1)
        RecipeId.CROISSANT -> painterResource(R.drawable.sprite_2)
        RecipeId.DANISH -> painterResource(R.drawable.sprite_3)
        RecipeId.SWIRL -> painterResource(R.drawable.sprite_4)
    }
    Image(painter, if (decorative) null else stringResource(recipe.nameRes()), modifier.size(size), contentScale = ContentScale.Fit)
}
