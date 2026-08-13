package com.donghyeon.mystock.feature.strategy.strategies

import com.donghyeon.mystock.feature.strategy.TestStrategyRepository
import com.donghyeon.mystock.feature.strategy.testStrategies
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StrategiesViewModelTest {

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

    private fun TestScope.viewModel(): StrategiesViewModel =
        StrategiesViewModel(repository).also { viewModel ->
            backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        }

    @Test
    fun `초기 로드 성공 시 전략 목록이 채워진다`() = runTest {
        val state = viewModel().uiState.value

        assertEquals(StrategiesContent.Ready(testStrategies()), state.content)
    }

    @Test
    fun `초기 로드 실패 시 실패 화면이 된다`() = runTest {
        repository.strategiesResult = Result.failure(IOException())

        assertEquals(StrategiesContent.Failed, viewModel().uiState.value.content)
    }

    @Test
    fun `당겨서 새로고침하면 다시 조회하고 실패해도 목록은 남긴다`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<StrategiesUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }
        repository.strategiesResult = Result.failure(IOException())

        viewModel.onIntent(StrategiesUiIntent.Refresh)

        assertEquals(2, repository.strategiesCallCount)
        assertEquals(StrategiesContent.Ready(testStrategies()), viewModel.uiState.value.content)
        assertTrue(effects.any { it is StrategiesUiEffect.ShowMessage })
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<StrategiesUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(StrategiesUiIntent.Navigate(StrategiesNavTarget.Detail("ma-cross")))

        assertEquals(listOf(StrategiesUiEffect.Navigate(StrategiesNavTarget.Detail("ma-cross"))), effects)
    }
}
