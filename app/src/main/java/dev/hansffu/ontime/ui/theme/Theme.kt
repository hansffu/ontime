package dev.hansffu.ontime.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun OntimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = onTimeColorScheme,
        typography = Typography,
        content = content,
    )
}
