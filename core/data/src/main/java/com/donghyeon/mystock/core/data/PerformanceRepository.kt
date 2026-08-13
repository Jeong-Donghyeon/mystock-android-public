package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.AccountPerformance

interface PerformanceRepository {
    suspend fun getAccountPerformance(accountId: String): Result<AccountPerformance>
}
