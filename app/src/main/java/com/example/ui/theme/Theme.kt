package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCoralLight,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryNightCard,
    onPrimaryContainer = SurfaceCream,
    secondary = AccentTeal,
    onSecondary = OnPrimaryWhite,
    tertiary = FameGold,
    onTertiary = OnSurfaceDark,
    background = PrimaryNightMaroon,
    onBackground = OnSurfaceNight,
    surface = SurfaceNightCard,
    onSurface = OnSurfaceNight,
    surfaceVariant = PrimaryNightCard,
    onSurfaceVariant = OnSurfaceNightMuted,
    error = ShameWarningRed,
    onError = OnPrimaryWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCoral,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryCoralLight,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = AccentTeal,
    onSecondary = OnPrimaryWhite,
    tertiary = FameGold,
    onTertiary = OnSurfaceDark,
    background = PrimaryCoral,
    onBackground = OnPrimaryWhite,
    surface = SurfaceCream,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceCreamLight,
    onSurfaceVariant = OnSurfaceMuted,
    error = ShameWarningRed,
    onError = OnPrimaryWhite
)

@Composable
fun StudyOSTheme(
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

