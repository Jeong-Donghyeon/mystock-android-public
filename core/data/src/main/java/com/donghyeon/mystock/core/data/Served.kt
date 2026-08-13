package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

// 키를 인증해야 실제 계좌가 연결된다(원천). 미인증이면 샘플 데이터인 Fake가 답한다
internal inline fun <T> AuthTokenStore.serve(fake: () -> T, remote: () -> T): T =
    if (authenticated.value) remote() else fake()

// 관찰 중에 인증이 바뀌면 그 자리에서 갈아탄다 — 재발급 거절로 미인증이 되는 것도 여기로 온다
@OptIn(ExperimentalCoroutinesApi::class)
internal fun <T> AuthTokenStore.serveFlow(fake: () -> Flow<T>, remote: () -> Flow<T>): Flow<T> =
    authenticated.flatMapLatest { if (it) remote() else fake() }
