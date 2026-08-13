package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

private const val PERCENT_SCALE = 100.0

enum class PnlFormat { AMOUNT, PERCENT }

@Composable
fun PnlText(
    value: Double,
    modifier: Modifier = Modifier,
    format: PnlFormat = PnlFormat.PERCENT,
    style: TextStyle = MyStockTheme.typography.number,
) {
    MsAmountText(
        text = formatPnl(value, format),
        style = style,
        color = pnlColor(roundPnl(value, format)),
        modifier = modifier,
    )
}

@Composable
fun PnlPairText(
    amount: Double,
    percent: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MyStockTheme.typography.number,
) {
    MsAmountText(
        text = formatPnlPair(amount, percent),
        style = style,
        color = pnlColor(roundPnl(amount, PnlFormat.AMOUNT)),
        modifier = modifier,
    )
}

@Composable
internal fun pnlColor(value: Double): Color = with(MyStockTheme.colors) {
    when {
        value > 0 -> priceUp
        value < 0 -> priceDown
        else -> textSecondary
    }
}

// 기호·색은 표기되는 자릿수로 반올림한 값을 따른다 — 0.4원이 ▲ 0으로 보이지 않게
internal fun roundPnl(value: Double, format: PnlFormat): Double = when (format) {
    PnlFormat.AMOUNT -> value.roundToLong().toDouble()
    PnlFormat.PERCENT -> (value * PERCENT_SCALE).roundToLong() / PERCENT_SCALE
}

fun formatPnl(rawValue: Double, format: PnlFormat): String {
    val value = roundPnl(rawValue, format)
    val magnitude = abs(value)
    return when (format) {
        PnlFormat.AMOUNT -> {
            val arrow = when {
                value > 0 -> "▲ "
                value < 0 -> "▼ "
                else -> ""
            }
            arrow + String.format(Locale.US, "%,.0f", magnitude)
        }

        PnlFormat.PERCENT -> {
            val sign = when {
                value > 0 -> "+"
                value < 0 -> "-"
                else -> ""
            }
            sign + String.format(Locale.US, "%.2f%%", magnitude)
        }
    }
}

internal fun formatPnlPair(amount: Double, percent: Double): String =
    formatPnl(amount, PnlFormat.AMOUNT) + " (" + formatPnl(percent, PnlFormat.PERCENT) + ")"

@PreviewThemes
@Composable
private fun PnlTextPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PnlText(value = 3.25)
            PnlText(value = -1.87)
            PnlText(value = 0.0)
            PnlText(value = 152000.0, format = PnlFormat.AMOUNT)
            PnlText(value = -48500.0, format = PnlFormat.AMOUNT)
            PnlPairText(amount = -48500.0, percent = -2.14)
        }
    }
}
