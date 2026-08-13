package com.donghyeon.mystock.feature.account.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.MsDialog
import com.donghyeon.mystock.design.component.MsSheetActions
import com.donghyeon.mystock.design.component.PreviewThemes
import com.donghyeon.mystock.feature.account.R

@Composable
internal fun ExitDialog(
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MsDialog(
        onDismissRequest = { onIntent(HomeUiIntent.Overlay.DismissExit) },
        modifier = modifier,
    ) {
        ExitDialogContent(onIntent = onIntent)
    }
}

@Composable
private fun ExitDialogContent(
    onIntent: (HomeUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(MyStockTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.home_exit_title),
            style = MyStockTheme.typography.subtitle,
            color = MyStockTheme.colors.textPrimary,
        )
        MsSheetActions(
            confirmText = stringResource(R.string.home_exit_confirm),
            onConfirm = { onIntent(HomeUiIntent.Navigate(HomeNavTarget.Exit)) },
            onCancel = { onIntent(HomeUiIntent.Overlay.DismissExit) },
            modifier = Modifier.padding(top = MyStockTheme.spacing.sm),
        )
    }
}

@PreviewThemes
@Composable
private fun ExitDialogPreview() {
    MyStockTheme {
        Surface(color = MyStockTheme.colors.surfaceElevated) {
            ExitDialogContent(onIntent = {})
        }
    }
}
