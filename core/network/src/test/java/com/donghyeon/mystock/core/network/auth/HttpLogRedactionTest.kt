package com.donghyeon.mystock.core.network.auth

import com.donghyeon.mystock.core.network.apiJson
import com.donghyeon.mystock.core.network.dto.KeysRequestDto
import com.donghyeon.mystock.core.network.dto.RefreshRequestDto
import com.donghyeon.mystock.core.network.dto.TokensDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpLogRedactionTest {

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/contract/$name")) { "픽스처가 없다: $name" }.readText()

    @Test
    fun `토큰 응답은 토큰 값만 가리고 만료 시각은 남긴다`() {
        val body = fixture("tokens.json")
        val tokens = apiJson.decodeFromString<TokensDto>(body)

        val logged = redactSecrets(body)

        assertFalse(logged.contains(tokens.accessToken))
        assertFalse(logged.contains(tokens.refreshToken))
        assertTrue(logged.contains(tokens.expiresAt))
    }

    @Test
    fun `키 인증 요청은 키 이름이 바뀌어도 값이 남지 않는다`() {
        val keys = mapOf("appKey" to "PS0000000000", "appSecret" to "abcdef0123", "newSecret" to "zzz-unknown")
        val body = apiJson.encodeToString(KeysRequestDto(keys))

        val logged = redactSecrets(body)

        keys.values.forEach { assertFalse(logged.contains(it)) }
    }

    @Test
    fun `재발급 요청은 리프레시 토큰 값이 남지 않는다`() {
        val body = apiJson.encodeToString(RefreshRequestDto("old-refresh"))

        assertFalse(redactSecrets(body).contains("old-refresh"))
    }

    @Test
    fun `시크릿이 없는 본문과 요청 줄은 그대로 남긴다`() {
        val body = fixture("account.json")

        assertEquals(body, redactSecrets(body))
        assertEquals(
            "--> POST http://localhost/api/v1/auth/keys",
            redactSecrets("--> POST http://localhost/api/v1/auth/keys")
        )
    }
}
