package dev.hansffu.ontime.ui.stoplist

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.viewmodels.FavoritesViewModel
import dev.hansffu.ontime.viewmodels.LocationViewModel

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterialApi::class)
@Composable
fun StopsScreen(
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel,
    onStopSelected: (Stop) -> Unit,
) {
    val columnState = rememberResponsiveColumnState()
    val favorites by favoritesViewModel.favoriteStops.observeAsState(emptyList())
    val nearbyStopState by rememberNearbyStopsState(locationViewModel = locationViewModel)
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            (nearbyStopState as? NearbyStopState.StopsFound)?.let { it.refresh() }
        })
    LaunchedEffect(nearbyStopState) {
        if (refreshing && nearbyStopState is NearbyStopState.StopsFound) {
            refreshing = false
        }
    }

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(stringResource(R.string.favorites_header))
                }
            }
            items(favorites) { stop ->
                Chip(label = stop.name, onClick = { onStopSelected(stop) })
            }
            item { ResponsiveListHeader { Text(text = stringResource(id = R.string.nearby_header)) } }
            nearbyStopsList(
                nearbyStopState = nearbyStopState,
                onStopSelected = onStopSelected,
                locationViewModel = locationViewModel
            )
        }
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

