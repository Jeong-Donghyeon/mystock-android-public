package com.donghyeon.mystock.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES = "auth"
private const val ACCESS_TOKEN = "accessToken"
private const val REFRESH_TOKEN = "refreshToken"

// 앱 전용 저장소에 두고 백업·기기 이전에서는 뺀다(:app의 data_extraction_rules). 다른 기기로 복원된 토큰은 그 기기의 인증이 아니다
@Singleton
internal class PreferencesTokenStore @Inject constructor(@ApplicationContext context: Context) : AuthTokenStore {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    @Volatile
    override var accessToken: String? = preferences.getString(ACCESS_TOKEN, null)
        private set

    @Volatile
    override var refreshToken: String? = preferences.getString(REFRESH_TOKEN, null)
        private set

    private val _authenticated = MutableStateFlow(accessToken != null)
    override val authenticated: StateFlow<Boolean> = _authenticated

    override fun save(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        preferences.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
        }
        _authenticated.value = true
    }

    override fun clear() {
        accessToken = null
        refreshToken = null
        preferences.edit { clear() }
        _authenticated.value = false
    }
}
