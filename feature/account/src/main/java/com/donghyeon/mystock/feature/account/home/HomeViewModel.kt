package com.donghyeon.mystock.feature.account.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donghyeon.mystock.core.data.AccountRepository
import com.donghyeon.mystock.core.data.HomeData
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.feature.account.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import com.donghyeon.mystock.feature.common.R as CommonR

private const val WATCH_TIMEOUT_MS = 5_000L
private const val SELECTED_ACCOUNT_ID = "selectedAccountId"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(HomeUiState(selectedAccountId = savedStateHandle[SELECTED_ACCOUNT_ID]))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val selectedAccountId = MutableStateFlow(_uiState.value.selectedAccountId)

    // 값이 바뀔 때마다 대시보드 관찰을 다시 시작해 즉시 새 스냅샷을 받는다
    private val reloads = MutableStateFlow(0)

    private val _effects = Channel<HomeUiEffect>(Channel.UNLIMITED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { loadWhileWatched() }
    }

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.Navigate -> emit(HomeUiEffect.Navigate(intent.target))
            HomeUiIntent.Refresh -> {
                dispatch(HomeMsg.Load.RefreshStarted)
                refresh()
            }
            is HomeUiIntent.Overlay -> overlay(intent)
            HomeUiIntent.ToggleOrderAlert -> toggleOrderAlert()
            HomeUiIntent.DenyNotificationPermission -> denyNotificationPermission()
            HomeUiIntent.DismissStale -> dispatch(HomeMsg.Local.StaleDismissed)
            HomeUiIntent.DismissPauseReason -> dispatch(HomeMsg.Local.PauseReasonDismissed)
        }
    }

    private fun dispatch(message: HomeMsg) {
        val state = _uiState.updateAndGet { reduce(it, message) }
        selectedAccountId.value = state.selectedAccountId
        savedStateHandle[SELECTED_ACCOUNT_ID] = state.selectedAccountId
    }

    private fun emit(effect: HomeUiEffect) {
        _effects.trySend(effect)
    }

    // 화면이 잠깐 가려졌다 돌아오는 동안은 관찰을 끊지 않는다
    private suspend fun loadWhileWatched() {
        _uiState.subscriptionCount
            .map { it > 0 }
            .distinctUntilChanged()
            .flatMapLatest { watching ->
                if (watching) {
                    flowOf(true)
                } else {
                    flow {
                        delay(WATCH_TIMEOUT_MS)
                        emit(false)
                    }
                }
            }
            .distinctUntilChanged()
            .collectLatest { watching ->
                if (!watching) return@collectLatest
                refresh()
                combine(selectedAccountId.filterNotNull(), reloads) { accountId, _ -> accountId }
                    .flatMapLatest { accountId ->
                        accountRepository.observeHomeData(accountId).map { accountId to it }
                    }
                    .collect { (accountId, result) ->
                        result
                            .onSuccess { data ->
                                dispatch(HomeMsg.Load.HomeDataLoaded(accountId, data, loadedAt = Instant.now(clock)))
                            }
                            .onFailure { dispatch(HomeMsg.Load.LoadFailed(accountId)) }
                    }
            }
    }

    private fun refresh() {
        reloads.value++
        viewModelScope.launch {
            accountRepository.getAccounts()
                .onSuccess { accounts -> dispatch(HomeMsg.Load.AccountsLoaded(accounts)) }
                .onFailure { dispatch(HomeMsg.Load.LoadFailed(accountId = null)) }
        }
    }

    private fun overlay(intent: HomeUiIntent.Overlay) {
        when (intent) {
            HomeUiIntent.Overlay.OpenAccountSheet -> dispatch(HomeMsg.Sheet.AccountSheetOpened)
            HomeUiIntent.Overlay.DismissAccountSheet -> dispatch(HomeMsg.Sheet.Closed)
            is HomeUiIntent.Overlay.SelectAccount -> dispatch(HomeMsg.Sheet.AccountPending(intent.accountId))
            HomeUiIntent.Overlay.ConfirmAccount -> dispatch(HomeMsg.Sheet.AccountConfirmed)
            is HomeUiIntent.Overlay.OpenCommand -> dispatch(HomeMsg.Sheet.CommandOpened(intent.command))
            HomeUiIntent.Overlay.DismissCommand -> dispatch(HomeMsg.Sheet.Closed)
            HomeUiIntent.Overlay.ConfirmCommand -> confirmCommand()
            HomeUiIntent.Overlay.RequestExit -> dispatch(HomeMsg.Local.ExitRequested)
            HomeUiIntent.Overlay.DismissExit -> dispatch(HomeMsg.Local.ExitDismissed)
        }
    }

    private fun confirmCommand() {
        val state = _uiState.value
        val accountId = state.selectedAccountId ?: return
        val command = (state.sheet as? HomeSheet.Command)?.command ?: return
        dispatch(HomeMsg.Sheet.CommandConfirmed)
        viewModelScope.launch {
            when (command) {
                HomeCommand.PAUSE -> accountRepository.pauseAccount(accountId)
                HomeCommand.RESUME -> accountRepository.resumeAccount(accountId)
                HomeCommand.CLOSE -> accountRepository.closeAccount(accountId)
                HomeCommand.LIQUIDATE -> accountRepository.liquidateAccount(accountId)
            }.onFailure { emit(HomeUiEffect.ShowMessage(R.string.home_command_failed)) }
            dispatch(HomeMsg.Local.CommandFinished)
            // 실패해도 다시 받는다 — 서버가 거절한 이유가 이미 바뀐 상태일 수 있다
            refresh()
        }
    }

    private fun toggleOrderAlert() {
        val account = _uiState.value.selectedAccount ?: return
        val enabled = !account.orderAlertEnabled
        setOrderAlert(account.id, enabled) {
            val message = if (enabled) R.string.home_order_alert_on else R.string.home_order_alert_off
            emit(HomeUiEffect.ShowMessage(message))
            if (enabled) emit(HomeUiEffect.RequestNotificationPermission)
        }
    }

    // 권한이 없으면 켜 두어도 알림이 오지 않으므로 되돌린다
    private fun denyNotificationPermission() {
        val account = _uiState.value.selectedAccount ?: return
        if (!account.orderAlertEnabled) return
        setOrderAlert(account.id, enabled = false) {
            emit(HomeUiEffect.ShowMessage(CommonR.string.common_notification_permission))
        }
    }

    private fun setOrderAlert(accountId: String, enabled: Boolean, onSuccess: () -> Unit) {
        dispatch(HomeMsg.Local.OrderAlertRequested(accountId, enabled))
        viewModelScope.launch {
            accountRepository.setOrderAlert(accountId, enabled)
                .onSuccess { onSuccess() }
                .onFailure {
                    dispatch(HomeMsg.Local.OrderAlertFailed(accountId, enabled))
                    emit(HomeUiEffect.ShowMessage(CommonR.string.common_notification_failed))
                }
        }
    }
}

