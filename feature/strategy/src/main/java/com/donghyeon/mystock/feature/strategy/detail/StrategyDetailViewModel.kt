package com.donghyeon.mystock.feature.strategy.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.feature.strategy.R
import com.donghyeon.mystock.feature.strategy.navigation.StrategyDetailKey
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
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import com.donghyeon.mystock.feature.common.R as CommonR

private const val VERSION_SCOPE = "versionScope"
private const val ACCOUNT_SCOPE = "accountScope"

@HiltViewModel(assistedFactory = StrategyDetailViewModel.Factory::class)
class StrategyDetailViewModel @AssistedInject constructor(
    @Assisted private val key: StrategyDetailKey,
    private val strategyRepository: StrategyRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(key: StrategyDetailKey): StrategyDetailViewModel
    }

    private val _uiState = MutableStateFlow(
        StrategyDetailUiState(
            startAccountExpected = key.accountId != null,
            versionScope = savedStateHandle[VERSION_SCOPE],
            accountScope = savedStateHandle[ACCOUNT_SCOPE],
        ),
    )
    val uiState: StateFlow<StrategyDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<StrategyDetailUiEffect>(Channel.UNLIMITED)
    val effects: Flow<StrategyDetailUiEffect> = _effects.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: StrategyDetailUiIntent) {
        when (intent) {
            is StrategyDetailUiIntent.Navigate -> emit(StrategyDetailUiEffect.Navigate(intent.target))
            StrategyDetailUiIntent.Refresh -> {
                dispatch(StrategyDetailMsg.Load.RefreshStarted)
                load()
            }
            is StrategyDetailUiIntent.SelectVersionScope -> dispatch(
                StrategyDetailMsg.Scope.VersionSelected(intent.version),
            )
            StrategyDetailUiIntent.OpenAccountSheet -> dispatch(StrategyDetailMsg.Sheet.AccountScopeOpened)
            StrategyDetailUiIntent.DismissAccountSheet -> dispatch(StrategyDetailMsg.Sheet.Closed)
            is StrategyDetailUiIntent.SelectAccountScope -> dispatch(
                StrategyDetailMsg.Scope.AccountSelected(intent.accountId),
            )
            StrategyDetailUiIntent.OpenStart -> dispatch(StrategyDetailMsg.Sheet.StartOpened)
            StrategyDetailUiIntent.DismissStart -> dispatch(StrategyDetailMsg.Sheet.Closed)
            StrategyDetailUiIntent.ConfirmStart -> confirmStart()
        }
    }

    private fun dispatch(message: StrategyDetailMsg) {
        val state = _uiState.updateAndGet { reduce(it, message) }
        savedStateHandle[VERSION_SCOPE] = state.versionScope
        savedStateHandle[ACCOUNT_SCOPE] = state.accountScope
    }

    private fun emit(effect: StrategyDetailUiEffect) {
        _effects.trySend(effect)
    }

    private fun load() {
        viewModelScope.launch {
            strategyRepository.getStrategyDetail(key.strategyKey, key.accountId)
                .onSuccess { dispatch(StrategyDetailMsg.Load.Loaded(it)) }
                .onFailure {
                    dispatch(StrategyDetailMsg.Load.LoadFailed)
                    if (_uiState.value.content is StrategyDetailContent.Ready) {
                        emit(StrategyDetailUiEffect.ShowMessage(CommonR.string.common_load_failed))
                    }
                }
        }
    }

    private fun confirmStart() {
        val accountId = _uiState.value.startAccount?.id ?: return
        if (_uiState.value.commanding) return
        dispatch(StrategyDetailMsg.Start.Confirmed)
        viewModelScope.launch {
            strategyRepository.startStrategy(key.strategyKey, accountId)
                .onSuccess { emit(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Home)) }
                .onFailure { emit(StrategyDetailUiEffect.ShowMessage(R.string.strategy_detail_start_failed)) }
            dispatch(StrategyDetailMsg.Start.Finished)
        }
    }
}

private fun reduce(state: StrategyDetailUiState, message: StrategyDetailMsg): StrategyDetailUiState =
    when (message) {
        is StrategyDetailMsg.Load -> reduceLoad(state, message)
        is StrategyDetailMsg.Scope -> reduceScope(state, message)
        is StrategyDetailMsg.Sheet -> reduceSheet(state, message)
        is StrategyDetailMsg.Start -> reduceStart(state, message)
    }

private fun reduceLoad(state: StrategyDetailUiState, message: StrategyDetailMsg.Load): StrategyDetailUiState =
    when (message) {
        StrategyDetailMsg.Load.RefreshStarted -> state.copy(refreshing = true)

        // 새 상세에 없는 범위는 전체로 되돌린다 — 칩은 전체인데 목록만 비는 일이 없게
        is StrategyDetailMsg.Load.Loaded -> state.copy(
            refreshing = false,
            content = StrategyDetailContent.Ready(message.detail),
            versionScope = state.versionScope?.takeIf { scope -> message.detail.versions.any { it.version == scope } },
            accountScope = state.accountScope?.takeIf { scope -> message.detail.runs.any { it.accountId == scope } },
        )

        StrategyDetailMsg.Load.LoadFailed -> state.copy(
            refreshing = false,
            content = if (state.content is StrategyDetailContent.Ready) state.content else StrategyDetailContent.Failed,
        )
    }

private fun reduceScope(state: StrategyDetailUiState, message: StrategyDetailMsg.Scope): StrategyDetailUiState =
    when (message) {
        is StrategyDetailMsg.Scope.VersionSelected -> state.copy(versionScope = message.version)
        is StrategyDetailMsg.Scope.AccountSelected -> state.copy(accountScope = message.accountId, sheet = null)
    }

private fun reduceSheet(state: StrategyDetailUiState, message: StrategyDetailMsg.Sheet): StrategyDetailUiState =
    when (message) {
        StrategyDetailMsg.Sheet.AccountScopeOpened -> state.copy(sheet = StrategyDetailSheet.AccountScope)
        StrategyDetailMsg.Sheet.StartOpened -> state.copy(
            sheet = if (state.startAccount != null) StrategyDetailSheet.Start else state.sheet,
        )
        StrategyDetailMsg.Sheet.Closed -> state.copy(sheet = null)
    }

private fun reduceStart(state: StrategyDetailUiState, message: StrategyDetailMsg.Start): StrategyDetailUiState =
    when (message) {
        StrategyDetailMsg.Start.Confirmed -> state.copy(sheet = null, commanding = true)
        StrategyDetailMsg.Start.Finished -> state.copy(commanding = false)
    }

private sealed interface StrategyDetailMsg {
    sealed interface Load : StrategyDetailMsg {
        data object RefreshStarted : Load

        data class Loaded(val detail: StrategyDetail) : Load

        data object LoadFailed : Load
    }

    sealed interface Scope : StrategyDetailMsg {
        data class VersionSelected(val version: String?) : Scope

        data class AccountSelected(val accountId: String?) : Scope
    }

    sealed interface Sheet : StrategyDetailMsg {
        data object AccountScopeOpened : Sheet

        data object StartOpened : Sheet

        data object Closed : Sheet
    }

    sealed interface Start : StrategyDetailMsg {
        data object Confirmed : Start

        data object Finished : Start
    }
}
