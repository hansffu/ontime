package dev.hansffu.ontime.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

@Composable
fun OntimeTheme(content: @Composable () -> Unit) {
    val colorScheme = dynamicColorScheme(LocalContext.current) ?: fallbackColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
