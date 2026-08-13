package com.donghyeon.mystock.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.donghyeon.mystock.R
import com.donghyeon.mystock.navigation.MyStockDeepLinks
import java.util.concurrent.atomic.AtomicInteger

// 프로세스가 새로 떠도 상태바에 남아 있는 이전 알림과 id가 겹치지 않게 시계에서 센다
private val nextId = AtomicInteger(System.currentTimeMillis().toInt())

internal object PushNotifications {

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            context.getString(R.string.notification_channel_id),
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    fun show(context: Context, title: String?, body: String?, link: String?) {
        val heading = title?.takeUnless { it.isBlank() }
        val text = body?.takeUnless { it.isBlank() }
        if (heading == null && text == null) return
        val manager = NotificationManagerCompat.from(context)
        // 사용자가 껐거나 권한이 없으면 notify가 조용히 버린다. POST_NOTIFICATIONS는 33부터라 그 아래엔 권한이 없다
        val permitted = manager.areNotificationsEnabled() &&
            (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                )
        if (!permitted) return
        val id = nextId.incrementAndGet()
        val notification = NotificationCompat
            .Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(heading)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(openIntent(context, id, link))
            .build()
        manager.notify(id, notification)
    }

    private fun openIntent(context: Context, id: Int, link: String?): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, MyStockDeepLinks.openable(link))
            .setPackage(context.packageName)
        return PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
