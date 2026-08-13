package com.donghyeon.mystock.navigation

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import com.donghyeon.mystock.feature.account.navigation.HomeKey

// FCM이 직접 그린 알림을 탭하면 링크가 Intent 데이터가 아니라 extras로 온다
private const val LINK_EXTRA = "link"

object MyStockDeepLinks {

    const val SCHEME = "mystock"

    const val HOME = "mystock://accounts"

    // 매니페스트가 이 스킴만 받는다. 다른 스킴으로 알림을 걸면 탭해도 열리지 않으므로 홈으로 바꾼다
    fun openable(link: String?): Uri =
        link?.toUri()?.takeIf { it.scheme == SCHEME } ?: HOME.toUri()

    fun resolve(intent: Intent): List<NavKey>? {
        if (intent.data == null && intent.getStringExtra(LINK_EXTRA) == null) return null
        // 목적지가 홈뿐이라 스킴도 host도 가리지 않는다. 서버가 앱보다 앞선 링크를 보내도 알림이 닿을 곳은 있어야 한다
        return listOf(HomeKey)
    }
}
