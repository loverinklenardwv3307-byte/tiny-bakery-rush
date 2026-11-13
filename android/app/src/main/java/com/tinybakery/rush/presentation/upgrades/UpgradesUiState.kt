package com.tinybakery.rush.presentation.upgrades

import com.tinybakery.rush.domain.model.Upgrade
import com.tinybakery.rush.domain.model.UpgradeTrack

data class UpgradeOfferUi(val track: UpgradeTrack, val level: Int, val offer: Upgrade?, val canBuy: Boolean, val missingCoins: Int)
data class UpgradesUiState(
    val loading: Boolean = true, val wallet: Int = 40, val currentDay: Int = 1,
    val offers: List<UpgradeOfferUi> = emptyList(), val busy: Boolean = false,
    val error: Boolean = false, val purchased: Boolean = false, val pausedSession: Boolean = false
)
