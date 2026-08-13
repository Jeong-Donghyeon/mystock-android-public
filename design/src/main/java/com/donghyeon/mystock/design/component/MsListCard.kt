package com.donghyeon.mystock.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.icon.MsIcons
import com.donghyeon.mystock.design.model.AccountState
import com.donghyeon.mystock.design.model.OrderSide
import com.donghyeon.mystock.design.model.RunOutcome

@Composable
fun MsListCard(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = MyStockTheme.stroke,
                    color = MyStockTheme.colors.border,
                    modifier = Modifier.padding(horizontal = MyStockTheme.spacing.lg),
                )
            }
            row()
        }
    }
}

@Composable
fun MsListRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    ListRowLayout(label = label, modifier = modifier) {
        MsAmountText(text = value)
    }
}

@Composable
fun MsSettingRow(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    emphasized: Boolean = false,
    value: String? = null,
) {
    SettingRowLayout(
        label = label,
        emphasized = emphasized,
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(role = Role.Button, onClick = onClick)
            } else {
                Modifier
            },
        ),
    ) {
        if (value != null) {
            Text(
                text = value,
                style = MyStockTheme.typography.numberCaption,
                color = MyStockTheme.colors.textTertiary,
            )
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

@Composable
fun MsSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingRowLayout(
        label = label,
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    ) {
        MsToggle(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun SettingRowLayout(
    label: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    trailing: @Composable RowScope.() -> Unit,
) {
    RowLayout(modifier = modifier.heightIn(min = MyStockTheme.spacing.touchMin)) {
        Text(
            text = label,
            style = if (emphasized) MyStockTheme.typography.bodyMedium else MyStockTheme.typography.body,
            color = MyStockTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
fun MsRunRow(
    name: String,
    version: String,
    outcome: RunOutcome,
    hours: String,
    pnlAmount: Double,
    pnlPercent: Double,
    modifier: Modifier = Modifier,
    accountName: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MyStockTheme.typography.bodyMedium,
                color = MyStockTheme.colors.textPrimary,
            )
            Text(
                text = version,
                style = MyStockTheme.typography.numberCaption,
                color = MyStockTheme.colors.textTertiary,
                modifier = Modifier.weight(1f),
            )
            when (outcome) {
                RunOutcome.RUNNING -> StatusChip(state = AccountState.RUNNING)
                RunOutcome.CLOSED -> MsChip(text = "회수")
                RunOutcome.LIQUIDATED -> MsChip(text = "청산")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
            ) {
                if (accountName != null) {
                    Text(
                        text = "$accountName · ",
                        style = MyStockTheme.typography.caption,
                        color = MyStockTheme.colors.textTertiary,
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Text(
                    text = hours,
                    style = MyStockTheme.typography.numberCaption,
                    color = MyStockTheme.colors.textTertiary,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            PnlPairText(amount = pnlAmount, percent = pnlPercent, modifier = Modifier.alignByBaseline())
        }
    }
}

@Composable
fun MsListPnlRow(
    label: String,
    amount: Double,
    percent: Double,
    modifier: Modifier = Modifier,
) {
    ListRowLayout(label = label, modifier = modifier) {
        PnlPairText(amount = amount, percent = percent)
    }
}

@Composable
fun MsHoldingRow(
    name: String,
    currentPrice: String,
    position: String,
    valuation: String,
    pnlAmount: Double,
    pnlPercent: Double,
    modifier: Modifier = Modifier,
) {
    MsShareRow(
        spacing = MyStockTheme.spacing.md,
        spread = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
        start = {
            Column {
                MsShareRow(
                    spacing = MyStockTheme.spacing.sm,
                    alignByBaseline = true,
                    start = {
                        FitText(
                            text = name,
                            style = MyStockTheme.typography.bodyMedium,
                            color = MyStockTheme.colors.textPrimary,
                        )
                    },
                    end = {
                        MsAmountText(
                            text = currentPrice,
                            style = MyStockTheme.typography.numberCaption,
                            color = MyStockTheme.colors.textTertiary,
                        )
                    },
                )
                MsAmountText(
                    text = position,
                    style = MyStockTheme.typography.numberCaption,
                    color = MyStockTheme.colors.textTertiary,
                )
            }
        },
        end = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
            ) {
                MsAmountText(text = valuation)
                PnlPairText(
                    amount = pnlAmount,
                    percent = pnlPercent,
                    style = MyStockTheme.typography.numberSmall,
                )
            }
        },
    )
}

@Composable
fun MsOrderRow(
    name: String,
    side: OrderSide,
    position: String,
    amount: String,
    modifier: Modifier = Modifier,
    realizedPnlAmount: Double? = null,
    realizedPnlPercent: Double? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MsShareRow(
                spacing = MyStockTheme.spacing.sm,
                modifier = Modifier.weight(1f),
                start = {
                    FitText(
                        text = name,
                        style = MyStockTheme.typography.bodyMedium,
                        color = MyStockTheme.colors.textPrimary,
                    )
                },
                end = {
                    if (realizedPnlAmount != null && realizedPnlPercent != null) {
                        PnlPairText(
                            amount = realizedPnlAmount,
                            percent = realizedPnlPercent,
                            style = MyStockTheme.typography.numberSmall,
                        )
                    }
                },
            )
            OrderSideChip(side = side)
        }
        MsShareRow(
            spacing = MyStockTheme.spacing.sm,
            spread = true,
            alignByBaseline = true,
            modifier = Modifier.fillMaxWidth(),
            start = {
                MsAmountText(
                    text = position,
                    style = MyStockTheme.typography.numberCaption,
                    color = MyStockTheme.colors.textTertiary,
                )
            },
            end = { MsAmountText(text = amount) },
        )
    }
}

@Composable
private fun ListRowLayout(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    RowLayout(modifier) {
        Text(
            text = label,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { value() }
    }
}

@Composable
private fun RowLayout(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@PreviewThemes
@Composable
private fun MsListCardPreview() {
    MyStockTheme {
        Column(Modifier.padding(16.dp)) {
            MsListCard(
                rows = listOf(
                    { MsListRow(label = "그대로 유지되는 보유 종목", value = "보유 종목 2") },
                    {
                        MsHoldingRow(
                            name = "삼성전자",
                            currentPrice = "71,200",
                            position = "10주 · 68,400",
                            valuation = "712,000",
                            pnlAmount = 28_000.0,
                            pnlPercent = 4.09,
                        )
                    },
                    {
                        MsHoldingRow(
                            name = "MSFT",
                            currentPrice = "\$394.20",
                            position = "3주 · \$402.60",
                            valuation = "1,587,641",
                            pnlAmount = -33_831.0,
                            pnlPercent = -2.09,
                        )
                    },
                    { MsListPnlRow(label = "평가 손익", amount = 33_370.0, percent = 0.78) },
                    {
                        MsOrderRow(
                            name = "삼성전자",
                            side = OrderSide.BUY,
                            position = "10주 · 68,400",
                            amount = "684,000",
                        )
                    },
                    {
                        MsOrderRow(
                            name = "NAVER",
                            side = OrderSide.SELL,
                            position = "4주 · 196,000",
                            amount = "784,000",
                            realizedPnlAmount = 0.0,
                            realizedPnlPercent = 0.0,
                        )
                    },
                ),
            )
        }
    }
}
