package com.donghyeon.mystock.feature.account.home

import androidx.annotation.StringRes
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Holding
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.feature.common.sampleStrategies
import java.time.Duration
import java.time.Instant

enum class HomeCommand { PAUSE, RESUME, CLOSE, LIQUIDATE }

sealed interface HomeNavTarget {
    data object Exit : HomeNavTarget

    data object Strategies : HomeNavTarget

    data object Settings : HomeNavTarget

    data class Performance(val accountId: String) : HomeNavTarget

    data class Strategy(val key: String, val accountId: String) : HomeNavTarget
}

data class HomeDashboard(
    val strategies: List<Strategy> = emptyList(),
    val performance: Performance? = null,
    val loadedAt: Instant? = null,
    val portfolio: Portfolio? = null,
    val orders: List<Order> = emptyList(),
    val orderCount: Long = 0,
) {
    val currentAmount: Long? get() = portfolio?.let { it.valuationAmount + it.cashAmount }

    val runningHours: Long?
        get() = performance?.let { p -> loadedAt?.let { Duration.between(p.startedAt, it).toHours() } }

    val valuationPnl: Pnl?
        get() = portfolio?.let {
            val amount = it.valuationAmount - it.purchaseAmount
            Pnl(amount, if (it.purchaseAmount == 0L) 0.0 else amount * PERCENT / it.purchaseAmount)
        }
}

private const val PERCENT = 100.0

// 서버가 같은 값을 돌려준 계좌의 낙관적 덮어쓰기는 끝난 것이다
internal fun Map<String, Boolean>.settledBy(accounts: List<Account>): Map<String, Boolean> =
    filterNot { (accountId, enabled) -> accounts.any { it.id == accountId && it.orderAlertEnabled == enabled } }

sealed interface HomeContent {
    data object Loading : HomeContent

    data object Failed : HomeContent

    data class Ready(
        val dashboard: HomeDashboard = HomeDashboard(),
        val stale: Boolean = false,
    ) : HomeContent
}

sealed interface HomeSheet {
    data class Account(val pendingAccountId: String?) : HomeSheet

    data class Command(val command: HomeCommand) : HomeSheet
}

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    val refreshing: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val orderAlertOverrides: Map<String, Boolean> = emptyMap(),
    val selectedAccountId: String? = null,
    val staleDismissed: Boolean = false,
    // 닫은 사유만 기억한다 — 서버가 다른 사유로 다시 멈추면 배너가 다시 뜬다
    val dismissedPauseReason: String? = null,
    val sheet: HomeSheet? = null,
    val exitConfirming: Boolean = false,
    val commanding: Boolean = false,
) {
    val selectedAccount: Account?
        get() {
            val account = accounts.firstOrNull { it.id == selectedAccountId } ?: return null
            val override = orderAlertOverrides[account.id] ?: return account
            return account.copy(orderAlertEnabled = override)
        }

    val dashboard: HomeDashboard? get() = (content as? HomeContent.Ready)?.dashboard

    val staleVisible: Boolean
        get() = (content as? HomeContent.Ready)?.stale == true && !staleDismissed

    val pauseReason: String? get() = selectedAccount?.pauseReason?.takeUnless { it == dismissedPauseReason }
}

sealed interface HomeUiIntent {
    data class Navigate(val target: HomeNavTarget) : HomeUiIntent

    data object Refresh : HomeUiIntent

    data object ToggleOrderAlert : HomeUiIntent

    data object DenyNotificationPermission : HomeUiIntent

    data object DismissStale : HomeUiIntent

    data object DismissPauseReason : HomeUiIntent

    // 화면 위에 하나만 뜨는 겹침 — 시트와 종료 확인
    sealed interface Overlay : HomeUiIntent {
        data object OpenAccountSheet : Overlay

        data object DismissAccountSheet : Overlay

        data class SelectAccount(val accountId: String) : Overlay

        data object ConfirmAccount : Overlay

        data class OpenCommand(val command: HomeCommand) : Overlay

        data object DismissCommand : Overlay

        data object ConfirmCommand : Overlay

        data object RequestExit : Overlay

        data object DismissExit : Overlay
    }
}

sealed interface HomeUiEffect {
    data class Navigate(val target: HomeNavTarget) : HomeUiEffect

    data class ShowMessage(@StringRes val message: Int) : HomeUiEffect

    data object RequestNotificationPermission : HomeUiEffect
}

private fun won(value: Long) = Price(value.toDouble(), Currency.KRW)

private fun dollar(value: Double) = Price(value, Currency.USD)

