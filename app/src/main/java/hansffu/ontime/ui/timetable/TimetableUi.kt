package hansffu.ontime.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import hansffu.ontime.TimetableViewModel
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.ui.components.OntimeList
import hansffu.ontime.ui.components.timetable.Timetable
import hansffu.ontime.utils.rememberScrollingScalingLazyListState


@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel,
    stopId: String,
    stopName: String
) {
    val lineDeparturesState: List<LineDeparture>? by timetableViewModel.getDepartures(stopId)
        .observeAsState()
    val isFavorite by timetableViewModel.isFavorite(stopId).observeAsState()

    val scalingLazyListState = rememberScrollingScalingLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        SwipeRefresh(state = swipeRefreshState, onRefresh = { /*TODO*/ }) {
            WithLoadedState(
                lineDepartures = lineDeparturesState,
                favorite = isFavorite,
                onLoading = {
                    PlaceholderList(stopName, scalingLazyListState)
                }) {
                Timetable(
                    stopName = stopName,
                    lineDepartures = lineDepartures,
                    isFavorite = favorite,
                    toggleFavorite = { favorite ->
                        timetableViewModel.toggleFavorite(id = stopId, name = stopName)
                    }
                )
            }
        }
    }
}

data class TimetableUiState(val lineDepartures: List<LineDeparture>, val favorite: Boolean)

@Composable
fun WithLoadedState(
    lineDepartures: List<LineDeparture>?,
    favorite: Boolean?,
    onLoading: @Composable () -> Unit,
    onLoaded: @Composable TimetableUiState.() -> Unit,
) {
    if (lineDepartures != null && favorite != null) {
        onLoaded(TimetableUiState(lineDepartures, favorite))
    } else {
        onLoading()
    }
}


@Composable
fun PlaceholderList(
    stopName: String,
    scalingLazyListState: ScalingLazyListState = rememberScrollingScalingLazyListState(),
) {
    OntimeList(stopName, scalingLazyListState) {
        items(3) {
            PlaceholderCard()
        }
    }
}

@Composable
fun PlaceholderCard() {
    Card(
        onClick = {},
        content = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "          ",
                            modifier = Modifier
                                .fillMaxWidth(.9f)
                                .placeholder(
                                    visible = true,
                                    highlight = PlaceholderHighlight.shimmer(),
                                    color = Color.Gray
                                )
                        )
                    }
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = "",
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .width(30.dp)
                                .placeholder(
                                    visible = true,
                                    highlight = PlaceholderHighlight.shimmer(),
                                    color = Color.Gray
                                )
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "",
                        color = MaterialTheme.colors.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth(.8f)
                            .padding(top = 5.dp)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                                color = Color.Gray
                            )
                    )

                }
            }
        }
    )
}

@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun PlaceholderPreview() {
    PlaceholderList(stopName = "Majorstuen")
}