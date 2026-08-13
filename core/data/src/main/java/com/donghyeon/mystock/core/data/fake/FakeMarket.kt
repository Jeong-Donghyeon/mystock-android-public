package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.RunOutcome
import java.time.Instant
import kotlin.random.Random

private const val DRIFT = 0.004
private const val MAX_HOLDINGS = 6
private const val BUY_CHANCE = 0.4
private const val SELL_CHANCE = 0.15
private const val BUY_CASH_RATIO = 0.25

internal fun FakeWorld.ticked(now: Instant, random: Random = Random): FakeWorld =
    accounts.map(FakeAccount::id).fold(this) { world, id -> world.tick(id, now, random) }

private fun FakeWorld.tick(accountId: String, now: Instant, random: Random): FakeWorld {
    val account = account(accountId)?.takeIf(FakeAccount::running) ?: return this
    val drifted = withAccount(accountId) { it.copy(holdings = it.holdings.map { h -> h.drifted(random) }) }
    return when (account.state) {
        AccountState.CLOSING -> drifted.close(accountId, now)
        AccountState.LIQUIDATING -> drifted.liquidate(accountId, now)
        AccountState.RUNNING -> drifted.trade(accountId, random)
        else -> drifted
    }.syncRun(accountId)
}

private fun FakeHolding.drifted(random: Random): FakeHolding =
    copy(currentPrice = currentPrice * (1 + random.nextDouble(-DRIFT, DRIFT)))

// 회수 중은 한 종목씩 팔고, 다 팔린 다음 틱에 대기가 된다
private fun FakeWorld.close(accountId: String, now: Instant): FakeWorld {
    val holding = account(accountId)?.holdings?.firstOrNull()
        ?: return finish(accountId, RunOutcome.CLOSED, now)
    return sell(accountId, holding)
}

// 청산 중은 다음 틱에 전량 시장가로 팔리고(장이 열린 셈), 다 팔린 다음 틱에 대기가 된다
private fun FakeWorld.liquidate(accountId: String, now: Instant): FakeWorld {
    val holdings = account(accountId)?.holdings.orEmpty()
    if (holdings.isEmpty()) return finish(accountId, RunOutcome.LIQUIDATED, now)
    return holdings.fold(this) { world, holding -> world.sell(accountId, holding) }
}

private fun FakeWorld.trade(accountId: String, random: Random): FakeWorld {
    val account = account(accountId)
        ?.takeIf { it.strategy != null && it.strategy.key != EMPTY_STRATEGY_KEY }
        ?: return this
    val sold = account.holdings
        .randomOrNull(random)
        ?.takeIf { random.nextDouble() < SELL_CHANCE }
        ?.let { sell(accountId, it) }
        ?: this
    return sold.buy(accountId, random)
}

private fun FakeWorld.buy(accountId: String, random: Random): FakeWorld {
    val holding = account(accountId)
        ?.takeIf { it.holdings.size < MAX_HOLDINGS && random.nextDouble() < BUY_CHANCE }
        ?.nextBuy(random)
        ?: return this
    return withAccount(accountId) {
        it.copy(
            cashAmount = it.cashAmount - holding.purchaseAmount,
            holdings = it.holdings + holding,
        )
    }.withOrder(accountId) { id -> holding.toOrder(id, OrderSide.BUY) }
}

private fun FakeAccount.nextBuy(random: Random): FakeHolding? {
    val held = holdings.map(FakeHolding::name).toSet()
    val tradable = strategy?.markets.orEmpty().filter { it in markets }
    val stock = fakeStocks
        .filter { it.market in tradable && it.name !in held }
        .randomOrNull(random)
        ?: return null
    val quantity = (cashAmount * BUY_CASH_RATIO / (stock.price * stock.market.fxRate())).toInt()
    return FakeHolding(stock.name, stock.market, quantity, stock.price, stock.price)
        .takeIf { quantity >= 1 }
}

internal fun FakeWorld.sell(accountId: String, holding: FakeHolding): FakeWorld =
    withAccount(accountId) {
        it.copy(
            cashAmount = it.cashAmount + holding.valuationAmount,
            holdings = it.holdings - holding,
        )
    }.withOrder(accountId) { id -> holding.toOrder(id, OrderSide.SELL) }

private fun FakeWorld.syncRun(accountId: String): FakeWorld {
    val account = account(accountId) ?: return this
    val pnl = account.performance()?.cumulativePnl
    return if (pnl == null) this else copy(runs = runs.map { if (it.id == account.runId) it.copy(pnl = pnl) else it })
}
