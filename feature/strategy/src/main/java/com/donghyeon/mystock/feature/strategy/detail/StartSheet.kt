package com.donghyeon.mystock.feature.strategy.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBottomSheet
import com.donghyeon.mystock.design.component.MsInfoNote
import com.donghyeon.mystock.design.component.MsSheetActions
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.strategy.R
import com.donghyeon.mystock.feature.strategy.StartAccountCard
import com.donghyeon.mystock.feature.strategy.StrategyCard

@Composable
internal fun StartSheet(
    state: StrategyDetailUiState,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsBottomSheet(
        onDismissRequest = { onIntent(StrategyDetailUiIntent.DismissStart) },
        modifier = modifier,
    ) {
        StartSheetContent(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun StartSheetContent(
    state: StrategyDetailUiState,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = state.detail ?: return
    val account = detail.startAccount ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = MyStockTheme.spacing.lg,
                end = MyStockTheme.spacing.lg,
                bottom = MyStockTheme.spacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.strategy_detail_start),
            style = MyStockTheme.typography.subtitle,
            color = MyStockTheme.colors.textPrimary,
            modifier = Modifier.padding(top = MyStockTheme.spacing.xs),
        )
        StartAccountCard(account = account)
        StrategyCard(strategy = detail.strategy)
        MsInfoNote(stringResource(R.string.strategy_detail_start_note))
        MsSheetActions(
            confirmText = stringResource(R.string.strategy_detail_start),
            onConfirm = { onIntent(StrategyDetailUiIntent.ConfirmStart) },
            onCancel = { onIntent(StrategyDetailUiIntent.DismissStart) },
        )
    }
}

@PreviewThemes
@Composable
private fun StartSheetPreview() {
    MyStockTheme {
        Surface(color = MyStockTheme.colors.surfaceElevated) {
            StartSheetContent(state = sampleStartSheetState, onIntent = {})
        }
    }
}
