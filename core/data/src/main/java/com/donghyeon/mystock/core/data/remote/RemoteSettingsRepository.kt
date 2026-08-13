package com.donghyeon.mystock.core.data.remote

import com.donghyeon.mystock.core.data.DeviceSettingsStore
import com.donghyeon.mystock.core.data.SettingsRepository
import com.donghyeon.mystock.core.data.apiResult
import com.donghyeon.mystock.core.data.di.ApplicationScope
import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.Settings
import com.donghyeon.mystock.core.network.AuthApi
import com.donghyeon.mystock.core.network.SessionApi
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import com.donghyeon.mystock.core.network.dto.KeysRequestDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemoteSettingsRepository @Inject constructor(
    private val api: AuthApi,
    private val session: SessionApi,
    private val tokens: AuthTokenStore,
    private val device: DeviceSettingsStore,
    private val push: RemotePushRegistrationRepository,
    @ApplicationScope private val scope: CoroutineScope,
) : SettingsRepository {

    // 토큰은 인증기가 백그라운드에서 지울 수 있어 저장소를 관찰해야 한다
    override val settings: StateFlow<Settings> =
        combine(device.notificationEnabled, tokens.authenticated) { notification, authenticated ->
            Settings(notificationEnabled = notification, keyAuthenticated = authenticated)
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            Settings(
                notificationEnabled = device.notificationEnabled.value,
                keyAuthenticated = tokens.authenticated.value
            ),
        )

    // 서버가 스위치 상태를 알아야 보낼지 정한다 (openapi.yaml `/devices`). 서버에 못 알리면 바꾸지 않는다
    override suspend fun setNotificationEnabled(enabled: Boolean): Result<Unit> {
        if (tokens.authenticated.value) {
            push.registerStored(notificationEnabled = enabled).onFailure { return Result.failure(it) }
        }
        device.setNotificationEnabled(enabled)
        return Result.success(Unit)
    }

    override suspend fun authenticateKeys(keys: ApiKeys): Result<Unit> =
        apiResult { api.issueTokens(KeysRequestDto(keys = ApiKeys.NAMES.zip(keys.values).toMap())) }
            .onSuccess { issued ->
                tokens.save(issued.accessToken, issued.refreshToken)
                // 인증 전에 받은 푸시 토큰은 이제야 보낼 수 있다. 실패해도 다음 실행의 onRegistered가 다시 보낸다
                push.registerStored()
            }
            .map { }

    override suspend fun revokeKeys(): Result<Unit> {
        // 해제한 기기에는 더 보내지 않는다. 실패해도 해제는 막지 않는다
        push.unregisterStored()
        return apiResult { session.logout() }.onSuccess { tokens.clear() }
    }
}
