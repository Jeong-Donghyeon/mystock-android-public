package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.OrderPage
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Strategy

data class HomeData(
    val accounts: List<Account>,
    val strategies: List<Strategy>,
    val performance: Performance?,
    val portfolio: Portfolio?,
    val orders: List<Order>,
    val orderCount: Long,
    val partial: Boolean,
)

/** 계좌 목록이 없으면 그릴 화면이 없어 실패다. 나머지는 받은 것으로 채우고 하나라도 빠졌으면 partial이다. */
internal fun assembleHomeData(
    accounts: Result<List<Account>>,
    strategies: Result<List<Strategy>>,
    performance: Result<Performance?>,
    portfolio: Result<Portfolio>,
    orders: Result<OrderPage>,
): Result<HomeData> = accounts.map { list ->
    HomeData(
        accounts = list,
        strategies = strategies.getOrDefault(emptyList()),
        performance = performance.getOrNull(),
        portfolio = portfolio.getOrNull(),
        orders = orders.getOrNull()?.orders.orEmpty(),
        orderCount = orders.getOrNull()?.totalCount ?: 0,
        partial = listOf(strategies, performance, portfolio, orders).any { it.isFailure },
    )
}
