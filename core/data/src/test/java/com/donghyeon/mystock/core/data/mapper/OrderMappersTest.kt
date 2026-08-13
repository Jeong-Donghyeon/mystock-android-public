package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.network.dto.OrderDto
import com.donghyeon.mystock.core.network.dto.OrderPageDto
import com.donghyeon.mystock.core.network.dto.PnlDto
import com.donghyeon.mystock.core.network.dto.PriceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class OrderMappersTest {

    private fun order(side: String, realizedPnl: PnlDto? = null) = OrderDto(
        id = "ord-3",
        stockName = "삼성전자",
        side = side,
        quantity = 10.0,
        price = PriceDto(71_200.0, "KRW"),
        amount = 712_000,
        realizedPnl = realizedPnl,
    )

    @Test
    fun `매도 주문은 차익을 담고 매수 주문은 차익이 없다`() {
        val sell = order("SELL", PnlDto(28_000, 4.09)).toDomain()
        val buy = order("BUY").toDomain()

        assertEquals(OrderSide.SELL, sell.side)
        assertEquals(Pnl(28_000, 4.09), sell.realizedPnl)
        assertEquals(OrderSide.BUY, buy.side)
        assertNull(buy.realizedPnl)
    }

    @Test
    fun `모르는 주문 방향은 실패다`() {
        assertThrows(IllegalArgumentException::class.java) { order("SHORT").toDomain() }
    }

    @Test
    fun `주문 페이지는 전체 건수와 서버가 내려준 순서 그대로의 주문이다`() {
        val page = OrderPageDto(totalCount = 12, orders = listOf(order("SELL"), order("BUY"))).toDomain()

        assertEquals(12L, page.totalCount)
        assertEquals(listOf(OrderSide.SELL, OrderSide.BUY), page.orders.map { it.side })
    }
}
