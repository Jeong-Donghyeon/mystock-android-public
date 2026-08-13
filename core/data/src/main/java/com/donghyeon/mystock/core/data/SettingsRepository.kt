package com.donghyeon.mystock.core.data

import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.Settings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<Settings>

    suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit>

    suspend fun authenticateKeys(keys: ApiKeys): Result<Unit>

    suspend fun revokeKeys(): Result<Unit>
}
