package com.donghyeon.mystock.feature.strategy.strategies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.donghyeon.mystock.design.component.LocalSnackbarHostState
import com.donghyeon.mystock.design.component.SnackbarController

@Composable
fun StrategiesRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {},
    viewModel: StrategiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = LocalSnackbarHostState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigate by rememberUpdatedState<(StrategiesNavTarget) -> Unit> { target ->
        when (target) {
            StrategiesNavTarget.Back -> onBack()
            is StrategiesNavTarget.Detail -> onOpenDetail(target.strategyKey)
        }
    }
    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState, resources) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val snackbar = SnackbarController(snackbarHostState, this)
            viewModel.effects.collect { effect ->
                when (effect) {
                    is StrategiesUiEffect.Navigate -> navigate(effect.target)
                    is StrategiesUiEffect.ShowMessage -> snackbar.show(resources.getString(effect.message))
                }
            }
        }
    }
    StrategiesScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
