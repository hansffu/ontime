package hansffu.ontime.ui.stoplist

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.*
import hansffu.ontime.R
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.components.OntimeList
import hansffu.ontime.utils.rememberScrollingScalingLazyListState

@Composable
fun StopListUi(
    stopListViewModel: StopListViewModel,
    stopListType: StopListType,
    onStopSelected: (Stop) -> Unit,
) {
    val scalingLazyListState: ScalingLazyListState = rememberScrollingScalingLazyListState()
    val stops by stopListViewModel.run {
        when (stopListType) {
            StopListType.FAVORITES -> favoriteStops
            StopListType.NEARBY -> nearbyStops
        }
    }.observeAsState(emptyList())

    Scaffold(positionIndicator = { PositionIndicator(scalingLazyListState) }) {

        OntimeList(
            headerText = HeaderText(stopListType)
        ) {
            items(stops) {
                StopChip(stop = it, onClick = { onStopSelected(it) })
            }
        }
    }
}

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

@Composable
fun HeaderText(stopListType: StopListType): String {
    val text = when (stopListType) {
        StopListType.FAVORITES -> R.string.favorites_header
        StopListType.NEARBY -> R.string.nearby_header
    }
    return stringResource(text)
}

@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun DefaultPreview() {
    StopListUi(StopListViewModel(Application()), StopListType.NEARBY) {}
}