private fun reduce(state: HomeUiState, message: HomeMsg): HomeUiState = when (message) {
    is HomeMsg.Load -> reduceLoad(state, message)
    is HomeMsg.Sheet -> reduceSheet(state, message)
    is HomeMsg.Local -> reduceLocal(state, message)
}

private fun reduceLoad(state: HomeUiState, message: HomeMsg.Load): HomeUiState = when (message) {
    HomeMsg.Load.RefreshStarted -> state.copy(refreshing = true)

    is HomeMsg.Load.AccountsLoaded -> reduceAccountsLoaded(state, message.accounts)

    // 계좌를 바꾼 직후 flatMapLatest 버퍼에 남은 이전 계좌 응답은 버린다
    is HomeMsg.Load.LoadFailed ->
        if (message.accountId != null && message.accountId != state.selectedAccountId) {
            state
        } else {
            state.copy(
                refreshing = false,
                content = if (state.accounts.isEmpty()) {
                    HomeContent.Failed
                } else {
                    HomeContent.Ready(dashboard = state.dashboard ?: HomeDashboard(), stale = true)
                },
            )
        }

    is HomeMsg.Load.HomeDataLoaded ->
        if (state.selectedAccountId != message.accountId) {
            state
        } else if (message.data.accounts.none { it.id == message.accountId }) {
            // 인증 전후로 서버가 바뀌어 고른 계좌가 사라졌다 — 첫 계좌로 옮기고 그 대시보드를 기다린다
            reduceAccountsLoaded(state.copy(content = HomeContent.Loading), message.data.accounts)
        } else {
            val accounts = message.data.accounts
            state.copy(
                refreshing = false,
                accounts = accounts,
                orderAlertOverrides = state.orderAlertOverrides.settledBy(accounts),
                content = HomeContent.Ready(
                    dashboard = HomeDashboard(
                        strategies = message.data.strategies,
                        performance = message.data.performance,
                        loadedAt = message.loadedAt,
                        portfolio = message.data.portfolio,
                        orders = message.data.orders,
                        orderCount = message.data.orderCount,
                    ),
                    stale = message.data.partial,
                ),
                staleDismissed = state.staleDismissed && message.data.partial,
            )
        }
}

