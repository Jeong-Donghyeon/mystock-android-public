package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDto(
    val code: String,
    val message: String,
    val details: List<String>,
)
