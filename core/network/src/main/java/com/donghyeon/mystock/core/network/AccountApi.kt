package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.network.dto.AccountDto
import com.donghyeon.mystock.core.network.dto.OrderPageDto
import com.donghyeon.mystock.core.network.dto.PerformanceDto
import com.donghyeon.mystock.core.network.dto.PortfolioDto
import com.donghyeon.mystock.core.network.dto.StartRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountApi {
    @GET("accounts")
    suspend fun getAccounts(): List<AccountDto>

    @GET("accounts/{accountId}/portfolio")
    suspend fun getPortfolio(@Path("accountId") accountId: String): PortfolioDto

    @GET("accounts/{accountId}/performance")
    suspend fun getPerformanceResponse(@Path("accountId") accountId: String): Response<PerformanceDto>

    @GET("accounts/{accountId}/orders")
    suspend fun getOrders(
        @Path("accountId") accountId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): OrderPageDto

    @POST("accounts/{accountId}/start")
    suspend fun startAccount(@Path("accountId") accountId: String, @Body request: StartRequestDto): AccountDto

    @POST("accounts/{accountId}/pause")
    suspend fun pauseAccount(@Path("accountId") accountId: String): AccountDto

    @POST("accounts/{accountId}/resume")
    suspend fun resumeAccount(@Path("accountId") accountId: String): AccountDto

    @POST("accounts/{accountId}/close")
    suspend fun closeAccount(@Path("accountId") accountId: String): AccountDto

    @POST("accounts/{accountId}/liquidate")
    suspend fun liquidateAccount(@Path("accountId") accountId: String): AccountDto

    @POST("accounts/{accountId}/order-alert")
    suspend fun setOrderAlert(
        @Path("accountId") accountId: String,
        @Query("enabled") enabled: Boolean,
    ): AccountDto
}

// 대기 계좌의 성과는 204로 본문이 없다. Retrofit은 suspend 함수의 nullable 반환을 모르므로 Response로 받아 푼다
suspend fun AccountApi.getPerformance(accountId: String): PerformanceDto? = getPerformanceResponse(accountId).body()
