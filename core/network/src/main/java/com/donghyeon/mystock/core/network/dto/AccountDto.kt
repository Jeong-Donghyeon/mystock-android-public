package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: String,
    val displayName: String,
    val state: String,
    val strategy: StrategyDto?,
    val todayPnl: PnlDto?,
    val cumulativePnl: PnlDto?,
    val pauseReason: String?,
    val orderAlertEnabled: Boolean,
)

@Serializable
data class PnlDto(
    val amount: Long,
    val percent: Double,
)

@Serializable
data class PriceDto(
    val value: Double,
    val currency: String,
)

@Serializable
data class StartRequestDto(
    val strategyKey: String,
)
