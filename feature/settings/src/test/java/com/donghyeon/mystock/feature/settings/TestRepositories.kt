package com.donghyeon.mystock.feature.settings

import com.donghyeon.mystock.core.data.SettingsRepository
import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.Settings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

fun testSettings(
    notificationEnabled: Boolean = false,
    keyAuthenticated: Boolean = false,
): Settings = Settings(notificationEnabled = notificationEnabled, keyAuthenticated = keyAuthenticated)

class TestSettingsRepository(initial: Settings = testSettings()) : SettingsRepository {
    private val _settings = MutableStateFlow(initial)
    override val settings: StateFlow<Settings> = _settings.asStateFlow()

    var notificationResult: Result<Unit> = Result.success(Unit)
    var authenticateResult: Result<Unit> = Result.success(Unit)
    var revokeResult: Result<Unit> = Result.success(Unit)
    var authenticateGate: CompletableDeferred<Unit>? = null
    var revokeGate: CompletableDeferred<Unit>? = null

    val notificationChanges = mutableListOf<Boolean>()
    val authenticatedKeys = mutableListOf<ApiKeys>()
    var revokeCallCount: Int = 0
        private set

    override suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit> {
        notificationChanges += enabled
        if (notificationResult.isSuccess) _settings.update { it.copy(notificationEnabled = enabled) }
        return notificationResult
    }

    override suspend fun authenticateKeys(keys: ApiKeys): Result<Unit> {
        authenticatedKeys += keys
        authenticateGate?.await()
        if (authenticateResult.isSuccess) _settings.update { it.copy(keyAuthenticated = true) }
        return authenticateResult
    }

    override suspend fun revokeKeys(): Result<Unit> {
        revokeCallCount++
        revokeGate?.await()
        if (revokeResult.isSuccess) _settings.update { it.copy(keyAuthenticated = false) }
        return revokeResult
    }
}
