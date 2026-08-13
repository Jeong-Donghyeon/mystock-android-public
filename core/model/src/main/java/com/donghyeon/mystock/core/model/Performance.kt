package com.donghyeon.mystock.core.model

import java.time.Instant

data class Performance(
    val todayPnl: Pnl?,
    val cumulativePnl: Pnl,
    val initialEquity: Long,
    val startedAt: Instant
)
