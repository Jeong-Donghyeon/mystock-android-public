package com.donghyeon.mystock.feature.strategy.detail

import androidx.annotation.StringRes
import com.donghyeon.mystock.core.model.StartAccount
import com.donghyeon.mystock.core.model.StrategyDetail
import com.donghyeon.mystock.core.model.StrategyRun
import com.donghyeon.mystock.feature.common.RunSummary
import com.donghyeon.mystock.feature.common.summarize
import com.donghyeon.mystock.feature.strategy.sampleDetail

sealed interface StrategyDetailNavTarget {
    data object Back : StrategyDetailNavTarget

    data class Versions(val strategyKey: String) : StrategyDetailNavTarget

    data object Home : StrategyDetailNavTarget
}

sealed interface StrategyDetailContent {
    data object Loading : StrategyDetailContent

    data object Failed : StrategyDetailContent

    data class Ready(val detail: StrategyDetail) : StrategyDetailContent
}

sealed interface StrategyDetailSheet {
    data object AccountScope : StrategyDetailSheet

    data object Start : StrategyDetailSheet
}

data class ScopeAccount(val id: String, val name: String)

data class StrategyDetailUiState(
    val content: StrategyDetailContent = StrategyDetailContent.Loading,
    // 대기 홈에서 열렸는지 — 로드 전에도 계좌 줄 자리를 잡아 두려고 상세와 별개로 안다
    val startAccountExpected: Boolean = false,
    val refreshing: Boolean = false,
    val versionScope: String? = null,
    val accountScope: String? = null,
    val sheet: StrategyDetailSheet? = null,
    val commanding: Boolean = false,
) {
    val detail: StrategyDetail? get() = (content as? StrategyDetailContent.Ready)?.detail

    val startAccount: StartAccount? get() = detail?.startAccount

    val scopeAccounts: List<ScopeAccount>
        get() = detail?.runs.orEmpty()
            .map { ScopeAccount(it.accountId, it.accountName) }
            .distinct()

    val scopedRuns: List<StrategyRun>
        get() = detail?.runs.orEmpty().filter { run ->
            (versionScope == null || run.version == versionScope) &&
                (accountScope == null || run.accountId == accountScope)
        }

    val summary: RunSummary? get() = summarize(scopedRuns)
}

sealed interface StrategyDetailUiIntent {
    data class Navigate(val target: StrategyDetailNavTarget) : StrategyDetailUiIntent

    data object Refresh : StrategyDetailUiIntent

    data class SelectVersionScope(val version: String?) : StrategyDetailUiIntent

    data object OpenAccountSheet : StrategyDetailUiIntent

    data object DismissAccountSheet : StrategyDetailUiIntent

    data class SelectAccountScope(val accountId: String?) : StrategyDetailUiIntent

    data object OpenStart : StrategyDetailUiIntent

    data object DismissStart : StrategyDetailUiIntent

    data object ConfirmStart : StrategyDetailUiIntent
}

sealed interface StrategyDetailUiEffect {
    data class Navigate(val target: StrategyDetailNavTarget) : StrategyDetailUiEffect

    data class ShowMessage(@StringRes val message: Int) : StrategyDetailUiEffect
}

private val sampleStartAccount = StartAccount(id = "acc-1", name = "토스증권", cashAmount = 5_483_000)

internal val sampleReadyState = StrategyDetailUiState(content = StrategyDetailContent.Ready(sampleDetail))

internal val sampleWaitingState = StrategyDetailUiState(
    content = StrategyDetailContent.Ready(sampleDetail.copy(startAccount = sampleStartAccount)),
    startAccountExpected = true,
)

internal val sampleNoRunState = sampleWaitingState.copy(versionScope = "0.1.5")

internal val sampleAccountScopedState = sampleWaitingState.copy(accountScope = "acc-2")

internal val sampleAccountSheetState = sampleWaitingState.copy(sheet = StrategyDetailSheet.AccountScope)

internal val sampleStartSheetState = sampleWaitingState.copy(sheet = StrategyDetailSheet.Start)

internal val sampleCommandingState = sampleWaitingState.copy(commanding = true)

internal val sampleLoadingState = StrategyDetailUiState(startAccountExpected = true)

internal val sampleFailedState = StrategyDetailUiState(content = StrategyDetailContent.Failed)
