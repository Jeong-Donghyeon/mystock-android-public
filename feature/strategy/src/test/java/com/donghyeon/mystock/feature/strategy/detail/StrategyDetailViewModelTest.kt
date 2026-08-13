package com.donghyeon.mystock.feature.strategy.detail

import androidx.lifecycle.SavedStateHandle
import com.donghyeon.mystock.feature.strategy.TestStrategyRepository
import com.donghyeon.mystock.feature.strategy.navigation.StrategyDetailKey
import com.donghyeon.mystock.feature.strategy.testDetail
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

@OptIn(ExperimentalCoroutinesApi::class)
class StrategyDetailViewModelTest {

    private lateinit var repository: TestStrategyRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = TestStrategyRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(
        key: StrategyDetailKey = StrategyDetailKey("ma-cross"),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): StrategyDetailViewModel =
        StrategyDetailViewModel(key, repository, savedStateHandle).also { viewModel ->
            backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        }

    private fun TestScope.effects(viewModel: StrategyDetailViewModel): List<StrategyDetailUiEffect> {
        val effects = mutableListOf<StrategyDetailUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }
        return effects
    }

    @Test
    fun `초기 로드는 키의 전략과 계좌로 상세를 조회한다`() = runTest {
        val state = viewModel(StrategyDetailKey("ma-cross", "acc-1")).uiState.value

        assertEquals(listOf("ma-cross" to "acc-1"), repository.detailRequests)
        assertEquals(testDetail().strategy, state.detail?.strategy)
        assertEquals("acc-1", state.startAccount?.id)
    }

    @Test
    fun `대기 홈에서 열면 로드 전부터 계좌 줄 자리를 안다`() = runTest {
        repository.detailGate = CompletableDeferred()

        assertTrue(viewModel(StrategyDetailKey("ma-cross", "acc-1")).uiState.value.startAccountExpected)
        assertFalse(viewModel().uiState.value.startAccountExpected)
    }

    @Test
    fun `전략 목록에서 열면 계좌 줄이 없다`() = runTest {
        assertNull(viewModel().uiState.value.startAccount)
    }

    @Test
    fun `초기 로드 실패 시 실패 화면이 된다`() = runTest {
        repository.detailResult = Result.failure(IOException())

        assertEquals(StrategyDetailContent.Failed, viewModel().uiState.value.content)
    }

    @Test
    fun `당겨서 새로고침 실패는 상세를 남기고 스낵바로 알린다`() = runTest {
        val viewModel = viewModel()
        val effects = effects(viewModel)
        repository.detailResult = Result.failure(IOException())

        viewModel.onIntent(StrategyDetailUiIntent.Refresh)

        assertEquals(2, repository.detailRequests.size)
        assertNotNull(viewModel.uiState.value.detail)
        assertFalse(viewModel.uiState.value.refreshing)
        assertTrue(effects.any { it is StrategyDetailUiEffect.ShowMessage })
    }

    @Test
    fun `범위 기본은 전체이고 합산은 모든 운용의 합이다`() = runTest {
        val state = viewModel().uiState.value

        assertNull(state.versionScope)
        assertNull(state.accountScope)
        assertEquals(3, state.scopedRuns.size)
        assertEquals(1_575_259L, state.summary?.totalPnlAmount)
        assertEquals(809L, state.summary?.hours)
    }

    @Test
    fun `버전 칩은 운용 내역과 합산의 범위만 좁히고 전체로 되돌릴 수 있다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(StrategyDetailUiIntent.SelectVersionScope("0.1.9"))

        val state = viewModel.uiState.value
        assertEquals(listOf("run-2"), state.scopedRuns.map { it.id })
        assertEquals(testDetail().strategy, state.detail?.strategy)

        viewModel.onIntent(StrategyDetailUiIntent.SelectVersionScope(null))

        assertEquals(3, viewModel.uiState.value.scopedRuns.size)
    }

    @Test
    fun `운용이 없는 버전을 고르면 합산이 비고 시작할 계좌는 그대로다`() = runTest {
        val viewModel = viewModel(StrategyDetailKey("ma-cross", "acc-1"))

        viewModel.onIntent(StrategyDetailUiIntent.SelectVersionScope("0.1.5"))

        val state = viewModel.uiState.value
        assertTrue(state.scopedRuns.isEmpty())
        assertNull(state.summary)
        assertEquals("acc-1", state.startAccount?.id)
    }

    @Test
    fun `계좌 범위 시트는 탭이 곧 적용이고 닫힌다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(StrategyDetailUiIntent.OpenAccountSheet)
        assertEquals(StrategyDetailSheet.AccountScope, viewModel.uiState.value.sheet)

        viewModel.onIntent(StrategyDetailUiIntent.SelectAccountScope("acc-2"))

        val state = viewModel.uiState.value
        assertNull(state.sheet)
        assertEquals("acc-2", state.accountScope)
        assertEquals(listOf("run-1"), state.scopedRuns.map { it.id })
        assertEquals(2, state.scopeAccounts.size)
    }

    @Test
    fun `계좌 범위 시트를 닫으면 범위는 그대로다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(StrategyDetailUiIntent.OpenAccountSheet)
        viewModel.onIntent(StrategyDetailUiIntent.DismissAccountSheet)

        assertNull(viewModel.uiState.value.sheet)
        assertNull(viewModel.uiState.value.accountScope)
    }

    @Test
    fun `새로 받은 상세에 없는 범위는 전체로 돌아간다`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(StrategyDetailUiIntent.SelectVersionScope("0.1.9"))
        viewModel.onIntent(StrategyDetailUiIntent.SelectAccountScope("acc-2"))
        repository.detailResult = Result.success(testDetail().copy(versions = emptyList(), runs = emptyList()))

        viewModel.onIntent(StrategyDetailUiIntent.Refresh)

        assertNull(viewModel.uiState.value.versionScope)
        assertNull(viewModel.uiState.value.accountScope)
    }

    @Test
    fun `프로세스가 다시 살아나도 고른 범위를 지킨다`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = viewModel(savedStateHandle = savedStateHandle)
        viewModel.onIntent(StrategyDetailUiIntent.SelectVersionScope("0.1.9"))
        viewModel.onIntent(StrategyDetailUiIntent.SelectAccountScope("acc-1"))

        val restored = viewModel(savedStateHandle = savedStateHandle).uiState.value

        assertEquals("0.1.9", restored.versionScope)
        assertEquals("acc-1", restored.accountScope)
    }

    @Test
    fun `시작을 확정하면 시트가 닫히고 홈으로 간다`() = runTest {
        val viewModel = viewModel(StrategyDetailKey("ma-cross", "acc-1"))
        val effects = effects(viewModel)

        viewModel.onIntent(StrategyDetailUiIntent.OpenStart)
        assertEquals(StrategyDetailSheet.Start, viewModel.uiState.value.sheet)

        viewModel.onIntent(StrategyDetailUiIntent.ConfirmStart)

        assertNull(viewModel.uiState.value.sheet)
        assertEquals(listOf("ma-cross" to "acc-1"), repository.startRequests)
        assertTrue(effects.contains(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Home)))
    }

    @Test
    fun `시작을 확정하면 응답이 올 때까지 오버레이로 막는다`() = runTest {
        repository.startGate = CompletableDeferred()
        val viewModel = viewModel(StrategyDetailKey("ma-cross", "acc-1"))
        val effects = effects(viewModel)

        viewModel.onIntent(StrategyDetailUiIntent.OpenStart)
        viewModel.onIntent(StrategyDetailUiIntent.ConfirmStart)

        val pending = viewModel.uiState.value
        assertNull(pending.sheet)
        assertTrue(pending.commanding)
        assertFalse(effects.contains(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Home)))

        repository.startGate?.complete(Unit)

        assertFalse(viewModel.uiState.value.commanding)
        assertTrue(effects.contains(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Home)))
    }

    @Test
    fun `시작 시트를 닫으면 요청 없이 화면에 남는다`() = runTest {
        val viewModel = viewModel(StrategyDetailKey("ma-cross", "acc-1"))

        viewModel.onIntent(StrategyDetailUiIntent.OpenStart)
        viewModel.onIntent(StrategyDetailUiIntent.DismissStart)

        assertNull(viewModel.uiState.value.sheet)
        assertTrue(repository.startRequests.isEmpty())
    }

    @Test
    fun `시작 실패는 스낵바로 알리고 화면에 남는다`() = runTest {
        repository.startResult = Result.failure(IOException())
        val viewModel = viewModel(StrategyDetailKey("ma-cross", "acc-1"))
        val effects = effects(viewModel)

        viewModel.onIntent(StrategyDetailUiIntent.OpenStart)
        viewModel.onIntent(StrategyDetailUiIntent.ConfirmStart)

        assertTrue(effects.any { it is StrategyDetailUiEffect.ShowMessage })
        assertFalse(effects.contains(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Home)))
        assertNotNull(viewModel.uiState.value.detail)
        assertFalse(viewModel.uiState.value.commanding)
    }

    @Test
    fun `계좌 없이 열린 상세는 시작하지 않는다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(StrategyDetailUiIntent.OpenStart)
        viewModel.onIntent(StrategyDetailUiIntent.ConfirmStart)

        assertNull(viewModel.uiState.value.sheet)
        assertTrue(repository.startRequests.isEmpty())
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(StrategyDetailUiIntent.Navigate(StrategyDetailNavTarget.Versions("ma-cross")))

        assertEquals(listOf(StrategyDetailUiEffect.Navigate(StrategyDetailNavTarget.Versions("ma-cross"))), effects)
    }
}
