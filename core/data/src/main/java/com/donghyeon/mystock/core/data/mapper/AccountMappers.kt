package com.donghyeon.mystock.core.data.mapper

import com.donghyeon.mystock.core.model.Account
import com.donghyeon.mystock.core.model.AccountState
import com.donghyeon.mystock.core.model.Pnl
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.core.network.dto.AccountDto
import com.donghyeon.mystock.core.network.dto.PnlDto
import com.donghyeon.mystock.core.network.dto.PriceDto

internal fun AccountDto.toDomain(): Account = Account(
    id = id,
    displayName = displayName,
    state = state.toEnum<AccountState>(),
    strategy = strategy?.toDomain(),
    todayPnl = todayPnl?.toDomain(),
    cumulativePnl = cumulativePnl?.toDomain(),
    pauseReason = pauseReason,
    orderAlertEnabled = orderAlertEnabled,
)

internal fun PnlDto.toDomain(): Pnl = Pnl(amount = amount, percent = percent)

internal fun PriceDto.toDomain(): Price = Price(value = value, currency = currency.toEnum())

// 문자열 enum은 계약의 값 그대로다. 모르는 값은 계약이 어긋난 것이라 실패시킨다
internal inline fun <reified E : Enum<E>> String.toEnum(): E = enumValueOf<E>(this)
