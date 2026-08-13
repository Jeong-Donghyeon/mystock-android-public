package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.core.model.StrategyRun
import com.donghyeon.mystock.core.network.dto.RunSummaryDto
import com.donghyeon.mystock.core.network.dto.RunsDto

internal fun RunsDto.toDomain(): AccountPerformance = AccountPerformance(
    accountName = accountName,
    runs = runs.map { it.toDomain() },
)

internal fun RunSummaryDto.toDomain(): StrategyRun = StrategyRun(
    id = id,
    strategyName = strategyName,
    version = version,
    accountId = accountId,
    accountName = accountName,
    outcome = outcome.toEnum(),
    hours = hours.toLong(),
    pnl = pnl.toDomain(),
)
