package com.donghyeon.mystock.feature.settings.settings

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
class SettingsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String, state: SettingsUiState) {
        // 진행 표시 애니메이션으로 골든이 흔들리지 않게 첫 프레임에서 잡는다
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                SettingsScreen(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage("settings-$name.png")
    }

    @Test
    fun `미인증`() = capture("unauthenticated", sampleUnauthenticatedState)

    @Test
    fun `인증 완료`() = capture("authenticated", sampleAuthenticatedState)

    // 오버레이는 별도 윈도우라 화면 전체를 찍어야 함께 잡힌다
    @Test
    fun `인증 진행 중`() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                SettingsScreen(state = sampleCommandingState, onIntent = {})
            }
        }
        captureScreenRoboImage("settings-commanding.png")
    }
}
