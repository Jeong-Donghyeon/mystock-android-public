package com.donghyeon.mystock.feature.account.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun HomeRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenStrategies: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPerformance: (String) -> Unit = {},
    onOpenStrategy: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) viewModel.onIntent(HomeUiIntent.DenyNotificationPermission)
    }
    val resources = LocalResources.current
    val snackbarHostState = LocalSnackbarHostState.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigate by rememberUpdatedState<(HomeNavTarget) -> Unit> { target ->
        when (target) {
            HomeNavTarget.Exit -> onBack()
            HomeNavTarget.Strategies -> onOpenStrategies()
            HomeNavTarget.Settings -> onOpenSettings()
            is HomeNavTarget.Performance -> onOpenPerformance(target.accountId)
            is HomeNavTarget.Strategy -> onOpenStrategy(target.key, target.accountId)
        }
    }
    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState, resources) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val snackbar = SnackbarController(snackbarHostState, this)
            viewModel.effects.collect { effect ->
                when (effect) {
                    is HomeUiEffect.Navigate -> navigate(effect.target)
                    is HomeUiEffect.ShowMessage -> snackbar.show(resources.getString(effect.message))
                    HomeUiEffect.RequestNotificationPermission ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                }
            }
        }
    }
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
