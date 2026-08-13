package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h640dp-normal-long-notround-any-xhdpi-keyshidden-nonav")
class MsBottomSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `작은 단말에서 내용이 화면 절반보다 길어도 처음부터 끝까지 펼쳐진다`() {
        composeRule.setContent {
            MyStockTheme {
                MsBottomSheet(onDismissRequest = {}) {
                    Column {
                        Box(Modifier.fillMaxWidth().height(500.dp))
                        Text("바닥")
                    }
                }
            }
        }

        composeRule.onNodeWithText("바닥").assertIsDisplayed()
    }
}
