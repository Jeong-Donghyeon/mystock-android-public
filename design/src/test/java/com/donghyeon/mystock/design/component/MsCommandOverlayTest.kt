package com.donghyeon.mystock.design.component

import androidx.activity.ComponentDialog
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.donghyeon.mystock.design.MyStockTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
class MsCommandOverlayTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show() {
        composeRule.setContent {
            MyStockTheme {
                MsCommandOverlay()
            }
        }
    }

    @Test
    fun `별도 윈도우로 뜬다`() {
        show()

        composeRule.onNode(isDialog()).assertIsDisplayed()
    }

    @Test
    fun `뒤로가기로 닫히지 않는다`() {
        show()
        val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

        dialog.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()

        assertTrue(dialog.isShowing)
        composeRule.onNode(isDialog()).assertIsDisplayed()
    }
}
