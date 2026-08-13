package com.donghyeon.mystock.feature.strategy.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsChip
import com.donghyeon.mystock.design.component.MsCommandOverlay
import com.donghyeon.mystock.design.component.MsDropdownChip
import com.donghyeon.mystock.design.component.MsEmptyCard
import com.donghyeon.mystock.design.component.MsFailedContent
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsPrimaryButton
import com.donghyeon.mystock.design.component.MsRunRow
import com.donghyeon.mystock.design.component.MsSectionLabel
import com.donghyeon.mystock.design.component.MsSettingRow
import com.donghyeon.mystock.design.component.MsSkeletonChip
import com.donghyeon.mystock.design.component.MsSkeletonText
import com.donghyeon.mystock.design.component.MsSummaryCard
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.common.formatHours
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.summaryStats
import com.donghyeon.mystock.feature.common.toUi
import com.donghyeon.mystock.feature.strategy.R
import com.donghyeon.mystock.feature.strategy.StartAccountCard
import com.donghyeon.mystock.feature.strategy.StartAccountCardSkeleton
import com.donghyeon.mystock.feature.strategy.StrategyCard
import com.donghyeon.mystock.feature.strategy.StrategyCardSkeleton
import com.donghyeon.mystock.feature.common.R as CommonR

private const val STAT_COLUMNS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyDetailScreen(
    state: StrategyDetailUiState,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsBackTopBar(
                title = stringResource(R.string.strategy_detail_title),
                onBack = { onIntent(StrategyDetailUiIntent.Navigate(StrategyDetailNavTarget.Back)) },
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onIntent(StrategyDetailUiIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val content = state.content) {
                    StrategyDetailContent.Loading -> DetailSkeleton(withAccount = state.startAccountExpected)
                    StrategyDetailContent.Failed -> MsFailedContent()
                    is StrategyDetailContent.Ready -> DetailBody(
                        state = state,
                        detail = content.detail,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }

    when (state.sheet) {
        StrategyDetailSheet.AccountScope -> AccountScopeSheet(state = state, onIntent = onIntent)
        StrategyDetailSheet.Start -> StartSheet(state = state, onIntent = onIntent)
        null -> Unit
    }

    if (state.commanding) MsCommandOverlay()
}

@Composable
private fun DetailBody(
    state: StrategyDetailUiState,
    detail: StrategyDetail,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startAccount = detail.startAccount
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = MyStockTheme.spacing.lg,
                end = MyStockTheme.spacing.lg,
                top = MyStockTheme.spacing.md,
                bottom = MyStockTheme.spacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        if (startAccount != null) {
            StartAccountCard(account = startAccount)
        }
        StrategyCard(strategy = detail.strategy, note = detail.note)
        MsListCard(
            rows = listOf {
                MsSettingRow(
                    label = stringResource(R.string.strategy_detail_versions),
                    emphasized = true,
                    onClick = {
                        onIntent(
                            StrategyDetailUiIntent.Navigate(
                                StrategyDetailNavTarget.Versions(detail.strategy.key),
                            ),
                        )
                    },
                )
            },
        )
        ScopeChips(state = state, detail = detail, onIntent = onIntent)
        val summary = state.summary
        if (summary != null) {
            MsSummaryCard(
                label = stringResource(CommonR.string.common_total_pnl),
                amount = summary.totalPnlAmount.toDouble(),
                stats = summaryStats(summary).map { (label, value) -> stringResource(label) to value },
            )
            MsSectionLabel(stringResource(CommonR.string.common_runs_count, state.scopedRuns.size))
            MsListCard(
                rows = state.scopedRuns.map { run ->
                    {
                        MsRunRow(
                            name = run.strategyName,
                            version = formatVersion(run.version),
                            outcome = run.outcome.toUi(),
                            accountName = run.accountName,
                            hours = formatHours(run.hours),
                            pnlAmount = run.pnl.amount.toDouble(),
                            pnlPercent = run.pnl.percent,
                        )
                    }
                },
            )
        } else {
            MsEmptyCard(stringResource(CommonR.string.common_no_run))
        }
        if (startAccount != null) {
            MsPrimaryButton(
                text = stringResource(R.string.strategy_detail_start),
                onClick = { onIntent(StrategyDetailUiIntent.OpenStart) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MyStockTheme.spacing.sm),
            )
        }
    }
}

@Composable
private fun ScopeChips(
    state: StrategyDetailUiState,
    detail: StrategyDetail,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountName = state.scopeAccounts.firstOrNull { it.id == state.accountScope }?.name
    val edge = MyStockTheme.spacing.lg
    // 칩 줄은 화면 여백 밖까지 스크롤되도록 좌우 패딩을 되돌린다
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = MyStockTheme.spacing.sm)
            .layout { measurable, constraints ->
                val extra = (edge * 2).roundToPx()
                val placeable = measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + extra))
                layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
            }
            .padding(start = edge),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MsDropdownChip(
            text = accountName ?: stringResource(R.string.strategy_detail_all_accounts),
            selected = accountName != null,
            onClick = { onIntent(StrategyDetailUiIntent.OpenAccountSheet) },
        )
        VerticalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(vertical = MyStockTheme.spacing.xs),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(end = edge),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        ) {
            MsChip(
                text = stringResource(R.string.strategy_detail_all),
                selected = state.versionScope == null,
                onClick = { onIntent(StrategyDetailUiIntent.SelectVersionScope(null)) },
            )
            detail.versions.forEach { version ->
                MsChip(
                    text = formatVersion(version.version),
                    selected = state.versionScope == version.version,
                    tabular = true,
                    onClick = { onIntent(StrategyDetailUiIntent.SelectVersionScope(version.version)) },
                )
            }
        }
    }
}

@Composable
private fun DetailSkeleton(
    withAccount: Boolean,
    modifier: Modifier = Modifier,
) {
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
        if (withAccount) {
            StartAccountCardSkeleton()
        }
        StrategyCardSkeleton()
        MsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MsSkeletonText(style = MyStockTheme.typography.bodyMedium, chars = 5, modifier = Modifier.weight(1f))
                MsSkeletonText(style = MyStockTheme.typography.caption, chars = 1)
            }
        }
        Row(
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        ) {
            repeat(STAT_COLUMNS) { MsSkeletonChip(chars = 5) }
        }
        MsCard {
            MsSkeletonText(style = MyStockTheme.typography.caption, chars = 5)
            MsSkeletonText(style = MyStockTheme.typography.numberLarge, chars = 9)
        }
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleReadyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailWaitingPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleWaitingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailNoRunPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleNoRunState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailAccountScopedPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleAccountScopedState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailLoadingPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleLoadingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyDetailFailedPreview() {
    MyStockTheme {
        StrategyDetailScreen(state = sampleFailedState, onIntent = {})
    }
}
