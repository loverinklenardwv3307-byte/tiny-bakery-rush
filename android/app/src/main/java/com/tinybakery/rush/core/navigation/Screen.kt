package com.tinybakery.rush.core.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(val sessionId: Long) : Screen
    data class GameOver(val resultId: Long) : Screen
    data object Upgrades : Screen
    data object RecipeBook : Screen
    data object Settings : Screen
}
