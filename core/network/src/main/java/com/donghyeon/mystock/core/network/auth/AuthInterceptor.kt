package com.donghyeon.mystock.core.network.auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val AUTHORIZATION = "Authorization"
private const val BEARER = "Bearer "

internal class AuthInterceptor(private val store: AuthTokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = store.accessToken ?: return chain.proceed(chain.request())
        return chain.proceed(chain.request().withBearer(token))
    }
}

internal fun Request.withBearer(token: String): Request = newBuilder().header(AUTHORIZATION, BEARER + token).build()

internal fun Request.bearerToken(): String? = header(AUTHORIZATION)?.removePrefix(BEARER)
