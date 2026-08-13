package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.network.dto.RunsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RunMappersTest {

    @Test
    fun `운용 한 건은 종료 사유와 시간과 손익을 옮긴다`() {
        val run = testRunDto(outcome = "LIQUIDATED").toDomain()

        assertEquals(RunOutcome.LIQUIDATED, run.outcome)
        assertEquals(17L, run.hours)
        assertEquals(Pnl(983_259, 17.93), run.pnl)
        assertEquals("토스증권", run.accountName)
    }

    @Test
    fun `모르는 종료 사유는 실패다`() {
        assertThrows(IllegalArgumentException::class.java) { testRunDto(outcome = "ABORTED").toDomain() }
    }

    @Test
    fun `운용 내역 응답은 계좌 이름과 서버가 내려준 순서 그대로의 운용 목록이다`() {
        val performance = RunsDto(
            accountName = "토스증권",
            runs = listOf(testRunDto().copy(id = "run-5"), testRunDto(outcome = "CLOSED").copy(id = "run-4")),
        ).toDomain()

        assertEquals("토스증권", performance.accountName)
        assertEquals(listOf("run-5", "run-4"), performance.runs.map { it.id })
    }
}
