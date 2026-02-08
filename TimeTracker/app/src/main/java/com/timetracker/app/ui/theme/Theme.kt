package com.timetracker.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary500,
    secondary = Secondary500,
    onSecondary = Color.White,
    secondaryContainer = Secondary100,
    onSecondaryContainer = Secondary500,
    tertiary = Accent500,
    onTertiary = Color.White,
    tertiaryContainer = Accent100,
    onTertiaryContainer = Accent500,
    background = Neutral50,
    onBackground = Neutral800,
    surface = Color.White,
    onSurface = Neutral800,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    surfaceTint = Primary500,
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral50,
    error = Error500,
    onError = Color.White,
    errorContainer = Error100,
    onErrorContainer = Error500,
    outline = Neutral300,
    outlineVariant = Neutral200,
    scrim = Neutral900.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary400,
    onPrimary = Neutral900,
    primaryContainer = Primary500.copy(alpha = 0.3f),
    onPrimaryContainer = Primary100,
    secondary = Secondary400,
    onSecondary = Neutral900,
    secondaryContainer = Secondary500.copy(alpha = 0.3f),
    onSecondaryContainer = Secondary100,
    tertiary = Accent400,
    onTertiary = Neutral900,
    tertiaryContainer = Accent500.copy(alpha = 0.3f),
    onTertiaryContainer = Accent100,
    background = Neutral900,
    onBackground = Neutral50,
    surface = Neutral800,
    onSurface = Neutral50,
    surfaceVariant = Neutral700,
    onSurfaceVariant = Neutral300,
    surfaceTint = Primary400,
    inverseSurface = Neutral50,
    inverseOnSurface = Neutral800,
    error = Color(0xFFFCA5A5),
    onError = Neutral900,
    errorContainer = Error500.copy(alpha = 0.3f),
    onErrorContainer = Color(0xFFFEE2E2),
    outline = Neutral600,
    outlineVariant = Neutral700,
    scrim = Neutral900.copy(alpha = 0.8f)
)

@Composable
fun TimeTrackerTheme(
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
