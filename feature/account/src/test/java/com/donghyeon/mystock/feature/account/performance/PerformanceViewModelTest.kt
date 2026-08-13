package com.donghyeon.mystock.feature.account.performance

import com.donghyeon.mystock.feature.account.TestPerformanceRepository
import com.donghyeon.mystock.feature.account.navigation.PerformanceKey
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceViewModelTest {

    private lateinit var repository: TestPerformanceRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = TestPerformanceRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): PerformanceViewModel =
        PerformanceViewModel(PerformanceKey("acc-1"), repository).also { viewModel ->
            backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        }

    @Test
    fun `초기 로드는 키의 계좌 성과를 조회하고 합산은 행의 합이다`() = runTest {
        repository.performanceResult = Result.success(samplePerformanceHistory)

        val state = viewModel().uiState.value

        assertEquals(listOf("acc-1"), repository.requests)
        assertEquals("토스증권", state.performance?.accountName)
        assertEquals(1_266_759L, state.summary?.totalPnlAmount)
        assertEquals(689L, state.summary?.hours)
    }

    @Test
    fun `운용이 없으면 합산도 없다`() = runTest {
        assertNull(viewModel().uiState.value.summary)
    }

    @Test
    fun `초기 로드 실패 시 실패 화면이 된다`() = runTest {
        repository.performanceResult = Result.failure(IOException())

        assertEquals(PerformanceContent.Failed, viewModel().uiState.value.content)
    }

    @Test
    fun `당겨서 새로고침 실패는 데이터를 남기고 스낵바로 알린다`() = runTest {
        repository.performanceResult = Result.success(samplePerformanceHistory)
        val viewModel = viewModel()
        val effects = mutableListOf<PerformanceUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }
        repository.performanceResult = Result.failure(IOException())

        viewModel.onIntent(PerformanceUiIntent.Refresh)

        assertEquals(2, repository.requests.size)
        assertTrue(viewModel.uiState.value.content is PerformanceContent.Ready)
        assertTrue(effects.any { it is PerformanceUiEffect.ShowMessage })
    }

    @Test
    fun `당겨서 새로고침 성공은 데이터를 새것으로 바꾼다`() = runTest {
        val viewModel = viewModel()
        repository.performanceResult = Result.success(samplePerformanceHistory)

        viewModel.onIntent(PerformanceUiIntent.Refresh)

        assertEquals("토스증권", viewModel.uiState.value.performance?.accountName)
        assertFalse(viewModel.uiState.value.refreshing)
    }

    @Test
    fun `실패 화면에서 새로고침이 또 실패하면 스낵바 없이 화면만 남긴다`() = runTest {
        repository.performanceResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = mutableListOf<PerformanceUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(PerformanceUiIntent.Refresh)

        assertEquals(PerformanceContent.Failed, viewModel.uiState.value.content)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        val viewModel = viewModel()
        val effects = mutableListOf<PerformanceUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }

        viewModel.onIntent(PerformanceUiIntent.Navigate(PerformanceNavTarget.Back))

        assertEquals(listOf(PerformanceUiEffect.Navigate(PerformanceNavTarget.Back)), effects)
    }
}
