package com.sajda.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SajdaPrimary = Color(0xFF0F5238)
val SajdaPrimaryContainer = Color(0xFF2D6A4F)
val SajdaPrimaryFixed = Color(0xFFB1F0CE)
val SajdaPrimaryFixedDim = Color(0xFF95D4B3)
val SajdaSecondary = Color(0xFF4B6359)
val SajdaSecondaryContainer = Color(0xFFCEE9DC)
val SajdaAccent = Color(0xFFD4E9DE)
val SajdaBackground = Color(0xFFF7FAF8)
val SajdaSurface = Color(0xFFF7FAF8)
val SajdaSurfaceLow = Color(0xFFF1F4F2)
val SajdaSurfaceLowest = Color(0xFFFFFFFF)
val SajdaSurfaceHigh = Color(0xFFE6E9E7)
val SajdaSurfaceHighest = Color(0xFFE0E3E1)
val SajdaOutline = Color(0xFF707973)
val SajdaOutlineVariant = Color(0xFFBFC9C1)
val SajdaOnSurface = Color(0xFF181C1B)
val SajdaOnSurfaceVariant = Color(0xFF404943)
val SajdaError = Color(0xFFBA1A1A)
val SajdaWarm = Color(0xFF713638)

val SajdaDarkBackground = Color(0xFF121715)
val SajdaDarkSurface = Color(0xFF151A18)
val SajdaDarkSurfaceLow = Color(0xFF1C2320)
val SajdaDarkSurfaceLowest = Color(0xFF232B28)
val SajdaDarkSurfaceHigh = Color(0xFF2C3431)
val SajdaDarkSurfaceHighest = Color(0xFF36403C)
val SajdaDarkOnSurface = Color(0xFFF0F4F1)
val SajdaDarkOnSurfaceVariant = Color(0xFFB8C2BC)

private val LightColorScheme = lightColorScheme(
    primary = SajdaPrimary,
    onPrimary = Color.White,
    primaryContainer = SajdaPrimaryContainer,
    onPrimaryContainer = SajdaPrimaryFixed,
    secondary = SajdaSecondary,
    onSecondary = Color.White,
    secondaryContainer = SajdaSecondaryContainer,
    onSecondaryContainer = SajdaPrimary,
    tertiary = SajdaWarm,
    onTertiary = Color.White,
    background = SajdaBackground,
    onBackground = SajdaOnSurface,
    surface = SajdaSurface,
    onSurface = SajdaOnSurface,
    surfaceVariant = SajdaSurfaceHighest,
    onSurfaceVariant = SajdaOnSurfaceVariant,
    surfaceTint = SajdaPrimaryContainer,
    outline = SajdaOutline,
    outlineVariant = SajdaOutlineVariant,
    error = SajdaError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    surfaceBright = SajdaSurface,
    surfaceDim = Color(0xFFD7DBD9),
    surfaceContainerLowest = SajdaSurfaceLowest,
    surfaceContainerLow = SajdaSurfaceLow,
    surfaceContainer = Color(0xFFEBEFED),
    surfaceContainerHigh = SajdaSurfaceHigh,
    surfaceContainerHighest = SajdaSurfaceHighest,
    inverseSurface = Color(0xFF2D3130),
    inverseOnSurface = Color(0xFFEEF1EF),
    inversePrimary = SajdaPrimaryFixedDim
)

private val DarkColorScheme = darkColorScheme(
    primary = SajdaPrimaryFixed,
    onPrimary = Color(0xFF003823),
    primaryContainer = Color(0xFF185239),
    onPrimaryContainer = Color(0xFFD3F7E1),
    secondary = Color(0xFFB2CCC0),
    onSecondary = Color(0xFF1D352B),
    secondaryContainer = Color(0xFF344C42),
    onSecondaryContainer = Color(0xFFD0EBDE),
    tertiary = Color(0xFFE2B9B9),
    onTertiary = Color(0xFF442021),
    background = SajdaDarkBackground,
    onBackground = SajdaDarkOnSurface,
    surface = SajdaDarkSurface,
    onSurface = SajdaDarkOnSurface,
    surfaceVariant = SajdaDarkSurfaceHighest,
    onSurfaceVariant = SajdaDarkOnSurfaceVariant,
    surfaceTint = SajdaPrimaryFixed,
    outline = Color(0xFF88938D),
    outlineVariant = Color(0xFF414A46),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceBright = SajdaDarkSurfaceHighest,
    surfaceDim = Color(0xFF0E1311),
    surfaceContainerLowest = SajdaDarkBackground,
    surfaceContainerLow = SajdaDarkSurfaceLow,
    surfaceContainer = Color(0xFF212826),
    surfaceContainerHigh = SajdaDarkSurfaceHigh,
    surfaceContainerHighest = SajdaDarkSurfaceHighest,
    inverseSurface = Color(0xFFE3E8E4),
    inverseOnSurface = Color(0xFF2A312F),
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
    return Brush.radialGradient(
        colors = listOf(
            colors.surface,
            colors.surfaceContainerLow,
            colors.background
        )
    )
}

@Composable
fun sajdaHeroBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(colors.primaryContainer, colors.primary)
    )
}
