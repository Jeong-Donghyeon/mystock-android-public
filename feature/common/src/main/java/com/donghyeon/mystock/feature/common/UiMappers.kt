package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.OrderSide
import com.donghyeon.mystock.core.model.RunOutcome
import com.donghyeon.mystock.design.model.AccountState as UiAccountState
import com.donghyeon.mystock.design.model.OrderSide as UiOrderSide
import com.donghyeon.mystock.design.model.RunOutcome as UiRunOutcome

fun AccountState.toUi(): UiAccountState = when (this) {
    AccountState.RUNNING -> UiAccountState.RUNNING
    AccountState.CLOSING -> UiAccountState.CLOSING
    AccountState.LIQUIDATING -> UiAccountState.LIQUIDATING
    AccountState.PAUSED -> UiAccountState.PAUSED
    AccountState.WAITING -> UiAccountState.WAITING
}

fun OrderSide.toUi(): UiOrderSide = when (this) {
    OrderSide.BUY -> UiOrderSide.BUY
    OrderSide.SELL -> UiOrderSide.SELL
}

fun RunOutcome.toUi(): UiRunOutcome = when (this) {
    RunOutcome.RUNNING -> UiRunOutcome.RUNNING
    RunOutcome.CLOSED -> UiRunOutcome.CLOSED
    RunOutcome.LIQUIDATED -> UiRunOutcome.LIQUIDATED
}
