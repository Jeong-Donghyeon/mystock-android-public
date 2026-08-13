package com.donghyeon.mystock.feature.strategy.versions

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
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.core.model.StrategyVersion
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsFailedContent
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsSkeletonText
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.common.formatDate
import com.donghyeon.mystock.feature.common.formatSignedPercent
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.strategy.R

private const val SKELETON_ROWS = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyVersionsScreen(
    state: StrategyVersionsUiState,
    onIntent: (StrategyVersionsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsBackTopBar(
                title = stringResource(R.string.strategy_versions_title),
                subtitle = state.detail?.strategy?.displayName,
                reserveSubtitle = state.content is StrategyVersionsContent.Loading,
                onBack = { onIntent(StrategyVersionsUiIntent.Navigate(StrategyVersionsNavTarget.Back)) },
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onIntent(StrategyVersionsUiIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val content = state.content) {
                    StrategyVersionsContent.Loading -> VersionsSkeleton()
                    StrategyVersionsContent.Failed -> MsFailedContent()
                    is StrategyVersionsContent.Ready -> VersionsBody(state = state, versions = content.detail.versions)
                }
            }
        }
    }
}

@Composable
private fun VersionsBody(
    state: StrategyVersionsUiState,
    versions: List<StrategyVersion>,
    modifier: Modifier = Modifier,
) {
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
    ) {
        MsListCard(
            rows = versions.map { version ->
                {
                    VersionRow(version = version, runSummary = state.runSummaryOf(version.version))
                }
            },
        )
    }
}

@Composable
private fun VersionRow(
    version: StrategyVersion,
    runSummary: VersionRunSummary?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
            Text(
                text = formatVersion(version.version),
                style = MyStockTheme.typography.number,
                color = MyStockTheme.colors.textPrimary,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = formatDate(version.releasedAt),
                style = MyStockTheme.typography.numberCaption,
                color = MyStockTheme.colors.textTertiary,
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
            )
            Text(
                text = runSummary?.let {
                    stringResource(
                        R.string.strategy_versions_run_summary,
                        it.count,
                        formatSignedPercent(it.averageReturn),
                    )
                } ?: stringResource(R.string.strategy_versions_no_run),
                style = if (runSummary == null) {
                    MyStockTheme.typography.caption
                } else {
                    MyStockTheme.typography.numberCaption
                },
                color = MyStockTheme.colors.textTertiary,
                modifier = Modifier.alignByBaseline(),
            )
        }
        Text(
            text = version.note,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun VersionsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = MyStockTheme.spacing.lg,
                end = MyStockTheme.spacing.lg,
                top = MyStockTheme.spacing.md,
                bottom = MyStockTheme.spacing.xl,
            ),
    ) {
        MsListCard(
            rows = List(SKELETON_ROWS) {
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MyStockTheme.spacing.md, horizontal = MyStockTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                            MsSkeletonText(style = MyStockTheme.typography.number, chars = 6)
                            MsSkeletonText(
                                style = MyStockTheme.typography.numberCaption,
                                chars = 8,
                                modifier = Modifier.weight(1f),
                            )
                            MsSkeletonText(style = MyStockTheme.typography.numberCaption, chars = 12)
                        }
                        MsSkeletonText(style = MyStockTheme.typography.caption, chars = 20)
                    }
                }
            },
        )
    }
}

@PreviewThemes
@Composable
private fun StrategyVersionsPreview() {
    MyStockTheme {
        StrategyVersionsScreen(state = sampleReadyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyVersionsLoadingPreview() {
    MyStockTheme {
        StrategyVersionsScreen(state = sampleLoadingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategyVersionsFailedPreview() {
    MyStockTheme {
        StrategyVersionsScreen(state = sampleFailedState, onIntent = {})
    }
}
