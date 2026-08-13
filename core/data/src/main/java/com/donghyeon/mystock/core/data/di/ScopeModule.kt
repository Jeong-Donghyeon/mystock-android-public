package com.donghyeon.mystock.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// 화면 수명보다 오래 살아야 하는 작업이 저마다 스코프를 만들지 않게 한 곳에서 준다
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
internal object ScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
