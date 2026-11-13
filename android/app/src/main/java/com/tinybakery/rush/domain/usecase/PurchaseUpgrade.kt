package com.tinybakery.rush.domain.usecase

import com.tinybakery.rush.domain.model.*

class PurchaseUpgrade(private val upgrades: List<Upgrade>) {
    operator fun invoke(progress: BakeryProgress, track: UpgradeTrack): BakeryProgress {
        val level = if (track == UpgradeTrack.OVEN) progress.ovenLevel else progress.counterLevel
        val offer = upgrades.firstOrNull { it.track == track && it.level == level + 1 } ?: error("Maximum level")
        require(progress.currentDay >= offer.unlockDay && progress.wallet >= offer.cost)
        return progress.copy(wallet = progress.wallet - offer.cost,
            ovenLevel = if (track == UpgradeTrack.OVEN) offer.level else progress.ovenLevel,
            counterLevel = if (track == UpgradeTrack.COUNTER) offer.level else progress.counterLevel)
    }
}
