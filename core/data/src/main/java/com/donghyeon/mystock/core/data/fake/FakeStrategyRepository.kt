package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyDetail
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FakeStrategyRepository @Inject constructor(
    private val backend: FakeBackend,
) : StrategyRepository {

    override suspend fun getStrategies(): Result<List<Strategy>> {
        delay(FAKE_LATENCY_MS)
        return Result.success(backend.strategies())
    }

    override suspend fun getStrategyDetail(key: String, accountId: String?): Result<StrategyDetail> {
        delay(FAKE_LATENCY_MS)
        return backend.strategyDetail(key, accountId)
    }

    override suspend fun startStrategy(key: String, accountId: String): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return backend.startStrategy(key, accountId)
    }
}
