package com.tinybakery.rush.presentation.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*
import com.tinybakery.rush.domain.model.*
import com.tinybakery.rush.presentation.dialog.PauseDialog

@Composable
fun GameRoute(id: Long, onResult: (Long) -> Unit, onMenu: () -> Unit, onAudio: (Boolean) -> Unit) {
    val vm: GameViewModel = viewModel(key = "game_$id", factory = ServiceLocator.factory(id))
    val state by vm.uiState.collectAsStateWithLifecycle()
    val latestResult by rememberUpdatedState(onResult)
    val latestAudio by rememberUpdatedState(onAudio)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) { vm.events.collect { latestResult(it) } }
    LaunchedEffect(state.phase) { latestAudio(state.phase == GamePhase.PLAYING) }
    DisposableEffect(lifecycle, vm) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) vm.enter()
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) vm.pause() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); vm.pause() }
    }
    val leave = { vm.leave(); onMenu() }
    GameScreen(state, vm::action, vm::mainAction, { vm.pause(true) }, vm::resume, vm::retry, vm::summary, leave)
    if (state.pauseDialog) PauseDialog(vm::resume, leave)
}

@Composable
fun GameScreen(state: GameUiState, onAction: (BakeryAction) -> Unit, onMainAction: () -> Unit,
    onPause: () -> Unit, onResume: () -> Unit, onRetry: () -> Unit, onSummary: () -> Unit, onMenu: () -> Unit) {
    val ovenBounds = remember { mutableStateMapOf<Int, Rect>() }
    var dragCenter by remember { mutableStateOf<Offset?>(null) }
    BakeryBackdrop(Backdrop.GAME) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val compact = maxHeight < 560.dp
            if (compact) {
                Row(Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1.2f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KitchenHeader(state, onMenu, onPause)
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (state.phase == GamePhase.LOADING) InlineStatus(stringResource(R.string.loading), loading = true)
                            if (state.error) InlineStatus(stringResource(R.string.save_error), onRetry)
                            if (state.session != null) {
                                CustomerQueue(state, onAction)
                                Worktop(state, onAction, ovenBounds, dragCenter)
                            }
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KitchenFooter(state, onAction, onMainAction, onResume, onRetry, onSummary, onMenu, ovenBounds, { dragCenter = it })
                    }
                }
            } else {
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KitchenHeader(state, onMenu, onPause)
                if (state.phase == GamePhase.LOADING) {
                    InlineStatus(stringResource(R.string.loading), loading = true)
                }
                if (state.error) InlineStatus(stringResource(R.string.save_error), onRetry)
                val session = state.session
                if (session != null) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.statusServed != null) Text(stringResource(R.string.delivery_status, state.statusServed, session.target),
                            Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.labelSmall, color = Indigo)
                        CustomerQueue(state, onAction)
                        Worktop(state, onAction, ovenBounds, dragCenter)
                    }
                    KitchenFooter(state, onAction, onMainAction, onResume, onRetry, onSummary, onMenu, ovenBounds, { dragCenter = it })
                } else if (state.error) SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
            }
            }
        }
    }
}

@Composable
private fun KitchenFooter(state: GameUiState, onAction: (BakeryAction) -> Unit, onMainAction: () -> Unit,
    onResume: () -> Unit, onRetry: () -> Unit, onSummary: () -> Unit, onMenu: () -> Unit,
    ovenBounds: Map<Int, Rect>, onDrag: (Offset?) -> Unit) {
    when (state.phase) {
        GamePhase.FINISHED -> BakeryPanel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.day_finished), style = MaterialTheme.typography.titleMedium, color = Indigo)
            PrimaryButton(R.string.view_summary, onSummary)
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        }
        GamePhase.FINISHING -> InlineStatus(stringResource(if (state.session?.finishReason == FinishReason.EARLY_COLLECTION) R.string.early_feedback else R.string.saving), loading = true)
        GamePhase.PAUSED -> BakeryPanel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.paused), style = MaterialTheme.typography.titleMedium, color = Indigo)
            PrimaryButton(R.string.resume, onResume)
        }
        GamePhase.ERROR -> BakeryPanel(Modifier.fillMaxWidth()) {
            SecondaryButton(R.string.retry_save, onRetry, Modifier.fillMaxWidth())
            SecondaryButton(R.string.menu, onMenu, Modifier.fillMaxWidth())
        }
        GamePhase.PLAYING -> KitchenControls(state, onAction, onMainAction, ovenBounds, onDrag)
        GamePhase.LOADING -> Unit
    }
}

