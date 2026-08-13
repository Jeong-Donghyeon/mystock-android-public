package com.donghyeon.mystock.feature.account.home

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
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBottomSheet
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsSheetActions
import com.donghyeon.mystock.design.component.PnlPairText
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.design.component.StatusChip
import com.donghyeon.mystock.feature.account.R
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.toUi
import com.donghyeon.mystock.feature.common.R as CommonR

@Composable
internal fun AccountSheet(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsBottomSheet(
        onDismissRequest = { onIntent(HomeUiIntent.Overlay.DismissAccountSheet) },
        modifier = modifier,
    ) {
        AccountSheetContent(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun AccountSheetContent(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendingAccountId = (state.sheet as? HomeSheet.Account)?.pendingAccountId
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
        state.accounts.forEach { account ->
            AccountCard(
                account = account,
                selected = account.id == pendingAccountId,
                onClick = { onIntent(HomeUiIntent.Overlay.SelectAccount(account.id)) },
            )
        }
        MsSheetActions(
            confirmText = stringResource(R.string.home_account_confirm),
            onConfirm = { onIntent(HomeUiIntent.Overlay.ConfirmAccount) },
            onCancel = { onIntent(HomeUiIntent.Overlay.DismissAccountSheet) },
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(
        modifier = modifier,
        onClick = onClick,
        selected = selected,
        contentPadding = PaddingValues(
            horizontal = MyStockTheme.spacing.lg,
            vertical = MyStockTheme.spacing.md,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName,
                    style = MyStockTheme.typography.bodyMedium,
                    color = MyStockTheme.colors.textPrimary,
                )
                val strategy = account.strategy
                if (strategy == null) {
                    Text(
                        text = stringResource(R.string.home_account_no_strategy),
                        style = MyStockTheme.typography.caption,
                        color = MyStockTheme.colors.textTertiary,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs)) {
                        Text(
                            text = strategy.displayName,
                            style = MyStockTheme.typography.caption,
                            color = MyStockTheme.colors.textTertiary,
                            modifier = Modifier.alignByBaseline(),
                        )
                        Text(
                            text = formatVersion(strategy.version),
                            style = MyStockTheme.typography.numberCaption,
                            color = MyStockTheme.colors.textTertiary,
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                }
                val todayPnl = account.todayPnl
                val cumulativePnl = account.cumulativePnl
                if (todayPnl != null) {
                    PnlLine(label = stringResource(R.string.home_account_today), pnl = todayPnl)
                }
                if (cumulativePnl != null) {
                    PnlLine(label = stringResource(R.string.home_account_cumulative), pnl = cumulativePnl)
                }
            }
            StatusChip(state = account.state.toUi())
        }
    }
}

@Composable
private fun PnlLine(
    label: String,
    pnl: Pnl,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
            modifier = Modifier.alignByBaseline(),
        )
        PnlPairText(
            amount = pnl.amount.toDouble(),
            percent = pnl.percent,
            style = MyStockTheme.typography.numberSmall,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@PreviewThemes
@Composable
private fun AccountSheetPreview() {
    MyStockTheme {
        Surface(color = MyStockTheme.colors.surfaceElevated) {
            AccountSheetContent(state = sampleAccountSheetState, onIntent = {})
        }
    }
}
