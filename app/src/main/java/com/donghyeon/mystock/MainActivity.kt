package com.donghyeon.mystock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation3.runtime.rememberNavBackStack
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.LocalSnackbarHostState
import com.donghyeon.mystock.design.component.MsSnackbarHost
import com.donghyeon.mystock.feature.account.navigation.HomeKey
import com.donghyeon.mystock.navigation.MyStockDeepLinks
import com.donghyeon.mystock.navigation.MyStockNavDisplay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialStack = MyStockDeepLinks.resolve(intent) ?: listOf(HomeKey)
        setContent {
            MyStockTheme {
                val backStack = rememberNavBackStack(*initialStack.toTypedArray())
                val snackbarHostState = remember { SnackbarHostState() }
                DisposableEffect(Unit) {
                    val listener = Consumer<Intent> { newIntent ->
                        intent = newIntent
                        MyStockDeepLinks.resolve(newIntent)?.let { routes ->
                            backStack.clear()
                            backStack.addAll(routes)
                        }
                    }
                    addOnNewIntentListener(listener)
                    onDispose { removeOnNewIntentListener(listener) }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MyStockTheme.colors.background,
                    snackbarHost = { MsSnackbarHost(snackbarHostState) },
                ) { innerPadding ->
                    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        MyStockNavDisplay(
                            backStack = backStack,
                            modifier = Modifier
                                .padding(innerPadding)
                                .consumeWindowInsets(innerPadding),
                            onExit = { finish() },
                        )
                    }
                }
            }
        }
    }
}
