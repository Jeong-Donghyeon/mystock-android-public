package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.network.dto.DeviceDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

interface DeviceApi {
    @POST("devices")
    suspend fun registerDevice(@Body device: DeviceDto)

    @DELETE("devices")
    suspend fun unregisterDevice(@Query("token") token: String)
}
