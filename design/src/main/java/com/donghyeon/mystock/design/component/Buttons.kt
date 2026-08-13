package com.donghyeon.mystock.design.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

private val ButtonHeight = 40.dp

@Composable
fun MsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(MyStockTheme.radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = MyStockTheme.spacing.lg),
        modifier = modifier.heightIn(min = ButtonHeight),
    ) {
        Text(text, style = MyStockTheme.typography.subtitle)
    }
}

@Composable
fun MsSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(MyStockTheme.radius.md),
        border = BorderStroke(MyStockTheme.stroke, colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.textPrimary,
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = MyStockTheme.spacing.lg),
        modifier = modifier.heightIn(min = ButtonHeight),
    ) {
        Text(text, style = MyStockTheme.typography.subtitle)
    }
}

@Composable
fun MsSheetActions(
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
    ) {
        MsSecondaryButton(text = "취소", onClick = onCancel, modifier = Modifier.weight(1f))
        MsPrimaryButton(text = confirmText, onClick = onConfirm, modifier = Modifier.weight(1f))
    }
}

@PreviewThemes
@Composable
private fun ButtonsPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MsPrimaryButton("시작", {})
                MsSecondaryButton("취소", {})
            }
            MsSheetActions(confirmText = "청산", onConfirm = {}, onCancel = {})
        }
    }
}
