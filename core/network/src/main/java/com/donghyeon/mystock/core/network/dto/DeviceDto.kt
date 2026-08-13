package com.donghyeon.mystock.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDto(
    val token: String,
    val notificationsEnabled: Boolean,
)
