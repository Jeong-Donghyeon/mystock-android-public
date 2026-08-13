package com.donghyeon.mystock.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.donghyeon.mystock.feature.account.navigation.accountEntries
import com.donghyeon.mystock.feature.settings.navigation.SettingsKey
import com.donghyeon.mystock.feature.settings.navigation.settingsEntries
import com.donghyeon.mystock.feature.strategy.navigation.StrategiesKey
import com.donghyeon.mystock.feature.strategy.navigation.StrategyDetailKey
import com.donghyeon.mystock.feature.strategy.navigation.strategyEntries
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import com.donghyeon.mystock.feature.common.R as CommonR

@Composable
fun MyStockNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {},
) {
    val context = LocalContext.current
    val licensesTitle = stringResource(CommonR.string.common_licenses)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            accountEntries(
                // 홈에서는 물러날 데가 없어 앱을 나간다
                onBack = { if (!backStack.pop()) onExit() },
                onOpenSettings = { backStack.push(SettingsKey) },
                onOpenStrategies = { backStack.push(StrategiesKey) },
                onOpenStrategy = { key, accountId -> backStack.push(StrategyDetailKey(key, accountId)) },
                onOpenPerformance = { backStack.push(it) },
            )
            strategyEntries(
                onBack = { backStack.pop() },
                onOpenDetail = { backStack.push(it) },
                onOpenVersions = { backStack.push(it) },
                onOpenHome = { backStack.popToRoot() },
            )
            settingsEntries(
                onBack = { backStack.pop() },
                onOpenLicenses = {
                    OssLicensesMenuActivity.setActivityTitle(licensesTitle)
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                },
            )
        },
    )
}

// 같은 키를 겹쳐 쌓거나 루트까지 비우지 않게 막는다
private fun NavBackStack<NavKey>.push(key: NavKey) {
    if (lastOrNull() != key) add(key)
}

private fun NavBackStack<NavKey>.pop(): Boolean {
    if (size <= 1) return false
    removeLastOrNull()
    return true
}

private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeLastOrNull()
}
