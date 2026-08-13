package com.donghyeon.mystock.core.model

data class Account(
    val id: String,
    val displayName: String,
    val state: AccountState,
    val strategy: Strategy?,
    val todayPnl: Pnl?,
    val cumulativePnl: Pnl?,
    val pauseReason: String?,
    val orderAlertEnabled: Boolean
)

data class Pnl(val amount: Long, val percent: Double)
