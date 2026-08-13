package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

private class TestTokenStore(authenticated: Boolean) : AuthTokenStore {
    override val authenticated = MutableStateFlow(authenticated)
    override val accessToken: String? = null
    override val refreshToken: String? = null

    override fun save(accessToken: String, refreshToken: String) = Unit

    override fun clear() = Unit
}

class ServedTest {

    @Test
    fun `미인증이면 Fake가 답하고 서버를 부르지 않는다`() {
        var calledServer = false

        val served = TestTokenStore(authenticated = false).serve(
            fake = { "샘플" },
            remote = {
                calledServer = true
                "서버"
            },
        )

        assertEquals("샘플", served)
        assertFalse(calledServer)
    }

    @Test
    fun `인증되면 서버가 답하고 Fake를 부르지 않는다`() {
        var calledFake = false

        val served = TestTokenStore(authenticated = true).serve(
            fake = {
                calledFake = true
                "샘플"
            },
            remote = { "서버" },
        )

        assertEquals("서버", served)
        assertFalse(calledFake)
    }

    @Test
    fun `관찰 중에 인증이 바뀌면 그 자리에서 서버로 갈아탄다`() = runTest {
        val tokens = TestTokenStore(authenticated = false)
        val received = mutableListOf<String>()

        val job = launch {
            tokens.serveFlow(fake = { flowOf("샘플") }, remote = { flowOf("서버") }).toList(received)
        }
        runCurrent()
        tokens.authenticated.value = true
        runCurrent()
        job.cancel()

        assertEquals(listOf("샘플", "서버"), received)
    }
}
