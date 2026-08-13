package com.donghyeon.mystock.feature.common

import com.donghyeon.mystock.core.model.Currency
import com.donghyeon.mystock.core.model.Market
import com.donghyeon.mystock.core.model.Price
import com.donghyeon.mystock.design.component.PnlFormat
import com.donghyeon.mystock.design.component.formatPnl
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd", Locale.US)

fun formatAmount(value: Long): String = String.format(Locale.US, "%,d", value)

fun formatPrice(price: Price): String = when (price.currency) {
    Currency.KRW -> String.format(Locale.US, "%,.0f", price.value)
    Currency.USD -> String.format(Locale.US, "$%,.2f", price.value)
}

// 정수 수량이 4.0으로 보이면 안 된다
fun formatPosition(quantity: Double, price: Price): String =
    "${BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()}주 · ${formatPrice(price)}"

fun formatHours(hours: Long): String = "${hours}h"

fun formatVersion(version: String): String = "v$version"

fun formatSignedPercent(value: Double): String = formatPnl(value, PnlFormat.PERCENT)

fun formatWinRate(value: Double): String = String.format(Locale.US, "%.0f%%", value)

fun formatDate(date: LocalDate): String = date.format(shortDate)

fun marketLabel(market: Market): String = when (market) {
    Market.KR -> "KR"
    Market.US -> "US"
}
