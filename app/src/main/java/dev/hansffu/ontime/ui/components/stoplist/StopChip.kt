package dev.hansffu.ontime.ui.components.stoplist

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dev.hansffu.ontime.model.Stop

@Composable
fun StopChip(stop: Stop, onClick: () -> Unit) {
    Chip(
        label = {
            Text(
                text = stop.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        },
        onClick = onClick,
        colors = ChipDefaults.primaryChipColors(MaterialTheme.colors.surface),
    )
}
