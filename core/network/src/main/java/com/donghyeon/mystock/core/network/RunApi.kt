package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.network.dto.RunsDto
import retrofit2.http.GET
import retrofit2.http.Path

interface RunApi {
    @GET("accounts/{accountId}/runs")
    suspend fun getRuns(@Path("accountId") accountId: String): RunsDto
}
