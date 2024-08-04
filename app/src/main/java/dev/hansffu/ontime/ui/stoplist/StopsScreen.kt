@file:OptIn(ExperimentalHorologistApi::class, ExperimentalMaterialApi::class)

package dev.hansffu.ontime.ui.stoplist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.ButtonSize
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.SearchButton
import dev.hansffu.ontime.ui.navigation.Screen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopState
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyViewModel
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.viewmodels.FavoritesViewModel

@Composable
fun StopsScreen(
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
    navController: NavController,
) {
    val columnState = rememberResponsiveColumnState()
    val favorites by favoritesViewModel.favoriteStops.observeAsState(emptyList())
    val nearbyStopState by nearbyViewModel.nearbyStopState.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        refreshThreshold = 50.dp,
        onRefresh = {
            refreshing = true
            (nearbyStopState as? NearbyStopState.StopsFound)?.let { it.refresh() }
        })
    LaunchedEffect(nearbyStopState) {
        if (refreshing && nearbyStopState is NearbyStopState.StopsFound) {
            refreshing = false
        }
    }

    StopScreenUi(
        columnState = columnState,
        pullRefreshState = pullRefreshState,
        nearbyStopState = nearbyStopState,
        favorites = favorites,
        navController = navController,
        refreshing = refreshing,
    )

}

@Composable
private fun StopScreenUi(
    columnState: ScalingLazyColumnState,
    pullRefreshState: PullRefreshState,
    nearbyStopState: NearbyStopState,
    navController: NavController,
    favorites: List<Stop>,
    refreshing: Boolean
) {
    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            item { SearchButtons(navController) }
            item {
                ResponsiveListHeader(contentPadding = firstItemPadding()) {
                    Text(stringResource(R.string.favorites_header))
                }
            }
            items(favorites) { stop ->
                Chip(
                    label = stop.name,
                    onClick = { navController.navigate(Screen.Timetable(stop)) })
            }
            (nearbyStopState as? NearbyStopState.StopsFound)?.let { nearbyStops ->
                item { ResponsiveListHeader { Text(text = stringResource(id = R.string.nearby_header)) } }
                items(nearbyStops.stops.take(3)) { stop ->
                    Chip(
                        label = stop.name,
                        onClick = { navController.navigate(Screen.Timetable(stop)) })
                }
            }
        }

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SearchButtons(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchButton(
            onSubmit = { navController.navigate(Screen.TextSearch(it)) },
            label = stringResource(id = R.string.search_for_stops),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Button(
            imageVector = Icons.Default.NearMe,
            buttonSize = ButtonSize.Small,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = { navController.navigate(Screen.Nearby) },
            contentDescription = stringResource(id = R.string.nearby_button)
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SearchButtonsPreview() {
    OntimeTheme {
        ScreenScaffold {
            ScalingLazyColumn(columnState = rememberResponsiveColumnState()) {
                item { SearchButtons(rememberNavController()) }
            }
        }
    }
}