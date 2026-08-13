package com.donghyeon.mystock.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.donghyeon.mystock.design.MyStockTheme

private const val SHRINK_STEPS = 32

// 금액은 줄을 바꾸지도 생략하지도 않는다 — 자리에 맞을 때까지 글자 크기를 줄인다
@Composable
fun MsAmountText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MyStockTheme.typography.number,
    color: Color = MyStockTheme.colors.textPrimary,
) {
    FitText(text = text, style = style, color = color, modifier = modifier)
}

// 금액과 같은 줄을 나누는 종목명도 같은 방식으로 줄어든다
@Composable
internal fun FitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // 크기 없는 스타일은 Text가 LocalTextStyle로 채우므로 같은 값에서 출발한다
    val resolved = LocalTextStyle.current.merge(style)
    val textMeasurer = rememberTextMeasurer()
    // Compose의 텍스트 캐시는 자동 축소 뒤 마지막에 고른 크기로 intrinsic 폭을 재서 측정 순서에 따라
    // 값이 달라진다 — 원래 크기의 폭으로 고정해 비례 분할이 흔들리지 않게 한다
    val wantedWidth = remember(textMeasurer, text, resolved) {
        textMeasurer.measure(text, resolved, softWrap = false, maxLines = 1).size.width
    }
    Text(
        text = text,
        style = style,
        color = color,
        autoSize = if (resolved.fontSize.isSpecified) FitAutoSize(resolved.fontSize) else null,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.then(FixedIntrinsicWidthElement(wantedWidth)),
    )
}

private data class FixedIntrinsicWidthElement(private val width: Int) : ModifierNodeElement<FixedIntrinsicWidthNode>() {
    override fun create() = FixedIntrinsicWidthNode(width)

    override fun update(node: FixedIntrinsicWidthNode) {
        node.width = width
    }
}

private class FixedIntrinsicWidthNode(var width: Int) : LayoutModifierNode, Modifier.Node() {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) = width

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) = width
}

// StepBased는 하한에서 올라가는 격자라 스타일 크기에 정확히 닿지 않는다 — 맞으면 원래 크기를 그대로 쓴다
private data class FitAutoSize(private val fontSize: TextUnit) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit =
        if (fits(constraints, text, fontSize)) fontSize else sizeAt(smallestFittingStep(constraints, text))

    // 아무 단계도 안 맞으면 마지막 단계(원래 크기의 1/32)가 남는다 — 0sp로 사라지지 않게
    private fun TextAutoSizeLayoutScope.smallestFittingStep(constraints: Constraints, text: AnnotatedString): Int {
        var fitting = SHRINK_STEPS - 1
        var overflowing = 0
        while (fitting - overflowing > 1) {
            val step = (fitting + overflowing) / 2
            if (fits(constraints, text, sizeAt(step))) fitting = step else overflowing = step
        }
        return fitting
    }

    private fun sizeAt(step: Int): TextUnit = fontSize * (SHRINK_STEPS - step) / SHRINK_STEPS

    private fun TextAutoSizeLayoutScope.fits(constraints: Constraints, text: AnnotatedString, size: TextUnit): Boolean {
        val result = performLayout(constraints, text, size)
        return !result.hasVisualOverflow && !(result.lineCount > 0 && result.isLineEllipsized(0))
    }
}

@PreviewThemes
@Composable
private fun MsAmountTextPreview() {
    MyStockTheme {
        Column(Modifier.padding(16.dp).width(120.dp)) {
            MsAmountText(text = "5,483,000")
            MsAmountText(text = "1,234,567,890")
            MsAmountText(text = "1,234,567,890", style = MyStockTheme.typography.numberLarge)
        }
    }
}
