package com.donghyeon.mystock.feature.settings.settings

import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.SettingsRepository
import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.AppVersion
import com.donghyeon.mystock.core.model.Settings
import com.donghyeon.mystock.feature.settings.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.donghyeon.mystock.feature.common.R as CommonR

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    appVersion: AppVersion,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(settings = settingsRepository.settings.value, version = appVersion.name),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SettingsUiEffect>(Channel.UNLIMITED)
    val effects: Flow<SettingsUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { observeWhileWatched() }
    }

    fun onIntent(intent: SettingsUiIntent) {
        when (intent) {
            is SettingsUiIntent.Navigate -> emit(SettingsUiEffect.Navigate(intent.target))
            is SettingsUiIntent.SetNotificationEnabled -> setNotificationEnabled(intent.enabled)
            SettingsUiIntent.DenyNotificationPermission -> denyNotificationPermission()
            SettingsUiIntent.Authenticate -> authenticate()
            SettingsUiIntent.Revoke -> revoke()
        }
    }

    private fun dispatch(message: SettingsMsg) {
        _uiState.update { reduce(it, message) }
    }

    private fun emit(effect: SettingsUiEffect) {
        _effects.trySend(effect)
    }

    private suspend fun observeWhileWatched() {
        _uiState.subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .collectLatest { watching ->
                if (!watching) return@collectLatest
                settingsRepository.settings.collect { dispatch(SettingsMsg.SettingsChanged(it)) }
            }
    }

    private fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(enabled)
                .onSuccess { if (enabled) emit(SettingsUiEffect.RequestNotificationPermission) }
                .onFailure { emit(SettingsUiEffect.ShowMessage(CommonR.string.common_notification_failed)) }
        }
    }

    private fun denyNotificationPermission() {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(false)
                .onSuccess { emit(SettingsUiEffect.ShowMessage(CommonR.string.common_notification_permission)) }
                .onFailure { emit(SettingsUiEffect.ShowMessage(CommonR.string.common_notification_failed)) }
        }
    }

    private fun authenticate() {
        if (_uiState.value.commanding) return
        val fields = _uiState.value.keys
        val keys = ApiKeys.of(fields.map { it.text })
        if (keys == null) {
            emit(SettingsUiEffect.ShowMessage(R.string.settings_keys_incomplete, listOf(ApiKeys.COUNT)))
            return
        }
        dispatch(SettingsMsg.CommandStarted)
        viewModelScope.launch {
            settingsRepository.authenticateKeys(keys)
                .onSuccess { fields.forEach { it.clearText() } }
                .onFailure { emit(SettingsUiEffect.ShowMessage(R.string.settings_auth_failed)) }
            dispatch(SettingsMsg.CommandFinished)
        }
    }

    private fun revoke() {
        if (_uiState.value.commanding) return
        dispatch(SettingsMsg.CommandStarted)
        viewModelScope.launch {
            settingsRepository.revokeKeys()
                .onFailure { emit(SettingsUiEffect.ShowMessage(R.string.settings_revoke_failed)) }
            dispatch(SettingsMsg.CommandFinished)
        }
    }
}

private fun reduce(state: SettingsUiState, message: SettingsMsg): SettingsUiState = when (message) {
    is SettingsMsg.SettingsChanged -> state.copy(settings = message.settings)
    SettingsMsg.CommandStarted -> state.copy(commanding = true)
    SettingsMsg.CommandFinished -> state.copy(commanding = false)
}

private sealed interface SettingsMsg {
    data class SettingsChanged(val settings: Settings) : SettingsMsg

    data object CommandStarted : SettingsMsg

    data object CommandFinished : SettingsMsg
}
