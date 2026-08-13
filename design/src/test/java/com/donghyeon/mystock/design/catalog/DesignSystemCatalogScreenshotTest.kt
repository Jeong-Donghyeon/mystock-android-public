package com.donghyeon.mystock.design.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.donghyeon.mystock.design.MyStockTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CATALOG_TAG = "catalog"

// 페이지가 통째로 그려지도록 단말 높이를 넉넉히 잡는다 — 뷰포트 밖은 그려지지 않아 골든에 담기지 않는다
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h2900dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class DesignSystemCatalogScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(page: CatalogPage, theme: String, darkTheme: Boolean) {
        // 스켈레톤 애니메이션으로 골든이 흔들리지 않게 첫 프레임에서 잡는다
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyStockTheme(darkTheme = darkTheme) {
                // 단말이 아니라 페이지 크기만큼만 담기도록 감싼 노드를 찍는다
                Box(Modifier.testTag(CATALOG_TAG)) { DesignSystemCatalog(page) }
            }
        }
        composeRule.onNodeWithTag(CATALOG_TAG)
            .captureRoboImage("catalog-${page.key}-$theme.png")
    }

    @Test
    fun `토큰 다크`() = capture(CatalogPage.TOKEN, "dark", darkTheme = true)

    @Test
    fun `토큰 라이트`() = capture(CatalogPage.TOKEN, "light", darkTheme = false)

    @Test
    fun `컨트롤 다크`() = capture(CatalogPage.CONTROL, "dark", darkTheme = true)

    @Test
    fun `컨트롤 라이트`() = capture(CatalogPage.CONTROL, "light", darkTheme = false)

    @Test
    fun `안내 다크`() = capture(CatalogPage.FEEDBACK, "dark", darkTheme = true)

    @Test
    fun `안내 라이트`() = capture(CatalogPage.FEEDBACK, "light", darkTheme = false)

    @Test
    fun `카드 다크`() = capture(CatalogPage.CARD, "dark", darkTheme = true)

    @Test
    fun `카드 라이트`() = capture(CatalogPage.CARD, "light", darkTheme = false)
}
