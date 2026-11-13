package com.tinybakery.rush.domain.usecase

import com.tinybakery.rush.domain.model.*

class FinalizeDay(private val recipes: List<Recipe>) {
    operator fun invoke(progress: BakeryProgress, session: DaySession): BakeryProgress {
        if (progress.results.any { it.sessionId == session.id }) return progress
        val reason = requireNotNull(session.finishReason)
        val first = reason == FinishReason.SUCCESS && session.day !in progress.completedDays
        val bonus = if (first) 10 + 2 * session.day else 0
        val currentDay = if (reason == FinishReason.SUCCESS) maxOf(progress.currentDay, minOf(60, session.day + 1)) else progress.currentDay
        val wallet = progress.wallet + session.deliveryCoins + session.tips + bonus
        val result = DayResult(session.id, session.day, reason, session.served, session.deliveryCoins, session.tips,
            bonus, session.bestCombo, wallet, currentDay,
            if (first) recipes.firstOrNull { it.fromDay == session.day + 1 }?.id else null)
        return progress.copy(
            wallet = wallet, currentDay = currentDay,
            completedDays = if (reason == FinishReason.SUCCESS) progress.completedDays + session.day else progress.completedDays,
            bestCombo = maxOf(progress.bestCombo, session.bestCombo), session = session, results = progress.results + result
        )
    }
}
