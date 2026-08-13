package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.StrategyCondition
import com.donghyeon.mystock.core.model.StrategyVersion
import com.donghyeon.mystock.core.network.dto.StartAccountDto
import com.donghyeon.mystock.core.network.dto.StrategyDetailDto
import com.donghyeon.mystock.core.network.dto.StrategyVersionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeParseException

class StrategyMappersTest {

    private fun detail(startAccount: StartAccountDto? = null) = StrategyDetailDto(
        strategy = testStrategyDto(),
        note = "설명",
        versions = listOf(StrategyVersionDto("0.2.0", "2026-07-14", "변경")),
        runs = listOf(testRunDto()),
        startAccount = startAccount,
    )

    @Test
    fun `전략은 시장 코드를 enum으로 바꾸고 조건은 문자열 그대로 나열한다`() {
        val strategy = testStrategyDto().toDomain()

        assertEquals("ma-cross", strategy.key)
        assertEquals(listOf(Market.KR, Market.US), strategy.markets)
        assertEquals(listOf(StrategyCondition("매수", "5일선이 20일선을 상향 돌파")), strategy.conditions)
    }

    @Test
    fun `모르는 시장은 실패다`() {
        assertThrows(IllegalArgumentException::class.java) { testStrategyDto(markets = listOf("JP")).toDomain() }
    }

    @Test
    fun `전략 상세는 배포일을 날짜로 바꾸고 시작 계좌를 그대로 옮긴다`() {
        val detail = detail(StartAccountDto("kis-mock", "한국투자증권", 5_483_000)).toDomain()

        assertEquals(listOf(StrategyVersion("0.2.0", LocalDate.of(2026, 7, 14), "변경")), detail.versions)
        assertEquals("run-5", detail.runs.single().id)
        assertEquals(StartAccount("kis-mock", "한국투자증권", 5_483_000), detail.startAccount)
    }

    @Test
    fun `시작 계좌가 없으면 null이다`() {
        assertNull(detail().toDomain().startAccount)
    }

    @Test
    fun `배포일이 ISO 날짜가 아니면 실패다`() {
        assertThrows(DateTimeParseException::class.java) { StrategyVersionDto("0.2.0", "26.07.14", "변경").toDomain() }
    }
}
