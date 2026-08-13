package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.OrderPage
import com.donghyeon.mystock.core.network.dto.OrderDto
import com.donghyeon.mystock.core.network.dto.OrderPageDto

internal fun OrderPageDto.toDomain(): OrderPage = OrderPage(
    totalCount = totalCount,
    orders = orders.map { it.toDomain() },
)

internal fun OrderDto.toDomain(): Order = Order(
    id = id,
    stockName = stockName,
    side = side.toEnum(),
    quantity = quantity,
    price = price.toDomain(),
    amount = amount,
    realizedPnl = realizedPnl?.toDomain(),
)
