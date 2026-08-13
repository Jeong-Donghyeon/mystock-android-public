package com.donghyeon.mystock.core.network.auth

import com.donghyeon.mystock.core.model.ApiException
import com.donghyeon.mystock.core.network.AuthApi
import com.donghyeon.mystock.core.network.dto.RefreshRequestDto
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Provider

// 서버가 토큰 자체를 거절한 코드. 시도 한도(429)는 토큰의 잘못이 아니라 지우지 않는다
private val REJECTED_TOKEN_CODES = setOf("INVALID_TOKEN", "UNAUTHORIZED")

/** 401이면 토큰을 재발급 받아 한 번 다시 보낸다. 재발급마저 서버가 토큰을 거절하면 지워 미인증으로 돌린다. */
internal class TokenAuthenticator(
    private val store: AuthTokenStore,
    // 재발급은 인증기 없는 별도 클라이언트로 간다. 이 인증기를 단 클라이언트를 타면 호스트당 동시 요청 한도에 갇힌다
    private val authApi: Provider<AuthApi>,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        val rejected = request.bearerToken()
        val fresh = if (rejected != null && response.priorResponse == null) freshToken(rejected) else null
        return fresh?.let { request.withBearer(it) }
    }

    // 여러 요청이 같은 401을 받으면 하나만 재발급하고 나머지는 그 결과를 쓴다
    private fun freshToken(rejected: String): String? = synchronized(lock) {
        val current = store.accessToken
        when {
            current == null -> null
            current != rejected -> current
            else -> refresh()
        }
    }

    private fun refresh(): String? {
        val refreshToken = store.refreshToken ?: return null
        return runCatching { runBlocking { authApi.get().refreshTokens(RefreshRequestDto(refreshToken)) } }
            .onSuccess { store.save(it.accessToken, it.refreshToken) }
            .onFailure { if (it is ApiException && it.code in REJECTED_TOKEN_CODES) store.clear() }
            .getOrNull()
            ?.accessToken
    }
}
