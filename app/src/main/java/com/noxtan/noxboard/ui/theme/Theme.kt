package com.noxtan.noxboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

@Composable
fun NoxBoardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NoxBoardDarkColorScheme,
        typography = Typography,
        content = content
    )
}