package com.tinybakery.rush.presentation.app

import androidx.lifecycle.ViewModel
import com.tinybakery.rush.core.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {
    private val mutableScreen = MutableStateFlow<Screen>(Screen.Splash)
    val screen = mutableScreen.asStateFlow()
    private var backStack: List<Screen> = emptyList()

    fun finishSplash() {
        if (screen.value == Screen.Splash) mutableScreen.value = Screen.Menu
    }

    fun navigate(destination: Screen) {
        if (destination == screen.value) return
        if (screen.value != Screen.Splash) backStack = backStack + screen.value
        mutableScreen.value = destination
    }

    fun back() {
        val previous = backStack.lastOrNull() ?: Screen.Menu
        backStack = backStack.dropLast(1)
        mutableScreen.value = previous
    }

    fun menu() {
        backStack = emptyList()
        mutableScreen.value = Screen.Menu
    }

    fun replay(sessionId: Long) {
        backStack = listOf(Screen.Menu)
        mutableScreen.value = Screen.Game(sessionId)
    }
}
