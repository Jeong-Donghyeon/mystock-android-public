package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceDto(
    val todayPnl: PnlDto?,
    val cumulativePnl: PnlDto,
    val initialEquity: Long,
    val startedAt: String,
)
