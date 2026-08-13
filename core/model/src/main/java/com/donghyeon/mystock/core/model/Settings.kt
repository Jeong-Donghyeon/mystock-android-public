package com.donghyeon.mystock.core.model

data class Settings(
    val notificationEnabled: Boolean,
    val keyAuthenticated: Boolean
)

data class ApiKeys(val values: List<String>) {
    init {
        require(isComplete(values))
    }

    // 비밀값이 로그·단언 메시지에 실리지 않게 한다
    override fun toString(): String = "ApiKeys(****)"

    companion object {
        // 설정 화면의 key-1~key-4 순서대로 서버 계좌 설정의 authKeys 이름에 붙인다 (openapi.yaml `/auth/keys`)
        val NAMES = listOf("appKey", "appSecret", "clientId", "clientSecret")
        val COUNT: Int get() = NAMES.size

        fun of(values: List<CharSequence>): ApiKeys? =
            values.map { it.trim().toString() }.takeIf(::isComplete)?.let(::ApiKeys)

        private fun isComplete(values: List<String>): Boolean =
            values.size == COUNT && values.none { it.isBlank() }
    }
}

data class AppVersion(val name: String)
