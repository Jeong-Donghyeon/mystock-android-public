package com.donghyeon.mystock.feature.account.home

import androidx.lifecycle.SavedStateHandle
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.feature.account.R
import com.donghyeon.mystock.feature.account.TestAccountRepository
import com.donghyeon.mystock.feature.account.testAccount
import com.donghyeon.mystock.feature.account.testPerformance
import com.donghyeon.mystock.feature.account.testPortfolio
import com.donghyeon.mystock.feature.common.sampleStrategies
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import com.donghyeon.mystock.feature.common.R as CommonR

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC)

    private lateinit var repository: TestAccountRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = TestAccountRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): HomeViewModel =
        homeViewModel().also { watch(it) }

    private fun homeViewModel(): HomeViewModel =
        HomeViewModel(repository, clock, SavedStateHandle())

    private fun TestScope.watch(viewModel: HomeViewModel) {
        backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
    }

    @Test
    fun `초기 로드 성공 시 첫 계좌가 선택되고 대시보드가 채워진다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1"), testAccount("acc-2")))
        repository.performanceResults = mapOf("acc-1" to Result.success(testPerformance()))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.ordersResults = mapOf("acc-1" to Result.success(emptyList()))

        val state = viewModel().uiState.value

        assertTrue(state.content is HomeContent.Ready)
        assertFalse(state.staleVisible)
        assertEquals("acc-1", state.selectedAccountId)
        assertNotNull(state.dashboard?.performance)
        assertNotNull(state.dashboard?.portfolio)
        assertEquals(72L, state.dashboard?.runningHours)
    }

    @Test
    fun `대시보드가 도착하기 전에는 로딩을 유지한다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.performanceResults = mapOf("acc-1" to Result.success(testPerformance()))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.ordersResults = mapOf("acc-1" to Result.success(emptyList()))
        repository.performanceGate = CompletableDeferred()
        val viewModel = viewModel()

        assertEquals(HomeContent.Loading, viewModel.uiState.value.content)
        assertEquals("acc-1", viewModel.uiState.value.selectedAccountId)

        repository.performanceGate?.complete(Unit)

        assertNotNull(viewModel.uiState.value.dashboard?.performance)
    }

    @Test
    fun `계좌가 없으면 로딩을 끝낸다`() = runTest {
        repository.accountsResult = Result.success(emptyList())

        val state = viewModel().uiState.value

        assertEquals(HomeContent.Ready(), state.content)
        assertNull(state.selectedAccountId)
    }

    @Test
    fun `보여줄 데이터가 없으면 전용 오류 화면으로 간다`() = runTest {
        repository.accountsResult = Result.failure(IOException())

        val state = viewModel().uiState.value

        assertEquals(HomeContent.Failed, state.content)
    }

    @Test
    fun `대시보드 일부 실패 시 stale로 표시하되 받은 데이터는 유지한다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.performanceResults = mapOf("acc-1" to Result.failure(IOException()))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.ordersResults = mapOf("acc-1" to Result.success(emptyList()))

        val state = viewModel().uiState.value

        assertTrue(state.staleVisible)
        assertNull(state.dashboard?.performance)
        assertNotNull(state.dashboard?.portfolio)
    }

    @Test
    fun `연결 끊김 배너는 닫을 수 있다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.staleVisible)

        viewModel.onIntent(HomeUiIntent.DismissStale)

        assertFalse(viewModel.uiState.value.staleVisible)
    }

    @Test
    fun `계좌 시트는 선택을 확정해야 문맥이 바뀐다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1"), testAccount("acc-2")))
        repository.performanceResults = mapOf(
            "acc-1" to Result.success(testPerformance()),
            "acc-2" to Result.failure(IOException()),
        )
        repository.portfolioResults = mapOf(
            "acc-1" to Result.success(testPortfolio()),
            "acc-2" to Result.success(testPortfolio()),
        )
        repository.ordersResults = mapOf(
            "acc-1" to Result.success(emptyList()),
            "acc-2" to Result.success(emptyList()),
        )
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenAccountSheet)
        viewModel.onIntent(HomeUiIntent.Overlay.SelectAccount("acc-2"))

        assertEquals("acc-1", viewModel.uiState.value.selectedAccountId)
        assertEquals(HomeSheet.Account(pendingAccountId = "acc-2"), viewModel.uiState.value.sheet)

        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmAccount)

        val state = viewModel.uiState.value
        assertEquals("acc-2", state.selectedAccountId)
        assertNull(state.sheet)
        assertNull(state.dashboard?.performance)
        assertNotNull(state.dashboard?.portfolio)
    }

    @Test
    fun `계좌를 바꾸면 새 대시보드가 올 때까지 스켈레톤을 보인다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1"), testAccount("acc-2")))
        val viewModel = viewModel()
        repository.performanceGate = CompletableDeferred()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenAccountSheet)
        viewModel.onIntent(HomeUiIntent.Overlay.SelectAccount("acc-2"))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmAccount)

        assertEquals(HomeContent.Loading, viewModel.uiState.value.content)
    }

    @Test
    fun `계좌 시트를 닫으면 문맥이 그대로다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1"), testAccount("acc-2")))
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenAccountSheet)
        viewModel.onIntent(HomeUiIntent.Overlay.SelectAccount("acc-2"))
        viewModel.onIntent(HomeUiIntent.Overlay.DismissAccountSheet)

        assertEquals("acc-1", viewModel.uiState.value.selectedAccountId)
        assertNull(viewModel.uiState.value.sheet)
    }

    @Test
    fun `명령을 확정하면 시트가 닫히고 계좌를 다시 조회한다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()
        val callsAfterInit = repository.getAccountsCallCount

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.CLOSE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        assertNull(viewModel.uiState.value.sheet)
        assertEquals(listOf("acc-1" to "close"), repository.commands)
        assertEquals(callsAfterInit + 1, repository.getAccountsCallCount)
    }

    @Test
    fun `명령을 확정하면 응답이 올 때까지 오버레이로 막고 응답 뒤에 계좌를 다시 조회한다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.commandGate = CompletableDeferred()
        val viewModel = viewModel()
        val callsAfterInit = repository.getAccountsCallCount

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.PAUSE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        val pending = viewModel.uiState.value
        assertNull(pending.sheet)
        assertTrue(pending.commanding)
        assertEquals(callsAfterInit, repository.getAccountsCallCount)

        repository.commandGate?.complete(Unit)

        assertFalse(viewModel.uiState.value.commanding)
        assertEquals(callsAfterInit + 1, repository.getAccountsCallCount)
    }

    @Test
    fun `명령이 실패해도 오버레이는 걷힌다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.commandResult = Result.failure(IOException())
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.PAUSE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        assertFalse(viewModel.uiState.value.commanding)
    }

    @Test
    fun `명령 시트를 닫으면 요청 없이 그대로다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.PAUSE))
        assertEquals(HomeSheet.Command(HomeCommand.PAUSE), viewModel.uiState.value.sheet)

        viewModel.onIntent(HomeUiIntent.Overlay.DismissCommand)

        assertNull(viewModel.uiState.value.sheet)
        assertTrue(repository.commands.isEmpty())
    }

    @Test
    fun `당겨서 새로고침은 계좌와 대시보드를 모두 다시 받는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        val viewModel = viewModel()
        val accountCalls = repository.getAccountsCallCount
        val subscriptions = repository.homeDataSubscriptions

        viewModel.onIntent(HomeUiIntent.Refresh)

        assertEquals(accountCalls + 1, repository.getAccountsCallCount)
        assertEquals(subscriptions + 1, repository.homeDataSubscriptions)
        assertFalse(viewModel.uiState.value.refreshing)
    }

    @Test
    fun `당겨서 새로고침이 실패하면 마지막 데이터를 두고 연결 끊김을 알린다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.performanceResults = mapOf("acc-1" to Result.success(testPerformance()))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.ordersResults = mapOf("acc-1" to Result.success(emptyList()))
        val viewModel = viewModel()
        repository.accountsResult = Result.failure(IOException())

        viewModel.onIntent(HomeUiIntent.Refresh)

        val state = viewModel.uiState.value
        assertTrue(state.staleVisible)
        assertNotNull(state.dashboard?.portfolio)
        assertFalse(state.refreshing)
    }

    @Test
    fun `청산 요청이 실패해도 마지막 데이터를 두고 스낵바로 알린다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.commandResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.LIQUIDATE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        assertTrue(viewModel.uiState.value.content is HomeContent.Ready)
        assertNotNull(viewModel.uiState.value.dashboard?.portfolio)
        assertTrue(effects.any { it is HomeUiEffect.ShowMessage })
    }

    @Test
    fun `청산을 확정하면 시트가 닫히고 마지막 데이터를 둔 채 청산 중 상태를 다시 받는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.onCommand = { accountId ->
            repository.accountsResult =
                Result.success(listOf(testAccount(accountId, state = AccountState.LIQUIDATING)))
            repository.performanceGate = CompletableDeferred()
        }
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.LIQUIDATE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        val pending = viewModel.uiState.value
        assertNull(pending.sheet)
        assertTrue(pending.content is HomeContent.Ready)
        assertNotNull(pending.dashboard?.portfolio)
        assertEquals(listOf("acc-1" to "liquidate"), repository.commands)

        repository.performanceGate?.complete(Unit)

        assertEquals(AccountState.LIQUIDATING, viewModel.uiState.value.selectedAccount?.state)
    }

    @Test
    fun `명령 실패는 스낵바로 알린다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.commandResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.PAUSE))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmCommand)

        assertTrue(effects.any { it is HomeUiEffect.ShowMessage })
    }

    @Test
    fun `알림을 끄면 실패 시 되돌린다`() = runTest {
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", orderAlertEnabled = true)))
        repository.orderAlertResult = Result.failure(IOException())
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)

        assertEquals(listOf("acc-1" to false), repository.orderAlerts)
        assertTrue(viewModel.uiState.value.selectedAccount?.orderAlertEnabled == true)
    }

    @Test
    fun `화면이 보지 않는 동안에는 서버를 부르지 않는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.performanceResults = mapOf("acc-1" to Result.success(testPerformance()))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.ordersResults = mapOf("acc-1" to Result.success(emptyList()))
        val viewModel = homeViewModel()

        assertEquals(0, repository.getAccountsCallCount)
        assertEquals(0, repository.homeDataSubscriptions)
        assertNull(viewModel.uiState.value.dashboard?.performance)

        watch(viewModel)

        assertEquals(1, repository.getAccountsCallCount)
        assertEquals(1, repository.homeDataSubscriptions)
        assertNotNull(viewModel.uiState.value.dashboard?.performance)
    }

    @Test
    fun `알림을 켜면 서버가 아직 모르는 동안에도 유지된다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.homeDataRetryGate = CompletableDeferred()
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)
        repository.homeDataRetryGate?.complete(Unit)

        assertEquals(listOf("acc-1" to true), repository.orderAlerts)
        assertTrue(viewModel.uiState.value.selectedAccount?.orderAlertEnabled == true)
    }

    @Test
    fun `서버가 알림 변경을 반영하면 낙관적 값을 버린다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.homeDataRetryGate = CompletableDeferred()
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", orderAlertEnabled = true)))
        repository.homeDataRetryGate?.complete(Unit)

        val state = viewModel.uiState.value
        assertTrue(state.orderAlertOverrides.isEmpty())
        assertTrue(state.selectedAccount?.orderAlertEnabled == true)
    }

    @Test
    fun `주문 체결 알림을 켜면 안내하고 알림 권한을 요청한다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)

        assertEquals(
            listOf(
                HomeUiEffect.ShowMessage(R.string.home_order_alert_on),
                HomeUiEffect.RequestNotificationPermission,
            ),
            effects,
        )
    }

    @Test
    fun `주문 체결 알림을 끄면 안내만 하고 권한을 요청하지 않는다`() = runTest {
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", orderAlertEnabled = true)))
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)

        assertEquals(listOf(HomeUiEffect.ShowMessage(R.string.home_order_alert_off)), effects)
    }

    @Test
    fun `알림 변경이 실패하면 켜고 끈 안내를 하지 않는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.orderAlertResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.ToggleOrderAlert)

        assertEquals(
            listOf(HomeUiEffect.ShowMessage(CommonR.string.common_notification_failed)),
            effects,
        )
    }

    @Test
    fun `알림 권한을 거부하면 주문 체결 알림을 되돌리고 안내한다`() = runTest {
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", orderAlertEnabled = true)))
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.DenyNotificationPermission)

        assertEquals(listOf("acc-1" to false), repository.orderAlerts)
        assertFalse(viewModel.uiState.value.selectedAccount?.orderAlertEnabled == true)
        assertTrue(effects.any { it is HomeUiEffect.ShowMessage })
    }

    @Test
    fun `닫은 연결 끊김 배너는 폴링이 계속 실패해도 다시 뜨지 않는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.homeDataFailure = IOException()
        repository.homeDataRetryGate = CompletableDeferred()
        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.staleVisible)

        viewModel.onIntent(HomeUiIntent.DismissStale)
        repository.homeDataRetryGate?.complete(Unit)

        assertFalse(viewModel.uiState.value.staleVisible)
    }

    @Test
    fun `프로세스가 다시 살아나도 선택한 계좌를 지킨다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1"), testAccount("acc-2")))
        val savedStateHandle = SavedStateHandle()
        val viewModel = HomeViewModel(repository, clock, savedStateHandle)
        watch(viewModel)

        viewModel.onIntent(HomeUiIntent.Overlay.OpenAccountSheet)
        viewModel.onIntent(HomeUiIntent.Overlay.SelectAccount("acc-2"))
        viewModel.onIntent(HomeUiIntent.Overlay.ConfirmAccount)

        val restored = HomeViewModel(repository, clock, savedStateHandle)
        watch(restored)

        assertEquals("acc-2", restored.uiState.value.selectedAccountId)
    }

    @Test
    fun `서버가 건 일시정지 사유 배너는 닫을 수 있고 다른 사유가 오면 다시 뜬다`() = runTest {
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", state = AccountState.PAUSED, pauseReason = "급격한 변동성 감지")))
        repository.homeDataRetryGate = CompletableDeferred()
        val viewModel = viewModel()

        assertEquals("급격한 변동성 감지", viewModel.uiState.value.pauseReason)

        viewModel.onIntent(HomeUiIntent.DismissPauseReason)

        assertNull(viewModel.uiState.value.pauseReason)

        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", state = AccountState.PAUSED, pauseReason = "서버 점검")))
        repository.homeDataRetryGate?.complete(Unit)

        assertEquals("서버 점검", viewModel.uiState.value.pauseReason)
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.Navigate(HomeNavTarget.Performance("acc-1")))

        assertEquals(listOf(HomeUiEffect.Navigate(HomeNavTarget.Performance("acc-1"))), effects)
    }

    @Test
    fun `홈 데이터의 계좌 목록에 고른 계좌가 없으면 첫 계좌로 옮기고 다시 받는다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        repository.homeDataRetryGate = CompletableDeferred()
        val viewModel = viewModel()
        assertEquals("acc-1", viewModel.uiState.value.selectedAccountId)

        repository.accountsResult = Result.success(listOf(testAccount("acc-9")))
        repository.homeDataRetryGate?.complete(Unit)

        val state = viewModel.uiState.value
        assertEquals("acc-9", state.selectedAccountId)
        assertEquals(2, repository.homeDataSubscriptions)
        assertTrue(state.content is HomeContent.Ready)
    }

    @Test
    fun `종료 요청은 확인을 띄우고 취소하면 닫힌다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()

        viewModel.onIntent(HomeUiIntent.Overlay.RequestExit)
        assertTrue(viewModel.uiState.value.exitConfirming)

        viewModel.onIntent(HomeUiIntent.Overlay.DismissExit)
        assertFalse(viewModel.uiState.value.exitConfirming)
    }

    @Test
    fun `종료를 확정하면 종료 이펙트를 보낸다`() = runTest {
        repository.accountsResult = Result.success(listOf(testAccount("acc-1")))
        val viewModel = viewModel()
        val effects = mutableListOf<HomeUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(HomeUiIntent.Overlay.RequestExit)
        viewModel.onIntent(HomeUiIntent.Navigate(HomeNavTarget.Exit))

        assertEquals(listOf(HomeUiEffect.Navigate(HomeNavTarget.Exit)), effects)
    }

    @Test
    fun `대기 계좌는 전략 목록을 받는다`() = runTest {
        repository.accountsResult =
            Result.success(listOf(testAccount("acc-1", state = AccountState.WAITING)))
        repository.portfolioResults = mapOf("acc-1" to Result.success(testPortfolio()))
        repository.strategies = sampleStrategies

        val state = viewModel().uiState.value

        assertEquals(sampleStrategies, state.dashboard?.strategies)
    }
}
