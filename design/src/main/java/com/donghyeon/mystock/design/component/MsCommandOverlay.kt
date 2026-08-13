package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.donghyeon.mystock.design.MyStockTheme

private val RingSize = 40.dp
private val RingStroke = 4.dp

// 별도 윈도우라 아래 화면의 탭·뒤로가기·접근성 포커스를 플랫폼이 막는다. 스크림은 윈도우 dim으로 시트와 같은 값
@Composable
fun MsCommandOverlay(modifier: Modifier = Modifier) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        DisposableEffect(window) {
            window?.setDimAmount(SCRIM_ALPHA)
            onDispose {}
        }
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(RingSize),
                color = MyStockTheme.colors.accent,
                strokeWidth = RingStroke,
                trackColor = Color.Transparent,
            )
        }
    }
}
