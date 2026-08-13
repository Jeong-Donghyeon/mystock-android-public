package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Price
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FormatsTest {

    @Test
    fun `원화 금액은 기호 없이 천 단위 구분자만 붙인다`() {
        assertEquals("1,234,567", formatAmount(1_234_567))
    }

    @Test
    fun `원화 가격은 정수이고 달러 가격은 기호와 소수 둘째 자리다`() {
        assertEquals("71,200", formatPrice(Price(71_200.4, Currency.KRW)))
        assertEquals("$189.99", formatPrice(Price(189.99, Currency.USD)))
    }

    @Test
    fun `수량과 가격은 가운뎃점으로 잇는다`() {
        assertEquals("3주 · $189.99", formatPosition(3.0, Price(189.99, Currency.USD)))
    }

    @Test
    fun `소수점 수량은 끝자리 0만 턴다`() {
        assertEquals("0.5주 · $189.99", formatPosition(0.5, Price(189.99, Currency.USD)))
        assertEquals("1.25주 · $189.99", formatPosition(1.250, Price(189.99, Currency.USD)))
        assertEquals("100주 · $189.99", formatPosition(100.0, Price(189.99, Currency.USD)))
    }

    @Test
    fun `단위는 값에 붙인다`() {
        assertEquals("17h", formatHours(17))
        assertEquals("v1.2.0", formatVersion("1.2.0"))
    }

    @Test
    fun `수익률 요약은 부호를 붙이고 소수 둘째 자리다`() {
        assertEquals("+7.80%", formatSignedPercent(7.8))
        assertEquals("-2.00%", formatSignedPercent(-2.0))
        assertEquals("0.00%", formatSignedPercent(0.0))
    }

    @Test
    fun `승률은 정수 퍼센트다`() {
        assertEquals("100%", formatWinRate(100.0))
        assertEquals("67%", formatWinRate(66.7))
    }

    @Test
    fun `날짜는 YY점MM점DD다`() {
        assertEquals("26.07.14", formatDate(LocalDate.of(2026, 7, 14)))
    }

    @Test
    fun `시장 칩 라벨은 시장 코드다`() {
        assertEquals("KR", marketLabel(Market.KR))
        assertEquals("US", marketLabel(Market.US))
    }
}
