package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.network.dto.KeysRequestDto
import com.donghyeon.mystock.core.network.dto.RefreshRequestDto
import com.donghyeon.mystock.core.network.dto.TokensDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/keys")
    suspend fun issueTokens(@Body request: KeysRequestDto): TokensDto

    @POST("auth/refresh")
    suspend fun refreshTokens(@Body request: RefreshRequestDto): TokensDto
}
