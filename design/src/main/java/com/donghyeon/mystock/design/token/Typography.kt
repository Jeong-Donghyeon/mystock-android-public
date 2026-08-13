package com.donghyeon.mystock.design.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.donghyeon.mystock.design.R

@Immutable
data class MyStockTypography(
    val title: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val bodyMedium: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val numberLarge: TextStyle,
    val number: TextStyle,
    val numberSmall: TextStyle,
    val numberCaption: TextStyle,
)

private fun pretendard(weight: FontWeight) = Font(
    resId = R.font.pretendard,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Sans = FontFamily(
    pretendard(FontWeight.Normal),
    pretendard(FontWeight.Medium),
    pretendard(FontWeight.SemiBold),
    pretendard(FontWeight.Bold),
)

private const val TABULAR = "tnum"

internal val myStockTypography = MyStockTypography(
    title = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    ),
    subtitle = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
    ),
    body = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    label = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    caption = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    numberLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontFeatureSettings = TABULAR,
    ),
    number = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = TABULAR,
    ),
    numberSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = TABULAR,
    ),
    numberCaption = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = TABULAR,
    ),
)

internal val LocalMyStockTypography = staticCompositionLocalOf<MyStockTypography> {
    error("MyStockTypography를 찾을 수 없습니다. MyStockTheme 안에서 사용하세요.")
}
