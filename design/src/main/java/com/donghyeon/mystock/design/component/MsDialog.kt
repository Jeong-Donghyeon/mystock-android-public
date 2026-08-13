package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.donghyeon.mystock.design.MyStockTheme

private val DialogRadius = 20.dp

@Composable
fun MsDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            color = MyStockTheme.colors.surfaceElevated,
            contentColor = MyStockTheme.colors.textPrimary,
            shape = RoundedCornerShape(DialogRadius),
            content = { Column(content = content) },
        )
    }
}
