package com.tinybakery.rush.domain.usecase

import com.tinybakery.rush.domain.model.*

class CreateDaySession(private val recipes: List<Recipe>, private val customers: List<CustomerId>) {
    operator fun invoke(progress: BakeryProgress, day: Int): DaySession {
        require(day in 1..progress.currentDay)
        val available = recipes.filter { it.fromDay <= day }
        val recipe = available[(day - 1) % available.size].id
        val interval = (12000L - 1000L * ((day - 1) / 10)).coerceAtLeast(7000L)
        return DaySession(
            id = progress.nextSessionId, day = day, ovenLevel = progress.ovenLevel,
            counterLevel = progress.counterLevel, nextSpawnMs = interval,
            orders = listOf(order(day, 0, 0L, progress.counterLevel)),
            selectedRecipe = recipe, doughRecipe = recipe,
            ovens = List(if (progress.ovenLevel >= 2) 2 else 1) { null }
        )
    }
    fun order(day: Int, number: Int, arrivedMs: Long, counterLevel: Int): CustomerOrder {
        val available = recipes.filter { it.fromDay <= day }
        val base = if (day == 1) 48000L else (42000L - 3000L * ((day - 1) / 10)).coerceAtLeast(24000L)
        val patience = base + when (counterLevel) { 1 -> 4000L; 2 -> 8000L; else -> 0L }
        return CustomerOrder(number, customers[number % customers.size], available[(day - 1 + number) % available.size].id, arrivedMs, patience)
    }
}
