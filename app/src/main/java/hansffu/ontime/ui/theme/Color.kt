package hansffu.ontime.ui.theme

import androidx.wear.compose.material.Colors
import androidx.compose.ui.graphics.Color

internal val onTimeColorPalette: Colors = Colors(
    background = Color(0xFF000000),
    surface = Color(0xFF212124),
    onSurface = Color(0xffffffff),
    onSurfaceVariant = Color(0xffdadce0),
    onBackground = Color(0xffffffff),
    primary = Color(0xfff44336),
)
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val Red400 = Color(0xFFCF6679)

/**
 * Custom color palette for Wear App.
 */
internal val wearColorPalette: Colors = Colors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200,
    secondaryVariant = Teal200,
    error = Red400,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onError = Color.Black
)