package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.data.PerformanceRepository
import com.donghyeon.mystock.core.model.AccountPerformance
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FakePerformanceRepository @Inject constructor(
    private val backend: FakeBackend,
) : PerformanceRepository {

    override suspend fun getAccountPerformance(accountId: String): Result<AccountPerformance> {
        delay(FAKE_LATENCY_MS)
        return backend.accountPerformance(accountId)
    }
}
