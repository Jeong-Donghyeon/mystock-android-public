package com.donghyeon.mystock.feature.strategy.strategies

import androidx.annotation.StringRes
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.feature.common.sampleStrategies

sealed interface StrategiesNavTarget {
    data object Back : StrategiesNavTarget

    data class Detail(val strategyKey: String) : StrategiesNavTarget
}

sealed interface StrategiesContent {
    data object Loading : StrategiesContent

    data object Failed : StrategiesContent

    data class Ready(val strategies: List<Strategy>) : StrategiesContent
}

data class StrategiesUiState(
    val content: StrategiesContent = StrategiesContent.Loading,
    val refreshing: Boolean = false,
)

sealed interface StrategiesUiIntent {
    data class Navigate(val target: StrategiesNavTarget) : StrategiesUiIntent

    data object Refresh : StrategiesUiIntent
}

sealed interface StrategiesUiEffect {
    data class Navigate(val target: StrategiesNavTarget) : StrategiesUiEffect

    data class ShowMessage(@StringRes val message: Int) : StrategiesUiEffect
}

internal val sampleReadyState = StrategiesUiState(content = StrategiesContent.Ready(sampleStrategies))

internal val sampleLoadingState = StrategiesUiState()

internal val sampleFailedState = StrategiesUiState(content = StrategiesContent.Failed)
