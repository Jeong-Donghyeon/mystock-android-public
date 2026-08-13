package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import java.time.Duration
import java.time.Instant

// 서버의 계좌 id와 같아야 인증 전후로 갈아탈 때 고른 계좌가 이어진다
private const val TOSS = "toss-live"
private const val KIS = "kis-mock"
private const val ACTIVE_RUN = "run-5"
private const val SEEDED_TODAY_PNL = 25_300L
private const val SEEDED_CUMULATIVE_PNL = 983_259L

internal fun seedWorld(now: Instant): FakeWorld = FakeWorld(
    accounts = listOf(seedToss(now), seedKis()),
    runs = seedRuns(now),
    nextOrder = 3,
    nextRun = 6,
)

private fun seedToss(now: Instant): FakeAccount {
    val account = FakeAccount(
        id = TOSS,
        displayName = "토스증권",
        markets = setOf(Market.KR, Market.US),
        state = AccountState.RUNNING,
        strategy = fakeStrategies.last(),
        orderAlertEnabled = true,
        cashAmount = 2_140_000,
        holdings = listOf(
            FakeHolding("삼성전자", Market.KR, 10, 68_400.0, 71_200.0),
            FakeHolding("NAVER", Market.KR, 4, 196_000.0, 196_000.0),
            FakeHolding("AAPL", Market.US, 4, 224.10, 231.40),
            FakeHolding("MSFT", Market.US, 3, 402.60, 394.20),
        ),
        orders = listOf(
            FakeHolding("AAPL", Market.US, 4, 224.10, 224.10).toOrder("ord-2", OrderSide.BUY),
            FakeHolding("삼성전자", Market.KR, 10, 68_400.0, 68_400.0).toOrder("ord-1", OrderSide.BUY),
        ),
        orderCount = 2,
        runId = ACTIVE_RUN,
        startedAt = now.minus(Duration.ofHours(17)),
        initialEquity = 0,
        dayStartEquity = 0,
    )
    // 기준 평가액은 보고 싶은 손익에서 거꾸로 잡는다
    return account.copy(
        initialEquity = account.equity - SEEDED_CUMULATIVE_PNL,
        dayStartEquity = account.equity - SEEDED_TODAY_PNL,
    )
}

private fun seedKis(): FakeAccount = FakeAccount(
    id = KIS,
    displayName = "한국투자증권",
    markets = setOf(Market.KR),
    state = AccountState.WAITING,
    strategy = null,
    orderAlertEnabled = false,
    cashAmount = 5_483_000,
    holdings = emptyList(),
    orders = emptyList(),
    orderCount = 0,
    runId = null,
    startedAt = null,
    initialEquity = 0,
    dayStartEquity = 0,
)

private fun seedRuns(now: Instant): List<FakeRun> = runSeeds.map { it.toRun(now) }

private data class RunSeed(
    val id: String,
    val strategyKey: String,
    val version: String,
    val accountId: String,
    val startedHoursAgo: Long,
    val hours: Long,
    val outcome: RunOutcome?,
    val pnl: Pnl,
)

private val runSeeds = listOf(
    RunSeed(ACTIVE_RUN, "ma-cross", "0.2.0", TOSS, 17, 17, null, Pnl(983_259, 17.93)),
    RunSeed("run-4", "ma-cross", "0.1.9", TOSS, 408, 360, RunOutcome.LIQUIDATED, Pnl(412_000, 7.80)),
    RunSeed("run-3", "breakout", "0.3.1", TOSS, 788, 288, RunOutcome.CLOSED, Pnl(-128_500, -2.41)),
    RunSeed("run-2", EMPTY_STRATEGY_KEY, "1.0.0", TOSS, 924, 24, RunOutcome.LIQUIDATED, Pnl(0, 0.0)),
    RunSeed("run-1", "ma-cross", "0.1.0", KIS, 1_432, 432, RunOutcome.CLOSED, Pnl(180_000, 3.20)),
)

private fun RunSeed.toRun(now: Instant): FakeRun {
    val startedAt = now.minus(Duration.ofHours(startedHoursAgo))
    return FakeRun(
        id = id,
        strategyKey = strategyKey,
        strategyName = fakeStrategies.first { it.key == strategyKey }.displayName,
        version = version,
        accountId = accountId,
        accountName = if (accountId == TOSS) "토스증권" else "한국투자증권",
        startedAt = startedAt,
        endedAt = startedAt.plus(Duration.ofHours(hours)).takeIf { outcome != null },
        outcome = outcome,
        pnl = pnl,
    )
}
