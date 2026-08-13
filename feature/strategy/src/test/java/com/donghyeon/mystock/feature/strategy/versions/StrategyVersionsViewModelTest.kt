package com.donghyeon.mystock.feature.strategy.versions

import com.donghyeon.mystock.feature.strategy.TestStrategyRepository
import com.donghyeon.mystock.feature.strategy.navigation.StrategyVersionsKey
import com.donghyeon.mystock.feature.strategy.testDetail
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StrategyVersionsViewModelTest {

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

    private fun TestScope.viewModel(): StrategyVersionsViewModel =
        StrategyVersionsViewModel(StrategyVersionsKey("ma-cross"), repository).also { viewModel ->
            backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        }

    @Test
    fun `초기 로드는 계좌 없이 상세를 조회해 버전과 운용을 채운다`() = runTest {
        val state = viewModel().uiState.value

        assertEquals(listOf("ma-cross" to null), repository.detailRequests)
        assertEquals(testDetail().versions, state.detail?.versions)
    }

    @Test
    fun `초기 로드 실패 시 실패 화면이 된다`() = runTest {
        repository.detailResult = Result.failure(IOException())

        assertEquals(StrategyVersionsContent.Failed, viewModel().uiState.value.content)
    }

    @Test
    fun `버전별 운용 요약은 그 버전의 운용에서 온다`() = runTest {
        val state = viewModel().uiState.value

        assertEquals(VersionRunSummary(count = 1, averageReturn = 17.93), state.runSummaryOf("0.2.0"))
        assertNull(state.runSummaryOf("0.1.5"))
    }

    @Test
    fun `당겨서 새로고침 실패는 내역을 남기고 스낵바로 알린다`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<StrategyVersionsUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }
        repository.detailResult = Result.failure(IOException())

        viewModel.onIntent(StrategyVersionsUiIntent.Refresh)

        assertEquals(2, repository.detailRequests.size)
        assertNotNull(viewModel.uiState.value.detail)
        assertTrue(effects.any { it is StrategyVersionsUiEffect.ShowMessage })
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<StrategyVersionsUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(StrategyVersionsUiIntent.Navigate(StrategyVersionsNavTarget.Back))

        assertEquals(listOf(StrategyVersionsUiEffect.Navigate(StrategyVersionsNavTarget.Back)), effects)
    }
}
