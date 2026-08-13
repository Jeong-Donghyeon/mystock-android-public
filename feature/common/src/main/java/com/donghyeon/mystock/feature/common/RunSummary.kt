package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.StrategyRun

data class RunSummary(
    val totalPnlAmount: Long,
    val hours: Long,
    val winRatePercent: Double,
    val medianReturn: Double,
    val bestReturn: Double,
    val worstReturn: Double,
    val averageReturn: Double,
)

fun summarize(runs: List<StrategyRun>): RunSummary? {
    if (runs.isEmpty()) return null
    val returns = runs.map { it.pnl.percent }.sorted()
    val middle = returns.size / 2
    val median = if (returns.size % 2 == 0) (returns[middle - 1] + returns[middle]) / 2 else returns[middle]
    return RunSummary(
        totalPnlAmount = runs.sumOf { it.pnl.amount },
        hours = runs.sumOf { it.hours },
        winRatePercent = runs.count { it.pnl.amount > 0 } * PERCENT / runs.size,
        medianReturn = median,
        bestReturn = returns.last(),
        worstReturn = returns.first(),
        averageReturn = returns.average(),
    )
}

private const val PERCENT = 100.0

// 라벨은 화면이 stringResource로 풀고, 값은 여기서 포맷한다
fun summaryStats(summary: RunSummary): List<Pair<Int, String>> = listOf(
    R.string.common_running_hours to formatHours(summary.hours),
    R.string.common_win_rate to formatWinRate(summary.winRatePercent),
    R.string.common_median_return to formatSignedPercent(summary.medianReturn),
    R.string.common_best_return to formatSignedPercent(summary.bestReturn),
    R.string.common_worst_return to formatSignedPercent(summary.worstReturn),
    R.string.common_average_return to formatSignedPercent(summary.averageReturn),
)
