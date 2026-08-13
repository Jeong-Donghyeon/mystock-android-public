package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    suspend fun getAccounts(): Result<List<Account>>

    fun observeHomeData(accountId: String): Flow<Result<HomeData>>

    suspend fun pauseAccount(accountId: String): Result<Unit>

    suspend fun resumeAccount(accountId: String): Result<Unit>

    suspend fun closeAccount(accountId: String): Result<Unit>

    suspend fun liquidateAccount(accountId: String): Result<Unit>

    suspend fun setOrderAlert(accountId: String, enabled: Boolean): Result<Unit>
}

internal const val HOME_POLL_INTERVAL_MS = 5_000L
