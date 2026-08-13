package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.core.model.StrategyDetail

interface StrategyRepository {
    suspend fun getStrategies(): Result<List<Strategy>>

    suspend fun getStrategyDetail(key: String, accountId: String?): Result<StrategyDetail>

    suspend fun startStrategy(key: String, accountId: String): Result<Unit>
}
