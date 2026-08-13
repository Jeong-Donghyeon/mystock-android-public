package com.donghyeon.mystock.feature.strategy.versions

import androidx.annotation.StringRes
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.feature.strategy.sampleDetail

sealed interface StrategyVersionsNavTarget {
    data object Back : StrategyVersionsNavTarget
}

sealed interface StrategyVersionsContent {
    data object Loading : StrategyVersionsContent

    data object Failed : StrategyVersionsContent

    data class Ready(val detail: StrategyDetail) : StrategyVersionsContent
}

data class VersionRunSummary(val count: Int, val averageReturn: Double)

data class StrategyVersionsUiState(
    val content: StrategyVersionsContent = StrategyVersionsContent.Loading,
    val refreshing: Boolean = false,
) {
    val detail: StrategyDetail? get() = (content as? StrategyVersionsContent.Ready)?.detail

    fun runSummaryOf(version: String): VersionRunSummary? = detail?.runs.orEmpty()
        .filter { it.version == version }
        .takeIf { it.isNotEmpty() }
        ?.let { runs -> VersionRunSummary(runs.size, runs.map { it.pnl.percent }.average()) }
}

sealed interface StrategyVersionsUiIntent {
    data class Navigate(val target: StrategyVersionsNavTarget) : StrategyVersionsUiIntent

    data object Refresh : StrategyVersionsUiIntent
}

sealed interface StrategyVersionsUiEffect {
    data class Navigate(val target: StrategyVersionsNavTarget) : StrategyVersionsUiEffect

    data class ShowMessage(@StringRes val message: Int) : StrategyVersionsUiEffect
}

internal val sampleReadyState = StrategyVersionsUiState(content = StrategyVersionsContent.Ready(sampleDetail))

internal val sampleLoadingState = StrategyVersionsUiState()

internal val sampleFailedState = StrategyVersionsUiState(content = StrategyVersionsContent.Failed)
