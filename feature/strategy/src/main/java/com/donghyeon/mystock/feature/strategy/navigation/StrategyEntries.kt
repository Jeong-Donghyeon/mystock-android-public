package com.donghyeon.mystock.feature.strategy.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.donghyeon.mystock.feature.strategy.detail.StrategyDetailRoute
import com.donghyeon.mystock.feature.strategy.strategies.StrategiesRoute
import com.donghyeon.mystock.feature.strategy.versions.StrategyVersionsRoute

fun EntryProviderScope<NavKey>.strategyEntries(
    onBack: () -> Unit,
    onOpenDetail: (StrategyDetailKey) -> Unit,
    onOpenVersions: (StrategyVersionsKey) -> Unit,
    onOpenHome: () -> Unit,
) {
    entry<StrategiesKey> {
        StrategiesRoute(
            onBack = onBack,
            onOpenDetail = { onOpenDetail(StrategyDetailKey(it)) },
        )
    }
    entry<StrategyDetailKey> { key ->
        StrategyDetailRoute(
            key = key,
            onBack = onBack,
            onOpenVersions = { onOpenVersions(StrategyVersionsKey(it)) },
            onOpenHome = onOpenHome,
        )
    }
    entry<StrategyVersionsKey> { key ->
        StrategyVersionsRoute(key = key, onBack = onBack)
    }
}
