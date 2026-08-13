package com.donghyeon.mystock.feature.account.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Holding
import com.donghyeon.mystock.core.model.Order
import com.donghyeon.mystock.core.model.Performance
import com.donghyeon.mystock.core.model.Portfolio
import com.donghyeon.mystock.core.model.Strategy
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsAccountTopBar
import com.donghyeon.mystock.design.component.MsAmountText
import com.donghyeon.mystock.design.component.MsBanner
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsCommandOverlay
import com.donghyeon.mystock.design.component.MsCondition
import com.donghyeon.mystock.design.component.MsFailedContent
import com.donghyeon.mystock.design.component.MsHoldingRow
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsOrderRow
import com.donghyeon.mystock.design.component.MsQuickAction
import com.donghyeon.mystock.design.component.MsSectionLabel
import com.donghyeon.mystock.design.component.MsStat
import com.donghyeon.mystock.design.component.MsStrategyCard
import com.donghyeon.mystock.design.component.MsTopBarAction
import com.donghyeon.mystock.design.component.PnlFormat
import com.donghyeon.mystock.design.component.PnlPairText
import com.donghyeon.mystock.design.component.PnlText
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.design.component.StatusChip
import com.donghyeon.mystock.design.icon.MsIcons
import com.donghyeon.mystock.feature.account.R
import com.donghyeon.mystock.feature.common.formatAmount
import com.donghyeon.mystock.feature.common.formatHours
import com.donghyeon.mystock.feature.common.formatPosition
import com.donghyeon.mystock.feature.common.formatPrice
import com.donghyeon.mystock.feature.common.formatVersion
import com.donghyeon.mystock.feature.common.marketLabel
import com.donghyeon.mystock.feature.common.toUi
import com.donghyeon.mystock.feature.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBarAccount =
        state.selectedAccount?.takeUnless { state.content is HomeContent.Loading }
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsAccountTopBar(
                accountName = topBarAccount?.displayName,
                onAccountClick = { onIntent(HomeUiIntent.Overlay.OpenAccountSheet) },
            ) {
                // 대기 홈은 본문이 전략 목록이라 상단바에 전략 아이콘을 두지 않는다(원천)
                if (topBarAccount?.state != AccountState.WAITING) {
                    MsTopBarAction(
                        icon = MsIcons.Strategy,
                        contentDescription = stringResource(R.string.home_strategies),
                        onClick = { onIntent(HomeUiIntent.Navigate(HomeNavTarget.Strategies)) },
                    )
                }
                MsTopBarAction(
                    icon = MsIcons.Settings,
                    contentDescription = stringResource(R.string.home_settings),
                    onClick = { onIntent(HomeUiIntent.Navigate(HomeNavTarget.Settings)) },
                )
            }
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = { onIntent(HomeUiIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val content = state.content) {
                    HomeContent.Loading -> HomeSkeleton()
                    HomeContent.Failed -> MsFailedContent()
                    is HomeContent.Ready -> HomeBody(
                        state = state,
                        dashboard = content.dashboard,
                        onIntent = onIntent,
                    )
                }
            }
        }
    }

    when (state.sheet) {
        is HomeSheet.Account -> AccountSheet(state = state, onIntent = onIntent)
        is HomeSheet.Command -> CommandSheet(state = state, onIntent = onIntent)
        null -> Unit
    }

    if (state.exitConfirming) ExitDialog(onIntent = onIntent)

    // 홈은 뒤로 갈 데가 없어 뒤로가기가 곧 종료다. 시트가 열려 있으면 시트가 먼저 닫힌다(원천)
    BackHandler(enabled = state.sheet == null) { onIntent(HomeUiIntent.Overlay.RequestExit) }

    if (state.commanding) MsCommandOverlay()
}

@Composable
private fun HomeBody(
    state: HomeUiState,
    dashboard: HomeDashboard,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val account = state.selectedAccount ?: return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MyStockTheme.spacing.lg,
            end = MyStockTheme.spacing.lg,
            top = MyStockTheme.spacing.md,
            bottom = MyStockTheme.spacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
    ) {
        item {
            StatusCard(
                account = account,
                cashAmount = dashboard.portfolio?.cashAmount,
                onIntent = onIntent,
            )
        }
        if (state.staleVisible) {
            item {
                MsBanner(
                    message = stringResource(R.string.home_disconnected),
                    onDismiss = { onIntent(HomeUiIntent.DismissStale) },
                )
            }
        }
        val pauseReason = state.pauseReason
        if (pauseReason != null) {
            item {
                MsBanner(
                    message = pauseReason,
                    onDismiss = { onIntent(HomeUiIntent.DismissPauseReason) },
                )
            }
        }
        if (account.state == AccountState.WAITING) {
            strategySection(strategies = dashboard.strategies, accountId = account.id, onIntent = onIntent)
        } else {
            runningSections(dashboard = dashboard)
        }
    }
}

