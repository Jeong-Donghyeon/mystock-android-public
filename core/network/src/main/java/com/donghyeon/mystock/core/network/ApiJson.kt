package com.donghyeon.mystock.core.network

import kotlinx.serialization.json.Json

// 서버가 응답에 필드를 더해도 앱이 깨지지 않아야 한다
internal val apiJson: Json = Json { ignoreUnknownKeys = true }
