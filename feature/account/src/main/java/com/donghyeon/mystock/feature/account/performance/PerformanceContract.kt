package com.donghyeon.mystock.feature.account.performance

import androidx.annotation.StringRes
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.StrategyRun
import com.donghyeon.mystock.feature.common.RunSummary
import com.donghyeon.mystock.feature.common.summarize

sealed interface PerformanceNavTarget {
    data object Back : PerformanceNavTarget
}

sealed interface PerformanceContent {
    data object Loading : PerformanceContent

    data object Failed : PerformanceContent

    data class Ready(val performance: AccountPerformance) : PerformanceContent
}

data class PerformanceUiState(
    val content: PerformanceContent = PerformanceContent.Loading,
    val refreshing: Boolean = false,
) {
    val performance: AccountPerformance? get() = (content as? PerformanceContent.Ready)?.performance

    val summary: RunSummary? get() = summarize(performance?.runs.orEmpty())
}

sealed interface PerformanceUiIntent {
    data class Navigate(val target: PerformanceNavTarget) : PerformanceUiIntent

    data object Refresh : PerformanceUiIntent
}

sealed interface PerformanceUiEffect {
    data class Navigate(val target: PerformanceNavTarget) : PerformanceUiEffect

    data class ShowMessage(@StringRes val message: Int) : PerformanceUiEffect
}

internal val samplePerformanceHistory = AccountPerformance(
    accountName = "토스증권",
    runs = listOf(
        StrategyRun("run-4", "이동평균 교차", "0.2.0", "acc-1", "토스증권", RunOutcome.RUNNING, 17, Pnl(983_259, 17.93)),
        StrategyRun("run-3", "이동평균 교차", "0.1.9", "acc-1", "토스증권", RunOutcome.LIQUIDATED, 360, Pnl(412_000, 7.80)),
        StrategyRun("run-2", "변동성 돌파", "0.3.1", "acc-1", "토스증권", RunOutcome.CLOSED, 288, Pnl(-128_500, -2.41)),
        StrategyRun("run-1", "빈 전략 (검증용)", "1.0.0", "acc-1", "토스증권", RunOutcome.LIQUIDATED, 24, Pnl(0, 0.0)),
    ),
)

internal val sampleReadyState = PerformanceUiState(content = PerformanceContent.Ready(samplePerformanceHistory))

internal val sampleEmptyState = PerformanceUiState(
    content = PerformanceContent.Ready(samplePerformanceHistory.copy(runs = emptyList())),
)

internal val sampleLoadingState = PerformanceUiState()

internal val sampleFailedState = PerformanceUiState(content = PerformanceContent.Failed)
