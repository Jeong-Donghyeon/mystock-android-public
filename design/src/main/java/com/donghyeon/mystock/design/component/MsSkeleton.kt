package com.donghyeon.mystock.design.component

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

@Composable
fun MsSkeleton(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
    )
    Box(
        modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(MyStockTheme.radius.sm))
            .background(MyStockTheme.colors.border)
            .clearAndSetSemantics {},
    )
}

@Composable
fun MsSkeletonText(
    style: TextStyle,
    chars: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val width = with(density) { style.fontSize.toDp() } * chars
    val height = with(density) { style.lineHeight.toDp() }
    MsSkeleton(modifier.size(width = width, height = height))
}

@Composable
fun MsSkeletonChip(
    chars: Int,
    modifier: Modifier = Modifier,
) {
    val style = MyStockTheme.typography.label
    val density = LocalDensity.current
    val width = with(density) { style.fontSize.toDp() } * chars + MyStockTheme.spacing.md * 2
    val height = with(density) { style.lineHeight.toDp() } + MyStockTheme.spacing.xs * 2
    MsSkeleton(modifier.size(width = width, height = height))
}

@PreviewThemes
@Composable
private fun MsSkeletonPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MsSkeletonText(style = MyStockTheme.typography.label, chars = 4)
            MsSkeletonText(style = MyStockTheme.typography.numberLarge, chars = 6)
        }
    }
}
