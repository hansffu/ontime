package hansffu.ontime.ui.stoplist

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import hansffu.ontime.FavoriteViewModel
import hansffu.ontime.R
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.components.ActionChip
import hansffu.ontime.ui.components.OntimeScaffold
import hansffu.ontime.ui.navigation.Screen
import hansffu.ontime.utils.rememberScrollingScalingLazyListState

@Composable
fun StopListUi(
    favoriteModel: FavoriteViewModel,
    stopListType: StopListType,
    onStopSelected: (Stop) -> Unit,
    navigateTo: (Screen) -> Unit,
    scalingLazyListState: ScalingLazyListState = rememberScrollingScalingLazyListState()
) {
    val stops by favoriteModel.run {
        when (stopListType) {
            StopListType.FAVORITES -> favoriteStops
            StopListType.NEARBY -> nearbyStops
        }
    }.observeAsState(emptyList())

    OntimeScaffold(scalingLazyListState = scalingLazyListState) {

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
            item { Header(stopListType) }
            items(stops.size) { index ->
                val stop = stops[index]
                StopChip(stop = stop, onClick = { onStopSelected(stop) })
            }
            if (stopListType === StopListType.FAVORITES) {
                item { Spacer(modifier = Modifier.size(8.dp)) }
                item {
                    ActionChip(
                        label = stringResource(R.string.nearby_header),
                        onClick = { navigateTo(Screen.NearbyStops) }
                    )
                }
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
private fun Header(stopListType: StopListType) {
    val text = when (stopListType) {
        StopListType.FAVORITES -> R.string.favorites_header
        StopListType.NEARBY -> R.string.nearby_header
    }
    ListHeader { Text(stringResource(text)) }
}

@Preview(
    showBackground = true,
    device = "spec:shape=Square,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun DefaultPreview() {
    StopListUi(FavoriteViewModel(Application()), StopListType.NEARBY, {}, {})
}

