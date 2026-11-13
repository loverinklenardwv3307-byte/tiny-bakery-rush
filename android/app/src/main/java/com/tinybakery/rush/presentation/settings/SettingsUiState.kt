package com.tinybakery.rush.presentation.settings

data class SettingsUiState(val loading: Boolean = true, val soundEnabled: Boolean = true, val musicEnabled: Boolean = true, val busy: Boolean = false, val error: Boolean = false, val saved: Boolean = false)
