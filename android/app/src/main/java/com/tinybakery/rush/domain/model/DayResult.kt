package com.tinybakery.rush.domain.model

data class DayResult(
    val sessionId: Long, val day: Int, val reason: FinishReason, val served: Int,
    val deliveryCoins: Int, val tips: Int, val bonus: Int, val bestCombo: Int,
    val walletAfter: Int, val unlockedDay: Int, val newRecipe: RecipeId? = null
) {
    val earned: Int get() = deliveryCoins + tips + bonus
    val complete: Boolean get() = reason == FinishReason.SUCCESS
}
