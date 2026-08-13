package com.donghyeon.mystock.feature.strategy.strategies

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.donghyeon.mystock.design.MyStockTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
class StrategiesScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String, state: StrategiesUiState) {
        // 스켈레톤 애니메이션으로 골든이 흔들리지 않게 첫 프레임에서 잡는다
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                StrategiesScreen(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage("strategies-$name.png")
    }

    @Test
    fun `전략 목록`() = capture("ready", sampleReadyState)

    @Test
    fun `불러오는 중`() = capture("loading", sampleLoadingState)

    @Test
    fun `연결 실패`() = capture("failed", sampleFailedState)
}
