package com.donghyeon.mystock.feature.settings.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.Settings

sealed interface SettingsNavTarget {
    data object Back : SettingsNavTarget

    data object Licenses : SettingsNavTarget
}

data class SettingsUiState(
    val settings: Settings,
    val version: String,
    val keys: List<TextFieldState> = List(ApiKeys.COUNT) { TextFieldState() },
    val commanding: Boolean = false,
)

sealed interface SettingsUiIntent {
    data class Navigate(val target: SettingsNavTarget) : SettingsUiIntent

    data class SetNotificationEnabled(val enabled: Boolean) : SettingsUiIntent

    data object DenyNotificationPermission : SettingsUiIntent

    data object Authenticate : SettingsUiIntent

    data object Revoke : SettingsUiIntent
}

sealed interface SettingsUiEffect {
    data class Navigate(val target: SettingsNavTarget) : SettingsUiEffect

    // 키 개수처럼 문구에 채울 값은 Route가 getString에 넘긴다
    data class ShowMessage(@StringRes val message: Int, val args: List<Any> = emptyList()) : SettingsUiEffect

    data object RequestNotificationPermission : SettingsUiEffect
}

internal val sampleUnauthenticatedState = SettingsUiState(
    settings = Settings(notificationEnabled = false, keyAuthenticated = false),
    version = "1.0.0",
)

internal val sampleAuthenticatedState = SettingsUiState(
    settings = Settings(notificationEnabled = true, keyAuthenticated = true),
    version = "1.0.0",
)

internal val sampleCommandingState = sampleUnauthenticatedState.copy(commanding = true)
