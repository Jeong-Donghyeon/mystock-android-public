package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.network.dto.StrategyDetailDto
import com.donghyeon.mystock.core.network.dto.StrategyDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StrategyApi {
    @GET("strategies")
    suspend fun getStrategies(): List<StrategyDto>

    @GET("strategies/{key}")
    suspend fun getStrategyDetail(
        @Path("key") key: String,
        @Query("accountId") accountId: String?,
    ): StrategyDetailDto
}
