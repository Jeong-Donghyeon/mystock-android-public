package com.donghyeon.mystock.core.data.remote

import com.donghyeon.mystock.core.data.AccountRepository
import com.donghyeon.mystock.core.data.HOME_POLL_INTERVAL_MS
import com.donghyeon.mystock.core.data.HomeData
import com.donghyeon.mystock.core.data.apiResult
import com.donghyeon.mystock.core.data.assembleHomeData
import com.donghyeon.mystock.core.data.fake.FakeAccountRepository
import com.donghyeon.mystock.core.data.mapper.toDomain
import com.donghyeon.mystock.core.data.serve
import com.donghyeon.mystock.core.data.serveFlow
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.network.AccountApi
import com.donghyeon.mystock.core.network.StrategyApi
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import com.donghyeon.mystock.core.network.getPerformance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// 홈의 주문 내역은 첫 페이지만 보인다. 서버 기본 크기와 같다
private const val HOME_ORDER_PAGE_SIZE = 20

@Singleton
internal class RemoteAccountRepository @Inject constructor(
    private val api: AccountApi,
    private val strategyApi: StrategyApi,
    private val tokens: AuthTokenStore,
    private val fake: FakeAccountRepository,
) : AccountRepository {

    override suspend fun getAccounts(): Result<List<Account>> =
        tokens.serve(fake = { fake.getAccounts() }, remote = { fetchAccounts() })

    override fun observeHomeData(accountId: String): Flow<Result<HomeData>> = tokens.serveFlow(
        fake = { fake.observeHomeData(accountId) },
        remote = {
            flow {
                while (true) {
                    emit(loadHomeData(accountId))
                    delay(HOME_POLL_INTERVAL_MS)
                }
            }
        },
    )

    override suspend fun pauseAccount(accountId: String): Result<Unit> =
        tokens.serve(fake = { fake.pauseAccount(accountId) }, remote = { command { api.pauseAccount(accountId) } })

    override suspend fun resumeAccount(accountId: String): Result<Unit> =
        tokens.serve(fake = { fake.resumeAccount(accountId) }, remote = { command { api.resumeAccount(accountId) } })

    override suspend fun closeAccount(accountId: String): Result<Unit> =
        tokens.serve(fake = { fake.closeAccount(accountId) }, remote = { command { api.closeAccount(accountId) } })

    override suspend fun liquidateAccount(accountId: String): Result<Unit> = tokens.serve(
        fake = { fake.liquidateAccount(accountId) },
        remote = { command { api.liquidateAccount(accountId) } },
    )

    override suspend fun setOrderAlert(accountId: String, enabled: Boolean): Result<Unit> = tokens.serve(
        fake = { fake.setOrderAlert(accountId, enabled) },
        remote = { command { api.setOrderAlert(accountId, enabled) } },
    )

    private suspend fun fetchAccounts(): Result<List<Account>> = apiResult { api.getAccounts().map { it.toDomain() } }

    // 홈은 5초마다 다시 받으므로 전이 뒤 계좌 상태도 그때 따라온다
    private suspend fun loadHomeData(accountId: String): Result<HomeData> = coroutineScope {
        val accounts = async { fetchAccounts() }
        val strategies = async { apiResult { strategyApi.getStrategies().map { it.toDomain() } } }
        val performance = async { apiResult { api.getPerformance(accountId)?.toDomain() } }
        val portfolio = async { apiResult { api.getPortfolio(accountId).toDomain() } }
        val orders = async { apiResult { api.getOrders(accountId, page = 0, size = HOME_ORDER_PAGE_SIZE).toDomain() } }
        assembleHomeData(accounts.await(), strategies.await(), performance.await(), portfolio.await(), orders.await())
    }

    private suspend inline fun command(crossinline call: suspend () -> Any): Result<Unit> = apiResult { call() }.map { }
}
