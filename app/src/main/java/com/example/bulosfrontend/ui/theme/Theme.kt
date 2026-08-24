package com.example.bulosfrontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SoftGreen,
    secondary = Sand,
    tertiary = MistBlue,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = WarmWhite,
    primaryContainer = SoftGreen,
    onPrimaryContainer = DeepForestGreen,
    secondary = WarmBrown,
    background = Cream,
    onBackground = DeepForestGreen,
    surface = WarmWhite,
    onSurface = DeepForestGreen,
    surfaceVariant = Sand,
    onSurfaceVariant = WarmBrown,
)

@Composable
fun BulosFrontEndTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
