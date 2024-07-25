package hansffu.ontime.ui.stoplist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import hansffu.ontime.FavoritesViewModel
import hansffu.ontime.LocationViewModel
import hansffu.ontime.R
import hansffu.ontime.model.Stop

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun FavoritesScreen(
    favoritesViewModel: FavoritesViewModel,
    locationViewModel: LocationViewModel,
    onStopSelected: (Stop) -> Unit
) {
    val columnState = rememberResponsiveColumnState()
    val favorites by favoritesViewModel.favoriteStops.observeAsState(emptyList())
    val nearbyStopState by rememberNearbyStopsState(locationViewModel = locationViewModel)

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(columnState = columnState) {
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
                locationViewModel = locationViewModel,
                nearbyStopState = nearbyStopState,
                onStopSelected = onStopSelected
            )
        }
    }
}

