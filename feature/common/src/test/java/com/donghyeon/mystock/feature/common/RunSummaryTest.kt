package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.core.model.StrategyRun
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunSummaryTest {

    private fun run(id: String, amount: Long, percent: Double, hours: Long = 10) =
        StrategyRun(id, "전략", "0.1.0", "acc-1", "계좌", RunOutcome.CLOSED, hours, Pnl(amount, percent))

    @Test
    fun `요약 통계는 여섯 칸을 시안 순서로 만든다`() {
        val stats = summaryStats(summarize(listOf(run("a", 100, 7.8, 809)))!!)

        assertEquals(
            listOf(
                R.string.common_running_hours,
                R.string.common_win_rate,
                R.string.common_median_return,
                R.string.common_best_return,
                R.string.common_worst_return,
                R.string.common_average_return,
            ),
            stats.map { it.first },
        )
        assertEquals(listOf("809h", "100%", "+7.80%", "+7.80%", "+7.80%", "+7.80%"), stats.map { it.second })
    }

    @Test
    fun `운용이 없으면 합산도 없다`() {
        assertNull(summarize(emptyList()))
    }

    @Test
    fun `합산 손익과 운용시간은 행의 합이다`() {
        val summary = summarize(listOf(run("a", 100, 1.0, 5), run("b", -40, -2.0, 7)))

        assertEquals(60L, summary?.totalPnlAmount)
        assertEquals(12L, summary?.hours)
    }

    @Test
    fun `승률은 손익이 양수인 운용의 비율이다`() {
        val summary = summarize(listOf(run("a", 100, 1.0), run("b", -40, -2.0), run("c", 10, 0.5)))

        assertEquals(66.67, summary?.winRatePercent ?: 0.0, 0.01)
    }

    @Test
    fun `중위 수익률은 짝수 개면 가운데 둘의 평균이다`() {
        val summary = summarize(listOf(run("a", 1, 17.93), run("b", 1, 7.80), run("c", 1, 3.20), run("d", 1, 1.0)))

        assertEquals(5.5, summary?.medianReturn ?: 0.0, 0.001)
        assertEquals(17.93, summary?.bestReturn ?: 0.0, 0.001)
        assertEquals(1.0, summary?.worstReturn ?: 0.0, 0.001)
        assertEquals(7.4825, summary?.averageReturn ?: 0.0, 0.001)
    }
}
