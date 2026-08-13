package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokensDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
)

/** 키 이름은 서버의 계좌 설정이 정한다 (openapi.yaml `/auth/keys`). */
@Serializable
data class KeysRequestDto(
    val keys: Map<String, String>,
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String,
)
