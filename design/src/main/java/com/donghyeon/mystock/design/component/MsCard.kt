package com.donghyeon.mystock.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

@Composable
fun MsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(MyStockTheme.spacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MyStockTheme.colors
    val shape = RoundedCornerShape(MyStockTheme.radius.md)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(MyStockTheme.stroke, if (selected) colors.accent else colors.border, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        content = content,
    )
}

private const val SUMMARY_COLUMNS = 3

@Composable
fun MsSummaryCard(
    label: String,
    amount: Double,
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        Text(
            text = label,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
        )
        PnlText(
            value = amount,
            format = PnlFormat.AMOUNT,
            style = MyStockTheme.typography.numberLarge,
        )
        HorizontalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(top = MyStockTheme.spacing.md),
        )
        stats.chunked(SUMMARY_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.padding(top = MyStockTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            ) {
                row.forEach { (statLabel, value) ->
                    MsStat(label = statLabel, value = value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@PreviewThemes
@Composable
private fun MsCardPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MsCard {
                Text(
                    text = "기본 카드",
                    style = MyStockTheme.typography.subtitle,
                    color = MyStockTheme.colors.textPrimary,
                )
            }
            MsCard(onClick = {}) {
                Text(
                    text = "탭 가능한 카드",
                    style = MyStockTheme.typography.subtitle,
                    color = MyStockTheme.colors.textPrimary,
                )
            }
            MsCard(selected = true) {
                Text(
                    text = "선택된 카드",
                    style = MyStockTheme.typography.subtitle,
                    color = MyStockTheme.colors.textPrimary,
                )
            }
            MsSummaryCard(
                label = "합산 손익",
                amount = 1_575_259.0,
                stats = listOf("운용시간" to "809h", "승률" to "100%", "중위 수익률" to "+7.80%"),
            )
        }
    }
}
