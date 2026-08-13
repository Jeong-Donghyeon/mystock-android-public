package com.donghyeon.mystock.feature.account.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.PerformanceRepository
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.feature.account.navigation.PerformanceKey
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

@HiltViewModel(assistedFactory = PerformanceViewModel.Factory::class)
class PerformanceViewModel @AssistedInject constructor(
    @Assisted private val key: PerformanceKey,
    private val performanceRepository: PerformanceRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: PerformanceKey): PerformanceViewModel
    }

    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PerformanceUiEffect>(Channel.UNLIMITED)
    val effects: Flow<PerformanceUiEffect> = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: PerformanceUiIntent) {
        when (intent) {
            is PerformanceUiIntent.Navigate -> emit(PerformanceUiEffect.Navigate(intent.target))
            PerformanceUiIntent.Refresh -> {
                dispatch(PerformanceMsg.RefreshStarted)
                load()
            }
        }
    }

    private fun dispatch(message: PerformanceMsg) {
        _uiState.update { reduce(it, message) }
    }

    private fun emit(effect: PerformanceUiEffect) {
        _effects.trySend(effect)
    }

    private fun load() {
        viewModelScope.launch {
            performanceRepository.getAccountPerformance(key.accountId)
                .onSuccess { dispatch(PerformanceMsg.Loaded(it)) }
                .onFailure {
                    dispatch(PerformanceMsg.LoadFailed)
                    if (_uiState.value.content is PerformanceContent.Ready) {
                        emit(PerformanceUiEffect.ShowMessage(CommonR.string.common_load_failed))
                    }
                }
        }
    }
}

private fun reduce(state: PerformanceUiState, message: PerformanceMsg): PerformanceUiState = when (message) {
    PerformanceMsg.RefreshStarted -> state.copy(refreshing = true)

    is PerformanceMsg.Loaded -> state.copy(refreshing = false, content = PerformanceContent.Ready(message.performance))

    PerformanceMsg.LoadFailed -> state.copy(
        refreshing = false,
        content = if (state.content is PerformanceContent.Ready) state.content else PerformanceContent.Failed,
    )
}

private sealed interface PerformanceMsg {
    data object RefreshStarted : PerformanceMsg

    data class Loaded(val performance: AccountPerformance) : PerformanceMsg

    data object LoadFailed : PerformanceMsg
}
