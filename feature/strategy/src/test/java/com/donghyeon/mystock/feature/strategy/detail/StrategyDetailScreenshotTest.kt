package com.donghyeon.mystock.feature.strategy.detail

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.donghyeon.mystock.design.MyStockTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
class StrategyDetailScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String, state: StrategyDetailUiState) {
        // 스켈레톤 애니메이션으로 골든이 흔들리지 않게 첫 프레임에서 잡는다
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                StrategyDetailScreen(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage("strategy-detail-$name.png")
    }

    @Test
    fun `전략에서 연 상세`() = capture("ready", sampleReadyState)

    @Test
    fun `대기 홈에서 연 상세`() = capture("waiting", sampleWaitingState)

    @Test
    fun `운용 없음`() = capture("no-run", sampleNoRunState)

    @Test
    fun `계좌 범위를 좁힌 상세`() = capture("account-scoped", sampleAccountScopedState)

    @Test
    fun `불러오는 중`() = capture("loading", sampleLoadingState)

    @Test
    fun `연결 실패`() = capture("failed", sampleFailedState)

    // 오버레이는 별도 윈도우라 화면 전체를 찍어야 함께 잡힌다
    @Test
    fun `시작 진행 중`() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                StrategyDetailScreen(state = sampleCommandingState, onIntent = {})
            }
        }
        captureScreenRoboImage("strategy-detail-commanding.png")
    }

    // 시트 컨테이너 없이 내용만 찍으므로 시트 배경을 깔아 준다
    private fun captureSheet(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                Surface(color = MyStockTheme.colors.surfaceElevated) { content() }
            }
        }
        composeRule.onRoot().captureRoboImage("strategy-detail-$name.png")
    }

    @Test
    fun `시작 시트`() = captureSheet("start-sheet") {
        StartSheetContent(state = sampleStartSheetState, onIntent = {})
    }

    @Test
    fun `계좌 범위 시트`() = captureSheet("account-sheet") {
        AccountScopeSheetContent(state = sampleAccountSheetState, onIntent = {})
    }
}
