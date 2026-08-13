package com.donghyeon.mystock.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons
import com.donghyeon.mystock.design.model.AccountState
import com.donghyeon.mystock.design.model.OrderSide

// 칩 안 아이콘(화살표·셰브런)은 아이콘 스케일 밖의 컴포넌트 스펙 치수다
private val ChipIconSize = 14.dp
private val DotSize = 6.dp

@Composable
fun StatusChip(
    state: AccountState,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    val (dotColor, label) = when (state) {
        AccountState.RUNNING -> colors.success to "운용 중"
        AccountState.CLOSING -> colors.warning to "회수 중"
        AccountState.LIQUIDATING -> colors.warning to "청산 중"
        AccountState.PAUSED -> colors.danger to "일시정지"
        AccountState.WAITING -> colors.textSecondary to "대기"
    }
    ChipSurface(modifier) {
        Dot(dotColor)
        ChipText(label)
    }
}

@Composable
fun KeyAuthChip(
    authenticated: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    ChipSurface(modifier) {
        Dot(if (authenticated) colors.success else colors.warning)
        ChipText(if (authenticated) "인증 완료" else "미인증")
    }
}

@Composable
fun OrderSideChip(
    side: OrderSide,
    modifier: Modifier = Modifier,
) {
    val (icon, label) = when (side) {
        OrderSide.BUY -> MsIcons.Buy to "매수"
        OrderSide.SELL -> MsIcons.Sell to "매도"
    }
    ChipSurface(modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyStockTheme.colors.success,
            modifier = Modifier.size(ChipIconSize),
        )
        ChipText(label)
    }
}

@Composable
fun MsChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tabular: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    ChipSurface(modifier, selected = selected, onClick = onClick) {
        ChipText(text, tabular = tabular)
    }
}

@Composable
fun MsDropdownChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChipSurface(modifier, selected = selected, outlined = true, onClick = onClick) {
        ChipText(text)
        Icon(
            imageVector = MsIcons.ChevronDown,
            contentDescription = null,
            tint = MyStockTheme.colors.textSecondary,
            modifier = Modifier.size(ChipIconSize),
        )
    }
}

@Composable
private fun ChipSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    outlined: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MyStockTheme.colors
    val shape = RoundedCornerShape(MyStockTheme.radius.full)
    val borderColor = when {
        selected -> colors.accent
        outlined -> colors.border
        else -> Color.Transparent
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(MyStockTheme.stroke, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = MyStockTheme.spacing.md, vertical = MyStockTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ChipText(text: String, tabular: Boolean = false) {
    Text(
        text = text,
        style = if (tabular) MyStockTheme.typography.numberSmall else MyStockTheme.typography.label,
        color = MyStockTheme.colors.textPrimary,
    )
}

@Composable
private fun Dot(color: Color) {
    Box(
        Modifier
            .size(DotSize)
            .clip(CircleShape)
            .background(color),
    )
}

@PreviewThemes
@Composable
private fun BadgesPreview() {
    MyStockTheme {
        FlowRow(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusChip(AccountState.RUNNING)
            StatusChip(AccountState.CLOSING)
            StatusChip(AccountState.LIQUIDATING)
            StatusChip(AccountState.PAUSED)
            StatusChip(AccountState.WAITING)
            KeyAuthChip(authenticated = true)
            KeyAuthChip(authenticated = false)
            OrderSideChip(OrderSide.BUY)
            OrderSideChip(OrderSide.SELL)
            MsChip("KR")
            MsChip("US")
            MsChip("청산")
            MsChip("v0.2.0", selected = true, tabular = true)
            MsDropdownChip("전체 계좌", selected = false, onClick = {})
            MsDropdownChip("토스증권", selected = true, onClick = {})
        }
    }
}
