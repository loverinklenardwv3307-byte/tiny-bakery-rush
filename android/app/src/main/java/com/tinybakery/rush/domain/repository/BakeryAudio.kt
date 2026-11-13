package com.tinybakery.rush.domain.repository

enum class BakeryTone { SHAPE, COLLECT, SERVE }
interface BakeryAudio {
    fun activate(): Boolean
    fun deactivate()
    suspend fun music()
    suspend fun effect(tone: BakeryTone)
}
