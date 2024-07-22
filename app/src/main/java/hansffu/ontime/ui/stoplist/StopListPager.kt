package hansffu.ontime.ui.stoplist

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.TimeText
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.pager.PagerScreen
import hansffu.ontime.LocationViewModel
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.components.Pager

@ExperimentalWearMaterialApi
@Composable
fun StopListPager(
    stopListViewModel: StopListViewModel,
    onStopSelected: (Stop) -> Unit,
    locationViewModel: LocationViewModel,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    PagerScreen(state = pagerState) { page ->
        ScreenScaffold {
            when (page) {
                0 -> NearbyStops(locationViewModel = locationViewModel)
                1 -> StopListUi(
                    stopListViewModel = stopListViewModel,
                    stopListType = StopListType.FAVORITES,
                    onStopSelected = onStopSelected,
                )
            }
        }
    }
}
