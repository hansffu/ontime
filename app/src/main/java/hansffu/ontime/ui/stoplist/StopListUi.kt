package hansffu.ontime.ui.stoplist

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import hansffu.ontime.FavoritesViewModel
import hansffu.ontime.LocationState
import hansffu.ontime.LocationViewModel
import hansffu.ontime.R
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.service.StopService

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun FavoriteStops(favoritesViewModel: FavoritesViewModel, onStopSelected: (Stop) -> Unit) {
    val columnState = rememberResponsiveColumnState()
    val favorites by favoritesViewModel.favoriteStops.observeAsState(emptyList())
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
            item { Text(stringResource(R.string.favorites_header)) }
            items(favorites) { stop ->
                Chip(label = stop.name, onClick = { onStopSelected(stop) })
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun NearbyStops(
    locationViewModel: LocationViewModel,
    onStopSelected: (Stop) -> Unit,
) {
    val columnState = rememberResponsiveColumnState()
    val locationStateHolder by locationViewModel.locationState

    ScreenScaffold(scrollState = columnState) {
        when (val locationState = locationStateHolder) {
            is LocationState.Uninitialized -> {
                LocationPermissionChecker(locationViewModel)
            }

            is LocationState.Loading -> LoadingState()

            is LocationState.LocationFound -> {
                var stops by remember { mutableStateOf<List<Stop>>(emptyList()) }
                val stopService by remember {
                    mutableStateOf(StopService())
                }
                LaunchedEffect(locationState.location) {
                    stops = stopService.findStopsNear(locationState.location)
                }
                ScalingLazyColumn(columnState = columnState) {
                    item { Text(stringResource(R.string.nearby_header)) }
                    items(stops) { stop ->
                        Chip(
                            label = stop.name,
                            onClick = { onStopSelected(stop) }
                        )
                    }
                }
            }

        }

    }
}

sealed interface NearbyStopState {
    data object Uninitialized : NearbyStopState
    data object Loading : NearbyStopState
    data class StopsFound(val stops: List<Stop>, val refresh: () -> Unit) : NearbyStopState
}

@Composable
fun rememberNearbyStopsState(locationViewModel: LocationViewModel): State<NearbyStopState> {
    val locationStateHolder by locationViewModel.locationState
    var nearbyStopState: MutableState<NearbyStopState> =
        remember { mutableStateOf(NearbyStopState.Uninitialized) }

    when (val locationState = locationStateHolder) {
        is LocationState.Uninitialized -> {
            nearbyStopState.value = NearbyStopState.Uninitialized
        }

        is LocationState.Loading -> {
            nearbyStopState.value = NearbyStopState.Loading
        }

        is LocationState.LocationFound -> {
            val stopService by remember {
                mutableStateOf(StopService())
            }
            LaunchedEffect(locationState.location) {
                val stops = stopService.findStopsNear(locationState.location)
                nearbyStopState.value =
                    NearbyStopState.StopsFound(stops) { locationViewModel.refreshLocation() }
            }
        }

    }
    return nearbyStopState
}

@OptIn(ExperimentalHorologistApi::class)
fun ScalingLazyListScope.nearbyStopsList(
    locationViewModel: LocationViewModel,
    nearbyStopState: NearbyStopState,
    onStopSelected: (Stop) -> Unit,
) {
    when (nearbyStopState) {
        is NearbyStopState.Uninitialized -> item { LocationPermissionChecker(locationViewModel) }
        is NearbyStopState.Loading -> item { LoadingState() }
        is NearbyStopState.StopsFound -> items(nearbyStopState.stops) {
            Chip(label = it.name, onClick = { onStopSelected(it) })
        }

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionChecker(locationViewModel: LocationViewModel) {
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    if (!locationPermissions.allPermissionsGranted) {
        PermissionRequester {
            locationPermissions.launchMultiplePermissionRequest()
        }
    } else {
        LaunchedEffect(locationPermissions.allPermissionsGranted) {
            Log.i("PermissionChecker", "Getting location")
            locationViewModel.refreshLocation()
        }
    }
}

@Composable
fun LoadingState() {
    Row {
        Text(text = "Henter stopp...")
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
