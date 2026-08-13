package com.donghyeon.mystock.konsist

import com.lemonappdev.konsist.api.Konsist
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelConventionTest {

    @Test
    fun `매매 상태는 운용 중·회수 중·청산 중·일시정지·대기 다섯이다`() {
        val states = Konsist.scopeFromProduction()
            .classes()
            .first { it.name == "AccountState" && it.resideInPackage("com.donghyeon.mystock.core.model") }
            .enumConstants
            .map { it.name }

        assertEquals(listOf("RUNNING", "CLOSING", "LIQUIDATING", "PAUSED", "WAITING"), states)
    }
}
