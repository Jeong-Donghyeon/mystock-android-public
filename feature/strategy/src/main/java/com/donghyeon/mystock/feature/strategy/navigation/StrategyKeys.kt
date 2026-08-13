package com.donghyeon.mystock.feature.strategy.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object StrategiesKey : NavKey

@Serializable
data class StrategyDetailKey(val strategyKey: String, val accountId: String? = null) : NavKey

@Serializable
data class StrategyVersionsKey(val strategyKey: String) : NavKey
