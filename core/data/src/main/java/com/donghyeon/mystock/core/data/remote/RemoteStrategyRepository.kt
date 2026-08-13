package com.donghyeon.mystock.core.data.remote

import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.data.apiResult
import com.donghyeon.mystock.core.data.fake.FakeStrategyRepository
import com.donghyeon.mystock.core.data.mapper.toDomain
import com.donghyeon.mystock.core.data.serve
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.core.network.AccountApi
import com.donghyeon.mystock.core.network.StrategyApi
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import com.donghyeon.mystock.core.network.dto.StartRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteStrategyRepository @Inject constructor(
    private val api: StrategyApi,
    private val accountApi: AccountApi,
    private val tokens: AuthTokenStore,
    private val fake: FakeStrategyRepository,
) : StrategyRepository {

    override suspend fun getStrategies(): Result<List<Strategy>> = tokens.serve(
        fake = { fake.getStrategies() },
        remote = { apiResult { api.getStrategies().map { it.toDomain() } } },
    )

    override suspend fun getStrategyDetail(key: String, accountId: String?): Result<StrategyDetail> = tokens.serve(
        fake = { fake.getStrategyDetail(key, accountId) },
        remote = { apiResult { api.getStrategyDetail(key, accountId).toDomain() } },
    )

    override suspend fun startStrategy(key: String, accountId: String): Result<Unit> = tokens.serve(
        fake = { fake.startStrategy(key, accountId) },
        remote = { apiResult { accountApi.startAccount(accountId, StartRequestDto(strategyKey = key)) }.map { } },
    )
}