internal val sampleAccounts = listOf(
    Account(
        id = "acc-1",
        displayName = "토스증권",
        state = AccountState.RUNNING,
        strategy = sampleStrategies.last(),
        todayPnl = Pnl(amount = 25_300, percent = 0.39),
        cumulativePnl = Pnl(amount = 983_259, percent = 17.93),
        pauseReason = null,
        orderAlertEnabled = true,
    ),
    Account(
        id = "acc-2",
        displayName = "한국투자증권",
        state = AccountState.WAITING,
        strategy = null,
        todayPnl = null,
        cumulativePnl = null,
        pauseReason = null,
        orderAlertEnabled = false,
    ),
)

internal val samplePortfolio = Portfolio(
    valuationAmount = 4_326_259,
    purchaseAmount = 4_292_889,
    cashAmount = 2_140_000,
    holdings = listOf(
        Holding("삼성전자", 10.0, won(71_200), won(68_400), 712_000, Pnl(28_000, 4.09)),
        Holding("NAVER", 4.0, won(196_000), won(196_000), 784_000, Pnl(0, 0.0)),
        Holding("AAPL", 4.0, dollar(231.40), dollar(224.10), 1_242_618, Pnl(39_201, 3.26)),
        Holding("MSFT", 3.0, dollar(394.20), dollar(402.60), 1_587_641, Pnl(-33_831, -2.09)),
    ),
)

private val samplePerformance = Performance(
    todayPnl = Pnl(amount = 25_300, percent = 0.39),
    cumulativePnl = Pnl(amount = 983_259, percent = 17.93),
    initialEquity = 5_483_000,
    startedAt = Instant.parse("2026-08-31T12:00:00Z"),
)

private val sampleLoadedAt = samplePerformance.startedAt.plus(Duration.ofHours(17))

private val sampleOrders = listOf(
    Order("ord-1", "삼성전자", OrderSide.BUY, 10.0, won(68_400), 684_000, null),
    Order("ord-2", "AAPL", OrderSide.BUY, 4.0, dollar(224.10), 1_203_417, null),
)

private val sampleDashboard = HomeDashboard(
    strategies = sampleStrategies,
    performance = samplePerformance,
    loadedAt = sampleLoadedAt,
    portfolio = samplePortfolio,
    orders = sampleOrders,
    orderCount = 12,
)

internal val sampleLoadingState = HomeUiState()

internal val sampleFailedState = HomeUiState(content = HomeContent.Failed)

internal val sampleRunningState = HomeUiState(
    content = HomeContent.Ready(sampleDashboard),
    accounts = sampleAccounts,
    selectedAccountId = "acc-1",
)

internal val sampleStaleState = sampleRunningState.copy(
    content = HomeContent.Ready(sampleDashboard, stale = true),
)

internal val sampleExitConfirmState = sampleRunningState.copy(exitConfirming = true)

internal val sampleCommandingState = sampleRunningState.copy(commanding = true)

internal val sampleAccountSheetState = sampleRunningState.copy(sheet = HomeSheet.Account(pendingAccountId = "acc-1"))

internal val samplePauseSheetState = sampleRunningState.copy(sheet = HomeSheet.Command(HomeCommand.PAUSE))

internal val sampleCloseSheetState = sampleRunningState.copy(sheet = HomeSheet.Command(HomeCommand.CLOSE))

internal val sampleLiquidateSheetState = sampleRunningState.copy(sheet = HomeSheet.Command(HomeCommand.LIQUIDATE))

internal val sampleRunningEmptyState = sampleRunningState.copy(
    content = HomeContent.Ready(
        HomeDashboard(
            strategies = sampleStrategies,
            performance = samplePerformance.copy(
                todayPnl = Pnl(amount = 0, percent = 0.0),
                cumulativePnl = Pnl(amount = 0, percent = 0.0),
            ),
            loadedAt = samplePerformance.startedAt,
            portfolio = Portfolio(
                valuationAmount = 0,
                purchaseAmount = 0,
                cashAmount = 5_483_000,
                holdings = emptyList(),
            ),
        ),
    ),
)

