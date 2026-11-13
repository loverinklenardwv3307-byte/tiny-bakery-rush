package com.tinybakery.rush.domain.usecase

import com.tinybakery.rush.domain.model.*
import kotlin.math.roundToLong

class ApplyBakeryAction(private val recipes: List<Recipe>, private val advance: AdvanceSession) {
    operator fun invoke(session: DaySession, action: BakeryAction, unlockedDay: Int): DaySession {
        if (session.finishReason != null) return session
        return when (action) {
            BakeryAction.Shape -> if (session.shapeSteps < 3) session.copy(
                doughRecipe = if (session.shapeSteps == 0) session.selectedRecipe else session.doughRecipe,
                shapeSteps = session.shapeSteps + 1
            ) else session
            is BakeryAction.SelectRecipe -> if (recipes.any { it.id == action.recipe && it.fromDay <= unlockedDay }) session.copy(selectedRecipe = action.recipe) else session
            is BakeryAction.Bake -> {
                val slot = action.slot ?: session.ovens.indexOfFirst { it == null }
                if (session.shapeSteps < 3 || slot !in session.ovens.indices || session.ovens[slot] != null) session
                else {
                    val multiplier = when (session.ovenLevel) { 1, 2 -> 0.85; 3 -> 0.70; else -> 1.0 }
                    val duration = (recipes.first { it.id == session.doughRecipe }.bakeMs * multiplier).roundToLong()
                    session.copy(
                        ovens = session.ovens.mapIndexed { index, load -> if (index == slot) OvenLoad(session.nextItemId, session.doughRecipe, 0L, duration) else load },
                        shapeSteps = 0, doughRecipe = session.selectedRecipe, nextItemId = session.nextItemId + 1
                    )
                }
            }
            is BakeryAction.Collect -> {
                val load = session.ovens.getOrNull(action.slot)
                when {
                    load == null || session.tray.size >= session.trayCapacity -> session
                    !load.golden -> session.copy(finishReason = FinishReason.EARLY_COLLECTION)
                    else -> session.copy(
                        ovens = session.ovens.mapIndexed { index, item -> if (index == action.slot) null else item },
                        tray = session.tray + TrayItem(load.itemId, load.recipe), selectedTrayId = load.itemId
                    )
                }
            }
            is BakeryAction.SelectTray -> if (session.tray.any { it.id == action.item }) session.copy(selectedTrayId = action.item) else session
            is BakeryAction.Serve -> serve(session, action.order)
            BakeryAction.CloseDay -> session.copy(finishReason = FinishReason.CLOSED)
        }
    }
    private fun serve(session: DaySession, orderNumber: Int?): DaySession {
        val item = session.tray.firstOrNull { it.id == session.selectedTrayId } ?: return session
        val order = session.orders.firstOrNull { it.recipe == item.recipe && (orderNumber == null || it.number == orderNumber) } ?: return session
        val quick = order.patienceMs - (session.elapsedMs - order.arrivedMs) >= order.patienceMs / 2
        val combo = if (quick) session.combo + 1 else 0
        val tips = if (quick) minOf(6, 2 * (combo / 2)) else 0
        val tray = session.tray.filterNot { it.id == item.id }
        val orders = session.orders.filterNot { it.number == order.number }
        val selected = tray.firstOrNull { it.recipe == orders.firstOrNull()?.recipe } ?: tray.firstOrNull()
        return advance(session.copy(
            tray = tray, orders = orders, selectedTrayId = selected?.id, served = session.served + 1,
            deliveryCoins = session.deliveryCoins + recipes.first { it.id == item.recipe }.coins,
            combo = combo, bestCombo = maxOf(session.bestCombo, combo), tips = session.tips + tips
        ), 0L)
    }
}
