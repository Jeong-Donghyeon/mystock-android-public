package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.data.fake.fakeStrategies
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.ApiException
import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.OrderPage
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Price
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Instant

class HomeAssemblyTest {

    private val accounts = listOf(
        Account("kis-mock", "한국투자증권", AccountState.WAITING, null, null, null, null, false),
    )
    private val strategies = fakeStrategies
    private val performance = Performance(Pnl(0, 0.0), Pnl(0, 0.0), 5_483_000, Instant.parse("2026-09-12T00:05:00Z"))
    private val portfolio = Portfolio(0, 0, 5_483_000, emptyList())
    private val orders = OrderPage(
        totalCount = 12,
        orders = listOf(Order("ord-1", "삼성전자", OrderSide.BUY, 10.0, Price(68_400.0, Currency.KRW), 684_000, null)),
    )

    @Test
    fun `전부 받으면 partial이 아니다`() {
        val data = assembleHomeData(
            accounts = Result.success(accounts),
            strategies = Result.success(strategies),
            performance = Result.success(performance),
            portfolio = Result.success(portfolio),
            orders = Result.success(orders),
        ).getOrThrow()

        assertFalse(data.partial)
        assertEquals(accounts, data.accounts)
        assertEquals(strategies, data.strategies)
        assertEquals(performance, data.performance)
        assertEquals(portfolio, data.portfolio)
        assertEquals(orders.orders, data.orders)
        assertEquals(12L, data.orderCount)
    }

    @Test
    fun `대기 계좌의 성과는 없는 것이지 실패가 아니다`() {
        val data = assembleHomeData(
            accounts = Result.success(accounts),
            strategies = Result.success(strategies),
            performance = Result.success(null),
            portfolio = Result.success(portfolio),
            orders = Result.success(orders),
        ).getOrThrow()

        assertNull(data.performance)
        assertFalse(data.partial)
    }

    @Test
    fun `계좌 목록이 실패하면 전체가 실패다`() {
        val offline = IOException("offline")

        val result = assembleHomeData(
            accounts = Result.failure(offline),
            strategies = Result.success(strategies),
            performance = Result.success(performance),
            portfolio = Result.success(portfolio),
            orders = Result.success(orders),
        )

        assertEquals(offline, result.exceptionOrNull())
    }

    @Test
    fun `일부만 실패하면 받은 것으로 채우고 partial로 알린다`() {
        val data = assembleHomeData(
            accounts = Result.success(accounts),
            strategies = Result.failure(IOException("offline")),
            performance = Result.success(performance),
            portfolio = Result.failure(ApiException("BROKER_ERROR", "브로커 응답을 처리하지 못했습니다")),
            orders = Result.failure(IOException("offline")),
        ).getOrThrow()

        assertTrue(data.partial)
        assertEquals(accounts, data.accounts)
        assertEquals(performance, data.performance)
        assertTrue(data.strategies.isEmpty())
        assertNull(data.portfolio)
        assertTrue(data.orders.isEmpty())
        assertEquals(0L, data.orderCount)
    }
}
