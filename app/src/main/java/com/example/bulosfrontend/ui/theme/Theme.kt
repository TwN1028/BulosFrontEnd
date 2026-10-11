package com.example.bulosfrontend.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

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
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val systemDensity = LocalDensity.current
    val scaledDensity = remember(systemDensity.density, systemDensity.fontScale, fontScale) {
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale * fontScale,
        )
    }
    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
    }
}
