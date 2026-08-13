package com.donghyeon.mystock.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.donghyeon.mystock.feature.settings.settings.SettingsRoute

fun EntryProviderScope<NavKey>.settingsEntries(
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    entry<SettingsKey> {
        SettingsRoute(onBack = onBack, onOpenLicenses = onOpenLicenses)
    }
}
