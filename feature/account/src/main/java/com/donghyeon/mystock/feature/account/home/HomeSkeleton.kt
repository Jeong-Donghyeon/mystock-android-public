package com.donghyeon.mystock.feature.account.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsQuickActionSkeleton
import com.donghyeon.mystock.design.component.MsSkeleton
import com.donghyeon.mystock.design.component.MsSkeletonChip
import com.donghyeon.mystock.design.component.MsSkeletonText

private const val QUICK_ACTIONS = 4
private const val STAT_COLUMNS = 3
private const val HOLDING_ROWS = 3

@Composable
internal fun HomeSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = MyStockTheme.spacing.lg,
                end = MyStockTheme.spacing.lg,
                top = MyStockTheme.spacing.md,
                bottom = MyStockTheme.spacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        StatusCardSkeleton()
        SummaryCardSkeleton()
        MsSkeletonText(
            style = MyStockTheme.typography.numberSmall,
            chars = 6,
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
        )
        HoldingsSkeleton()
    }
}

@Composable
private fun StatusCardSkeleton(modifier: Modifier = Modifier) {
    MsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MsSkeletonChip(chars = 4)
            MsSkeletonText(
                style = MyStockTheme.typography.caption,
                chars = 10,
                modifier = Modifier.weight(1f),
            )
            MsSkeleton(modifier = Modifier.size(MyStockTheme.iconSize.md))
        }
        HorizontalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(top = MyStockTheme.spacing.md),
        )
        Row(modifier = Modifier.padding(top = MyStockTheme.spacing.xs)) {
            repeat(QUICK_ACTIONS) { MsQuickActionSkeleton() }
        }
    }
}

@Composable
private fun SummaryCardSkeleton(modifier: Modifier = Modifier) {
    MsCard(modifier = modifier) {
        MsSkeletonText(style = MyStockTheme.typography.caption, chars = 4)
        MsSkeletonText(style = MyStockTheme.typography.numberLarge, chars = 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MyStockTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MsSkeletonText(style = MyStockTheme.typography.caption, chars = 4)
            MsSkeletonText(style = MyStockTheme.typography.number, chars = 9)
        }
        HorizontalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(top = MyStockTheme.spacing.md),
        )
        repeat(2) {
            Row(
                modifier = Modifier.padding(top = MyStockTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            ) {
                repeat(STAT_COLUMNS) {
                    Column(modifier = Modifier.weight(1f)) {
                        MsSkeletonText(style = MyStockTheme.typography.caption, chars = 4)
                        MsSkeletonText(style = MyStockTheme.typography.number, chars = 5)
                    }
                }
            }
        }
    }
}

@Composable
private fun HoldingsSkeleton(modifier: Modifier = Modifier) {
    MsListCard(
        modifier = modifier,
        rows = List(HOLDING_ROWS) {
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = MyStockTheme.spacing.md,
                            horizontal = MyStockTheme.spacing.lg,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        MsSkeletonText(style = MyStockTheme.typography.bodyMedium, chars = 10)
                        MsSkeletonText(style = MyStockTheme.typography.numberCaption, chars = 11)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
                    ) {
                        MsSkeletonText(style = MyStockTheme.typography.number, chars = 5)
                        MsSkeletonText(style = MyStockTheme.typography.numberSmall, chars = 9)
                    }
                }
            }
        },
    )
}
