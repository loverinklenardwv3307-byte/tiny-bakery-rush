package com.tinybakery.rush.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tinybakery.rush.data.sample.SampleData
import com.tinybakery.rush.domain.repository.*
import com.tinybakery.rush.domain.usecase.*
import com.tinybakery.rush.presentation.app.*
import com.tinybakery.rush.presentation.splash.SplashViewModel
import com.tinybakery.rush.presentation.menu.MenuViewModel
import com.tinybakery.rush.presentation.game.GameViewModel
import com.tinybakery.rush.presentation.gameover.GameOverViewModel
import com.tinybakery.rush.presentation.upgrades.UpgradesViewModel
import com.tinybakery.rush.presentation.recipebook.RecipeBookViewModel
import com.tinybakery.rush.presentation.settings.SettingsViewModel

class AppViewModelFactory(
    private val repository: BakeryRepository, private val advance: AdvanceSession,
    private val apply: ApplyBakeryAction, private val audio: BakeryAudio, private val id: Long?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        AppViewModel::class.java -> AppViewModel()
        AudioViewModel::class.java -> AudioViewModel(repository, audio)
        SplashViewModel::class.java -> SplashViewModel(repository)
        MenuViewModel::class.java -> MenuViewModel(repository)
        GameViewModel::class.java -> GameViewModel(requireNotNull(id), repository, advance, apply, SampleData.recipes, audio)
        GameOverViewModel::class.java -> GameOverViewModel(requireNotNull(id), repository)
        UpgradesViewModel::class.java -> UpgradesViewModel(repository, SampleData.upgrades)
        RecipeBookViewModel::class.java -> RecipeBookViewModel(repository, SampleData.recipes)
        SettingsViewModel::class.java -> SettingsViewModel(repository)
        else -> throw IllegalArgumentException(modelClass.name)
    } as T
}
