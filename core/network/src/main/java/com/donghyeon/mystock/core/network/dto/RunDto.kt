package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RunsDto(
    val accountName: String,
    val runs: List<RunSummaryDto>,
)

@Serializable
data class RunSummaryDto(
    val id: String,
    val strategyName: String,
    val version: String,
    val accountId: String,
    val accountName: String,
    val outcome: String,
    val hours: Int,
    val pnl: PnlDto,
)
