package com.tinybakery.rush.domain.model

enum class UpgradeTrack { OVEN, COUNTER }
data class Upgrade(val track: UpgradeTrack, val level: Int, val unlockDay: Int, val cost: Int)
