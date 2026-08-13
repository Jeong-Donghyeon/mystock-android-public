package com.donghyeon.mystock.feature.account.performance

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
import com.donghyeon.mystock.feature.account.navigation.PerformanceKey

@Composable
fun PerformanceRoute(
    key: PerformanceKey,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: PerformanceViewModel = hiltViewModel<PerformanceViewModel, PerformanceViewModel.Factory>(
        creationCallback = { it.create(key) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = LocalSnackbarHostState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigate by rememberUpdatedState<(PerformanceNavTarget) -> Unit> { target ->
        when (target) {
            PerformanceNavTarget.Back -> onBack()
        }
    }
    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState, resources) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val snackbar = SnackbarController(snackbarHostState, this)
            viewModel.effects.collect { effect ->
                when (effect) {
                    is PerformanceUiEffect.Navigate -> navigate(effect.target)
                    is PerformanceUiEffect.ShowMessage -> snackbar.show(resources.getString(effect.message))
                }
            }
        }
    }
    PerformanceScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
