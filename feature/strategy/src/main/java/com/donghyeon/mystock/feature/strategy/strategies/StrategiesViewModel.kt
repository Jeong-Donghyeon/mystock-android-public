package com.donghyeon.mystock.feature.strategy.strategies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.model.Strategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.donghyeon.mystock.feature.common.R as CommonR

@HiltViewModel
class StrategiesViewModel @Inject constructor(
    private val strategyRepository: StrategyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrategiesUiState())
    val uiState: StateFlow<StrategiesUiState> = _uiState.asStateFlow()

    private val _effects = Channel<StrategiesUiEffect>(Channel.UNLIMITED)
    val effects: Flow<StrategiesUiEffect> = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: StrategiesUiIntent) {
        when (intent) {
            is StrategiesUiIntent.Navigate -> emit(StrategiesUiEffect.Navigate(intent.target))
            StrategiesUiIntent.Refresh -> {
                dispatch(StrategiesMsg.RefreshStarted)
                load()
            }
        }
    }

    private fun dispatch(message: StrategiesMsg) {
        _uiState.update { reduce(it, message) }
    }

    private fun emit(effect: StrategiesUiEffect) {
        _effects.trySend(effect)
    }

    private fun load() {
        viewModelScope.launch {
            strategyRepository.getStrategies()
                .onSuccess { dispatch(StrategiesMsg.Loaded(it)) }
                .onFailure {
                    dispatch(StrategiesMsg.LoadFailed)
                    if (_uiState.value.content is StrategiesContent.Ready) {
                        emit(StrategiesUiEffect.ShowMessage(CommonR.string.common_load_failed))
                    }
                }
        }
    }
}

private fun reduce(state: StrategiesUiState, message: StrategiesMsg): StrategiesUiState = when (message) {
    StrategiesMsg.RefreshStarted -> state.copy(refreshing = true)

    is StrategiesMsg.Loaded -> state.copy(refreshing = false, content = StrategiesContent.Ready(message.strategies))

    StrategiesMsg.LoadFailed -> state.copy(
        refreshing = false,
        content = if (state.content is StrategiesContent.Ready) state.content else StrategiesContent.Failed,
    )
}

private sealed interface StrategiesMsg {
    data object RefreshStarted : StrategiesMsg

    data class Loaded(val strategies: List<Strategy>) : StrategiesMsg

    data object LoadFailed : StrategiesMsg
}
