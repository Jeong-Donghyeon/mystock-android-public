package com.donghyeon.mystock.core.data

interface PushRegistrationRepository {
    suspend fun register(installationId: String): Result<Unit>
}
