package com.donghyeon.mystock.feature.strategy

import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyCondition
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.core.model.StrategyRun
import com.donghyeon.mystock.core.model.StrategyVersion
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDate

class TestStrategyRepository : StrategyRepository {
    var strategiesResult: Result<List<Strategy>> = Result.success(testStrategies())
    var detailResult: Result<StrategyDetail> = Result.success(testDetail())
    var startResult: Result<Unit> = Result.success(Unit)
    var detailGate: CompletableDeferred<Unit>? = null
    var startGate: CompletableDeferred<Unit>? = null

    var strategiesCallCount: Int = 0
        private set
    val detailRequests = mutableListOf<Pair<String, String?>>()
    val startRequests = mutableListOf<Pair<String, String>>()

    override suspend fun getStrategies(): Result<List<Strategy>> {
        strategiesCallCount++
        return strategiesResult
    }

    override suspend fun getStrategyDetail(key: String, accountId: String?): Result<StrategyDetail> {
        detailRequests += key to accountId
        detailGate?.await()
        return detailResult.map { detail ->
            detail.copy(startAccount = accountId?.let { testStartAccount(it) })
        }
    }

    override suspend fun startStrategy(key: String, accountId: String): Result<Unit> {
        startRequests += key to accountId
        startGate?.await()
        return startResult
    }
}

fun testStrategy(key: String = "ma-cross", version: String = "0.2.0"): Strategy = Strategy(
    key = key,
    displayName = "전략 $key",
    description = "설명 $key",
    version = version,
    markets = listOf(Market.KR, Market.US),
    conditions = listOf(StrategyCondition("매수", "조건 $key")),
)

fun testStrategies(): List<Strategy> = listOf(testStrategy("empty", "1.0.0"), testStrategy("ma-cross"))

fun testRun(
    id: String,
    version: String,
    pnl: Pnl,
    hours: Long,
    accountId: String = "acc-1",
): StrategyRun = StrategyRun(
    id = id,
    strategyName = "전략 ma-cross",
    version = version,
    accountId = accountId,
    accountName = "계좌 $accountId",
    outcome = RunOutcome.LIQUIDATED,
    hours = hours,
    pnl = pnl,
)

fun testDetail(): StrategyDetail = StrategyDetail(
    strategy = testStrategy(),
    note = "상세 설명",
    versions = listOf(
        StrategyVersion("0.2.0", LocalDate.of(2026, 7, 14), "변경"),
        StrategyVersion("0.1.9", LocalDate.of(2026, 6, 28), "조정"),
        StrategyVersion("0.1.5", LocalDate.of(2026, 6, 10), "추가"),
        StrategyVersion("0.1.0", LocalDate.of(2026, 5, 28), "최초 배포"),
    ),
    runs = listOf(
        testRun("run-3", "0.2.0", Pnl(983_259, 17.93), hours = 17).copy(outcome = RunOutcome.RUNNING),
        testRun("run-2", "0.1.9", Pnl(412_000, 7.80), hours = 360),
        testRun("run-1", "0.1.0", Pnl(180_000, 3.20), hours = 432, accountId = "acc-2"),
    ),
    startAccount = null,
)

fun testStartAccount(id: String = "acc-1"): StartAccount =
    StartAccount(id = id, name = "계좌 $id", cashAmount = 5_483_000)
