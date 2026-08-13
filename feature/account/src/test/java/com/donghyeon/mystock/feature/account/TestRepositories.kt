package com.donghyeon.mystock.feature.account

import com.donghyeon.mystock.core.data.AccountRepository
import com.donghyeon.mystock.core.data.HomeData
import com.donghyeon.mystock.core.data.PerformanceRepository
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Strategy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

class TestAccountRepository : AccountRepository {
    var accountsResult: Result<List<Account>> = Result.success(emptyList())
    var performanceGate: CompletableDeferred<Unit>? = null
    var homeDataFailure: Throwable? = null
    var homeDataRetryGate: CompletableDeferred<Unit>? = null
    var performanceResults: Map<String, Result<Performance>> = emptyMap()
    var portfolioResults: Map<String, Result<Portfolio>> = emptyMap()
    var ordersResults: Map<String, Result<List<Order>>> = emptyMap()
    var strategies: List<Strategy> = emptyList()
    var commandResult: Result<Unit> = Result.success(Unit)
    var commandGate: CompletableDeferred<Unit>? = null
    var orderAlertResult: Result<Unit> = Result.success(Unit)

    var getAccountsCallCount: Int = 0
        private set
    var homeDataSubscriptions: Int = 0
        private set
    val commands = mutableListOf<Pair<String, String>>()
    var onCommand: ((accountId: String) -> Unit)? = null
    val orderAlerts = mutableListOf<Pair<String, Boolean>>()

    override suspend fun getAccounts(): Result<List<Account>> {
        getAccountsCallCount++
        return accountsResult
    }

    override fun observeHomeData(accountId: String): Flow<Result<HomeData>> = flow {
        homeDataSubscriptions++
        performanceGate?.await()
        emit(homeDataEmission(accountId))
        homeDataRetryGate?.let { gate ->
            gate.await()
            emit(homeDataEmission(accountId))
        }
    }

    private fun homeDataEmission(accountId: String): Result<HomeData> =
        homeDataFailure?.let { Result.failure(it) } ?: homeDataResult(accountId)

    private fun homeDataResult(accountId: String): Result<HomeData> =
        accountsResult.map { accounts ->
            val performance = performanceResults[accountId]
            val portfolio = portfolioResults[accountId]
            val orders = ordersResults[accountId]
            HomeData(
                accounts = accounts,
                strategies = strategies,
                performance = performance?.getOrNull(),
                portfolio = portfolio?.getOrNull(),
                orders = orders?.getOrNull().orEmpty(),
                orderCount = orders?.getOrNull().orEmpty().size.toLong(),
                partial = listOf(performance, portfolio, orders).any { it == null || it.isFailure },
            )
        }

    override suspend fun pauseAccount(accountId: String): Result<Unit> = command(accountId, "pause")

    override suspend fun resumeAccount(accountId: String): Result<Unit> =
        command(accountId, "resume")

    override suspend fun closeAccount(accountId: String): Result<Unit> = command(accountId, "close")

    override suspend fun liquidateAccount(accountId: String): Result<Unit> =
        command(accountId, "liquidate")

    override suspend fun setOrderAlert(accountId: String, enabled: Boolean): Result<Unit> {
        orderAlerts += accountId to enabled
        return orderAlertResult
    }

    private suspend fun command(accountId: String, name: String): Result<Unit> {
        commands += accountId to name
        onCommand?.invoke(accountId)
        commandGate?.await()
        return commandResult
    }
}

class TestPerformanceRepository : PerformanceRepository {
    var performanceResult: Result<AccountPerformance> = Result.success(AccountPerformance("계좌 acc-1", emptyList()))
    val requests = mutableListOf<String>()

    override suspend fun getAccountPerformance(accountId: String): Result<AccountPerformance> {
        requests += accountId
        return performanceResult
    }
}

fun testAccount(
    id: String,
    state: AccountState = AccountState.RUNNING,
    orderAlertEnabled: Boolean = false,
    pauseReason: String? = null,
): Account = Account(
    id = id,
    displayName = "계좌 $id",
    state = state,
    strategy = null,
    todayPnl = null,
    cumulativePnl = null,
    pauseReason = pauseReason,
    orderAlertEnabled = orderAlertEnabled,
)

fun testPerformance(): Performance = Performance(
    todayPnl = Pnl(amount = 25_300, percent = 0.39),
    cumulativePnl = Pnl(amount = 983_259, percent = 17.93),
    initialEquity = 5_483_000,
    startedAt = Instant.parse("2026-07-12T00:00:00Z"),
)

fun testPortfolio(): Portfolio = Portfolio(
    valuationAmount = 4_326_259,
    purchaseAmount = 4_292_889,
    cashAmount = 2_140_000,
    holdings = emptyList(),
)
