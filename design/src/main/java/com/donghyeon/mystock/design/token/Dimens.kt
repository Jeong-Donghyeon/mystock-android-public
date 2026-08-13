package com.donghyeon.mystock.design.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val touchMin: Dp = 48.dp,
)

@Immutable
data class Radius(
    val sm: Dp = 6.dp,
    val md: Dp = 12.dp,
    val full: Dp = 999.dp,
)

@Immutable
data class IconSize(
    val sm: Dp = 16.dp,
    val md: Dp = 20.dp,
    val lg: Dp = 24.dp,
)

internal val defaultSpacing = Spacing()
internal val defaultRadius = Radius()
internal val defaultIconSize = IconSize()
internal val defaultStroke: Dp = 1.dp

internal val LocalSpacing = staticCompositionLocalOf<Spacing> {
    error("Spacing 토큰을 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}

internal val LocalRadius = staticCompositionLocalOf<Radius> {
    error("Radius 토큰을 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}

internal val LocalIconSize = staticCompositionLocalOf<IconSize> {
    error("IconSize 토큰을 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}

internal val LocalStroke = staticCompositionLocalOf<Dp> {
    error("stroke 토큰을 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}
