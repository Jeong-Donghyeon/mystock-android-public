package com.donghyeon.mystock.feature.account.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.donghyeon.mystock.feature.account.home.HomeRoute
import com.donghyeon.mystock.feature.account.performance.PerformanceRoute

fun EntryProviderScope<NavKey>.accountEntries(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStrategies: () -> Unit,
    onOpenStrategy: (key: String, accountId: String) -> Unit,
    onOpenPerformance: (PerformanceKey) -> Unit,
) {
    entry<HomeKey> {
        HomeRoute(
            onBack = onBack,
            onOpenSettings = onOpenSettings,
            onOpenStrategies = onOpenStrategies,
            onOpenStrategy = onOpenStrategy,
            onOpenPerformance = { onOpenPerformance(PerformanceKey(it)) },
        )
    }
    entry<PerformanceKey> { key ->
        PerformanceRoute(key = key, onBack = onBack)
    }
}
