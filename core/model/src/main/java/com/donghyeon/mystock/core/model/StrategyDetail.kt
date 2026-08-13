package com.donghyeon.mystock.core.model

import java.time.LocalDate

data class StrategyDetail(
    val strategy: Strategy,
    val note: String,
    val versions: List<StrategyVersion>,
    val runs: List<StrategyRun>,
    val startAccount: StartAccount?
)

data class StrategyVersion(
    val version: String,
    val releasedAt: LocalDate,
    val note: String
)

data class StrategyRun(
    val id: String,
    val strategyName: String,
    val version: String,
    val accountId: String,
    val accountName: String,
    val outcome: RunOutcome,
    val hours: Long,
    val pnl: Pnl
)

enum class RunOutcome { RUNNING, CLOSED, LIQUIDATED }

data class AccountPerformance(
    val accountName: String,
    val runs: List<StrategyRun>
)

data class StartAccount(
    val id: String,
    val name: String,
    val cashAmount: Long
)
