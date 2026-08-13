package com.donghyeon.mystock.feature.account.home

import androidx.annotation.StringRes
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
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsBottomSheet
import com.donghyeon.mystock.design.component.MsCondition
import com.donghyeon.mystock.design.component.MsInfoNote
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsListPnlRow
import com.donghyeon.mystock.design.component.MsListRow
import com.donghyeon.mystock.design.component.MsSheetActions
import com.donghyeon.mystock.design.component.MsStrategyCard
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.account.R
import com.donghyeon.mystock.feature.common.formatAmount
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.marketLabel

private data class CommandCopy(
    @StringRes val title: Int,
    @StringRes val holdingsLabel: Int,
    @StringRes val note: Int,
)

private fun copyOf(command: HomeCommand): CommandCopy = when (command) {
    HomeCommand.PAUSE -> CommandCopy(
        R.string.home_pause,
        R.string.home_command_pause_holdings,
        R.string.home_command_pause_note,
    )
    HomeCommand.RESUME -> CommandCopy(
        R.string.home_resume,
        R.string.home_command_resume_holdings,
        R.string.home_command_resume_note,
    )
    HomeCommand.CLOSE -> CommandCopy(
        R.string.home_close,
        R.string.home_command_close_holdings,
        R.string.home_command_close_note,
    )
    HomeCommand.LIQUIDATE -> CommandCopy(
        R.string.home_liquidate,
        R.string.home_command_liquidate_holdings,
        R.string.home_command_liquidate_note,
    )
}

@Composable
internal fun CommandSheet(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsBottomSheet(
        onDismissRequest = { onIntent(HomeUiIntent.Overlay.DismissCommand) },
        modifier = modifier,
    ) {
        CommandSheetContent(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun CommandSheetContent(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val command = (state.sheet as? HomeSheet.Command)?.command ?: return
    val account = state.selectedAccount ?: return
    val dashboard = state.dashboard
    val copy = copyOf(command)
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
            text = stringResource(copy.title),
            style = MyStockTheme.typography.subtitle,
            color = MyStockTheme.colors.textPrimary,
            modifier = Modifier.padding(top = MyStockTheme.spacing.xs),
        )
        val strategy = account.strategy
        if (command == HomeCommand.RESUME && strategy != null) {
            MsStrategyCard(
                name = strategy.displayName,
                version = formatVersion(strategy.version),
                description = strategy.description,
                markets = strategy.markets.map(::marketLabel),
                conditions = strategy.conditions.map { MsCondition(it.label, it.text) },
            )
        }
        val portfolio = dashboard?.portfolio
        val valuationPnl = dashboard?.valuationPnl
        if (portfolio != null && valuationPnl != null) {
            MsListCard(rows = holdingRows(command, copy.holdingsLabel, portfolio, valuationPnl))
        }
        MsInfoNote(text = stringResource(copy.note))
        MsSheetActions(
            confirmText = stringResource(copy.title),
            onConfirm = { onIntent(HomeUiIntent.Overlay.ConfirmCommand) },
            onCancel = { onIntent(HomeUiIntent.Overlay.DismissCommand) },
        )
    }
}

private fun holdingRows(
    command: HomeCommand,
    @StringRes holdingsLabel: Int,
    portfolio: Portfolio,
    valuationPnl: Pnl,
): List<@Composable () -> Unit> = buildList {
    add {
        MsListRow(
            label = stringResource(holdingsLabel),
            value = stringResource(R.string.home_holdings_count, portfolio.holdings.size),
        )
    }
    portfolio.holdings.forEach { holding -> add { HoldingRow(holding) } }
    add {
        MsListPnlRow(
            label = stringResource(R.string.home_command_valuation_pnl),
            amount = valuationPnl.amount.toDouble(),
            percent = valuationPnl.percent,
        )
    }
    if (command == HomeCommand.LIQUIDATE) {
        add {
            MsListRow(
                label = stringResource(R.string.home_command_valuation_total),
                value = formatAmount(portfolio.valuationAmount),
            )
        }
    }
}

@Composable
private fun CommandSheetPreview(state: HomeUiState) {
    MyStockTheme {
        Surface(color = MyStockTheme.colors.surfaceElevated) {
            CommandSheetContent(state = state, onIntent = {})
        }
    }
}

@PreviewThemes
@Composable
private fun PauseSheetPreview() = CommandSheetPreview(samplePauseSheetState)

@PreviewThemes
@Composable
private fun ResumeSheetPreview() = CommandSheetPreview(sampleResumeSheetState)

@PreviewThemes
@Composable
private fun CloseSheetPreview() = CommandSheetPreview(sampleCloseSheetState)

@PreviewThemes
@Composable
private fun LiquidateSheetPreview() = CommandSheetPreview(sampleLiquidateSheetState)
