package hansffu.ontime.ui.timetable

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import hansffu.ontime.StopPlaceQuery
import hansffu.ontime.TimetableViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.utils.rememberScrollingScalingLazyListState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun Timetable(
    stop: Stop,
    timetableViewModel: TimetableViewModel,
    scalingLazyListState: ScalingLazyListState = rememberScrollingScalingLazyListState()
) {
    val lineDeparturesState by timetableViewModel.getLineDepartures().observeAsState()
    val isFavorite by timetableViewModel.isFavorite.observeAsState()

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
        item { ListHeader { Text(stop.name) } }
        lineDeparturesState.let { lineDepartures ->
            when (lineDepartures) {
                null -> item { Text("Laster...") }
                else -> items(lineDepartures.size) { index ->
                    val times = lineDepartures[index].departures
                        .mapNotNull(StopPlaceQuery.EstimatedCall::expectedArrivalTime)
                        .map { it.toDate() }
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
        isFavorite?.let {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ToggleChip(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        checked = it,
                        onCheckedChange = { timetableViewModel.toggleFavorite(stop) },
                        label = { Text("Favoritt") },
                        appIcon = { Icon(Icons.Default.Favorite, "Favoritt") }
                    )
                }

            }
        }
    }


}

@Composable
fun LineDepartureCard(lineNumber: String, stopName: String, departureTimes: List<Date>) {
    Card(
        onClick = {},
        content = {
            Column() {
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
                            color = MaterialTheme.colors.onSurfaceVariant2
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
fun DefaultPreview() {
    OntimeTheme {
        LineDepartureCard(
            "23",
            "Lysaker and very long text",
            listOf(
                Date(),
                Date.from(Instant.now().plus(2, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(12, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(22, ChronoUnit.MINUTES)),
            )
        )
    }
}

@SuppressLint("SimpleDateFormat")
private fun Date.toTimeString(): String {
    val timeMins = (time - Date().time) / 60000
    return when {
        timeMins <= 0 -> "Nå"
        timeMins >= 20 -> SimpleDateFormat("HH:mm").format(this)
        else -> "$timeMins\u00A0min"
    }
}

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("SimpleDateFormat")
fun String.toDate(): Date = try {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    sdf.parse(this)
} catch (e: ParseException) {
    Log.e("String to time", "parse error: $this", e)
    Date()
}
