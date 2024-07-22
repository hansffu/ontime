package hansffu.ontime.ui.stoplist

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import hansffu.ontime.LocationState
import hansffu.ontime.LocationViewModel
import hansffu.ontime.R
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.components.OntimeList
import hansffu.ontime.ui.components.stoplist.StopChip

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun StopListUi(
    stopListViewModel: StopListViewModel,
    stopListType: StopListType,
    onStopSelected: (Stop) -> Unit,
) {
    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()
    val stops by stopListViewModel.run {
        when (stopListType) {
            StopListType.FAVORITES -> favoriteStops
            StopListType.NEARBY -> nearbyStops
        }
    }.observeAsState(emptyList())

    Scaffold(positionIndicator = { PositionIndicator(scalingLazyListState) }) {

        OntimeList(
            headerText = headerText(stopListType)
        ) {
            items(stops.size) {
                StopChip(stop = stops[it], onClick = { onStopSelected(stops[it]) })
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalHorologistApi::class)
@Composable
fun NearbyStops(locationViewModel: LocationViewModel) {
    val columnState = rememberResponsiveColumnState()
    val locationStateHolder by locationViewModel.locationState
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    ScreenScaffold(scrollState = columnState) {
        when (val locationState = locationStateHolder) {
            is LocationState.Uninitialized -> {
                if (!locationPermissions.allPermissionsGranted) {
                    PermissionRequester {
                        locationPermissions.launchMultiplePermissionRequest()
                    }
                } else {
                    LaunchedEffect(locationStateHolder) {
                        locationViewModel.refreshLocation()
                    }
                }
            }

            is LocationState.Loading -> {
                Box(modifier = Modifier.fillMaxRectangle()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            is LocationState.LocationFound -> {

                ScalingLazyColumn(columnState = columnState) {
                    item { Text("Nærliggende holdeplasser") }
                    item { Text("location: ${locationState.location}") }
                }
            }

        }

    }
}

@Composable
fun PermissionRequester(launchRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxRectangle()
    ) {
        Text(
            text = "For å kunne vise nærliggende holdeplasser trenger vi tilgang til posisjonen din.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = launchRequest,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Gi tilgang")
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:shape=Round,width=300,height=300,unit=px,dpi=240",
    backgroundColor = 0x000000
)
@Composable
fun PermissionPreview() {
    PermissionRequester {}
}

@Composable
fun headerText(stopListType: StopListType): String {
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
