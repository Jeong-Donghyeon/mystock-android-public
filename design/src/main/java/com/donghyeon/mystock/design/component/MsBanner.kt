package com.donghyeon.mystock.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons

@Composable
fun MsBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MyStockTheme.radius.md))
            .background(colors.warning.copy(alpha = 0.15f))
            .padding(start = MyStockTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MyStockTheme.typography.label,
            color = colors.warning,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = MyStockTheme.spacing.md),
        )
        Box(
            modifier = Modifier
                .size(MyStockTheme.spacing.touchMin)
                .clickable(role = Role.Button, onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MsIcons.Close,
                contentDescription = "닫기",
                tint = colors.warning,
                modifier = Modifier.size(MyStockTheme.iconSize.sm),
            )
        }
    }
}

@PreviewThemes
@Composable
private fun MsBannerPreview() {
    MyStockTheme {
        MsBanner(
            message = "서버가 일시정지했습니다 — 급격한 변동성 감지",
            onDismiss = {},
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}