private fun LazyListScope.strategySection(
    strategies: List<Strategy>,
    accountId: String,
    onIntent: (HomeUiIntent) -> Unit,
) {
    if (strategies.isEmpty()) return
    item { MsSectionLabel(stringResource(R.string.home_strategies_count, strategies.size)) }
    items(strategies, key = { it.key }) { strategy ->
        MsStrategyCard(
            name = strategy.displayName,
            version = formatVersion(strategy.version),
            description = strategy.description,
            markets = strategy.markets.map(::marketLabel),
            conditions = strategy.conditions.map { MsCondition(it.label, it.text) },
            onClick = { onIntent(HomeUiIntent.Navigate(HomeNavTarget.Strategy(strategy.key, accountId))) },
        )
    }
}

private fun LazyListScope.runningSections(dashboard: HomeDashboard) {
    val performance = dashboard.performance
    if (performance != null) {
        item {
            SummaryCard(
                performance = performance,
                runningHours = dashboard.runningHours,
                portfolio = dashboard.portfolio,
                currentAmount = dashboard.currentAmount,
            )
        }
    }
    val holdings = dashboard.portfolio?.holdings.orEmpty()
    if (holdings.isNotEmpty()) {
        item { MsSectionLabel(stringResource(R.string.home_holdings_count, holdings.size)) }
        item { MsListCard(rows = holdings.map { holding -> { HoldingRow(holding) } }) }
    }
    if (dashboard.orders.isNotEmpty()) {
        item { MsSectionLabel(stringResource(R.string.home_orders_count, dashboard.orderCount)) }
        item { MsListCard(rows = dashboard.orders.map { order -> { OrderRow(order) } }) }
    }
}

@Composable
internal fun HoldingRow(holding: Holding) {
    MsHoldingRow(
        name = holding.name,
        currentPrice = formatPrice(holding.currentPrice),
        position = formatPosition(holding.quantity, holding.averagePrice),
        valuation = formatAmount(holding.valuationAmount),
        pnlAmount = holding.pnl.amount.toDouble(),
        pnlPercent = holding.pnl.percent,
    )
}

@Composable
private fun OrderRow(order: Order) {
    MsOrderRow(
        name = order.stockName,
        side = order.side.toUi(),
        position = formatPosition(order.quantity, order.price),
        amount = formatAmount(order.amount),
        realizedPnlAmount = order.realizedPnl?.amount?.toDouble(),
        realizedPnlPercent = order.realizedPnl?.percent,
    )
}

@Composable
private fun StatusCard(
    account: Account,
    cashAmount: Long?,
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusChip(state = account.state.toUi())
            Text(
                text = account.strategy
                    ?.let { "${it.displayName} ${formatVersion(it.version)}" }
                    ?: stringResource(R.string.home_waiting_hint),
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textTertiary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = MsIcons.Performance,
                contentDescription = stringResource(R.string.home_performance),
                tint = MyStockTheme.colors.textTertiary,
                // 카드 첫 줄 높이를 시안대로 두려고 터치 영역을 늘리는 대신 리플만 넓힌다
                modifier = Modifier
                    .size(MyStockTheme.iconSize.md)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = false,
                            radius = MyStockTheme.spacing.touchMin / 2,
                        ),
                        role = Role.Button,
                        onClick = { onIntent(HomeUiIntent.Navigate(HomeNavTarget.Performance(account.id))) },
                    ),
            )
        }
        HorizontalDivider(
            thickness = MyStockTheme.stroke,
            color = MyStockTheme.colors.border,
            modifier = Modifier.padding(top = MyStockTheme.spacing.md),
        )
        if (account.state == AccountState.WAITING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MyStockTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(CommonR.string.common_cash),
                    style = MyStockTheme.typography.caption,
                    color = MyStockTheme.colors.textTertiary,
                )
                // 포트폴리오를 못 받았으면 0처럼 보이는 값을 만들지 않고 배너에 맡긴다
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (cashAmount != null) {
                        MsAmountText(
                            text = formatAmount(cashAmount),
                            style = MyStockTheme.typography.numberLarge,
                        )
                    }
                }
            }
        } else {
            Row(modifier = Modifier.padding(top = MyStockTheme.spacing.xs)) {
                QuickActions(account = account, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun RowScope.QuickActions(
    account: Account,
    onIntent: (HomeUiIntent) -> Unit,
) {
    if (account.state == AccountState.RUNNING) {
        MsQuickAction(
            icon = MsIcons.Pause,
            label = stringResource(R.string.home_pause),
            onClick = { onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.PAUSE)) },
        )
    } else {
        MsQuickAction(
            icon = MsIcons.Resume,
            label = stringResource(R.string.home_resume),
            onClick = { onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.RESUME)) },
        )
    }
    // 회수·청산은 이미 진행 중인 상태에서는 다시 낼 수 없다
    if (account.state != AccountState.LIQUIDATING) {
        if (account.state != AccountState.CLOSING) {
            MsQuickAction(
                icon = MsIcons.Recall,
                label = stringResource(R.string.home_close),
                onClick = { onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.CLOSE)) },
            )
        }
        MsQuickAction(
            icon = MsIcons.Liquidate,
            label = stringResource(R.string.home_liquidate),
            onClick = { onIntent(HomeUiIntent.Overlay.OpenCommand(HomeCommand.LIQUIDATE)) },
        )
    }
    MsQuickAction(
        icon = if (account.orderAlertEnabled) MsIcons.BellFilled else MsIcons.Bell,
        label = stringResource(R.string.home_order_alert),
        onClick = { onIntent(HomeUiIntent.ToggleOrderAlert) },
    )
}

