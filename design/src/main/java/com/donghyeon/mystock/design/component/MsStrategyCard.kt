package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons

private val ConditionLabelWidth = 26.dp

data class MsCondition(val label: String, val text: String)

@Composable
fun MsStrategyCard(
    name: String,
    version: String,
    description: String,
    markets: List<String>,
    conditions: List<MsCondition>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    note: String? = null,
) {
    MsCard(modifier = modifier, onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                    Text(
                        text = name,
                        style = MyStockTheme.typography.bodyMedium,
                        color = MyStockTheme.colors.textPrimary,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Text(
                        text = version,
                        style = MyStockTheme.typography.numberCaption,
                        color = MyStockTheme.colors.textTertiary,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(
                    text = description,
                    style = MyStockTheme.typography.caption,
                    color = MyStockTheme.colors.textTertiary,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs)) {
                    markets.forEach { MsChip(text = it) }
                }
                if (onClick != null) {
                    Icon(
                        imageVector = MsIcons.ChevronRight,
                        contentDescription = null,
                        tint = MyStockTheme.colors.textTertiary,
                        modifier = Modifier.size(MyStockTheme.iconSize.md),
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
        )
        Column(
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
        ) {
            conditions.forEach { condition ->
                Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                    Text(
                        text = condition.label,
                        style = MyStockTheme.typography.caption,
                        color = MyStockTheme.colors.textTertiary,
                        modifier = Modifier.width(ConditionLabelWidth),
                    )
                    Text(
                        text = condition.text,
                        style = MyStockTheme.typography.caption,
                        color = MyStockTheme.colors.textSecondary,
                    )
                }
            }
        }
        if (note != null) {
            HorizontalDivider(
                thickness = MyStockTheme.stroke,
                color = MyStockTheme.colors.border,
                modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
            )
            Text(
                text = note,
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textSecondary,
                modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
            )
        }
    }
}

@PreviewThemes
@Composable
private fun MsStrategyCardPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MsStrategyCard(
                name = "이동평균 교차",
                version = "v0.2.0",
                description = "단기·장기 이동평균 교차로 매수·매도",
                markets = listOf("KR", "US"),
                conditions = listOf(
                    MsCondition("매수", "5일선이 20일선을 상향 돌파"),
                    MsCondition("매도", "5일선이 20일선을 하향 돌파"),
                    MsCondition("대상", "국내·미국 대형주"),
                ),
                onClick = {},
            )
            MsStrategyCard(
                name = "빈 전략 (검증용)",
                version = "v1.0.0",
                description = "매매하지 않는 검증용 전략",
                markets = listOf("KR"),
                conditions = listOf(
                    MsCondition("매수", "주문을 내지 않음"),
                    MsCondition("매도", "주문을 내지 않음"),
                ),
            )
        }
    }
}
