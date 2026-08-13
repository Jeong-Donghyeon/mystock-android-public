package com.donghyeon.mystock.feature.account.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.core.model.AccountPerformance
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsEmptyCard
import com.donghyeon.mystock.design.component.MsFailedContent
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsRunRow
import com.donghyeon.mystock.design.component.MsSectionLabel
import com.donghyeon.mystock.design.component.MsSkeletonChip
import com.donghyeon.mystock.design.component.MsSkeletonText
import com.donghyeon.mystock.design.component.MsSummaryCard
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.account.R
import com.donghyeon.mystock.feature.common.RunSummary
import com.donghyeon.mystock.feature.common.formatHours
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.summaryStats
import com.donghyeon.mystock.feature.common.toUi
import com.donghyeon.mystock.feature.common.R as CommonR

private const val SKELETON_ROWS = 3
private const val STAT_COLUMNS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    state: PerformanceUiState,
    onIntent: (PerformanceUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsBackTopBar(
                title = stringResource(R.string.performance_title),
                subtitle = state.performance?.accountName,
                reserveSubtitle = state.content is PerformanceContent.Loading,
                onBack = { onIntent(PerformanceUiIntent.Navigate(PerformanceNavTarget.Back)) },
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onIntent(PerformanceUiIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val content = state.content) {
                    PerformanceContent.Loading -> PerformanceSkeleton()
                    PerformanceContent.Failed -> MsFailedContent()
                    is PerformanceContent.Ready ->
                        PerformanceBody(performance = content.performance, summary = state.summary)
                }
            }
        }
    }
}

@Composable
private fun PerformanceBody(
    performance: AccountPerformance,
    summary: RunSummary?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding()),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        if (summary == null) {
            MsEmptyCard(stringResource(CommonR.string.common_no_run))
            return
        }
        MsSummaryCard(
            label = stringResource(CommonR.string.common_total_pnl),
            amount = summary.totalPnlAmount.toDouble(),
            stats = summaryStats(summary).map { (label, value) -> stringResource(label) to value },
        )
        MsSectionLabel(stringResource(CommonR.string.common_runs_count, performance.runs.size))
        MsListCard(
            rows = performance.runs.map { run ->
                {
                    // 계좌명은 제목이 말하므로 행에 두지 않는다
                    MsRunRow(
                        name = run.strategyName,
                        version = formatVersion(run.version),
                        outcome = run.outcome.toUi(),
                        hours = formatHours(run.hours),
                        pnlAmount = run.pnl.amount.toDouble(),
                        pnlPercent = run.pnl.percent,
                    )
                }
            },
        )
    }
}

@Composable
private fun contentPadding() = PaddingValues(
    start = MyStockTheme.spacing.lg,
    end = MyStockTheme.spacing.lg,
    top = MyStockTheme.spacing.md,
    bottom = MyStockTheme.spacing.xl,
)

@Composable
private fun PerformanceSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding()),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        MsCard {
            MsSkeletonText(style = MyStockTheme.typography.caption, chars = 5)
            MsSkeletonText(style = MyStockTheme.typography.numberLarge, chars = 9)
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
        MsSkeletonText(
            style = MyStockTheme.typography.numberSmall,
            chars = 6,
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
        )
        MsListCard(
            rows = List(SKELETON_ROWS) {
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MsSkeletonText(style = MyStockTheme.typography.bodyMedium, chars = 7)
                            MsSkeletonText(
                                style = MyStockTheme.typography.numberCaption,
                                chars = 5,
                                modifier = Modifier.weight(1f),
                            )
                            MsSkeletonChip(chars = 3)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                            MsSkeletonText(
                                style = MyStockTheme.typography.numberCaption,
                                chars = 3,
                                modifier = Modifier.weight(1f),
                            )
                            MsSkeletonText(style = MyStockTheme.typography.number, chars = 14)
                        }
                    }
                }
            },
        )
    }
}

@PreviewThemes
@Composable
private fun PerformancePreview() {
    MyStockTheme {
        PerformanceScreen(state = sampleReadyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun PerformanceEmptyPreview() {
    MyStockTheme {
        PerformanceScreen(state = sampleEmptyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun PerformanceLoadingPreview() {
    MyStockTheme {
        PerformanceScreen(state = sampleLoadingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun PerformanceFailedPreview() {
    MyStockTheme {
        PerformanceScreen(state = sampleFailedState, onIntent = {})
    }
}
