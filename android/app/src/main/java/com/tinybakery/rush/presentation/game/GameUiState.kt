package com.tinybakery.rush.presentation.game

import com.tinybakery.rush.R
import com.tinybakery.rush.domain.model.*

enum class GamePhase { LOADING, PLAYING, PAUSED, FINISHING, FINISHED, ERROR }
enum class MainAction { SHAPE, BAKE, SERVE }
data class OrderUi(val order: CustomerOrder, val hearts: Int, val canServe: Boolean)
data class OvenUi(val index: Int, val load: OvenLoad?, val labelRes: Int, val seconds: Int, val progress: Float, val canCollect: Boolean)
data class GameUiState(
    val session: DaySession? = null, val phase: GamePhase = GamePhase.LOADING,
    val recipes: List<RecipeId> = emptyList(), val orders: List<OrderUi> = emptyList(), val ovens: List<OvenUi> = emptyList(),
    val hintRes: Int = R.string.dough_hint, val mainAction: MainAction = MainAction.SHAPE,
    val canAct: Boolean = false, val canShape: Boolean = false, val canBake: Boolean = false,
    val pauseDialog: Boolean = false, val statusServed: Int? = null, val error: Boolean = false
)
