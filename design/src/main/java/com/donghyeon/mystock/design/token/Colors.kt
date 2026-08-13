package com.donghyeon.mystock.design.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MyStockColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val priceUp: Color,
    val priceDown: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
)

// 프리뷰 어노테이션이 같은 값을 써야 해서 상수로 둔다
const val DARK_BACKGROUND_ARGB = 0xFF0B0D10
const val LIGHT_BACKGROUND_ARGB = 0xFFF5F6F8

internal val darkColors = MyStockColors(
    background = Color(DARK_BACKGROUND_ARGB),
    surface = Color(0xFF14171C),
    surfaceElevated = Color(0xFF1C2027),
    border = Color(0xFF262B33),
    textPrimary = Color(0xFFE8EAED),
    textSecondary = Color(0xFF9AA0A8),
    textTertiary = Color(0xFF6B7280),
    accent = Color(0xFFFF6B00),
    onAccent = Color(0xFFFFFFFF),
    priceUp = Color(0xFFFF4A4A),
    priceDown = Color(0xFF4A90FF),
    success = Color(0xFF22C55E),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFFF4A4A),
)

internal val lightColors = MyStockColors(
    background = Color(LIGHT_BACKGROUND_ARGB),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEDEFF3),
    border = Color(0xFFE2E5EA),
    textPrimary = Color(0xFF1A1D21),
    textSecondary = Color(0xFF5B6470),
    textTertiary = Color(0xFF8B93A0),
    accent = Color(0xFFE86000),
    onAccent = Color(0xFFFFFFFF),
    priceUp = Color(0xFFD62E3C),
    priceDown = Color(0xFF2563EB),
    success = Color(0xFF188A42),
    warning = Color(0xFFB45309),
    danger = Color(0xFFD62E3C),
)

internal val LocalMyStockColors = staticCompositionLocalOf<MyStockColors> {
    error("MyStockColors를 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}
