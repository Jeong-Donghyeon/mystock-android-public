package com.donghyeon.mystock.design.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("MsSnackbarHost를 그리는 곳에서 LocalSnackbarHostState를 제공해야 한다")
}

class SnackbarController(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun show(message: String) {
        job?.cancel()
        job = scope.launch { hostState.showSnackbar(message) }
    }
}

@Composable
fun MsSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        MsSnackbar(message = data.visuals.message)
    }
}

// M3 Snackbar는 그림자를 강제하므로 표면을 직접 그린다
@Composable
fun MsSnackbar(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(MyStockTheme.spacing.md),
        shape = RoundedCornerShape(MyStockTheme.radius.md),
        color = MyStockTheme.colors.surfaceElevated,
        contentColor = MyStockTheme.colors.textPrimary,
        border = BorderStroke(MyStockTheme.stroke, MyStockTheme.colors.border),
    ) {
        Text(
            text = message,
            style = MyStockTheme.typography.label,
            modifier = Modifier.padding(
                horizontal = MyStockTheme.spacing.lg,
                vertical = MyStockTheme.spacing.md,
            ),
        )
    }
}

@PreviewThemes
@Composable
private fun MsSnackbarPreview() {
    MyStockTheme {
        MsSnackbar(
            message = "중지하지 못했습니다 — 잠시 후 다시 시도해 주세요",
            modifier = Modifier.width(360.dp),
        )
    }
}
