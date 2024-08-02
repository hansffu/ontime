@file:OptIn(ExperimentalHorologistApi::class)

package dev.hansffu.ontime.ui.stoplist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevice
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.ButtonSize
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.navigation.Screen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopState
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyViewModel
import dev.hansffu.ontime.ui.stoplist.nearby.nearbyStopsList
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.viewmodels.FavoritesViewModel

@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterialApi::class)
@Composable
fun StopsScreen(
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
    onStopSelected: (Stop) -> Unit,
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

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            item { SearchButtons() }
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
                nearbyViewModel = nearbyViewModel
            )
        }
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SearchButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            imageVector = Icons.Default.Search,
            buttonSize = ButtonSize.Small,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = {},
            contentDescription = stringResource(id = R.string.search_button)
        )

        Button(
            imageVector = Icons.Default.NearMe,
            buttonSize = ButtonSize.Small,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = {},
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
                item { SearchButtons() }
            }
        }
    }
}