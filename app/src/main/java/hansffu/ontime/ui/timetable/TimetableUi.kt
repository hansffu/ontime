package hansffu.ontime.ui.timetable

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import hansffu.ontime.TimetableViewModel
import hansffu.ontime.graphql.StopPlaceQuery
import hansffu.ontime.model.LineDeparture
import hansffu.ontime.ui.components.OntimeScaffold
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.utils.rememberScrollingScalingLazyListState
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Composable
fun Timetable(
    timetableViewModel: TimetableViewModel,
    stopId: String,
    stopName: String
) {
    val lineDeparturesState: List<LineDeparture>? by timetableViewModel.getDepartures(stopId)
        .observeAsState()
    val isFavorite by timetableViewModel.isFavorite(stopId).observeAsState()

    val scalingLazyListState = rememberScrollingScalingLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    OntimeScaffold(scalingLazyListState) {
        SwipeRefresh(state = swipeRefreshState, onRefresh = { /*TODO*/ }) {

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                state = scalingLazyListState,
                contentPadding = PaddingValues(
                    top = 28.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 40.dp
                ),
            ) {
                item { Spacer(modifier = Modifier.size(20.dp)) }
                item { ListHeader { Text(stopName) } }
                lineDeparturesState.let { lineDepartures ->
                    when (lineDepartures) {
                        null -> items(3) { PlaceholderCard() }
                        else -> items(lineDepartures.size) { index ->
                            val times = lineDepartures[index].departures
                                .mapNotNull(StopPlaceQuery.EstimatedCall::expectedArrivalTime)
                            with(lineDepartures[index]) {
                                LineDepartureCard(
                                    lineDirectionRef.lineRef,
                                    lineDirectionRef.destinationRef,
                                    times
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.size(8.dp)) }
                isFavorite?.let { favorite ->
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            ToggleChip(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                checked = favorite,
                                onCheckedChange = {
                                    timetableViewModel.toggleFavorite(id = stopId, name = stopName)
                                },
                                label = { Text("Favoritt") },
                                appIcon = { Icon(Icons.Default.Favorite, "Favoritt") }
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun LineDepartureCard(lineNumber: String, stopName: String, departureTimes: List<OffsetDateTime>) {
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
                            text = stopName,
                            style = MaterialTheme.typography.title3,
                            color = MaterialTheme.colors.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(contentAlignment = Alignment.TopEnd) {
                        Text(
                            text = lineNumber,
                            style = MaterialTheme.typography.title3,
                            color = MaterialTheme.colors.onSurface,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departureTimes.forEach {
                        Text(
                            text = it.toTimeString(),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
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
fun DefaultPreview() {
    OntimeTheme {
        LineDepartureCard(
            "23",
            "Lysaker and very long text",
            listOf(
                OffsetDateTime.now(),
                OffsetDateTime.from(Instant.now().plus(2, ChronoUnit.MINUTES)),
                OffsetDateTime.from(Instant.now().plus(12, ChronoUnit.MINUTES)),
                OffsetDateTime.from(Instant.now().plus(22, ChronoUnit.MINUTES)),
            )
        )
    }
}

@SuppressLint("SimpleDateFormat")
private fun OffsetDateTime.toTimeString(): String {
    val timeMins = Duration.between(OffsetDateTime.now(), this).toMinutes()
    return when {
        timeMins <= 0 -> "Nå"
        timeMins >= 20 -> format(DateTimeFormatter.ofPattern("HH:mm"))
        else -> "$timeMins\u00A0min"
    }
}
