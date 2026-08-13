package com.donghyeon.mystock.feature.account.home

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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

// Android 14 글꼴 크기 설정의 최대 배율 — 여기서 들어가면 그 아래 배율은 다 들어간다
private const val MAX_FONT_SCALE = 2f

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5)
class HomeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(name: String, state: HomeUiState, fontScale: Float = 1f) {
        // 스켈레톤 애니메이션으로 골든이 흔들리지 않게 첫 프레임에서 잡는다
        composeRule.mainClock.autoAdvance = false
        // Configuration으로 바꿔야 단말과 같은 경로(비선형 스케일 포함)로 배율이 적용된다
        RuntimeEnvironment.setFontScale(fontScale)
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                HomeScreen(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage("home-$name.png")
    }

    @Test
    fun `운용 중`() = capture("running", sampleRunningState)

    @Test
    fun `운용 중 — 보유 없음`() = capture("running-empty", sampleRunningEmptyState)

    @Test
    fun `운용 중 — 당일 손익 없음`() = capture("running-no-today-pnl", sampleRunningNoTodayPnlState)

    @Test
    fun `운용 중 — 큰 금액은 줄을 바꾸지 않고 줄어든다`() = capture("running-large-amount", sampleRunningLargeAmountState)

    @Test
    fun `운용 중 — 최대 글자 배율에서도 금액은 한 줄이다`() =
        capture("running-large-amount-font-scale", sampleRunningLargeAmountState, fontScale = MAX_FONT_SCALE)

    @Test
    fun `대기 — 최대 글자 배율에서도 예수금은 한 줄이다`() =
        capture("waiting-large-amount-font-scale", sampleWaitingLargeAmountState, fontScale = MAX_FONT_SCALE)

    @Test
    fun `일시정지`() = capture("paused", samplePausedState)

    @Test
    fun `회수 중`() = capture("closing", sampleClosingState)

    @Test
    fun `청산 중`() = capture("liquidating", sampleLiquidatingState)

    @Test
    fun `대기`() = capture("waiting", sampleWaitingState)

    @Test
    fun `불러오는 중`() = capture("loading", sampleLoadingState)

    @Test
    fun `연결 실패`() = capture("failed", sampleFailedState)

    @Test
    fun `상태 미확인`() = capture("stale", sampleStaleState)

    @Test
    fun `일시정지 — 연결 끊김이 사유 위에 쌓임`() = capture("paused-stale", samplePausedStaleState)

    // 모달 컨테이너 없이 내용만 찍으므로 모달 배경을 깔아 준다
    private fun captureModal(name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                Surface(color = MyStockTheme.colors.surfaceElevated) { content() }
            }
        }
        composeRule.onRoot().captureRoboImage("home-$name.png")
    }

    private fun commandSheet(state: HomeUiState): @Composable () -> Unit = {
        CommandSheetContent(state = state, onIntent = {})
    }

    @Test
    fun `계좌 시트`() = captureModal("account-sheet") {
        AccountSheetContent(state = sampleAccountSheetState, onIntent = {})
    }

    @Test
    fun `일시정지 시트`() = captureModal("pause-sheet", commandSheet(samplePauseSheetState))

    @Test
    fun `재개 시트`() = captureModal("resume-sheet", commandSheet(sampleResumeSheetState))

    @Test
    fun `회수 시트`() = captureModal("close-sheet", commandSheet(sampleCloseSheetState))

    @Test
    fun `청산 시트`() = captureModal("liquidate-sheet", commandSheet(sampleLiquidateSheetState))

    // 다이얼로그는 별도 윈도우라 화면 전체를 찍어야 함께 잡힌다
    @Test
    fun `종료 확인`() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                HomeScreen(state = sampleExitConfirmState, onIntent = {})
            }
        }
        captureScreenRoboImage("home-exit-dialog.png")
    }

    @Test
    fun `명령 진행 중`() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = true) {
                HomeScreen(state = sampleCommandingState, onIntent = {})
            }
        }
        captureScreenRoboImage("home-commanding.png")
    }
}
