package com.donghyeon.mystock.core.network

import com.donghyeon.mystock.core.model.ApiException
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class ApiErrorCallAdapterTest {

    private fun api(status: Int, body: String, mediaType: String = "application/json"): AccountApi =
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .callFactory { request -> FakeCall(request, status, body, mediaType) }
            .addCallAdapterFactory(ApiErrorCallAdapterFactory(apiJson))
            .addConverterFactory(apiJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()

    @Test
    fun `서버가 거절하면 오류 본문의 code와 message를 담은 ApiException이 난다`() {
        val api = api(409, """{ "code": "INVALID_TRANSITION", "message": "대기 계좌는 일시정지할 수 없습니다", "details": [] }""")

        val exception = assertThrows(ApiException::class.java) { runBlocking { api.pauseAccount("acc-1") } }

        assertEquals("INVALID_TRANSITION", exception.code)
        assertEquals("대기 계좌는 일시정지할 수 없습니다", exception.message)
    }

    @Test
    fun `오류 본문이 계약 형태가 아니면 상태 코드로 ApiException을 만든다`() {
        val api = api(502, "<html>Bad Gateway</html>", mediaType = "text/html")

        val exception = assertThrows(ApiException::class.java) { runBlocking { api.getAccounts() } }

        assertEquals("HTTP_502", exception.code)
    }

    @Test
    fun `성공 응답은 그대로 디코딩된다`() {
        val api = api(200, "[]")

        assertEquals(emptyList<Any>(), runBlocking { api.getAccounts() })
    }

    @Test
    fun `본문 없는 204는 null이 된다`() {
        val api = api(204, "")

        assertNull(runBlocking { api.getPerformance("acc-2") })
    }
}

private class FakeCall(
    private val request: Request,
    private val status: Int,
    private val body: String,
    private val mediaType: String,
) : Call {
    override fun request(): Request = request

    override fun execute(): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(status)
        .message("")
        .body(body.toResponseBody(mediaType.toMediaType()))
        .build()

    override fun enqueue(responseCallback: Callback) = responseCallback.onResponse(this, execute())

    override fun cancel() = Unit

    override fun isExecuted(): Boolean = false

    override fun isCanceled(): Boolean = false

    override fun timeout(): Timeout = Timeout.NONE

    override fun clone(): Call = FakeCall(request, status, body, mediaType)
}
