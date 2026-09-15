package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = CyberAmber,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF604100),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = CyberGreen,
    error = CyberRedAlert,
    onError = Color.White,
    background = CyberDark,
    onBackground = Color(0xFFE2E8F0),
    surface = CyberSurfaceDark,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = CyberLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB1ECFA),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF825500),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF291800),
    tertiary = Color(0xFF00703C),
    error = CyberRedAlert,
    onError = Color.White,
    background = CyberLightSurface,
    onBackground = CyberLightText,
    surface = CyberLightCard,
    onSurface = CyberLightText,
    surfaceVariant = Color(0xFFE0E7F1),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

