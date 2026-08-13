package com.donghyeon.mystock.di

import com.donghyeon.mystock.BuildConfig
import com.donghyeon.mystock.core.model.AppVersion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideAppVersion(): AppVersion = AppVersion(BuildConfig.VERSION_NAME)
}
