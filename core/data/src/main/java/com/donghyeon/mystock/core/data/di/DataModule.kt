package com.donghyeon.mystock.core.data.di

import com.donghyeon.mystock.core.data.AccountRepository
import com.donghyeon.mystock.core.data.PerformanceRepository
import com.donghyeon.mystock.core.data.PushRegistrationRepository
import com.donghyeon.mystock.core.data.SettingsRepository
import com.donghyeon.mystock.core.data.StrategyRepository
import com.donghyeon.mystock.core.data.remote.RemoteAccountRepository
import com.donghyeon.mystock.core.data.remote.RemotePerformanceRepository
import com.donghyeon.mystock.core.data.remote.RemotePushRegistrationRepository
import com.donghyeon.mystock.core.data.remote.RemoteSettingsRepository
import com.donghyeon.mystock.core.data.remote.RemoteStrategyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    @Provides
    @Singleton
    fun provideAccountRepository(remote: RemoteAccountRepository): AccountRepository = remote

    @Provides
    @Singleton
    fun provideSettingsRepository(remote: RemoteSettingsRepository): SettingsRepository = remote

    @Provides
    @Singleton
    fun provideStrategyRepository(remote: RemoteStrategyRepository): StrategyRepository = remote

    @Provides
    @Singleton
    fun providePerformanceRepository(remote: RemotePerformanceRepository): PerformanceRepository = remote

    @Provides
    @Singleton
    fun providePushRegistrationRepository(
        remote: RemotePushRegistrationRepository,
    ): PushRegistrationRepository = remote
}
