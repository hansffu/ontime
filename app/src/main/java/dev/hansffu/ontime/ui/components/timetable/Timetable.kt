package dev.hansffu.ontime.ui.components.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.model.LineDeparture

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Timetable(
    stopName: String,
    lineDepartures: List<LineDeparture>?,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
    scalingLazyColumnState: ScalingLazyColumnState,
) {

    ScalingLazyColumn(scalingLazyColumnState) {
        item {
            ResponsiveListHeader(contentPadding = firstItemPadding()) {
                Text(stopName)
            }
        }

        if (lineDepartures != null) {
            items(lineDepartures) { lineDeparture ->
                val times = lineDeparture.departures.mapNotNull { it.expectedArrivalTime }
                LineDepartureCard(lineDeparture.lineDirectionRef, times)
            }
        } else {
            item {
                Row {
                    Text(text = "Henter avganger...")
                }
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
                    onCheckedChange = { toggleFavorite() },
                    label = { Text("Favoritt") },
                    appIcon = { Icon(Icons.Default.Favorite, "Favoritt") },
                    toggleControl = { }
                )
            }

        }
    }
}
