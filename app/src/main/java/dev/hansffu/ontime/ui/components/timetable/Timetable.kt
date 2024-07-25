package dev.hansffu.ontime.ui.components.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.ui.components.LineDepartureCard
import dev.hansffu.ontime.ui.components.OntimeList

@Composable
fun Timetable(
    stopName: String,
    lineDepartures: List<LineDeparture>,
    isFavorite: Boolean,
    toggleFavorite: (Boolean) -> Unit,
    scalingLazyListState: ScalingLazyListState,

    ) {

    OntimeList(stopName, scalingLazyListState) {
        items(lineDepartures.size) { index ->
            with(lineDepartures[index]) {
                val times = departures.mapNotNull { it.expectedArrivalTime }
                LineDepartureCard(lineDirectionRef, times)
            }
        }
        item { Spacer(modifier = Modifier.size(8.dp)) }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    checked = isFavorite,
                    onCheckedChange = toggleFavorite,
                    label = { Text("Favoritt") },
                    appIcon = { Icon(Icons.Default.Favorite, "Favoritt") },
                    toggleControl = { }
                )
            }

        }
    }
}
