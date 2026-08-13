package com.donghyeon.mystock.core.network.di

import com.donghyeon.mystock.core.network.AccountApi
import com.donghyeon.mystock.core.network.ApiErrorCallAdapterFactory
import com.donghyeon.mystock.core.network.AuthApi
import com.donghyeon.mystock.core.network.BuildConfig
import com.donghyeon.mystock.core.network.DeviceApi
import com.donghyeon.mystock.core.network.RunApi
import com.donghyeon.mystock.core.network.SessionApi
import com.donghyeon.mystock.core.network.StrategyApi
import com.donghyeon.mystock.core.network.apiJson
import com.donghyeon.mystock.core.network.auth.AuthInterceptor
import com.donghyeon.mystock.core.network.auth.AuthTokenStore
import com.donghyeon.mystock.core.network.auth.PreferencesTokenStore
import com.donghyeon.mystock.core.network.auth.TokenAuthenticator
import com.donghyeon.mystock.core.network.auth.redactSecrets
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

// 홈은 5초마다 폴링한다. 한 주기 안에 연결되지 않으면 다음 폴링이 대신하는 편이 낫다
private const val CONNECT_TIMEOUT_SECONDS = 5L

// 계좌 상태 응답은 서버가 브로커까지 다녀온다. 서버의 브로커 read-timeout(15초)보다 길어야
// 브로커가 느릴 때 앱이 먼저 끊지 않고 서버의 502 BrokerError를 받는다
private const val CALL_TIMEOUT_SECONDS = 20L

private val JSON_MEDIA_TYPE = "application/json".toMediaType()

private val debugLogging = HttpLoggingInterceptor {
    HttpLoggingInterceptor.Logger.DEFAULT.log(redactSecrets(it))
}.apply {
    level = HttpLoggingInterceptor.Level.BODY
    redactHeader("Authorization")
}

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenStore(store: PreferencesTokenStore): AuthTokenStore = store

    @Provides
    @Singleton
    fun provideOkHttpClient(store: AuthTokenStore, authApi: Provider<AuthApi>): OkHttpClient =
        baseClient(store).newBuilder().authenticator(TokenAuthenticator(store, authApi)).build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofit(client)

    // 인증 API는 인증기 없는 자기 클라이언트(디스패처 별도)를 쓴다. 홈 폴링 다섯 건이 한꺼번에 401을 받아
    // 호스트당 동시 요청 한도(5)를 다 차지한 동안에도 재발급이 줄을 서지 않고 나가야 한다
    @Provides
    @Singleton
    fun provideAuthApi(store: AuthTokenStore): AuthApi = retrofit(baseClient(store)).create()

    @Provides
    @Singleton
    fun provideSessionApi(retrofit: Retrofit): SessionApi = retrofit.create()

    @Provides
    @Singleton
    fun provideAccountApi(retrofit: Retrofit): AccountApi = retrofit.create()

    @Provides
    @Singleton
    fun provideStrategyApi(retrofit: Retrofit): StrategyApi = retrofit.create()

    @Provides
    @Singleton
    fun provideRunApi(retrofit: Retrofit): RunApi = retrofit.create()

    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi = retrofit.create()
}

private fun baseClient(store: AuthTokenStore): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .addInterceptor(AuthInterceptor(store))
    // AuthInterceptor 뒤에 둬서 실제로 나가는 헤더를 찍되, 토큰 자체는 가린다
    .apply { if (BuildConfig.DEBUG) addInterceptor(debugLogging) }
    .build()

private fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .client(client)
    .addCallAdapterFactory(ApiErrorCallAdapterFactory(apiJson))
    .addConverterFactory(apiJson.asConverterFactory(JSON_MEDIA_TYPE))
    .build()
