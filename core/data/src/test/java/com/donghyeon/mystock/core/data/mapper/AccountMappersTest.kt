package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.core.network.dto.AccountDto
import com.donghyeon.mystock.core.network.dto.PnlDto
import com.donghyeon.mystock.core.network.dto.PriceDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AccountMappersTest {

    private fun account(state: String = "RUNNING", pauseReason: String? = null) = AccountDto(
        id = "toss-live",
        displayName = "토스증권",
        state = state,
        strategy = testStrategyDto(),
        todayPnl = PnlDto(25_300, 0.39),
        cumulativePnl = PnlDto(983_259, 17.93),
        pauseReason = pauseReason,
        orderAlertEnabled = true,
    )

    @Test
    fun `계좌는 상태와 전략과 손익을 그대로 옮긴다`() {
        val account = account(state = "PAUSED", pauseReason = "브로커 오류").toDomain()

        assertEquals(AccountState.PAUSED, account.state)
        assertEquals("브로커 오류", account.pauseReason)
        assertEquals(listOf(Market.KR, Market.US), account.strategy?.markets)
        assertEquals(Pnl(25_300, 0.39), account.todayPnl)
        assertEquals(Pnl(983_259, 17.93), account.cumulativePnl)
    }

    @Test
    fun `대기 계좌의 전략과 손익은 null이다`() {
        val account = account(state = "WAITING")
            .copy(strategy = null, todayPnl = null, cumulativePnl = null)
            .toDomain()

        assertEquals(AccountState.WAITING, account.state)
        assertNull(account.strategy)
        assertNull(account.todayPnl)
        assertNull(account.cumulativePnl)
    }

    @Test
    fun `모르는 상태는 실패다`() {
        assertThrows(IllegalArgumentException::class.java) { account(state = "HALTED").toDomain() }
    }

    @Test
    fun `가격은 통화 코드로 통화를 정한다`() {
        assertEquals(Price(231.40, Currency.USD), PriceDto(231.40, "USD").toDomain())
        assertEquals(Price(71_200.0, Currency.KRW), PriceDto(71_200.0, "KRW").toDomain())
        assertThrows(IllegalArgumentException::class.java) { PriceDto(1.0, "JPY").toDomain() }
    }
}