// 자릿수가 많은 금액은 줄을 바꾸지 않고 자리에 맞춰 글자 크기를 줄인다
internal val sampleRunningLargeAmountState = sampleRunningState.copy(
    content = HomeContent.Ready(
        sampleDashboard.copy(
            performance = samplePerformance.copy(
                todayPnl = Pnl(amount = -12_345_678, percent = -12.34),
                cumulativePnl = Pnl(amount = 1_234_567_890, percent = 123.45),
                initialEquity = 1_234_567_890,
            ),
            portfolio = Portfolio(
                valuationAmount = 1_357_924_680,
                purchaseAmount = 1_234_567_890,
                cashAmount = 2_468_135_790,
                holdings = listOf(
                    Holding("SK하이닉스", 10_000.0, won(1_234_000), won(1_180_000), 12_340_000_000, Pnl(540_000_000, 4.58)),
                    Holding(
                        "MSFT",
                        3_000.0,
                        dollar(1_394.20),
                        dollar(1_402.60),
                        1_587_641_000,
                        Pnl(-33_831_000, -2.09)
                    ),
                ),
            ),
            orders = listOf(
                Order("ord-1", "SK하이닉스", OrderSide.BUY, 10_000.0, won(1_180_000), 11_800_000_000, null),
                Order(
                    "ord-6",
                    "MSFT",
                    OrderSide.SELL,
                    3_000.0,
                    dollar(1_394.20),
                    1_587_641_000,
                    Pnl(-33_831_000, -2.09)
                ),
            ),
        ),
    ),
)

// 세션 스냅샷이 없거나 브로커가 답하지 않으면 당일 손익만 빠진다
internal val sampleRunningNoTodayPnlState = sampleRunningState.copy(
    content = HomeContent.Ready(sampleDashboard.copy(performance = samplePerformance.copy(todayPnl = null))),
)

internal val sampleWaitingState = HomeUiState(
    content = HomeContent.Ready(
        HomeDashboard(
            strategies = sampleStrategies,
            portfolio = Portfolio(
                valuationAmount = 0,
                purchaseAmount = 0,
                cashAmount = 5_483_000,
                holdings = emptyList(),
            ),
        ),
    ),
    accounts = sampleAccounts,
    selectedAccountId = "acc-2",
)

internal val sampleWaitingLargeAmountState = sampleWaitingState.copy(
    content = HomeContent.Ready(
        HomeDashboard(
            strategies = sampleStrategies,
            portfolio = Portfolio(
                valuationAmount = 0,
                purchaseAmount = 0,
                cashAmount = 12_345_678_900,
                holdings = emptyList(),
            ),
        ),
    ),
)

internal val samplePausedState = sampleRunningState.copy(
    accounts = sampleAccounts.map { account ->
        if (account.id == "acc-1") {
            account.copy(
                state = AccountState.PAUSED,
                pauseReason = "서버가 일시정지했습니다 — 급격한 변동성 감지",
            )
        } else {
            account
        }
    },
)

internal val sampleResumeSheetState = samplePausedState.copy(sheet = HomeSheet.Command(HomeCommand.RESUME))

// 연결 끊김이 사유 배너 위에 쌓인다
internal val samplePausedStaleState = samplePausedState.copy(
    content = HomeContent.Ready(sampleDashboard, stale = true),
)

// 장이 닫혀 있어 시장가 주문이 체결되기 전 — 보유·주문은 운용 중 그대로다
internal val sampleLiquidatingState = sampleRunningState.copy(
    accounts = sampleAccounts.map { account ->
        if (account.id == "acc-1") account.copy(state = AccountState.LIQUIDATING) else account
    },
)

internal val sampleClosingState = sampleRunningState.copy(
    content = HomeContent.Ready(
        HomeDashboard(
            strategies = sampleStrategies,
            performance = samplePerformance,
            loadedAt = sampleLoadedAt,
            portfolio = samplePortfolio.copy(
                valuationAmount = 1_242_618,
                purchaseAmount = 1_203_417,
                cashAmount = 5_223_641,
                holdings = samplePortfolio.holdings.filter { it.name == "AAPL" },
            ),
            orders = listOf(
                Order("ord-3", "삼성전자", OrderSide.SELL, 10.0, won(71_200), 712_000, Pnl(28_000, 4.09)),
                Order("ord-4", "NAVER", OrderSide.SELL, 4.0, won(196_000), 784_000, Pnl(0, 0.0)),
                Order(
                    "ord-5",
                    "MSFT",
                    OrderSide.SELL,
                    3.0,
                    dollar(394.20),
                    1_587_641,
                    Pnl(-33_831, -2.09),
                ),
            ),
            orderCount = 15,
        ),
    ),
    accounts = sampleAccounts.map { account ->
        if (account.id == "acc-1") {
            account.copy(state = AccountState.CLOSING, orderAlertEnabled = false)
        } else {
            account
        }
    },
)
