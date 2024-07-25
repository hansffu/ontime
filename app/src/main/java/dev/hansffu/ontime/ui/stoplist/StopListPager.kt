package dev.hansffu.ontime.ui.stoplist

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.pager.PagerScreen
import dev.hansffu.ontime.FavoritesViewModel
import dev.hansffu.ontime.LocationViewModel
import dev.hansffu.ontime.model.Stop

@ExperimentalWearMaterialApi
@Composable
fun StopListPager(
    favoritesViewModel: FavoritesViewModel,
    onStopSelected: (Stop) -> Unit,
    locationViewModel: LocationViewModel,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    PagerScreen(state = pagerState) { page ->
        ScreenScaffold {
            when (page) {
                0 -> NearbyStops(
                    locationViewModel = locationViewModel,
                    onStopSelected = onStopSelected,
                )

                1 -> FavoriteStops(
                    favoritesViewModel = favoritesViewModel,
                    onStopSelected = onStopSelected,
                )
            }
        }
    }
}
