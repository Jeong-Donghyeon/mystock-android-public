package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ApiResultTest {

    @Test
    fun `성공한 호출은 값을 담는다`() = runTest {
        assertEquals(Result.success(3), apiResult { 3 })
    }

    @Test
    fun `서버 거절과 통신 실패는 예외 대신 failure로 돌아온다`() = runTest {
        val rejected = ApiException("INVALID_TRANSITION", "대기 계좌는 일시정지할 수 없습니다")
        val offline = IOException("offline")

        assertEquals(rejected, apiResult<Unit> { throw rejected }.exceptionOrNull())
        assertEquals(offline, apiResult<Unit> { throw offline }.exceptionOrNull())
    }

    @Test
    fun `취소는 실패로 둔갑하지 않고 그대로 전파된다`() = runTest {
        val escaped = runCatching { apiResult<Unit> { throw CancellationException("cancelled") } }

        assertTrue(escaped.exceptionOrNull() is CancellationException)
    }
}
