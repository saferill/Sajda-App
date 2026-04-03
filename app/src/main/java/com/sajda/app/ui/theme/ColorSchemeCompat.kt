package com.sajda.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val ColorScheme.isLightPalette: Boolean
    get() = background.luminance() > 0.5f

val ColorScheme.surfaceContainerLowest: Color
    get() = if (isLightPalette) SajdaSurfaceLowest else SajdaDarkBackground

val ColorScheme.surfaceContainerLow: Color
    get() = if (isLightPalette) SajdaSurfaceLow else SajdaDarkSurfaceLow

val ColorScheme.surfaceContainer: Color
    get() = if (isLightPalette) Color(0xFFEBEFED) else Color(0xFF212826)

val ColorScheme.surfaceContainerHigh: Color
    get() = if (isLightPalette) SajdaSurfaceHigh else SajdaDarkSurfaceHigh

val ColorScheme.surfaceContainerHighest: Color
    get() = if (isLightPalette) SajdaSurfaceHighest else SajdaDarkSurfaceHighest
