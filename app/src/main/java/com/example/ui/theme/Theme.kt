package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrokenScreenDarkScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004F54),
    onPrimaryContainer = Color(0xFF80F6FF),
    secondary = NeonMagenta,
    onSecondary = Color(0xFF5A0028),
    secondaryContainer = Color(0xFF7E003A),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = NeonGreen,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep custom sleek cracked neon glass aesthetic
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BrokenScreenDarkScheme,
        typography = Typography,
        content = content
    )
}
