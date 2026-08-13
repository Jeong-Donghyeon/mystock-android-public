package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.Strategy
import java.time.Instant

// 계약: 대기 상태가 아닌 계좌면 startAccount는 null이다
internal fun FakeAccount.toStartAccount(): StartAccount? =
    takeIf { state == AccountState.WAITING }?.let { StartAccount(id = id, name = displayName, cashAmount = cashAmount) }

internal fun FakeWorld.start(accountId: String, strategy: Strategy, now: Instant): FakeWorld {
    val account = account(accountId) ?: return this
    val run = FakeRun(
        id = "run-$nextRun",
        strategyKey = strategy.key,
        strategyName = strategy.displayName,
        version = strategy.version,
        accountId = account.id,
        accountName = account.displayName,
        startedAt = now,
        endedAt = null,
        outcome = null,
        pnl = Pnl(0, 0.0),
    )
    return copy(nextRun = nextRun + 1, runs = listOf(run) + runs).withAccount(accountId) {
        it.copy(
            state = AccountState.RUNNING,
            strategy = strategy,
            runId = run.id,
            startedAt = now,
            initialEquity = it.equity,
            dayStartEquity = it.equity,
            orders = emptyList(),
            orderCount = 0,
        )
    }
}

internal fun FakeWorld.finish(accountId: String, outcome: RunOutcome, now: Instant): FakeWorld {
    val account = account(accountId) ?: return this
    val pnl = account.performance()?.cumulativePnl
    return copy(
        runs = runs.map {
            if (it.id == account.runId) it.copy(outcome = outcome, endedAt = now, pnl = pnl ?: it.pnl) else it
        },
    ).withAccount(accountId) {
        it.copy(
            state = AccountState.WAITING,
            strategy = null,
            runId = null,
            startedAt = null,
            orders = emptyList(),
            orderCount = 0,
        )
    }
}
