package com.donghyeon.mystock.design.component

import org.junit.Assert.assertEquals
import org.junit.Test

class PnlFormatTest {

    @Test
    fun `상승 금액은 상승 기호를 붙인다`() {
        assertEquals("▲ 128,400", formatPnl(128400.0, PnlFormat.AMOUNT))
    }

    @Test
    fun `하락 금액은 부호 대신 하락 기호로 표기한다`() {
        assertEquals("▼ 9,300", formatPnl(-9300.0, PnlFormat.AMOUNT))
    }

    @Test
    fun `보합 금액은 기호 없이 0으로 표기한다`() {
        assertEquals("0", formatPnl(0.0, PnlFormat.AMOUNT))
    }

    @Test
    fun `수익률은 기호 대신 부호로 갈음한다`() {
        assertEquals("+3.42%", formatPnl(3.42, PnlFormat.PERCENT))
        assertEquals("-1.08%", formatPnl(-1.08, PnlFormat.PERCENT))
    }

    @Test
    fun `보합 수익률은 부호 없이 표기한다`() {
        assertEquals("0.00%", formatPnl(0.0, PnlFormat.PERCENT))
    }

    @Test
    fun `수익률은 소수 둘째 자리까지 표기한다`() {
        assertEquals("+12.35%", formatPnl(12.3456, PnlFormat.PERCENT))
    }

    @Test
    fun `표기 자릿수에서 0이 되는 값은 보합으로 본다`() {
        assertEquals("0", formatPnl(0.4, PnlFormat.AMOUNT))
        assertEquals("0.00%", formatPnl(-0.004, PnlFormat.PERCENT))
        assertEquals(0.0, roundPnl(0.4, PnlFormat.AMOUNT), 0.0)
    }

    @Test
    fun `금액과 수익률을 한 줄로 묶는다`() {
        assertEquals("▲ 983,259 (+17.93%)", formatPnlPair(983259.0, 17.93))
        assertEquals("0 (0.00%)", formatPnlPair(0.0, 0.0))
    }
}
