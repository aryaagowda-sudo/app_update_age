package com.example.appup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CybersecurityColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberTextInverse,
    primaryContainer = CyberSurfaceElevated,
    onPrimaryContainer = CyberPrimary,
    secondary = CyberSecondary,
    onSecondary = CyberTextPrimary,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberPrimary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderGlow
)

@Composable
fun AppUpTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CybersecurityColorScheme,
        typography = Typography,
        content = content
    )
}
