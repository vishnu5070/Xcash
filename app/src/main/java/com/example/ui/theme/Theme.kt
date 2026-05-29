package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = AbsoluteWhite,
    onPrimary = PureBlack,
    secondary = TextSecondary,
    onSecondary = AbsoluteWhite,
    background = PureBlack,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = ActionButtonGray,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

private val MonochromeLightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = AbsoluteWhite,
    secondary = LightTextSecondary,
    onSecondary = PureBlack,
    background = AbsoluteWhite,
    onBackground = LightTextPrimary,
    surface = LightCardBackground,
    onSurface = LightTextPrimary,
    surfaceVariant = LightActionButtonGray,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MonochromeDarkColorScheme else MonochromeLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
