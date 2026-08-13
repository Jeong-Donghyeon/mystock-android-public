package com.donghyeon.mystock.feature.strategy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsCondition
import com.donghyeon.mystock.design.component.MsSkeletonChip
import com.donghyeon.mystock.design.component.MsSkeletonText
import com.donghyeon.mystock.design.component.MsStrategyCard
import com.donghyeon.mystock.feature.common.formatAmount
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.marketLabel
import com.donghyeon.mystock.feature.common.R as CommonR

private const val SKELETON_CONDITIONS = 3

@Composable
internal fun StrategyCard(
    strategy: Strategy,
    modifier: Modifier = Modifier,
    note: String? = null,
    onClick: (() -> Unit)? = null,
) {
    MsStrategyCard(
        name = strategy.displayName,
        version = formatVersion(strategy.version),
        description = strategy.description,
        markets = strategy.markets.map(::marketLabel),
        conditions = strategy.conditions.map { MsCondition(it.label, it.text) },
        modifier = modifier,
        onClick = onClick,
        note = note,
    )
}

@Composable
internal fun StartAccountCard(
    account: StartAccount,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MyStockTheme.typography.bodyMedium,
                    color = MyStockTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(CommonR.string.common_cash),
                    style = MyStockTheme.typography.caption,
                    color = MyStockTheme.colors.textTertiary,
                )
            }
            Text(
                text = formatAmount(account.cashAmount),
                style = MyStockTheme.typography.number,
                color = MyStockTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
internal fun StrategyCardSkeleton(modifier: Modifier = Modifier) {
    MsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MsSkeletonText(style = MyStockTheme.typography.bodyMedium, chars = 8)
                MsSkeletonText(style = MyStockTheme.typography.caption, chars = 16)
            }
            MsSkeletonChip(chars = 2)
        }
        Column(
            modifier = Modifier.padding(top = MyStockTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
        ) {
            repeat(SKELETON_CONDITIONS) {
                Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                    MsSkeletonText(style = MyStockTheme.typography.caption, chars = 2)
                    MsSkeletonText(style = MyStockTheme.typography.caption, chars = 14)
                }
            }
        }
    }
}

@Composable
internal fun StartAccountCardSkeleton(modifier: Modifier = Modifier) {
    MsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MsSkeletonText(style = MyStockTheme.typography.bodyMedium, chars = 5)
                MsSkeletonText(style = MyStockTheme.typography.caption, chars = 3)
            }
            MsSkeletonText(style = MyStockTheme.typography.number, chars = 9)
        }
    }
}
