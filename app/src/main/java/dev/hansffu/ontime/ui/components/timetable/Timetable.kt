@file:OptIn(ExperimentalWearMaterialApi::class)

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
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.model.LineDeparture
import dev.hansffu.ontime.model.LineDirectionRef

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Timetable(
    stopId: String,
    stopName: String,
    timetableData: TimetableData,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
    toggleFavoriteDeparture: (LineDirectionRef) -> Unit,
    scalingLazyColumnState: ScalingLazyColumnState,
) {

    ScalingLazyColumn(scalingLazyColumnState) {
        item {
            ResponsiveListHeader(contentPadding = firstItemPadding()) {
                Text(stopName)
            }
        }

        if (!timetableData.loading) {
            item { Row { Text("Favoritter (${timetableData.favoriteDepartures.size})") } }
            items(timetableData.favoriteDepartures) { lineDeparture ->
                val times = lineDeparture.departures.mapNotNull { it.expectedArrivalTime }
                LineDepartureCard(
                    lineDirectionRef = lineDeparture.lineDirectionRef,
                    departureTimes = times,
                    isFavorite = true,
                    toggleFavorite = toggleFavoriteDeparture,
                    color = lineDeparture.color
                )
            }
            item { Row { Text("Andre") } }
            items(timetableData.otherDepartures) { lineDeparture ->
                val times = lineDeparture.departures.mapNotNull { it.expectedArrivalTime }
                LineDepartureCard(
                    lineDirectionRef = lineDeparture.lineDirectionRef,
                    departureTimes = times,
                    isFavorite = false,
                    toggleFavorite = toggleFavoriteDeparture,
                    color = lineDeparture.color
                )
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

data class TimetableData(
    val loading: Boolean,
    val favoriteDepartures: List<LineDeparture>,
    val otherDepartures: List<LineDeparture>
)
