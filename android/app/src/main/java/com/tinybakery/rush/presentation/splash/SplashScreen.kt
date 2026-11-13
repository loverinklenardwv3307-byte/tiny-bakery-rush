package com.tinybakery.rush.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinybakery.rush.R
import com.tinybakery.rush.core.di.ServiceLocator
import com.tinybakery.rush.core.theme.*
import com.tinybakery.rush.core.ui.*

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val vm: SplashViewModel = viewModel(factory = ServiceLocator.factory())
    val finished by vm.finished.collectAsStateWithLifecycle()
    val latestFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(finished) { if (finished && vm.consumeFinished()) latestFinished() }
    SplashScreen()
}

@Composable
fun SplashScreen() {
    val loop = rememberInfiniteTransition(label = "bakeryFloat")
    val float by loop.animateFloat(-6f, 6f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "hero")
    val glow by loop.animateFloat(0.88f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "flour")
    val opening = stringResource(R.string.opening)
    BakeryBackdrop(Backdrop.SPLASH) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.made_with_care), style = MaterialTheme.typography.labelSmall,
                color = Cream.copy(alpha = 0.78f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Image(painterResource(R.drawable.logo), null, Modifier.size(224.dp).graphicsLayer { translationY = float.dp.toPx(); alpha = glow }, contentScale = ContentScale.Fit)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.splash_title), Modifier.semantics { heading() }, color = Cream,
                style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.splash_subtitle), color = Cream, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            LinearProgressIndicator(Modifier.width(136.dp).height(4.dp).semantics { contentDescription = opening }, color = Cream, trackColor = Cream.copy(alpha = 0.22f))
            Spacer(Modifier.height(16.dp))
            Text(opening, style = MaterialTheme.typography.labelSmall, color = Cream.copy(alpha = 0.8f))
        }
    }
}