@Composable
private fun SummaryCard(
    performance: Performance,
    runningHours: Long?,
    portfolio: Portfolio?,
    currentAmount: Long?,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        // 당일 손익은 견줄 세션 스냅샷이 없거나 브로커가 답하지 않으면 없다 — 계좌 시트처럼 줄을 뺀다
        val todayPnl = performance.todayPnl
        if (todayPnl != null) {
            Text(
                text = stringResource(R.string.home_today_pnl),
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textTertiary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm)) {
                PnlText(
                    value = todayPnl.amount.toDouble(),
                    format = PnlFormat.AMOUNT,
                    style = MyStockTheme.typography.numberLarge,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .alignByBaseline(),
                )
                PnlText(
                    value = todayPnl.percent,
                    format = PnlFormat.PERCENT,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (todayPnl != null) MyStockTheme.spacing.md else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.home_cumulative_pnl),
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textTertiary,
                modifier = Modifier.alignByBaseline(),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                PnlPairText(
                    amount = performance.cumulativePnl.amount.toDouble(),
                    percent = performance.cumulativePnl.percent,
                )
            }
        }
        if (portfolio != null && currentAmount != null && runningHours != null) {
            HorizontalDivider(
                thickness = MyStockTheme.stroke,
                color = MyStockTheme.colors.border,
                modifier = Modifier.padding(top = MyStockTheme.spacing.md),
            )
            Row(
                modifier = Modifier.padding(top = MyStockTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            ) {
                MsStat(
                    label = stringResource(R.string.home_initial_equity),
                    value = formatAmount(performance.initialEquity),
                    modifier = Modifier.weight(1f)
                )
                MsStat(
                    label = stringResource(R.string.home_current_equity),
                    value = formatAmount(currentAmount),
                    modifier = Modifier.weight(1f),
                )
                MsStat(
                    label = stringResource(CommonR.string.common_running_hours),
                    value = formatHours(runningHours),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.padding(top = MyStockTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
            ) {
                MsStat(
                    label = stringResource(R.string.home_valuation),
                    value = formatAmount(portfolio.valuationAmount),
                    modifier = Modifier.weight(1f)
                )
                MsStat(
                    label = stringResource(R.string.home_purchase),
                    value = formatAmount(portfolio.purchaseAmount),
                    modifier = Modifier.weight(1f),
                )
                MsStat(
                    label = stringResource(CommonR.string.common_cash),
                    value = formatAmount(portfolio.cashAmount),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@PreviewThemes
@Composable
private fun HomeRunningPreview() {
    MyStockTheme {
        HomeScreen(state = sampleRunningState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeWaitingPreview() {
    MyStockTheme {
        HomeScreen(state = sampleWaitingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeRunningNoTodayPnlPreview() {
    MyStockTheme {
        HomeScreen(state = sampleRunningNoTodayPnlState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeStalePreview() {
    MyStockTheme {
        HomeScreen(state = sampleStaleState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeRunningEmptyPreview() {
    MyStockTheme {
        HomeScreen(state = sampleRunningEmptyState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomePausedPreview() {
    MyStockTheme {
        HomeScreen(state = samplePausedState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeClosingPreview() {
    MyStockTheme {
        HomeScreen(state = sampleClosingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeLiquidatingPreview() {
    MyStockTheme {
        HomeScreen(state = sampleLiquidatingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeLoadingPreview() {
    MyStockTheme {
        HomeScreen(state = sampleLoadingState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomeFailedPreview() {
    MyStockTheme {
        HomeScreen(state = sampleFailedState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun HomePausedStalePreview() {
    MyStockTheme {
        HomeScreen(state = samplePausedStaleState, onIntent = {})
    }
}
