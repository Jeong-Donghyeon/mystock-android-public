package com.donghyeon.mystock.feature.account.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data class PerformanceKey(val accountId: String) : NavKey
