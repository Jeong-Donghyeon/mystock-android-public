package com.donghyeon.mystock.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainWidth
import kotlin.math.max

// 두 조각이 나란히 들어가면 제 폭대로 두고, 안 들어가면 원래 폭에 비례해 나눈다 —
// 한쪽이 자리를 다 차지해 다른 쪽이 사실상 사라지는 일이 없게. 조각은 그 안에서 스스로 줄어든다
@Composable
internal fun MsShareRow(
    spacing: Dp,
    start: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    spread: Boolean = false,
    alignByBaseline: Boolean = false,
    end: @Composable () -> Unit,
) {
    val gap = with(LocalDensity.current) { spacing.roundToPx() }
    val policy = remember(gap, spread, alignByBaseline) { SharePolicy(gap, spread, alignByBaseline) }
    Layout(contents = listOf(start, end), modifier = modifier, measurePolicy = policy)
}

private class SharePolicy(
    private val gap: Int,
    private val spread: Boolean,
    private val alignByBaseline: Boolean,
) : MultiContentMeasurePolicy {
    override fun MeasureScope.measure(measurables: List<List<Measurable>>, constraints: Constraints): MeasureResult {
        val (startMeasurable, endMeasurable) = measurables.map { it.firstOrNull() }
        val gapUsed = if (startMeasurable != null && endMeasurable != null) gap else 0
        val (startMax, endMax) = shareWidths(
            startWant = startMeasurable.wantedWidth(constraints),
            endWant = endMeasurable.wantedWidth(constraints),
            // 간격보다 좁은 자리에서 음수 폭으로 Constraints를 만들지 않는다
            available = (constraints.maxWidth - gapUsed).coerceAtLeast(0),
            bounded = constraints.hasBoundedWidth,
        )
        val start = Piece(startMeasurable?.measure(Constraints(maxWidth = startMax, maxHeight = constraints.maxHeight)))
        val end = Piece(endMeasurable?.measure(Constraints(maxWidth = endMax, maxHeight = constraints.maxHeight)))
        val width = constraints.constrainWidth(
            if (spread && constraints.hasBoundedWidth) constraints.maxWidth else start.width + gapUsed + end.width,
        )
        val baseline = max(start.baseline, end.baseline)
        val height = if (alignByBaseline) {
            max(baseline - start.baseline + start.height, baseline - end.baseline + end.height)
        } else {
            max(start.height, end.height)
        }
        val y = { piece: Piece ->
            if (alignByBaseline) baseline - piece.baseline else Alignment.CenterVertically.align(piece.height, height)
        }
        val lines: Map<AlignmentLine, Int> = if (alignByBaseline) mapOf(FirstBaseline to baseline) else emptyMap()
        return layout(width, height, lines) {
            start.placeable?.placeRelative(0, y(start))
            end.placeable?.placeRelative(if (spread) width - end.width else start.width + gapUsed, y(end))
        }
    }
}

private fun IntrinsicMeasurable?.wantedWidth(constraints: Constraints): Int =
    this?.maxIntrinsicWidth(constraints.maxHeight) ?: 0

// 들어갈 때는 intrinsic 폭으로 조이지 않는다 — 반올림 차이로 한 단계 줄어드는 것을 막는다
private fun shareWidths(startWant: Int, endWant: Int, available: Int, bounded: Boolean): Pair<Int, Int> = when {
    !bounded -> Constraints.Infinity to Constraints.Infinity
    startWant + endWant <= available -> (available - endWant) to (available - startWant)
    else -> (available.toLong() * startWant / (startWant + endWant)).toInt().let { it to available - it }
}

private class Piece(val placeable: Placeable?) {
    val width = placeable?.width ?: 0
    val height = placeable?.height ?: 0
    val baseline = placeable?.let {
        it[FirstBaseline].takeIf { line -> line != AlignmentLine.Unspecified } ?: it.height
    } ?: 0
}
