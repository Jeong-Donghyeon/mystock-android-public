package com.donghyeon.mystock.core.data.fake

import com.donghyeon.mystock.core.data.AccountRepository
import com.donghyeon.mystock.core.data.HOME_POLL_INTERVAL_MS
import com.donghyeon.mystock.core.data.HomeData
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FakeAccountRepository @Inject constructor(
    private val backend: FakeBackend,
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>> {
        delay(FAKE_LATENCY_MS)
        return Result.success(backend.accounts())
    }

    override fun observeHomeData(accountId: String): Flow<Result<HomeData>> = flow {
        while (true) {
            emit(backend.homeData(accountId))
            delay(HOME_POLL_INTERVAL_MS)
            backend.tick()
        }
    }

    override suspend fun pauseAccount(accountId: String): Result<Unit> =
        transition(accountId, AccountState.PAUSED, from = setOf(AccountState.RUNNING))

    override suspend fun resumeAccount(accountId: String): Result<Unit> =
        transition(
            accountId,
            AccountState.RUNNING,
            from = setOf(AccountState.PAUSED, AccountState.CLOSING, AccountState.LIQUIDATING),
        )

    override suspend fun closeAccount(accountId: String): Result<Unit> =
        transition(accountId, AccountState.CLOSING, from = setOf(AccountState.RUNNING, AccountState.PAUSED))

    override suspend fun liquidateAccount(accountId: String): Result<Unit> = transition(
        accountId,
        AccountState.LIQUIDATING,
        from = setOf(AccountState.RUNNING, AccountState.PAUSED, AccountState.CLOSING),
    )

    override suspend fun setOrderAlert(accountId: String, enabled: Boolean): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return backend.setOrderAlert(accountId, enabled)
    }

    private suspend fun transition(
        accountId: String,
        state: AccountState,
        from: Set<AccountState>,
    ): Result<Unit> {
        delay(FAKE_LATENCY_MS)
        return backend.transition(accountId, state, from)
    }
}
