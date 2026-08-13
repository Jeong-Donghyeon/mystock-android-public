package com.donghyeon.mystock.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons

// 계좌명이 없으면 로딩·오류 화면이라 높이만 확보하고 액션도 두지 않는다 — 데이터 전환에 상단바가 튀지 않게
@Composable
fun MsAccountTopBar(
    accountName: String?,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MyStockTheme.spacing.lg,
                    top = MyStockTheme.spacing.sm,
                    end = MyStockTheme.spacing.sm,
                    bottom = MyStockTheme.spacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = MyStockTheme.spacing.touchMin)
                    .clip(RoundedCornerShape(MyStockTheme.radius.md))
                    .then(
                        if (accountName != null) {
                            Modifier.clickable(role = Role.Button, onClick = onAccountClick)
                        } else {
                            Modifier
                        },
                    )
                    .padding(
                        top = MyStockTheme.spacing.xs,
                        end = MyStockTheme.spacing.sm,
                        bottom = MyStockTheme.spacing.xs
                    ),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (accountName != null) {
                    Text(
                        text = accountName,
                        style = MyStockTheme.typography.title,
                        color = MyStockTheme.colors.textPrimary,
                    )
                    Icon(
                        imageVector = MsIcons.ChevronDown,
                        contentDescription = null,
                        tint = MyStockTheme.colors.textSecondary,
                        modifier = Modifier.size(MyStockTheme.iconSize.md),
                    )
                }
            }
            if (accountName != null) {
                Row(content = actions)
            }
        }
        HorizontalDivider(thickness = MyStockTheme.stroke, color = MyStockTheme.colors.border)
    }
}

@Composable
fun MsTopBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(MyStockTheme.spacing.touchMin)
            .clip(RoundedCornerShape(MyStockTheme.radius.full))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MyStockTheme.colors.textSecondary,
            modifier = Modifier.size(MyStockTheme.iconSize.md),
        )
    }
}

@Preview
@Composable
private fun MsAccountTopBarPreview() {
    MyStockTheme {
        Column {
            MsAccountTopBar(accountName = "토스증권", onAccountClick = {}) {
                MsTopBarAction(icon = MsIcons.Strategy, contentDescription = "전략", onClick = {})
                MsTopBarAction(icon = MsIcons.Settings, contentDescription = "설정", onClick = {})
            }
            MsAccountTopBar(accountName = null, onAccountClick = {})
        }
    }
}
