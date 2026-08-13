package com.donghyeon.mystock.design.component

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class SnackbarControllerTest {

    @Test
    fun `메시지가 연달아 오면 쌓아 두지 않고 마지막 것만 보여 준다`() = runBlocking {
        val hostState = SnackbarHostState()
        val controller = SnackbarController(hostState, this)

        controller.show("첫 번째")
        yield()
        controller.show("두 번째")
        controller.show("세 번째")
        yield()

        assertEquals("세 번째", hostState.currentSnackbarData?.visuals?.message)
        coroutineContext.cancelChildren()
    }

    @Test
    fun `보이던 스낵바를 내리면 다음 메시지가 그 자리를 이어받는다`() = runBlocking {
        val hostState = SnackbarHostState()
        val controller = SnackbarController(hostState, this)

        controller.show("첫 번째")
        yield()
        hostState.currentSnackbarData?.dismiss()
        controller.show("두 번째")
        yield()

        assertEquals("두 번째", hostState.currentSnackbarData?.visuals?.message)
        coroutineContext.cancelChildren()
    }
}
