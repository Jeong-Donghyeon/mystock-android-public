package com.donghyeon.mystock.design.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme

private val TrackWidth = 44.dp
private val TrackHeight = 26.dp
private val TrackPadding = 3.dp
private val ThumbSize = 20.dp
private const val TOGGLE_DURATION_MS = 200

@Composable
fun MsToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MyStockTheme.colors
    val spec = tween<Color>(TOGGLE_DURATION_MS)
    val trackColor by animateColorAsState(if (checked) colors.accent else colors.surface, spec)
    val thumbColor by animateColorAsState(if (checked) colors.onAccent else colors.textTertiary, spec)
    val borderColor by animateColorAsState(if (checked) colors.border.copy(alpha = 0f) else colors.border, spec)
    val thumbOffset
        by animateDpAsState(
            targetValue = if (checked) TrackWidth - TrackPadding * 2 - ThumbSize else MyStockTheme.stroke,
            animationSpec = tween(TOGGLE_DURATION_MS),
        )
    val shape = RoundedCornerShape(MyStockTheme.radius.full)
    Box(
        modifier = modifier
            .size(width = TrackWidth, height = TrackHeight)
            .clip(shape)
            .background(trackColor)
            .border(MyStockTheme.stroke, borderColor, shape)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                } else {
                    Modifier
                },
            )
            .padding(TrackPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
                .size(ThumbSize)
                .clip(CircleShape)
                .background(thumbColor),
        )
    }
}

@PreviewThemes
@Composable
private fun MsTogglePreview() {
    MyStockTheme {
        Column(Modifier.padding(16.dp)) {
            MsToggle(checked = true, onCheckedChange = {})
            MsToggle(checked = false, onCheckedChange = {})
        }
    }
}
