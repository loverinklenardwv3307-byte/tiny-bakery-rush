package com.tinybakery.rush.presentation.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.navigation.Screen
import com.tinybakery.rush.presentation.game.GameRoute
import com.tinybakery.rush.presentation.game.GameViewModel
import com.tinybakery.rush.presentation.gameover.GameOverRoute
import com.tinybakery.rush.presentation.menu.MenuRoute
import com.tinybakery.rush.presentation.recipebook.RecipeBookRoute
import com.tinybakery.rush.presentation.settings.SettingsRoute
import com.tinybakery.rush.presentation.splash.SplashRoute
import com.tinybakery.rush.presentation.upgrades.UpgradesRoute

@Composable
fun AppRoot() {
    val vm: AppViewModel = viewModel(factory = ServiceLocator.factory())
    val audio: AudioViewModel = viewModel(factory = ServiceLocator.factory())
    val screen by vm.screen.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val gameScreen = screen as? Screen.Game
    val game: GameViewModel? = if (gameScreen != null) viewModel(key = "game_${gameScreen.sessionId}", factory = ServiceLocator.factory(gameScreen.sessionId)) else null
    DisposableEffect(lifecycle) {
        audio.foreground(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) audio.foreground(true)
            if (event == Lifecycle.Event.ON_STOP) audio.foreground(false)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); audio.foreground(false) }
    }
    LaunchedEffect(screen) { audio.allowed(screen != Screen.Splash && screen !is Screen.Game) }
    BackHandler(enabled = screen != Screen.Menu && screen != Screen.Splash) { game?.leave(); vm.back() }
    AnimatedContent(targetState = screen, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(250)) }, label = "bakeryNavigation") { destination ->
        when (destination) {
            Screen.Splash -> SplashRoute(vm::finishSplash)
            Screen.Menu -> MenuRoute(vm::navigate)
            is Screen.Game -> GameRoute(destination.sessionId, { id -> if (vm.screen.value == destination) vm.navigate(Screen.GameOver(id)) }, vm::menu,
                { allowed -> if (vm.screen.value == destination) audio.allowed(allowed) })
            is Screen.GameOver -> GameOverRoute(destination.resultId, vm::replay, vm::menu)
            Screen.Upgrades -> UpgradesRoute(vm::back)
            Screen.RecipeBook -> RecipeBookRoute(vm::back)
            Screen.Settings -> SettingsRoute(vm::back)
        }
    }
}
