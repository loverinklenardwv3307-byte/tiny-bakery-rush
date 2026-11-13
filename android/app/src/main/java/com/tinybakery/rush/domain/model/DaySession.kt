package com.tinybakery.rush.domain.model

enum class FinishReason { SUCCESS, EARLY_COLLECTION, OVERBAKED, PATIENCE, TIMEOUT, CLOSED }
data class CustomerOrder(val number: Int, val customer: CustomerId, val recipe: RecipeId, val arrivedMs: Long, val patienceMs: Long)
data class OvenLoad(val itemId: Int, val recipe: RecipeId, val ageMs: Long, val bakeMs: Long) {
    val golden: Boolean get() = ageMs >= bakeMs
}
data class TrayItem(val id: Int, val recipe: RecipeId)
data class DaySession(
    val id: Long,
    val day: Int,
    val ovenLevel: Int,
    val counterLevel: Int,
    val elapsedMs: Long = 0L,
    val nextSpawnMs: Long,
    val spawned: Int = 1,
    val orders: List<CustomerOrder>,
    val selectedRecipe: RecipeId,
    val doughRecipe: RecipeId,
    val shapeSteps: Int = 0,
    val ovens: List<OvenLoad?>,
    val tray: List<TrayItem> = emptyList(),
    val selectedTrayId: Int? = null,
    val nextItemId: Int = 1,
    val served: Int = 0,
    val deliveryCoins: Int = 0,
    val tips: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val finishReason: FinishReason? = null
) {
    val target: Int get() = 3 + (day - 1) / 10
    val remainingMs: Long get() = (60000L - elapsedMs).coerceAtLeast(0L)
    val trayCapacity: Int get() = if (counterLevel >= 1) 2 else 1
    val queueCapacity: Int get() = if (counterLevel >= 2) 4 else 3
    val spawnIntervalMs: Long get() = (12000L - 1000L * ((day - 1) / 10)).coerceAtLeast(7000L)
}
sealed interface BakeryAction {
    data object Shape : BakeryAction
    data class SelectRecipe(val recipe: RecipeId) : BakeryAction
    data class Bake(val slot: Int? = null) : BakeryAction
    data class Collect(val slot: Int) : BakeryAction
    data class SelectTray(val item: Int) : BakeryAction
    data class Serve(val order: Int? = null) : BakeryAction
    data object CloseDay : BakeryAction
}
