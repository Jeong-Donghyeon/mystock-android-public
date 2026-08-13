package com.donghyeon.mystock

import android.app.Application
import com.donghyeon.mystock.core.data.SettingsRepository
import com.donghyeon.mystock.core.data.di.ApplicationScope
import com.donghyeon.mystock.push.PushNotifications
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class MyStockApplication : Application() {

    @Inject
    lateinit var settings: Provider<SettingsRepository>

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // 푸시가 이 프로세스를 깨웠을 수도 있다 — 채널이 없으면 곧바로 올 알림이 버려진다
        PushNotifications.ensureChannel(this)
        // Firebase 초기화가 디스크를 읽으므로 첫 화면을 막지 않게 뒤로 보낸다.
        // 등록에 성공하면 onRegistered가 오므로, 지난 등록이 서버에 닿지 못했어도 실행할 때마다 다시 보낸다
        scope.launch(Dispatchers.IO) {
            // 설정 저장소는 토큰·기기 설정 파일을 읽으며 만들어진다. 첫 화면이 메인 스레드에서 그 읽기를 기다리지 않게 여기서 먼저 만든다
            settings.get()
            FirebaseMessaging.getInstance().register()
        }
    }
}
