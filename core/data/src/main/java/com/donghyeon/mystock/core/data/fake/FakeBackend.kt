package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.data.HomeData
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.ApiException
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyDetail
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

internal const val FAKE_LATENCY_MS = 300L

private const val ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND"
private const val STRATEGY_NOT_FOUND = "STRATEGY_NOT_FOUND"
private const val INVALID_TRANSITION = "INVALID_TRANSITION"
private const val MARKET_NOT_SUPPORTED = "MARKET_NOT_SUPPORTED"

private fun accountNotFound() = ApiException(ACCOUNT_NOT_FOUND, "없는 계좌입니다")

internal fun strategyNotFound() = ApiException(STRATEGY_NOT_FOUND, "없는 전략입니다")

/**
 * 서버 대신 상태를 들고 있는 가짜 백엔드.
 * 세 리포지토리가 같은 상태를 보도록 한 곳에 모았고, 명령은 서버가 할 검증까지 그대로 한다.
 */
@Singleton
internal class FakeBackend @Inject constructor(private val clock: Clock) {

    private val mutex = Mutex()
    private var world: FakeWorld = seedWorld(Instant.now(clock))

    suspend fun accounts(): List<Account> = mutex.withLock {
        world.accounts.map(FakeAccount::toAccount)
    }

    suspend fun homeData(accountId: String): Result<HomeData> = mutex.withLock {
        val account = world.account(accountId)
            ?: return Result.failure(accountNotFound())
        Result.success(
            HomeData(
                accounts = world.accounts.map(FakeAccount::toAccount),
                strategies = fakeStrategies,
                performance = account.performance(),
                portfolio = account.portfolio(),
                orders = account.orders,
                orderCount = account.orderCount,
                partial = false,
            ),
        )
    }

    suspend fun tick() = mutex.withLock {
        world = world.ticked(Instant.now(clock))
    }

    // 전이 규칙은 원천 readme의 매매 상태 절을 따른다 — 서버 역할이라 허용되지 않는 전이는 거절한다
    suspend fun transition(accountId: String, to: AccountState, from: Set<AccountState>): Result<Unit> =
        mutex.withLock {
            val rejected = reject(accountId, from)
            if (rejected != null) return Result.failure(rejected)
            world = world.withAccount(accountId) { it.copy(state = to) }
            Result.success(Unit)
        }

    suspend fun setOrderAlert(accountId: String, enabled: Boolean): Result<Unit> = mutex.withLock {
        val rejected = reject(accountId, from = AccountState.entries.toSet())
        if (rejected != null) return Result.failure(rejected)
        world = world.withAccount(accountId) { it.copy(orderAlertEnabled = enabled) }
        Result.success(Unit)
    }

    // 시작은 대기 계좌에서만 — 전략 상세가 시작 버튼을 보이는 조건과 같은 규칙을 서버 쪽에도 둔다
    suspend fun startStrategy(strategyKey: String, accountId: String): Result<Unit> = mutex.withLock {
        val strategy = fakeStrategies.firstOrNull { it.key == strategyKey }
            ?: return Result.failure(strategyNotFound())
        val rejected = reject(accountId, from = setOf(AccountState.WAITING), strategy = strategy)
        if (rejected != null) return Result.failure(rejected)
        world = world.start(accountId, strategy, Instant.now(clock))
        Result.success(Unit)
    }

    fun strategies(): List<Strategy> = fakeStrategies

    suspend fun strategyDetail(key: String, accountId: String?): Result<StrategyDetail> = mutex.withLock {
        val strategy = fakeStrategies.firstOrNull { it.key == key }
            ?: return Result.failure(strategyNotFound())
        val account = accountId?.let { world.account(it) ?: return Result.failure(accountNotFound()) }
        val now = Instant.now(clock)
        Result.success(
            StrategyDetail(
                strategy = strategy,
                note = fakeNotes[key].orEmpty(),
                versions = fakeVersions[key].orEmpty(),
                runs = world.runs.filter { it.strategyKey == key }.map { it.toStrategyRun(now) },
                startAccount = account?.toStartAccount(),
            ),
        )
    }

    suspend fun accountPerformance(accountId: String): Result<AccountPerformance> = mutex.withLock {
        val account = world.account(accountId)
            ?: return Result.failure(accountNotFound())
        Result.success(
            AccountPerformance(
                accountName = account.displayName,
                runs = world.runs
                    .filter { it.accountId == accountId }
                    .map { it.toStrategyRun(Instant.now(clock)) },
            ),
        )
    }

    // 거절도 서버와 같은 모양이어야 화면이 인증 전후로 같게 움직인다.
    // 시작은 계좌의 브로커가 여는 시장과 전략의 시장이 하나도 겹치지 않아도 거절한다(계약)
    private fun reject(accountId: String, from: Set<AccountState>, strategy: Strategy? = null): ApiException? {
        val account = world.account(accountId) ?: return accountNotFound()
        return when {
            account.state !in from -> ApiException(INVALID_TRANSITION, "현재 상태에서 허용되지 않는 명령입니다")
            strategy != null && strategy.markets.none { it in account.markets } ->
                ApiException(MARKET_NOT_SUPPORTED, "계좌가 지원하지 않는 시장의 전략입니다")
            else -> null
        }
    }
}
