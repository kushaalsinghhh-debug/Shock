package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WinX7ColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextWhite,
    primaryContainer = DarkPurple,
    onPrimaryContainer = TextWhite,
    secondary = DarkPurple,
    onSecondary = TextWhite,
    background = BgDark,
    onBackground = TextWhite,
    surface = CardDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextGrey,
    error = ErrorRed,
    onError = TextWhite,
    outline = SurfaceBorder
)

@Composable
fun WinX7Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WinX7ColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for template backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WinX7Theme(content = content)
}
