package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Holding
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.network.dto.HoldingDto
import com.donghyeon.mystock.core.network.dto.PortfolioDto

internal fun PortfolioDto.toDomain(): Portfolio = Portfolio(
    valuationAmount = valuationAmount,
    purchaseAmount = purchaseAmount,
    cashAmount = cashAmount,
    holdings = holdings.map { it.toDomain() },
)

internal fun HoldingDto.toDomain(): Holding = Holding(
    name = name,
    quantity = quantity,
    currentPrice = currentPrice.toDomain(),
    averagePrice = averagePrice.toDomain(),
    valuationAmount = valuationAmount,
    pnl = pnl.toDomain(),
)
