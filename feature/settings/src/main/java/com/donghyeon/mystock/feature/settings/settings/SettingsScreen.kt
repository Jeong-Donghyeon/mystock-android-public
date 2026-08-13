package com.donghyeon.mystock.feature.settings.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.KeyAuthChip
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsCommandOverlay
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsPrimaryButton
import com.donghyeon.mystock.design.component.MsSecondaryButton
import com.donghyeon.mystock.design.component.MsSectionLabel
import com.donghyeon.mystock.design.component.MsSettingRow
import com.donghyeon.mystock.design.component.MsTextField
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.settings.R
import com.donghyeon.mystock.feature.common.R as CommonR

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    // 키 입력 중 인증을 누르면 키보드가 오버레이 위에 남으므로 포커스를 지워 내린다
    LaunchedEffect(state.commanding) { if (state.commanding) focusManager.clearFocus() }
    Surface(modifier = modifier.fillMaxSize(), color = MyStockTheme.colors.background) {
        Column {
            MsBackTopBar(
                title = stringResource(R.string.settings_title),
                onBack = { onIntent(SettingsUiIntent.Navigate(SettingsNavTarget.Back)) },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = MyStockTheme.spacing.lg,
                        end = MyStockTheme.spacing.lg,
                        top = MyStockTheme.spacing.md,
                        bottom = MyStockTheme.spacing.xl,
                    ),
                verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
            ) {
                MsListCard(
                    rows = listOf {
                        MsSettingRow(
                            label = stringResource(R.string.settings_notification),
                            checked = state.settings.notificationEnabled,
                            onCheckedChange = { onIntent(SettingsUiIntent.SetNotificationEnabled(it)) },
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MsSectionLabel(text = stringResource(R.string.settings_key_auth), modifier = Modifier.weight(1f))
                    KeyAuthChip(authenticated = state.settings.keyAuthenticated)
                }
                if (state.settings.keyAuthenticated) {
                    AuthenticatedCard(onIntent = onIntent)
                } else {
                    KeyEntryCard(keys = state.keys, onIntent = onIntent)
                }
                MsListCard(
                    rows = listOf(
                        {
                            MsSettingRow(
                                label = stringResource(CommonR.string.common_licenses),
                                onClick = { onIntent(SettingsUiIntent.Navigate(SettingsNavTarget.Licenses)) },
                            )
                        },
                        { MsSettingRow(label = stringResource(R.string.settings_version), value = state.version) },
                    ),
                )
            }
        }
    }

    if (state.commanding) MsCommandOverlay()
}

@Composable
private fun KeyEntryCard(
    keys: List<TextFieldState>,
    onIntent: (SettingsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md)) {
            Text(
                text = stringResource(R.string.settings_unauthenticated_note),
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textTertiary,
            )
            keys.forEachIndexed { index, key ->
                MsTextField(
                    state = key,
                    label = stringResource(R.string.settings_key_label, index + 1),
                    placeholder = stringResource(R.string.settings_key_placeholder),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (index == keys.lastIndex) ImeAction.Done else ImeAction.Next,
                    ),
                )
            }
            MsPrimaryButton(
                text = stringResource(R.string.settings_authenticate),
                onClick = { onIntent(SettingsUiIntent.Authenticate) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AuthenticatedCard(
    onIntent: (SettingsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md)) {
            Text(
                text = stringResource(R.string.settings_authenticated_note),
                style = MyStockTheme.typography.caption,
                color = MyStockTheme.colors.textTertiary,
            )
            MsSecondaryButton(
                text = stringResource(R.string.settings_revoke),
                onClick = { onIntent(SettingsUiIntent.Revoke) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewThemes
@Composable
private fun SettingsUnauthenticatedPreview() {
    MyStockTheme {
        SettingsScreen(state = sampleUnauthenticatedState, onIntent = {})
    }
}

@PreviewThemes
@Composable
private fun SettingsAuthenticatedPreview() {
    MyStockTheme {
        SettingsScreen(state = sampleAuthenticatedState, onIntent = {})
    }
}
