package com.donghyeon.mystock.feature.strategy.versions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.feature.strategy.navigation.StrategyVersionsKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.donghyeon.mystock.feature.common.R as CommonR

@HiltViewModel(assistedFactory = StrategyVersionsViewModel.Factory::class)
class StrategyVersionsViewModel @AssistedInject constructor(
    @Assisted private val key: StrategyVersionsKey,
    private val strategyRepository: StrategyRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: StrategyVersionsKey): StrategyVersionsViewModel
    }

    private val _uiState = MutableStateFlow(StrategyVersionsUiState())
    val uiState: StateFlow<StrategyVersionsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<StrategyVersionsUiEffect>(Channel.UNLIMITED)
    val effects: Flow<StrategyVersionsUiEffect> = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: StrategyVersionsUiIntent) {
        when (intent) {
            is StrategyVersionsUiIntent.Navigate -> emit(StrategyVersionsUiEffect.Navigate(intent.target))
            StrategyVersionsUiIntent.Refresh -> {
                dispatch(StrategyVersionsMsg.RefreshStarted)
                load()
            }
        }
    }

    private fun dispatch(message: StrategyVersionsMsg) {
        _uiState.update { reduce(it, message) }
    }

    private fun emit(effect: StrategyVersionsUiEffect) {
        _effects.trySend(effect)
    }

    private fun load() {
        viewModelScope.launch {
            strategyRepository.getStrategyDetail(key.strategyKey, accountId = null)
                .onSuccess { dispatch(StrategyVersionsMsg.Loaded(it)) }
                .onFailure {
                    dispatch(StrategyVersionsMsg.LoadFailed)
                    if (_uiState.value.content is StrategyVersionsContent.Ready) {
                        emit(StrategyVersionsUiEffect.ShowMessage(CommonR.string.common_load_failed))
                    }
                }
        }
    }
}

private fun reduce(state: StrategyVersionsUiState, message: StrategyVersionsMsg): StrategyVersionsUiState =
    when (message) {
        StrategyVersionsMsg.RefreshStarted -> state.copy(refreshing = true)

        is StrategyVersionsMsg.Loaded ->
            state.copy(refreshing = false, content = StrategyVersionsContent.Ready(message.detail))

        StrategyVersionsMsg.LoadFailed -> state.copy(
            refreshing = false,
            content = if (state.content is StrategyVersionsContent.Ready) {
                state.content
            } else {
                StrategyVersionsContent.Failed
            },
        )
    }

private sealed interface StrategyVersionsMsg {
    data object RefreshStarted : StrategyVersionsMsg

    data class Loaded(val detail: StrategyDetail) : StrategyVersionsMsg

    data object LoadFailed : StrategyVersionsMsg
}
