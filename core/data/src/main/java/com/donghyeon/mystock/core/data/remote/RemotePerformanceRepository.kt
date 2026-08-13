package com.donghyeon.mystock.core.data.remote

import com.donghyeon.mystock.core.data.PerformanceRepository
import com.donghyeon.mystock.core.data.apiResult
import com.donghyeon.mystock.core.data.fake.FakePerformanceRepository
import com.donghyeon.mystock.core.data.mapper.toDomain
import com.donghyeon.mystock.core.data.serve
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.core.network.RunApi
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemotePerformanceRepository @Inject constructor(
    private val api: RunApi,
    private val tokens: AuthTokenStore,
    private val fake: FakePerformanceRepository,
) : PerformanceRepository {

    override suspend fun getAccountPerformance(accountId: String): Result<AccountPerformance> = tokens.serve(
        fake = { fake.getAccountPerformance(accountId) },
        remote = { apiResult { api.getRuns(accountId).toDomain() } },
    )
}
