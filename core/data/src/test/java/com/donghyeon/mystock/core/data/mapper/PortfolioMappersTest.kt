package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.core.network.dto.HoldingDto
import com.donghyeon.mystock.core.network.dto.PnlDto
import com.donghyeon.mystock.core.network.dto.PortfolioDto
import com.donghyeon.mystock.core.network.dto.PriceDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PortfolioMappersTest {

    @Test
    fun `포트폴리오는 원화 금액과 현지 통화 가격의 보유 종목을 옮긴다`() {
        val portfolio = PortfolioDto(
            valuationAmount = 4_326_259,
            purchaseAmount = 4_292_889,
            cashAmount = 2_140_000,
            holdings = listOf(
                HoldingDto(
                    name = "AAPL",
                    quantity = 4.0,
                    currentPrice = PriceDto(231.40, "USD"),
                    averagePrice = PriceDto(224.10, "USD"),
                    valuationAmount = 1_242_618,
                    pnl = PnlDto(39_201, 3.26),
                ),
            ),
        ).toDomain()

        assertEquals(2_140_000L, portfolio.cashAmount)
        val holding = portfolio.holdings.single()
        assertEquals(4.0, holding.quantity, 0.0)
        assertEquals(Price(231.40, Currency.USD), holding.currentPrice)
        assertEquals(Price(224.10, Currency.USD), holding.averagePrice)
        assertEquals(Pnl(39_201, 3.26), holding.pnl)
    }
}
