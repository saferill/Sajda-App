package com.sajda.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SajdaPrimary = Color(0xFF00595C)
val SajdaPrimaryContainer = Color(0xFF0D7377)
val SajdaPrimaryFixed = Color(0xFF9DF0F4)
val SajdaPrimaryFixedDim = Color(0xFF81D4D8)
val SajdaSecondary = Color(0xFF8E4E14)
val SajdaSecondaryContainer = Color(0xFFFFAB69)
val SajdaAccent = Color(0xFFFFDCC4)
val SajdaBackground = Color(0xFFF9F9F9)
val SajdaSurface = Color(0xFFF9F9F9)
val SajdaSurfaceLow = Color(0xFFF3F3F3)
val SajdaSurfaceLowest = Color(0xFFFFFFFF)
val SajdaSurfaceHigh = Color(0xFFE8E8E8)
val SajdaSurfaceHighest = Color(0xFFE2E2E2)
val SajdaOutline = Color(0xFF6E7979)
val SajdaOutlineVariant = Color(0xFFBEC9C9)
val SajdaOnSurface = Color(0xFF1A1C1C)
val SajdaOnSurfaceVariant = Color(0xFF3E4949)
val SajdaError = Color(0xFFBA1A1A)
val SajdaWarm = Color(0xFF434F6E)

val SajdaDarkBackground = Color(0xFF0E1517)
val SajdaDarkSurface = Color(0xFF10181A)
val SajdaDarkSurfaceLow = Color(0xFF162022)
val SajdaDarkSurfaceLowest = Color(0xFF1A2528)
val SajdaDarkSurfaceHigh = Color(0xFF223033)
val SajdaDarkSurfaceHighest = Color(0xFF2B3B3E)
val SajdaDarkOnSurface = Color(0xFFF0F1F1)
val SajdaDarkOnSurfaceVariant = Color(0xFFB7C4C4)

private val LightColorScheme = lightColorScheme(
    primary = SajdaPrimary,
    onPrimary = Color.White,
    primaryContainer = SajdaPrimaryContainer,
    onPrimaryContainer = Color(0xFFA2F5F9),
    secondary = SajdaSecondary,
    onSecondary = Color.White,
    secondaryContainer = SajdaSecondaryContainer,
    onSecondaryContainer = Color(0xFF783D01),
    tertiary = SajdaWarm,
    onTertiary = Color.White,
    background = SajdaBackground,
    onBackground = SajdaOnSurface,
    surface = SajdaSurface,
    onSurface = SajdaOnSurface,
    surfaceVariant = SajdaSurfaceHighest,
    onSurfaceVariant = SajdaOnSurfaceVariant,
    surfaceTint = SajdaPrimary,
    outline = SajdaOutline,
    outlineVariant = SajdaOutlineVariant,
    error = SajdaError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    inverseSurface = Color(0xFF2F3131),
    inverseOnSurface = Color(0xFFF0F1F1),
    inversePrimary = SajdaPrimaryFixedDim
)

private val DarkColorScheme = darkColorScheme(
    primary = SajdaPrimaryFixed,
    onPrimary = Color(0xFF002E30),
    primaryContainer = Color(0xFF004F52),
    onPrimaryContainer = Color(0xFFA2F5F9),
    secondary = Color(0xFFFFB780),
    onSecondary = Color(0xFF4E2600),
    secondaryContainer = Color(0xFF6F3800),
    onSecondaryContainer = Color(0xFFFFDCC4),
    tertiary = Color(0xFFB9C6EA),
    onTertiary = Color(0xFF16213D),
    tertiaryContainer = Color(0xFF3A4664),
    onTertiaryContainer = Color(0xFFE0E7FF),
    background = SajdaDarkBackground,
    onBackground = SajdaDarkOnSurface,
    surface = SajdaDarkSurface,
    onSurface = SajdaDarkOnSurface,
    surfaceVariant = SajdaDarkSurfaceHighest,
    onSurfaceVariant = SajdaDarkOnSurfaceVariant,
    surfaceTint = SajdaPrimaryFixed,
    outline = Color(0xFF7B8C8C),
    outlineVariant = Color(0xFF334346),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE7ECEC),
    inverseOnSurface = Color(0xFF202729),
    inversePrimary = SajdaPrimary
)

@Composable
fun SajdaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SajdaTypography,
        content = content
    )
}

@Composable
fun sajdaBackgroundBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            colors.background,
            colors.surface,
            colors.surfaceContainerLow
        )
    )
}

@Composable
fun sajdaHeroBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            colors.primaryContainer,
            colors.primary,
            colors.primary.copy(alpha = 0.86f)
        )
    )
}
