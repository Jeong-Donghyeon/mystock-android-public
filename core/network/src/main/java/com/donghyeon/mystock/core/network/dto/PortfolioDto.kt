package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PortfolioDto(
    val valuationAmount: Long,
    val purchaseAmount: Long,
    val cashAmount: Long,
    val holdings: List<HoldingDto>,
)

@Serializable
data class HoldingDto(
    val name: String,
    val quantity: Double,
    val currentPrice: PriceDto,
    val averagePrice: PriceDto,
    val valuationAmount: Long,
    val pnl: PnlDto,
)
