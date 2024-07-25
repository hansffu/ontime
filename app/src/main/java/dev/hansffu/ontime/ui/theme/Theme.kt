package dev.hansffu.ontime.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun OntimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = onTimeColorPalette,
        typography = Typography,
        content = content
    )
}