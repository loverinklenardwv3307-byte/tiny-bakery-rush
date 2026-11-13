package com.tinybakery.rush.data.sample

import com.tinybakery.rush.domain.model.*

object SampleData {
    val recipes = listOf(
        Recipe(RecipeId.BUN, 1, 4000L, 8), Recipe(RecipeId.CROISSANT, 11, 5000L, 12),
        Recipe(RecipeId.DANISH, 26, 6000L, 16), Recipe(RecipeId.SWIRL, 41, 7000L, 20)
    )
    val customers = listOf(CustomerId.MILA, CustomerId.OMAR, CustomerId.NORA)
    val upgrades = listOf(
        Upgrade(UpgradeTrack.OVEN, 1, 6, 100), Upgrade(UpgradeTrack.OVEN, 2, 16, 260),
        Upgrade(UpgradeTrack.OVEN, 3, 36, 520), Upgrade(UpgradeTrack.COUNTER, 1, 11, 120),
        Upgrade(UpgradeTrack.COUNTER, 2, 26, 300)
    )
    val initialProgress = BakeryProgress()
}
