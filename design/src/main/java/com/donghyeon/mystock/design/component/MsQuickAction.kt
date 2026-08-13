package com.donghyeon.mystock.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons

private val QuickActionHeight = 56.dp

@Composable
fun RowScope.MsQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(MyStockTheme.radius.md))
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = QuickActionHeight)
            .padding(vertical = MyStockTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MyStockTheme.spacing.xs,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyStockTheme.colors.textSecondary,
            modifier = Modifier.size(MyStockTheme.iconSize.md),
        )
        Text(
            text = label,
            style = MyStockTheme.typography.label,
            color = MyStockTheme.colors.textSecondary,
        )
    }
}

@Composable
fun RowScope.MsQuickActionSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .weight(1f)
            .heightIn(min = QuickActionHeight)
            .padding(vertical = MyStockTheme.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MyStockTheme.spacing.xs,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        MsSkeleton(modifier = Modifier.size(MyStockTheme.iconSize.md))
        MsSkeletonText(style = MyStockTheme.typography.label, chars = 3)
    }
}

@PreviewThemes
@Composable
private fun MsQuickActionPreview() {
    MyStockTheme {
        Row(Modifier.padding(16.dp)) {
            MsQuickAction(icon = MsIcons.Pause, label = "일시정지", onClick = {})
            MsQuickAction(icon = MsIcons.Recall, label = "회수", onClick = {})
            MsQuickAction(icon = MsIcons.Liquidate, label = "청산", onClick = {})
            MsQuickAction(icon = MsIcons.BellFilled, label = "알림", onClick = {})
        }
    }
}
