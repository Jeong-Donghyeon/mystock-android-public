package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyCondition
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.core.model.StrategyVersion
import com.donghyeon.mystock.core.network.dto.StartAccountDto
import com.donghyeon.mystock.core.network.dto.StrategyConditionDto
import com.donghyeon.mystock.core.network.dto.StrategyDetailDto
import com.donghyeon.mystock.core.network.dto.StrategyDto
import com.donghyeon.mystock.core.network.dto.StrategyVersionDto
import java.time.LocalDate

internal fun StrategyDto.toDomain(): Strategy = Strategy(
    key = key,
    displayName = displayName,
    description = description,
    version = version,
    markets = markets.map { it.toEnum() },
    conditions = conditions.map { it.toDomain() },
)

internal fun StrategyConditionDto.toDomain(): StrategyCondition = StrategyCondition(label = label, text = text)

internal fun StrategyDetailDto.toDomain(): StrategyDetail = StrategyDetail(
    strategy = strategy.toDomain(),
    note = note,
    versions = versions.map { it.toDomain() },
    runs = runs.map { it.toDomain() },
    startAccount = startAccount?.toDomain(),
)

internal fun StrategyVersionDto.toDomain(): StrategyVersion = StrategyVersion(
    version = version,
    releasedAt = LocalDate.parse(releasedAt),
    note = note,
)

internal fun StartAccountDto.toDomain(): StartAccount = StartAccount(id = id, name = name, cashAmount = cashAmount)
