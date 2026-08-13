package com.donghyeon.mystock.core.network

import retrofit2.http.POST

// 로그아웃은 보낸 액세스 토큰의 쌍을 지우므로 만료된 토큰이면 재발급 뒤 보내야 한다 — 인증기 있는 클라이언트를 탄다
interface SessionApi {
    @POST("auth/logout")
    suspend fun logout()
}
