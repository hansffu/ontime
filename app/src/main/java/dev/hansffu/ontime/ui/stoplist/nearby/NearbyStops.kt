@file:OptIn(ExperimentalHorologistApi::class)

package dev.hansffu.ontime.ui.stoplist.nearby

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.fillMaxRectangle
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.navigation.NavigationControl

sealed interface NearbyStopState {
    data object Uninitialized : NearbyStopState
    data object NoPermission : NearbyStopState
    data object Loading : NearbyStopState
    data class StopsFound(val stops: List<Stop>, val refresh: () -> Unit) : NearbyStopState
}

@Composable
fun NearbyStopsScreen(
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
    navigate: NavigationControl,
) {
    val nearbyStopState by nearbyViewModel.nearbyStopState.collectAsState()
    NearbyStopsUi(
        nearbyViewModel = nearbyViewModel,
        nearbyStopState = nearbyStopState,
        navigate = navigate
    )
}

@Composable
fun NearbyStopsUi(
    nearbyViewModel: NearbyViewModel,
    nearbyStopState: NearbyStopState,
    navigate: NavigationControl,
    columnState: ScalingLazyColumnState = rememberColumnState()
) {
    ScreenScaffold {
        ScalingLazyColumn(columnState = columnState) {
            when (nearbyStopState) {
                is NearbyStopState.Uninitialized,
                NearbyStopState.NoPermission,
                -> item { LocationPermissionChecker(nearbyViewModel) }

                is NearbyStopState.Loading -> item { LoadingState() }
                is NearbyStopState.StopsFound -> {
                    item { ResponsiveListHeader { Text(text = stringResource(id = R.string.nearby_header)) } }
                    items(nearbyStopState.stops) {
                        Chip(label = it.name, onClick = { navigate.toStops(it) })
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPermissionChecker(nearbyViewModel: NearbyViewModel) {
    val locationPermissions = rememberMultiplePermissionsState(nearbyViewModel.locationPermissions)
    if (!locationPermissions.allPermissionsGranted) {
        PermissionRequester {
            locationPermissions.launchMultiplePermissionRequest()
        }
    } else {
        LaunchedEffect(locationPermissions.allPermissionsGranted) {
            Log.i("PermissionChecker", "Getting location")
            nearbyViewModel.refresh()
        }
    }
}

@Composable
private fun LoadingState() {
    Row {
        Text(text = "Henter stopp...")
    }
}

@Composable
private fun PermissionRequester(launchRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxRectangle()
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
