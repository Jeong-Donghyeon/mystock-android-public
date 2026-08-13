package com.donghyeon.mystock.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.donghyeon.mystock.design.token.IconSize
import com.donghyeon.mystock.design.token.LocalIconSize
import com.donghyeon.mystock.design.token.LocalMyStockColors
import com.donghyeon.mystock.design.token.LocalMyStockTypography
import com.donghyeon.mystock.design.token.LocalRadius
import com.donghyeon.mystock.design.token.LocalSpacing
import com.donghyeon.mystock.design.token.LocalStroke
import com.donghyeon.mystock.design.token.MyStockColors
import com.donghyeon.mystock.design.token.MyStockTypography
import com.donghyeon.mystock.design.token.Radius
import com.donghyeon.mystock.design.token.Spacing
import com.donghyeon.mystock.design.token.darkColors
import com.donghyeon.mystock.design.token.defaultIconSize
import com.donghyeon.mystock.design.token.defaultRadius
import com.donghyeon.mystock.design.token.defaultSpacing
import com.donghyeon.mystock.design.token.defaultStroke
import com.donghyeon.mystock.design.token.lightColors
import com.donghyeon.mystock.design.token.myStockTypography

@Composable
fun MyStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkColors else lightColors
    val materialScheme = remember(darkTheme) {
        val base = if (darkTheme) darkColorScheme() else lightColorScheme()
        base.copy(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            secondary = colors.textSecondary,
            onSecondary = colors.background,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceElevated,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            outlineVariant = colors.border,
            error = colors.danger,
            onError = colors.onAccent,
        )
    }
    CompositionLocalProvider(
        LocalMyStockColors provides colors,
        LocalMyStockTypography provides myStockTypography,
        LocalSpacing provides defaultSpacing,
        LocalRadius provides defaultRadius,
        LocalIconSize provides defaultIconSize,
        LocalStroke provides defaultStroke,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = materialTypography,
            content = content,
        )
    }
}

private val materialTypography = Typography(
    titleLarge = myStockTypography.title,
    titleMedium = myStockTypography.subtitle,
    bodyLarge = myStockTypography.body,
    bodyMedium = myStockTypography.body,
    labelLarge = myStockTypography.label,
    labelSmall = myStockTypography.label,
)

object MyStockTheme {
    val colors: MyStockColors
        @Composable @ReadOnlyComposable
        get() = LocalMyStockColors.current

    val typography: MyStockTypography
        @Composable @ReadOnlyComposable
        get() = LocalMyStockTypography.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current

    val radius: Radius
        @Composable @ReadOnlyComposable
        get() = LocalRadius.current

    val iconSize: IconSize
        @Composable @ReadOnlyComposable
        get() = LocalIconSize.current

    val stroke: Dp
        @Composable @ReadOnlyComposable
        get() = LocalStroke.current
}
