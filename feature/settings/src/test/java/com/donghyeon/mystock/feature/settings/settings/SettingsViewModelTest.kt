package com.donghyeon.mystock.feature.settings.settings

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.donghyeon.mystock.core.model.ApiKeys
import com.donghyeon.mystock.core.model.AppVersion
import com.donghyeon.mystock.feature.settings.TestSettingsRepository
import com.donghyeon.mystock.feature.settings.testSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import com.donghyeon.mystock.feature.common.R as CommonR

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var repository: TestSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = TestSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): SettingsViewModel =
        SettingsViewModel(repository, AppVersion("0.1.0")).also { viewModel ->
            backgroundScope.launch(Dispatchers.Main) { viewModel.uiState.collect {} }
        }

    private fun TestScope.effects(viewModel: SettingsViewModel): List<SettingsUiEffect> {
        val effects = mutableListOf<SettingsUiEffect>()
        backgroundScope.launch(Dispatchers.Main) { viewModel.effects.collect { effects += it } }
        return effects
    }

    private fun fillKeys(viewModel: SettingsViewModel) {
        viewModel.uiState.value.keys.forEachIndexed { index, key -> key.setTextAndPlaceCursorAtEnd(" key-$index ") }
    }

    private fun keyTexts(viewModel: SettingsViewModel): List<String> =
        viewModel.uiState.value.keys.map { it.text.toString() }

    @Test
    fun `초기 상태는 저장된 설정과 앱 버전이다`() = runTest {
        repository = TestSettingsRepository(testSettings(notificationEnabled = true))

        val viewModel = viewModel()
        val state = viewModel.uiState.value

        assertTrue(state.settings.notificationEnabled)
        assertFalse(state.settings.keyAuthenticated)
        assertEquals("0.1.0", state.version)
        assertEquals(List(ApiKeys.COUNT) { "" }, keyTexts(viewModel))
    }

    @Test
    fun `알림 스위치는 시트 없이 그 자리에서 저장된다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsUiIntent.SetNotificationEnabled(true))

        assertEquals(listOf(true), repository.notificationChanges)
        assertTrue(viewModel.uiState.value.settings.notificationEnabled)
    }

    @Test
    fun `키 넷을 모두 채워야 인증을 요청한다`() = runTest {
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.uiState.value.keys[0].setTextAndPlaceCursorAtEnd("only-one")
        viewModel.onIntent(SettingsUiIntent.Authenticate)

        assertTrue(repository.authenticatedKeys.isEmpty())
        assertTrue(effects.any { it is SettingsUiEffect.ShowMessage })
    }

    @Test
    fun `인증에 성공하면 입력한 키를 비우고 인증 완료가 된다`() = runTest {
        val viewModel = viewModel()

        fillKeys(viewModel)
        viewModel.onIntent(SettingsUiIntent.Authenticate)

        assertEquals(listOf(ApiKeys(List(ApiKeys.COUNT) { "key-$it" })), repository.authenticatedKeys)
        assertTrue(viewModel.uiState.value.settings.keyAuthenticated)
        assertEquals(List(ApiKeys.COUNT) { "" }, keyTexts(viewModel))
    }

    @Test
    fun `인증 실패는 스낵바로 알리고 입력한 키를 남긴다`() = runTest {
        repository.authenticateResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = effects(viewModel)

        fillKeys(viewModel)
        viewModel.onIntent(SettingsUiIntent.Authenticate)

        assertFalse(viewModel.uiState.value.settings.keyAuthenticated)
        assertEquals(" key-0 ", keyTexts(viewModel)[0])
        assertTrue(effects.any { it is SettingsUiEffect.ShowMessage })
        assertFalse(viewModel.uiState.value.commanding)
    }

    @Test
    fun `인증 해제는 저장소에 요청하고 실패는 스낵바로 알린다`() = runTest {
        repository = TestSettingsRepository(testSettings(keyAuthenticated = true))
        repository.revokeResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.Revoke)

        assertEquals(1, repository.revokeCallCount)
        assertTrue(viewModel.uiState.value.settings.keyAuthenticated)
        assertTrue(effects.any { it is SettingsUiEffect.ShowMessage })
    }

    @Test
    fun `인증 해제에 성공하면 미인증이 된다`() = runTest {
        repository = TestSettingsRepository(testSettings(keyAuthenticated = true))
        val viewModel = viewModel()

        viewModel.onIntent(SettingsUiIntent.Revoke)

        assertEquals(1, repository.revokeCallCount)
        assertFalse(viewModel.uiState.value.settings.keyAuthenticated)
    }

    @Test
    fun `알림 저장 실패는 스낵바로 알리고 값은 그대로다`() = runTest {
        repository.notificationResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.SetNotificationEnabled(true))

        assertFalse(viewModel.uiState.value.settings.notificationEnabled)
        assertTrue(effects.any { it is SettingsUiEffect.ShowMessage })
    }

    @Test
    fun `알림을 켜면 알림 권한을 요청한다`() = runTest {
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.SetNotificationEnabled(true))

        assertEquals(listOf(SettingsUiEffect.RequestNotificationPermission), effects)
    }

    @Test
    fun `알림을 끄면 권한을 요청하지 않는다`() = runTest {
        repository = TestSettingsRepository(testSettings(notificationEnabled = true))
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.SetNotificationEnabled(false))

        assertTrue(effects.isEmpty())
    }

    @Test
    fun `알림 권한을 거부하면 스위치를 되돌리고 안내한다`() = runTest {
        repository = TestSettingsRepository(testSettings(notificationEnabled = true))
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.DenyNotificationPermission)

        assertEquals(listOf(false), repository.notificationChanges)
        assertFalse(viewModel.uiState.value.settings.notificationEnabled)
        assertTrue(effects.any { it is SettingsUiEffect.ShowMessage })
    }

    @Test
    fun `권한 거부를 되돌리지 못하면 저장 실패를 알린다`() = runTest {
        repository = TestSettingsRepository(testSettings(notificationEnabled = true))
        repository.notificationResult = Result.failure(IOException())
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.DenyNotificationPermission)

        assertTrue(viewModel.uiState.value.settings.notificationEnabled)
        assertEquals(listOf(SettingsUiEffect.ShowMessage(CommonR.string.common_notification_failed)), effects)
    }

    @Test
    fun `키 인증은 응답이 올 때까지 오버레이로 막는다`() = runTest {
        repository.authenticateGate = CompletableDeferred()
        val viewModel = viewModel()

        fillKeys(viewModel)
        viewModel.onIntent(SettingsUiIntent.Authenticate)
        assertTrue(viewModel.uiState.value.commanding)

        repository.authenticateGate?.complete(Unit)

        assertFalse(viewModel.uiState.value.commanding)
        assertTrue(viewModel.uiState.value.settings.keyAuthenticated)
    }

    @Test
    fun `인증 해제는 응답이 올 때까지 오버레이로 막는다`() = runTest {
        repository = TestSettingsRepository(testSettings(keyAuthenticated = true))
        repository.revokeGate = CompletableDeferred()
        val viewModel = viewModel()

        viewModel.onIntent(SettingsUiIntent.Revoke)
        assertTrue(viewModel.uiState.value.commanding)

        repository.revokeGate?.complete(Unit)

        assertFalse(viewModel.uiState.value.commanding)
        assertFalse(viewModel.uiState.value.settings.keyAuthenticated)
    }

    @Test
    fun `키 넷이 안 채워지면 오버레이 없이 안내만 한다`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsUiIntent.Authenticate)

        assertFalse(viewModel.uiState.value.commanding)
    }

    @Test
    fun `인증 요청이 진행 중이면 연타를 무시한다`() = runTest {
        repository.authenticateGate = CompletableDeferred()
        val viewModel = viewModel()

        fillKeys(viewModel)
        viewModel.onIntent(SettingsUiIntent.Authenticate)
        viewModel.onIntent(SettingsUiIntent.Authenticate)
        viewModel.onIntent(SettingsUiIntent.Revoke)

        assertEquals(1, repository.authenticatedKeys.size)
        assertEquals(0, repository.revokeCallCount)

        repository.authenticateGate?.complete(Unit)

        assertTrue(viewModel.uiState.value.settings.keyAuthenticated)
    }

    @Test
    fun `인증 요청 중에 고친 입력은 이미 보낸 키에 영향을 주지 않는다`() = runTest {
        repository.authenticateGate = CompletableDeferred()
        val viewModel = viewModel()

        fillKeys(viewModel)
        viewModel.onIntent(SettingsUiIntent.Authenticate)
        viewModel.uiState.value.keys[0].setTextAndPlaceCursorAtEnd("edited")
        repository.authenticateGate?.complete(Unit)

        assertEquals(listOf(ApiKeys(List(ApiKeys.COUNT) { "key-$it" })), repository.authenticatedKeys)
        assertEquals(List(ApiKeys.COUNT) { "" }, keyTexts(viewModel))
    }

    @Test
    fun `이동 의도는 그대로 이펙트가 된다`() = runTest {
        val viewModel = viewModel()
        val effects = effects(viewModel)

        viewModel.onIntent(SettingsUiIntent.Navigate(SettingsNavTarget.Licenses))

        assertEquals(listOf(SettingsUiEffect.Navigate(SettingsNavTarget.Licenses)), effects)
    }
}
