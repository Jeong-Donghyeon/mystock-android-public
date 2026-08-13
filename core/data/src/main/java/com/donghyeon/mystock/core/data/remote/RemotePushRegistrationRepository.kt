package com.donghyeon.mystock.core.data.remote

import com.donghyeon.mystock.core.data.DeviceSettingsStore
import com.donghyeon.mystock.core.data.PushRegistrationRepository
import com.donghyeon.mystock.core.data.apiResult
import com.donghyeon.mystock.core.network.DeviceApi
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import com.donghyeon.mystock.core.network.dto.DeviceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RemotePushRegistrationRepository @Inject constructor(
    private val api: DeviceApi,
    private val tokens: AuthTokenStore,
    private val device: DeviceSettingsStore,
) : PushRegistrationRepository {

    // 인증 전에는 보낼 수 없으니 토큰만 두고, 키 인증이 끝나면 그때 보낸다
    override suspend fun register(installationId: String): Result<Unit> {
        device.pushToken = installationId
        return if (tokens.authenticated.value) registerStored() else Result.success(Unit)
    }

    suspend fun registerStored(notificationEnabled: Boolean = device.notificationEnabled.value): Result<Unit> {
        val token = device.pushToken ?: return Result.success(Unit)
        return apiResult { api.registerDevice(DeviceDto(token = token, notificationsEnabled = notificationEnabled)) }
    }

    suspend fun unregisterStored(): Result<Unit> {
        val token = device.pushToken ?: return Result.success(Unit)
        return apiResult { api.unregisterDevice(token) }
    }
}
