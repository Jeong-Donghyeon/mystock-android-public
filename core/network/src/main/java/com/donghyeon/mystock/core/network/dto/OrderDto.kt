package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val stockName: String,
    val side: String,
    val quantity: Double,
    val price: PriceDto,
    val amount: Long,
    val realizedPnl: PnlDto?,
)

@Serializable
data class OrderPageDto(
    val totalCount: Long,
    val orders: List<OrderDto>,
)
