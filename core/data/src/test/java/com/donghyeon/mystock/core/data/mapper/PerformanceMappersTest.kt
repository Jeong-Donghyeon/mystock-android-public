package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.network.dto.PerformanceDto
import com.donghyeon.mystock.core.network.dto.PnlDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant
import java.time.format.DateTimeParseException

class PerformanceMappersTest {

    private fun performance(
        todayPnl: PnlDto? = PnlDto(25_300, 0.39),
        startedAt: String = "2026-09-12T00:05:00Z",
    ) = PerformanceDto(
        todayPnl = todayPnl,
        cumulativePnl = PnlDto(983_259, 17.93),
        initialEquity = 5_483_000,
        startedAt = startedAt,
    )

    @Test
    fun `성과는 시작 시각을 Instant로 바꾸고 손익을 옮긴다`() {
        val performance = performance().toDomain()

        assertEquals(Instant.parse("2026-09-12T00:05:00Z"), performance.startedAt)
        assertEquals(Pnl(25_300, 0.39), performance.todayPnl)
        assertEquals(Pnl(983_259, 17.93), performance.cumulativePnl)
        assertEquals(5_483_000L, performance.initialEquity)
    }

    @Test
    fun `당일 손익은 스냅샷이 없으면 null이다`() {
        assertNull(performance(todayPnl = null).toDomain().todayPnl)
    }

    @Test
    fun `시작 시각이 ISO 형식이 아니면 실패다`() {
        assertThrows(DateTimeParseException::class.java) { performance(startedAt = "2026-09-12 00:05").toDomain() }
    }
}