@Composable
private fun KitchenHeader(state: GameUiState, onMenu: () -> Unit, onPause: () -> Unit) {
    val session = state.session
    BakeryPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onMenu, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(4.dp)) {
                Image(painterResource(R.drawable.icon_home), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.menu), style = MaterialTheme.typography.labelSmall)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.day, session?.day ?: 1), Modifier.semantics { heading() }, style = MaterialTheme.typography.labelSmall, color = Indigo)
                val seconds = ((session?.remainingMs ?: 60000L) + 999L) / 1000L
                Text(stringResource(R.string.timer, seconds / 60, seconds % 60), style = MaterialTheme.typography.titleLarge, color = Indigo)
            }
            TextButton(onClick = onPause, enabled = state.phase == GamePhase.PLAYING || state.phase == GamePhase.PAUSED,
                modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(4.dp)) {
                Image(painterResource(R.drawable.icon_pause), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.pause), style = MaterialTheme.typography.labelSmall)
            }
        }
        if (session != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.orders_served, session.served, session.target), style = MaterialTheme.typography.bodyMedium, color = Indigo)
            if (session.combo > 0) Text(stringResource(R.string.combo, session.combo), style = MaterialTheme.typography.labelSmall, color = Indigo)
        }
    }
}

@Composable
private fun CustomerQueue(state: GameUiState, onAction: (BakeryAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.your_customers), style = MaterialTheme.typography.titleMedium, color = Indigo)
        if (state.orders.isEmpty()) BakeryPanel(Modifier.fillMaxWidth()) { Text(stringResource(R.string.waiting_customers), style = MaterialTheme.typography.bodyMedium) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.orders, key = { it.order.number }) { customer ->
                val name = stringResource(customer.order.customer.nameRes())
                val recipe = stringResource(customer.order.recipe.nameRes())
                val description = stringResource(R.string.customer_state, name, recipe, customer.hearts)
                Surface(Modifier.width(154.dp).semantics(mergeDescendants = true) { stateDescription = description },
                    shape = RoundedCornerShape(18.dp), color = Cream.copy(alpha = 0.96f), shadowElevation = 3.dp,
                    border = BorderStroke(if (customer == state.orders.firstOrNull()) 2.dp else 1.dp, Indigo.copy(alpha = if (customer == state.orders.firstOrNull()) 0.65f else 0.13f))) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RecipeArt(customer.order.recipe, 48.dp, decorative = true)
                            Column {
                                Text(name, style = MaterialTheme.typography.titleMedium, color = Indigo)
                                Text(stringResource(when (customer.hearts) { 3 -> R.string.hearts_three; 2 -> R.string.hearts_two; 1 -> R.string.hearts_one; else -> R.string.hearts_none }), color = ActionRed, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(recipe, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        TextButton(onClick = { onAction(BakeryAction.Serve(customer.order.number)) }, enabled = customer.canServe,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.serve)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Worktop(state: GameUiState, onAction: (BakeryAction) -> Unit, ovenBounds: MutableMap<Int, Rect>, dragCenter: Offset?) {
    val session = state.session ?: return
    BakeryPanel(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.worktop), style = MaterialTheme.typography.titleMedium, color = Indigo)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.ovens.forEach { oven ->
                val hovering = dragCenter?.let { ovenBounds[oven.index]?.contains(it) } == true && oven.load == null
                OvenSlot(oven, hovering, Modifier.weight(1f).onGloballyPositioned { ovenBounds[oven.index] = it.boundsInRoot() },
                    { onAction(BakeryAction.Collect(oven.index)) })
            }
        }
        Text(stringResource(R.string.choose_recipe), style = MaterialTheme.typography.labelSmall, color = Indigo)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.recipes) { recipe ->
                val selected = recipe == session.selectedRecipe
                val description = stringResource(recipe.nameRes())
                OutlinedButton(onClick = { onAction(BakeryAction.SelectRecipe(recipe)) }, enabled = state.phase == GamePhase.PLAYING,
                    modifier = Modifier.widthIn(min = 96.dp).heightIn(min = 64.dp).semantics { stateDescription = description; this.selected = selected },
                    shape = RoundedCornerShape(14.dp), border = BorderStroke(if (selected) 2.dp else 1.dp, Indigo.copy(alpha = if (selected) 0.8f else 0.18f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Lavender else Cream), contentPadding = PaddingValues(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RecipeArt(recipe, 40.dp, decorative = true)
                        Text(description, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        HorizontalDivider(color = Indigo.copy(alpha = 0.1f))
        Text(stringResource(R.string.ready_tray), style = MaterialTheme.typography.labelSmall, color = Indigo)
        if (session.tray.isEmpty()) Text(stringResource(R.string.tray_empty), style = MaterialTheme.typography.bodyMedium)
        else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            session.tray.forEach { item ->
                val selected = session.selectedTrayId == item.id
                val description = stringResource(if (selected) R.string.tray_selected else R.string.tray_available)
                OutlinedButton(onClick = { onAction(BakeryAction.SelectTray(item.id)) }, enabled = state.phase == GamePhase.PLAYING,
                    modifier = Modifier.heightIn(min = 64.dp).semantics { stateDescription = description; this.selected = selected },
                    border = BorderStroke(if (selected) 2.dp else 1.dp, Indigo), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(6.dp)) {
                    RecipeArt(item.recipe, 48.dp)
                    Text(stringResource(R.string.select), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        TipJar(session.tips)
    }
}

@Composable
private fun OvenSlot(oven: OvenUi, hovering: Boolean, modifier: Modifier, onCollect: () -> Unit) {
    val label = stringResource(oven.labelRes)
    val description = stringResource(R.string.oven_state, stringResource(R.string.oven_number, oven.index + 1), label)
    Surface(modifier.semantics { stateDescription = description }, shape = RoundedCornerShape(16.dp), color = Lavender,
        border = BorderStroke(if (hovering || oven.load?.golden == true) 2.dp else 1.dp, if (oven.load?.golden == true) ActionRed else Indigo.copy(alpha = 0.35f))) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(R.string.oven_number, oven.index + 1), style = MaterialTheme.typography.labelSmall, color = Indigo)
            Box(Modifier.size(width = 88.dp, height = 60.dp).background(Indigo, RoundedCornerShape(12.dp)).padding(7.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().border(1.dp, Cream.copy(alpha = 0.4f), RoundedCornerShape(6.dp)))
                oven.load?.let { RecipeArt(it.recipe, 48.dp, decorative = true) }
            }
            Text(label, Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.labelSmall,
                color = if (oven.load?.golden == true) ActionRed else Indigo, textAlign = TextAlign.Center)
            if (oven.load != null) {
                LinearProgressIndicator(progress = { oven.progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = if (oven.load.golden) ActionRed else Indigo, trackColor = Cream)
                if (!oven.load.golden) Text(stringResource(R.string.seconds_left, oven.seconds), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                SecondaryButton(R.string.collect, onCollect, Modifier.fillMaxWidth(), enabled = oven.canCollect)
            }
        }
    }
}

@Composable
private fun KitchenControls(state: GameUiState, onAction: (BakeryAction) -> Unit, onMainAction: () -> Unit,
    ovenBounds: Map<Int, Rect>, onDrag: (Offset?) -> Unit) {
    val session = state.session ?: return
    BakeryPanel(Modifier.fillMaxWidth().zIndex(2f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DoughPad(state, { onAction(BakeryAction.Shape) }, { point ->
                ovenBounds.entries.firstOrNull { it.value.contains(point) }?.let { onAction(BakeryAction.Bake(it.key)) }
            }, onDrag)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(session.doughRecipe.nameRes()), style = MaterialTheme.typography.titleMedium, color = Indigo)
                Text(stringResource(if (session.shapeSteps == 3) R.string.dough_ready else R.string.shaping_steps, session.shapeSteps), style = MaterialTheme.typography.labelSmall, color = Indigo)
                Text(stringResource(state.hintRes), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(when (state.mainAction) { MainAction.SHAPE -> R.string.shape; MainAction.BAKE -> R.string.bake; MainAction.SERVE -> R.string.serve }, onMainAction, Modifier.weight(1f), enabled = state.canAct)
            TextButton(onClick = { onAction(BakeryAction.CloseDay) }, modifier = Modifier.widthIn(min = 96.dp).heightIn(min = 60.dp)) { Text(stringResource(R.string.close_day), style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun DoughPad(state: GameUiState, onShape: () -> Unit, onDrop: (Offset) -> Unit, onDrag: (Offset?) -> Unit) {
    val session = state.session ?: return
    var offset by remember { mutableStateOf(Offset.Zero) }
    var center by remember { mutableStateOf(Offset.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val visualOffset by animateOffsetAsState(if (dragging) offset else Offset.Zero, tween(if (dragging) 0 else 180), label = "doughDrop")
    val bounce by animateFloatAsState(if (session.shapeSteps % 2 == 1) 1.06f else 1f, spring(dampingRatio = 0.65f, stiffness = 450f), label = "doughShape")
    val description = stringResource(if (session.shapeSteps == 3) R.string.dough_ready else R.string.shaping_steps, session.shapeSteps)
    Column(Modifier.width(88.dp).zIndex(if (dragging) 10f else 0f), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(88.dp).onGloballyPositioned { center = it.boundsInRoot().center }
            .graphicsLayer { translationX = visualOffset.x; translationY = visualOffset.y; scaleX = bounce; scaleY = bounce }
            .semantics { stateDescription = description }
            .clickable(enabled = state.canShape, onClick = onShape)
            .pointerInput(session.shapeSteps, state.phase) {
                if (session.shapeSteps == 3 && state.phase == GamePhase.PLAYING) detectDragGestures(
                    onDragStart = { dragging = true; offset = Offset.Zero; origin = center },
                    onDragEnd = { onDrop(origin + offset); dragging = false; offset = Offset.Zero; onDrag(null) },
                    onDragCancel = { dragging = false; offset = Offset.Zero; onDrag(null) },
                    onDrag = { change, amount -> change.consume(); offset += amount; onDrag(origin + offset) })
            }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.sprite_5), stringResource(R.string.dough_description, stringResource(session.doughRecipe.nameRes())), Modifier.size(72.dp), contentScale = ContentScale.Fit)
        }
    }
}
