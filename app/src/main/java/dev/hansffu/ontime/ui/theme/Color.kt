package dev.hansffu.ontime.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

internal val onTimeColorScheme = ColorScheme(
    background = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF171313),
    surfaceContainer = Color(0xFF241D1D),
    surfaceContainerHigh = Color(0xFF332727),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFE6BDB8),
    onBackground = Color(0xFFFFFFFF),
    primary = Color(0xFFFFB4AB),
    primaryDim = Color(0xFFE5736A),
    primaryContainer = Color(0xFF8C1D18),
    onPrimary = Color(0xFF5F110E),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB8),
    secondaryDim = Color(0xFFC99F9A),
    secondaryContainer = Color(0xFF5A4040),
    onSecondary = Color(0xFF432B2B),
    onSecondaryContainer = Color(0xFFFFDAD6),
    outline = Color(0xFFAF8C88),
    outlineVariant = Color(0xFF604744),
)

@Composable
internal fun primaryTintedSurfaceColor(): Color =
    with(MaterialTheme.colorScheme) {
        lerp(surfaceContainer, primary, 0.18f)
    }
