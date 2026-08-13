package com.donghyeon.mystock.core.data

import kotlin.coroutines.cancellation.CancellationException

// runCatching은 취소까지 삼켜 화면을 떠난 뒤의 취소가 실패 상태로 둔갑한다
internal suspend inline fun <T> apiResult(crossinline block: suspend () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }
