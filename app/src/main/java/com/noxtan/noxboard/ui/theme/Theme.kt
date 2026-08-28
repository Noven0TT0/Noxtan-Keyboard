package com.noxtan.noxboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NoxBoardDarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = PureBlack,
    background = AppBackground,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextGray,
    outline = PrimaryPurpleDark
)

private val NoxBoardLightColorScheme = lightColorScheme(
    primary = PrimaryPurpleDark,
    onPrimary = PureWhite,
    background = AppBackgroundLight,
    onBackground = TextBlack,
    surface = SurfaceLight,
    onSurface = TextBlack,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextGrayLight,
    outline = PrimaryPurple
)

@Composable
fun NoxBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        NoxBoardDarkColorScheme
    } else {
        NoxBoardLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}