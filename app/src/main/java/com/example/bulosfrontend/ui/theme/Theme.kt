package com.example.bulosfrontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF88AA7E),
    onPrimary = Color(0xFF102512),
    primaryContainer = Color(0xFF294B2B),
    onPrimaryContainer = Color(0xFFE6F0DE),
    secondary = Color(0xFFC9AE8C),
    onSecondary = Color(0xFF2B1D13),
    secondaryContainer = Color(0xFF4A3828),
    onSecondaryContainer = Color(0xFFF1DFC8),
    tertiary = Color(0xFFA9BAC7),
    onTertiary = Color(0xFF17232B),
    background = DarkHomeBody,
    onBackground = DarkWarmText,
    surface = DarkEarthSurface,
    onSurface = DarkWarmText,
    surfaceVariant = Color(0xFF332B23),
    onSurfaceVariant = DarkMutedText,
    outline = Color(0xFF51473B),
    outlineVariant = DarkWarmOutline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

val LocalBulosDarkTheme = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = WarmWhite,
    primaryContainer = SoftGreen,
    onPrimaryContainer = DeepForestGreen,
    secondary = WarmBrown,
    onSecondary = WarmWhite,
    secondaryContainer = Sand,
    onSecondaryContainer = MainText,
    tertiary = HomeSettingsInk,
    onTertiary = WarmWhite,
    background = Cream,
    onBackground = DeepForestGreen,
    surface = WarmWhite,
    onSurface = DeepForestGreen,
    surfaceVariant = Sand,
    onSurfaceVariant = WarmBrown,
    outline = HomeCardBorder,
    outlineVariant = HomeCardBorder,
)

@Composable
fun BulosFrontEndTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val systemDensity = LocalDensity.current
    val scaledDensity = remember(systemDensity.density, systemDensity.fontScale, fontScale) {
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale * fontScale,
        )
    }
    CompositionLocalProvider(
        LocalBulosDarkTheme provides darkTheme,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
