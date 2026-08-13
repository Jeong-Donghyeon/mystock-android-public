package com.donghyeon.mystock.push

import com.donghyeon.mystock.core.data.PushRegistrationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MyStockMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var registrations: PushRegistrationRepository

    // FCM은 콜백이 돌아오기 전까지만 프로세스를 깨워 두므로 등록을 여기서 끝낸다
    override fun onRegistered(installationId: String) {
        runBlocking { registrations.register(installationId) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        PushNotifications.show(
            context = this,
            title = message.notification?.title ?: message.data["title"],
            body = message.notification?.body ?: message.data["body"],
            link = message.data["link"],
        )
    }
}
