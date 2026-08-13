package com.donghyeon.mystock.core.model

enum class OrderSide { BUY, SELL }

data class Order(
    val id: String,
    val stockName: String,
    val side: OrderSide,
    val quantity: Double,
    val price: Price,
    val amount: Long,
    val realizedPnl: Pnl?
)

data class OrderPage(
    val totalCount: Long,
    val orders: List<Order>
)