private fun reduceAccountsLoaded(state: HomeUiState, accounts: List<Account>): HomeUiState {
    val selectedId = state.selectedAccountId
        ?.takeIf { id -> accounts.any { it.id == id } }
        ?: accounts.firstOrNull()?.id
    return state.copy(
        // 선택한 계좌가 있으면 대시보드가 와야 새로고침이 끝난다
        refreshing = state.refreshing && selectedId != null,
        content = if (state.content is HomeContent.Loading && selectedId != null) {
            state.content
        } else {
            HomeContent.Ready(dashboard = state.dashboard ?: HomeDashboard())
        },
        staleDismissed = false,
        accounts = accounts,
        orderAlertOverrides = state.orderAlertOverrides.settledBy(accounts),
        selectedAccountId = selectedId,
    )
}

private fun reduceSheet(state: HomeUiState, message: HomeMsg.Sheet): HomeUiState =
    when (message) {
        HomeMsg.Sheet.AccountSheetOpened -> state.copy(
            sheet = HomeSheet.Account(pendingAccountId = state.selectedAccountId),
        )

        HomeMsg.Sheet.Closed -> state.copy(sheet = null)

        is HomeMsg.Sheet.AccountPending -> state.copy(
            sheet = (state.sheet as? HomeSheet.Account)?.copy(pendingAccountId = message.accountId) ?: state.sheet,
        )

        HomeMsg.Sheet.AccountConfirmed -> {
            val accountId = (state.sheet as? HomeSheet.Account)?.pendingAccountId ?: state.selectedAccountId
            if (accountId == state.selectedAccountId) {
                state.copy(sheet = null)
            } else {
                // 문맥이 통째로 바뀌므로 새 계좌의 대시보드가 올 때까지 스켈레톤을 보인다
                state.copy(
                    sheet = null,
                    selectedAccountId = accountId,
                    content = HomeContent.Loading,
                    dismissedPauseReason = null,
                )
            }
        }

        is HomeMsg.Sheet.CommandOpened -> state.copy(sheet = HomeSheet.Command(message.command))

        HomeMsg.Sheet.CommandConfirmed -> state.copy(sheet = null, dismissedPauseReason = null, commanding = true)
    }

private fun reduceLocal(state: HomeUiState, message: HomeMsg.Local): HomeUiState =
    when (message) {
        is HomeMsg.Local.OrderAlertRequested -> {
            val override = message.accountId to message.enabled
            state.copy(orderAlertOverrides = state.orderAlertOverrides + override)
        }

        is HomeMsg.Local.OrderAlertFailed ->
            if (state.orderAlertOverrides[message.accountId] == message.enabled) {
                state.copy(orderAlertOverrides = state.orderAlertOverrides - message.accountId)
            } else {
                state
            }

        HomeMsg.Local.StaleDismissed -> state.copy(staleDismissed = true)

        HomeMsg.Local.PauseReasonDismissed -> state.copy(dismissedPauseReason = state.selectedAccount?.pauseReason)

        HomeMsg.Local.ExitRequested -> state.copy(exitConfirming = true)

        HomeMsg.Local.ExitDismissed -> state.copy(exitConfirming = false)

        HomeMsg.Local.CommandFinished -> state.copy(commanding = false)
    }

private sealed interface HomeMsg {
    sealed interface Load : HomeMsg {
        data object RefreshStarted : Load

        data class AccountsLoaded(val accounts: List<Account>) : Load

        data class LoadFailed(val accountId: String?) : Load

        data class HomeDataLoaded(
            val accountId: String,
            val data: HomeData,
            val loadedAt: Instant,
        ) : Load
    }

    sealed interface Sheet : HomeMsg {
        data object AccountSheetOpened : Sheet

        data class AccountPending(val accountId: String) : Sheet

        data object AccountConfirmed : Sheet

        data class CommandOpened(val command: HomeCommand) : Sheet

        data object CommandConfirmed : Sheet

        data object Closed : Sheet
    }

    sealed interface Local : HomeMsg {
        data class OrderAlertRequested(val accountId: String, val enabled: Boolean) : Local

        data class OrderAlertFailed(val accountId: String, val enabled: Boolean) : Local

        data object StaleDismissed : Local

        data object PauseReasonDismissed : Local

        data object ExitRequested : Local

        data object ExitDismissed : Local

        data object CommandFinished : Local
    }
}
