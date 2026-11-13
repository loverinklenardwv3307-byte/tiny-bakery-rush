package com.tinybakery.rush.presentation.upgrades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinybakery.rush.domain.model.*
import com.tinybakery.rush.domain.repository.BakeryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UpgradesViewModel(private val repository: BakeryRepository, private val upgrades: List<Upgrade>) : ViewModel() {
    private val mutableState = MutableStateFlow(UpgradesUiState())
    val uiState = mutableState.asStateFlow()
    private var pending: UpgradeTrack? = null
    init {
        viewModelScope.launch { repository.state.collect { store ->
            val p = store.progress
            mutableState.update { old ->
                if (p == null) old.copy(loading = store.loading, error = store.error)
                else old.copy(loading = false, error = store.error, wallet = p.wallet, currentDay = p.currentDay,
                    pausedSession = p.session?.finishReason == null && p.session != null,
                    offers = UpgradeTrack.entries.map { track ->
                        val level = if (track == UpgradeTrack.OVEN) p.ovenLevel else p.counterLevel
                        val offer = upgrades.firstOrNull { it.track == track && it.level == level + 1 }
                        UpgradeOfferUi(track, level, offer, offer != null && p.currentDay >= offer.unlockDay && p.wallet >= offer.cost,
                            if (offer == null) 0 else (offer.cost - p.wallet).coerceAtLeast(0))
                    })
            }
        } }
        viewModelScope.launch { repository.load() }
    }
    fun purchase(track: UpgradeTrack) {
        if (uiState.value.busy) return
        pending = track
        mutableState.update { it.copy(busy = true, error = false, purchased = false) }
        viewModelScope.launch {
            try { repository.purchase(track); pending = null; mutableState.update { it.copy(purchased = true) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(error = true) } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }
    fun retry() { pending?.let(::purchase) ?: run { viewModelScope.launch { repository.load() } } }
}
