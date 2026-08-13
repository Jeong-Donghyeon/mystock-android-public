package com.donghyeon.mystock.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES = "device"
private const val NOTIFICATION_ENABLED = "notificationEnabled"
private const val PUSH_TOKEN = "pushToken"

/** 이 기기에만 속한 값 — 알림 스위치와 푸시 토큰. 백업·기기 이전에서 빼므로(:app의 data_extraction_rules) 다른 기기로 옮겨가지 않는다. */
@Singleton
internal class DeviceSettingsStore @Inject constructor(@ApplicationContext context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private val _notificationEnabled = MutableStateFlow(preferences.getBoolean(NOTIFICATION_ENABLED, false))
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled

    var pushToken: String?
        get() = preferences.getString(PUSH_TOKEN, null)
        set(value) {
            preferences.edit { putString(PUSH_TOKEN, value) }
        }

    fun setNotificationEnabled(enabled: Boolean) {
        preferences.edit { putBoolean(NOTIFICATION_ENABLED, enabled) }
        _notificationEnabled.value = enabled
    }
}
