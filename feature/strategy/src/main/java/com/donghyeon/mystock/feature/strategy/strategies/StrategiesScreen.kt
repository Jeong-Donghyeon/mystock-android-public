package com.donghyeon.mystock.feature.strategy.strategies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsFailedContent
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.strategy.R
import com.donghyeon.mystock.feature.strategy.StrategyCard
import com.donghyeon.mystock.feature.strategy.StrategyCardSkeleton

private const val SKELETON_CARDS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategiesScreen(
    state: StrategiesUiState,
    onIntent: (StrategiesUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsBackTopBar(
                title = stringResource(R.string.strategies_title),
                onBack = { onIntent(StrategiesUiIntent.Navigate(StrategiesNavTarget.Back)) },
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onIntent(StrategiesUiIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val content = state.content) {
                    StrategiesContent.Loading -> StrategiesSkeleton()
                    StrategiesContent.Failed -> MsFailedContent()
                    is StrategiesContent.Ready -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = contentPadding(),
                        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
                    ) {
                        items(content.strategies, key = { it.key }) { strategy ->
                            StrategyCard(
                                strategy = strategy,
                                onClick = {
                                    onIntent(StrategiesUiIntent.Navigate(StrategiesNavTarget.Detail(strategy.key)))
                                },
                            )
                        }
                    }
                }
            }
        }
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
private fun StrategiesSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding()),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        repeat(SKELETON_CARDS) { StrategyCardSkeleton() }
    }
}

@PreviewThemes
@Composable
private fun StrategiesPreview() {
    MyStockTheme {
        StrategiesScreen(state = sampleReadyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategiesLoadingPreview() {
    MyStockTheme {
        StrategiesScreen(state = sampleLoadingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun StrategiesFailedPreview() {
    MyStockTheme {
        StrategiesScreen(state = sampleFailedState, onIntent = {})
    }
}
