package com.donghyeon.mystock.core.model

data class Portfolio(
    val valuationAmount: Long,
    val purchaseAmount: Long,
    val cashAmount: Long,
    val holdings: List<Holding>
)

data class Holding(
    val name: String,
    val quantity: Double,
    val currentPrice: Price,
    val averagePrice: Price,
    val valuationAmount: Long,
    val pnl: Pnl
)
