package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StrategyDto(
    val key: String,
    val displayName: String,
    val description: String,
    val version: String,
    val markets: List<String>,
    val conditions: List<StrategyConditionDto>,
)

@Serializable
data class StrategyConditionDto(
    val label: String,
    val text: String,
)

@Serializable
data class StrategyDetailDto(
    val strategy: StrategyDto,
    val note: String,
    val versions: List<StrategyVersionDto>,
    val runs: List<RunSummaryDto>,
    val startAccount: StartAccountDto?,
)

@Serializable
data class StrategyVersionDto(
    val version: String,
    val releasedAt: String,
    val note: String,
)

@Serializable
data class StartAccountDto(
    val id: String,
    val name: String,
    val cashAmount: Long,
)
