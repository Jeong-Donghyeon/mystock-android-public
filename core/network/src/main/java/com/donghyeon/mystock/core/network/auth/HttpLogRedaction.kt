package com.donghyeon.mystock.core.network.auth

// keys는 키 이름을 서버의 계좌 설정이 정해 바뀔 수 있으므로, 이름을 나열하지 않고 객체째 가린다
private val SECRET_FIELD = Regex("\"(keys|accessToken|refreshToken)\"\\s*:\\s*(\\{[^}]*\\}|\"(?:[^\"\\\\]|\\\\.)*\")")

internal fun redactSecrets(message: String): String = message.replace(SECRET_FIELD, "\"$1\":\"****\"")
