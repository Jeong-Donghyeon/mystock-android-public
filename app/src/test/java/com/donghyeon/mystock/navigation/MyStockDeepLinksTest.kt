package com.donghyeon.mystock.navigation

import android.content.Intent
import androidx.core.net.toUri
import com.donghyeon.mystock.feature.account.navigation.HomeKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// docs/API.md의 딥링크 절이 이 테스트의 원천이다
@RunWith(RobolectricTestRunner::class)
class MyStockDeepLinksTest {

    private fun viewIntent(link: String) = Intent(Intent.ACTION_VIEW, link.toUri())

    @Test
    fun `accounts는 홈을 연다`() {
        assertEquals(listOf(HomeKey), MyStockDeepLinks.resolve(viewIntent("mystock://accounts")))
    }

    @Test
    fun `앱이 모르는 링크도 홈으로 보낸다`() {
        assertEquals(listOf(HomeKey), MyStockDeepLinks.resolve(viewIntent("mystock://nowhere")))
    }

    @Test
    fun `스킴이 달라도 홈으로 보낸다`() {
        assertEquals(listOf(HomeKey), MyStockDeepLinks.resolve(viewIntent("https://mystock.app/accounts")))
    }

    @Test
    fun `링크가 없는 인텐트는 해석하지 않는다`() {
        assertNull(MyStockDeepLinks.resolve(Intent(Intent.ACTION_MAIN)))
    }

    @Test
    fun `시스템이 그린 알림은 링크가 extras로 온다`() {
        val intent = Intent(Intent.ACTION_MAIN).putExtra("link", "mystock://accounts")
        assertEquals(listOf(HomeKey), MyStockDeepLinks.resolve(intent))
    }

    @Test
    fun `서버가 준 링크가 우리 스킴이면 그대로 연다`() {
        assertEquals("mystock://accounts".toUri(), MyStockDeepLinks.openable("mystock://accounts"))
    }

    @Test
    fun `우리가 열 수 없는 스킴은 홈으로 바꾼다`() {
        assertEquals("mystock://accounts".toUri(), MyStockDeepLinks.openable("https://mystock.app/accounts"))
    }

    @Test
    fun `서버가 열 곳을 주지 않으면 홈으로 연다`() {
        assertEquals("mystock://accounts".toUri(), MyStockDeepLinks.openable(null))
    }
}
