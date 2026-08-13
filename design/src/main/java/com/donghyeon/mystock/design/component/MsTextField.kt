package com.donghyeon.mystock.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

@Composable
fun MsTextField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = MyStockTheme.colors
    val shape = RoundedCornerShape(MyStockTheme.radius.md)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = MyStockTheme.typography.caption,
            color = colors.textTertiary,
        )
        BasicTextField(
            state = state,
            lineLimits = TextFieldLineLimits.SingleLine,
            // 키·식별자 입력이 주 용도라 자동 교정을 끈다
            keyboardOptions = keyboardOptions.copy(autoCorrectEnabled = false),
            textStyle = MyStockTheme.typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            modifier = Modifier.fillMaxWidth(),
            decorator = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MyStockTheme.spacing.touchMin)
                        .clip(shape)
                        .background(colors.surfaceElevated)
                        .border(MyStockTheme.stroke, colors.border, shape)
                        .padding(horizontal = MyStockTheme.spacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MyStockTheme.typography.body,
                            color = colors.textTertiary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@PreviewThemes
@Composable
private fun MsTextFieldPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MsTextField(state = rememberTextFieldState(), label = "key-1", placeholder = "키를 붙여 넣으세요")
            MsTextField(state = rememberTextFieldState("abcd-1234-efgh"), label = "key-2")
        }
    }
}
