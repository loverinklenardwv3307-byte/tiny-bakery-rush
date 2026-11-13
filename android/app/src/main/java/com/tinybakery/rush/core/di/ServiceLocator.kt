package com.tinybakery.rush.core.di

import android.content.Context
import com.tinybakery.rush.data.audio.LocalBakeryAudio
import com.tinybakery.rush.data.local.BakeryPreferences
import com.tinybakery.rush.data.repository.BakeryRepositoryImpl
import com.tinybakery.rush.data.sample.SampleData
import com.tinybakery.rush.domain.repository.*
import com.tinybakery.rush.domain.usecase.*

object ServiceLocator {
    private data class Dependencies(val repository: BakeryRepository, val advance: AdvanceSession, val apply: ApplyBakeryAction, val audio: BakeryAudio)
    private var dependencies: Dependencies? = null

    @Synchronized
    fun initialize(context: Context) {
        if (dependencies != null) return
        val create = CreateDaySession(SampleData.recipes, SampleData.customers)
        val advance = AdvanceSession(create)
        val repository = BakeryRepositoryImpl(BakeryPreferences(context.applicationContext), create,
            FinalizeDay(SampleData.recipes), PurchaseUpgrade(SampleData.upgrades))
        dependencies = Dependencies(repository, advance, ApplyBakeryAction(SampleData.recipes, advance), LocalBakeryAudio(context.applicationContext))
    }

    fun factory(id: Long? = null): AppViewModelFactory {
        val d = requireNotNull(dependencies)
        return AppViewModelFactory(d.repository, d.advance, d.apply, d.audio, id)
    }
}
