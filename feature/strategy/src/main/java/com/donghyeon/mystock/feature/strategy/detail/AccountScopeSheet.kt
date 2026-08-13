package com.donghyeon.mystock.feature.strategy.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBottomSheet
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.strategy.R
import com.donghyeon.mystock.feature.common.R as CommonR

@Composable
internal fun AccountScopeSheet(
    state: StrategyDetailUiState,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsBottomSheet(
        onDismissRequest = { onIntent(StrategyDetailUiIntent.DismissAccountSheet) },
        modifier = modifier,
    ) {
        AccountScopeSheetContent(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun AccountScopeSheetContent(
    state: StrategyDetailUiState,
    onIntent: (StrategyDetailUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accounts = state.scopeAccounts
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MyStockTheme.spacing.lg,
                end = MyStockTheme.spacing.lg,
                bottom = MyStockTheme.spacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        Text(
            text = stringResource(CommonR.string.common_account_sheet_title),
            style = MyStockTheme.typography.subtitle,
            color = MyStockTheme.colors.textPrimary,
            modifier = Modifier.padding(top = MyStockTheme.spacing.xs),
        )
        ScopeRow(
            name = stringResource(R.string.strategy_detail_all),
            trailing = stringResource(R.string.strategy_detail_account_count, accounts.size),
            selected = state.accountScope == null,
            onClick = { onIntent(StrategyDetailUiIntent.SelectAccountScope(null)) },
        )
        accounts.forEach { account ->
            ScopeRow(
                name = account.name,
                trailing = null,
                selected = state.accountScope == account.id,
                onClick = { onIntent(StrategyDetailUiIntent.SelectAccountScope(account.id)) },
            )
        }
    }
}

@Composable
private fun ScopeRow(
    name: String,
    trailing: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(
        modifier = modifier,
        onClick = onClick,
        selected = selected,
        contentPadding = PaddingValues(horizontal = MyStockTheme.spacing.lg, vertical = MyStockTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MyStockTheme.typography.bodyMedium,
                color = MyStockTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MyStockTheme.typography.numberCaption,
                    color = MyStockTheme.colors.textTertiary,
                )
            }
        }
    }
}

@PreviewThemes
@Composable
private fun AccountScopeSheetPreview() {
    MyStockTheme {
        Surface(color = MyStockTheme.colors.surfaceElevated) {
            AccountScopeSheetContent(state = sampleAccountSheetState, onIntent = {})
        }
    }
}
