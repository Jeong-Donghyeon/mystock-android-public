package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Holding
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyRun
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToLong

private const val ORDER_LIMIT = 20

internal data class FakeWorld(
    val accounts: List<FakeAccount>,
    val runs: List<FakeRun>,
    val nextOrder: Int,
    val nextRun: Int,
)

internal data class FakeAccount(
    val id: String,
    val displayName: String,
    val markets: Set<Market>,
    val state: AccountState,
    val strategy: Strategy?,
    val orderAlertEnabled: Boolean,
    val cashAmount: Long,
    val holdings: List<FakeHolding>,
    val orders: List<Order>,
    val orderCount: Long,
    val runId: String?,
    val startedAt: Instant?,
    // 운용을 시작한 시점과 오늘 장 시작 시점의 평가액 — 누적·오늘 손익의 기준
    val initialEquity: Long,
    val dayStartEquity: Long,
) {
    val valuationAmount: Long get() = holdings.sumOf(FakeHolding::valuationAmount)

    val equity: Long get() = valuationAmount + cashAmount

    val running: Boolean get() = state != AccountState.WAITING
}

/** 수량·단가는 종목 통화, 금액은 원화. 환산은 [fxRate]가 한다. */
internal data class FakeHolding(
    val name: String,
    val market: Market,
    val quantity: Int,
    val averagePrice: Double,
    val currentPrice: Double,
) {
    val purchaseAmount: Long get() = (quantity * averagePrice * market.fxRate()).roundToLong()

    val valuationAmount: Long get() = (quantity * currentPrice * market.fxRate()).roundToLong()
}

internal data class FakeRun(
    val id: String,
    val strategyKey: String,
    val strategyName: String,
    val version: String,
    val accountId: String,
    val accountName: String,
    val startedAt: Instant,
    val endedAt: Instant?,
    val outcome: RunOutcome?,
    val pnl: Pnl,
)

internal fun pnlOf(amount: Long, base: Long): Pnl =
    Pnl(amount, if (base == 0L) 0.0 else amount * 100.0 / base)

internal fun FakeAccount.performance(): Performance? {
    if (!running || startedAt == null) return null
    return Performance(
        todayPnl = pnlOf(equity - dayStartEquity, dayStartEquity),
        cumulativePnl = pnlOf(equity - initialEquity, initialEquity),
        initialEquity = initialEquity,
        startedAt = startedAt,
    )
}

internal fun FakeAccount.toAccount(): Account {
    val performance = performance()
    return Account(
        id = id,
        displayName = displayName,
        state = state,
        strategy = strategy,
        todayPnl = performance?.todayPnl,
        cumulativePnl = performance?.cumulativePnl,
        pauseReason = null,
        orderAlertEnabled = orderAlertEnabled,
    )
}

internal fun FakeAccount.portfolio(): Portfolio = Portfolio(
    valuationAmount = valuationAmount,
    purchaseAmount = holdings.sumOf(FakeHolding::purchaseAmount),
    cashAmount = cashAmount,
    holdings = holdings.map(FakeHolding::toHolding),
)

internal fun FakeHolding.toHolding(): Holding = Holding(
    name = name,
    quantity = quantity.toDouble(),
    currentPrice = Price(currentPrice, market.currency()),
    averagePrice = Price(averagePrice, market.currency()),
    valuationAmount = valuationAmount,
    pnl = pnlOf(valuationAmount - purchaseAmount, purchaseAmount),
)

internal fun FakeHolding.toOrder(id: String, side: OrderSide): Order = Order(
    id = id,
    stockName = name,
    side = side,
    quantity = quantity.toDouble(),
    price = Price(currentPrice, market.currency()),
    amount = valuationAmount,
    realizedPnl = if (side == OrderSide.SELL) {
        pnlOf(valuationAmount - purchaseAmount, purchaseAmount)
    } else {
        null
    },
)

internal fun FakeRun.toStrategyRun(now: Instant): StrategyRun = StrategyRun(
    id = id,
    strategyName = strategyName,
    version = version,
    accountId = accountId,
    accountName = accountName,
    outcome = outcome ?: RunOutcome.RUNNING,
    hours = Duration.between(startedAt, endedAt ?: now).toHours(),
    pnl = pnl,
)

internal fun FakeWorld.account(accountId: String): FakeAccount? =
    accounts.firstOrNull { it.id == accountId }

internal fun FakeWorld.withAccount(accountId: String, block: (FakeAccount) -> FakeAccount): FakeWorld =
    copy(accounts = accounts.map { if (it.id == accountId) block(it) else it })

internal fun FakeWorld.withOrder(accountId: String, build: (String) -> Order): FakeWorld =
    copy(nextOrder = nextOrder + 1).withAccount(accountId) { account ->
        account.copy(
            orders = (listOf(build("ord-$nextOrder")) + account.orders).take(ORDER_LIMIT),
            orderCount = account.orderCount + 1,
        )
    }
