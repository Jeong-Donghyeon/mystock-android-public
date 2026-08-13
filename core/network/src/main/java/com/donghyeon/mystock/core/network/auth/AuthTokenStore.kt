package com.donghyeon.mystock.core.network.auth

import kotlinx.coroutines.flow.StateFlow

/** 서버가 내준 토큰 쌍. 있으면 인증된 것이고, 서버가 재발급까지 거절하면 지워져 미인증이 된다. */
interface AuthTokenStore {
    val authenticated: StateFlow<Boolean>

    val accessToken: String?

    val refreshToken: String?

    fun save(accessToken: String, refreshToken: String)

    fun clear()
}
