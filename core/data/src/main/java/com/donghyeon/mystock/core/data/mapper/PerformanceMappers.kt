package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.network.dto.PerformanceDto
import java.time.Instant

internal fun PerformanceDto.toDomain(): Performance = Performance(
    todayPnl = todayPnl?.toDomain(),
    cumulativePnl = cumulativePnl.toDomain(),
    initialEquity = initialEquity,
    startedAt = Instant.parse(startedAt),
)
