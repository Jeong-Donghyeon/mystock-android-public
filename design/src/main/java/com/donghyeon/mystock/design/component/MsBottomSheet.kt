package com.donghyeon.mystock.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

private val SheetTopRadius = 20.dp
private val GrabBarWidth = 36.dp
private val GrabBarHeight = 4.dp
internal const val SCRIM_ALPHA = 0.55f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        // 작은 단말에서 내용이 길면 절반 높이에서 멈추므로 항상 끝까지 펼친다
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MyStockTheme.colors.surfaceElevated,
        contentColor = MyStockTheme.colors.textPrimary,
        scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA),
        shape = RoundedCornerShape(topStart = SheetTopRadius, topEnd = SheetTopRadius),
        dragHandle = { GrabBar() },
        content = content,
    )
}

@Composable
private fun GrabBar() {
    Box(
        Modifier
            .padding(vertical = MyStockTheme.spacing.sm)
            .size(width = GrabBarWidth, height = GrabBarHeight)
            .clip(RoundedCornerShape(MyStockTheme.radius.full))
            .background(MyStockTheme.colors.border),
    )
}
