@file:OptIn(ExperimentalWearMaterialApi::class)

package dev.hansffu.ontime.ui.components.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import dev.hansffu.ontime.viewmodels.TimetableUiState

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterialApi::class)
@Composable
fun Timetable(
    uiState: TimetableUiState,
    toggleFavorite: () -> Unit,
    toggleFavoriteDeparture: (LineDirectionRef) -> Unit,
    scalingLazyColumnState: ScalingLazyColumnState,
) {
    ScalingLazyColumn(columnState = scalingLazyColumnState) {
        item {
            ResponsiveListHeader(contentPadding = firstItemPadding()) {
                Text(uiState.stopName)
            }
        }
        when (uiState) {
            is TimetableUiState.Loading -> item {
                Row {
                    Text(text = "Henter avganger...")
                }
            }

            is TimetableUiState.Success -> {
                item { Row { Text("Favoritter (${uiState.favoriteDepartures.size})") } }
                items(uiState.favoriteDepartures) { lineDeparture ->
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
                items(uiState.otherDepartures) { lineDeparture ->
                    val times = lineDeparture.departures.mapNotNull { it.expectedArrivalTime }
                    LineDepartureCard(
                        lineDirectionRef = lineDeparture.lineDirectionRef,
                        departureTimes = times,
                        isFavorite = false,
                        toggleFavorite = toggleFavoriteDeparture,
                        color = lineDeparture.color
                    )
                }

                item { Spacer(modifier = Modifier.size(8.dp)) }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ToggleChip(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            checked = uiState.isFavorite,
                            onCheckedChange = { toggleFavorite() },
                            label = { Text("Favoritt") },
                            appIcon = { Icon(Icons.Default.Favorite, "Favoritt") },
                            toggleControl = { }
                        )
                    }

                }
            }
        }
    }
}

data class TimetableData(
    val loading: Boolean,
    val favoriteDepartures: List<LineDeparture>,
    val otherDepartures: List<LineDeparture>
)
