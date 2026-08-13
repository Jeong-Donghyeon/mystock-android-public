package com.donghyeon.mystock.core.model

/** 서버가 거절한 요청. 서버 계약상 message는 사용자에게 그대로 보여줄 수 있는 문장이다. */
class ApiException(val code: String, override val message: String) : RuntimeException(message)
