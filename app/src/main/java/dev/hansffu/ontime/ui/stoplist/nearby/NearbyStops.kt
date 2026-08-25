package dev.hansffu.ontime.ui.stoplist.nearby

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.stopListSection

sealed interface NearbyStopState {
    data object Uninitialized : NearbyStopState
    data object NoPermission : NearbyStopState
    data object Loading : NearbyStopState
    data class StopsFound(val stops: List<Stop>, val refresh: () -> Unit) : NearbyStopState
}

@Composable
fun NearbyStopsScreen(
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
    navController: NavController,
) {
    val nearbyStopState by nearbyViewModel.nearbyStopState.collectAsState()
    NearbyStopsUi(
        nearbyViewModel = nearbyViewModel,
        nearbyStopState = nearbyStopState,
        navController = navController,
    )
}

@Composable
fun NearbyStopsUi(
    nearbyViewModel: NearbyViewModel,
    nearbyStopState: NearbyStopState,
    navController: NavController,
    columnState: ScalingLazyListState = rememberScalingLazyListState()
) {
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        ScalingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
        ) {
            when (nearbyStopState) {
                is NearbyStopState.Uninitialized,
                NearbyStopState.NoPermission,
                -> item { LocationPermissionChecker(nearbyViewModel) }

                is NearbyStopState.Loading -> item { LoadingState() }
                is NearbyStopState.StopsFound -> {
                    stopListSection(R.string.nearby_header, nearbyStopState.stops, navController)
                }

            }
        }
    }
}

@Composable
private fun LocationPermissionChecker(nearbyViewModel: NearbyViewModel) {
    val context = LocalContext.current
    val permissions = nearbyViewModel.locationPermissions
    var allPermissionsGranted by remember(permissions) {
        mutableStateOf(context.hasPermissions(permissions))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        allPermissionsGranted = permissions.all { results[it] == true }
    }

    if (allPermissionsGranted) {
        LaunchedEffect(Unit) {
            Log.i("PermissionChecker", "Getting location")
            nearbyViewModel.refresh()
        }
    } else {
        PermissionRequester { permissionLauncher.launch(permissions.toTypedArray()) }
    }
}

private fun Context.hasPermissions(permissions: List<String>): Boolean =
    permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

@Composable
private fun LoadingState() {
    Row {
        Text(text = "Henter stopp...")
    }
}

@Composable
private fun PermissionRequester(launchRequest: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
