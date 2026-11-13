package com.tinybakery.rush.domain.usecase

import com.tinybakery.rush.domain.model.*

class AdvanceSession(private val create: CreateDaySession) {
    operator fun invoke(session: DaySession, deltaMs: Long): DaySession {
        if (session.finishReason != null) return session
        val elapsed = session.elapsedMs + deltaMs.coerceAtLeast(0L)
        val fullTray = session.tray.size >= session.trayCapacity
        val ovens = session.ovens.map { load ->
            load?.let {
                val age = if (fullTray) {
                    if (it.golden) it.ageMs else (it.ageMs + deltaMs).coerceAtMost(it.bakeMs)
                } else it.ageMs + deltaMs
                it.copy(ageMs = age)
            }
        }
        var next = session.copy(elapsedMs = elapsed, ovens = ovens)
        if (elapsed >= next.nextSpawnMs && next.orders.size < next.queueCapacity && next.spawned < next.target) {
            next = next.copy(
                orders = next.orders + create.order(next.day, next.spawned, elapsed, next.counterLevel),
                spawned = next.spawned + 1, nextSpawnMs = elapsed + next.spawnIntervalMs
            )
        }
        val reason = when {
            next.served >= next.target -> FinishReason.SUCCESS
            ovens.any { it != null && it.ageMs >= it.bakeMs + 4000L } -> FinishReason.OVERBAKED
            next.orders.any { elapsed - it.arrivedMs >= it.patienceMs } -> FinishReason.PATIENCE
            elapsed >= 60000L -> FinishReason.TIMEOUT
            else -> null
        }
        return next.copy(finishReason = reason)
    }
}
