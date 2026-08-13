package com.donghyeon.mystock.core.network.auth

import com.donghyeon.mystock.core.model.ApiException
import com.donghyeon.mystock.core.network.AuthApi
import com.donghyeon.mystock.core.network.dto.KeysRequestDto
import com.donghyeon.mystock.core.network.dto.RefreshRequestDto
import com.donghyeon.mystock.core.network.dto.TokensDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TokenAuthenticatorTest {

    private val store = InMemoryTokenStore(accessToken = "old-access", refreshToken = "old-refresh")
    private val authApi = FakeAuthApi()
    private val authenticator = TokenAuthenticator(store) { authApi }

    private fun request(token: String? = "old-access"): Request =
        Request.Builder()
            .url("http://localhost/api/v1/accounts")
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .build()

    private fun unauthorized(request: Request, prior: Response? = null): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("")
        .priorResponse(prior)
        .build()

    @Test
    fun `401이면 토큰을 재발급 받아 새 토큰으로 한 번 다시 보낸다`() {
        authApi.refreshResult = Result.success(TokensDto("new-access", "new-refresh", "2026-09-15T00:00:00Z"))

        val retry = authenticator.authenticate(null, unauthorized(request()))

        assertEquals("Bearer new-access", retry?.header("Authorization"))
        assertEquals(listOf("old-refresh"), authApi.refreshed)
        assertEquals("new-access" to "new-refresh", store.accessToken to store.refreshToken)
        assertTrue(store.authenticated.value)
    }

    @Test
    fun `재발급도 거절되면 토큰을 지워 미인증이 된다`() {
        authApi.refreshResult = Result.failure(ApiException("INVALID_TOKEN", "다시 로그인해 주세요"))

        val retry = authenticator.authenticate(null, unauthorized(request()))

        assertNull(retry)
        assertNull(store.accessToken)
        assertFalse(store.authenticated.value)
    }

    @Test
    fun `재발급이 통신 실패면 토큰은 그대로 두고 이번 요청만 실패한다`() {
        authApi.refreshResult = Result.failure(IOException("offline"))

        val retry = authenticator.authenticate(null, unauthorized(request()))

        assertNull(retry)
        assertEquals("old-access", store.accessToken)
        assertTrue(store.authenticated.value)
    }

    @Test
    fun `토큰 없이 받은 401은 재발급하지 않는다`() {
        store.clear()

        val retry = authenticator.authenticate(null, unauthorized(request(token = null)))

        assertNull(retry)
        assertTrue(authApi.refreshed.isEmpty())
    }

    @Test
    fun `다른 요청이 이미 갈아끼운 토큰이 있으면 재발급 없이 그 토큰으로 다시 보낸다`() {
        store.save("rotated-access", "rotated-refresh")

        val retry = authenticator.authenticate(null, unauthorized(request(token = "old-access")))

        assertEquals("Bearer rotated-access", retry?.header("Authorization"))
        assertTrue(authApi.refreshed.isEmpty())
    }

    @Test
    fun `다시 보낸 요청도 401이면 더 시도하지 않는다`() {
        authApi.refreshResult = Result.success(TokensDto("new-access", "new-refresh", "2026-09-15T00:00:00Z"))
        val first = unauthorized(request())

        val retry = authenticator.authenticate(null, unauthorized(request(token = "new-access"), prior = first))

        assertNull(retry)
        assertTrue(authApi.refreshed.isEmpty())
    }

    @Test
    fun `시도 한도 같은 일시적 거절은 토큰을 지우지 않는다`() {
        authApi.refreshResult = Result.failure(ApiException("TOO_MANY_ATTEMPTS", "인증 시도가 너무 잦습니다"))

        val retry = authenticator.authenticate(null, unauthorized(request()))

        assertNull(retry)
        assertEquals("old-refresh", store.refreshToken)
        assertTrue(store.authenticated.value)
    }
}

private class InMemoryTokenStore(accessToken: String?, refreshToken: String?) : AuthTokenStore {
    override var accessToken: String? = accessToken
        private set
    override var refreshToken: String? = refreshToken
        private set
    private val _authenticated = MutableStateFlow(accessToken != null)
    override val authenticated: StateFlow<Boolean> = _authenticated

    override fun save(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        _authenticated.value = true
    }

    override fun clear() {
        accessToken = null
        refreshToken = null
        _authenticated.value = false
    }
}

private class FakeAuthApi : AuthApi {
    var refreshResult: Result<TokensDto> = Result.failure(IllegalStateException("not scripted"))
    val refreshed = mutableListOf<String>()

    override suspend fun issueTokens(request: KeysRequestDto): TokensDto = error("unused")

    override suspend fun refreshTokens(request: RefreshRequestDto): TokensDto {
        refreshed += request.refreshToken
        return refreshResult.getOrThrow()
    }
}
