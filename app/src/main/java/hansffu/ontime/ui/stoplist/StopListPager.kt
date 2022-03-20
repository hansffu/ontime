package hansffu.ontime.ui.stoplist

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType

@OptIn(ExperimentalPagerApi::class, ExperimentalWearMaterialApi::class)
@Composable
fun StopListPager(
    stopListViewModel: StopListViewModel,
    onStopSelected: (Stop) -> Unit,
) {
    val pagerState = rememberPagerState()
    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        HorizontalPager(count = StopListType.values().size, state = pagerState) { page ->
            val stopListType = StopListType.values()[page]
            StopListUi(
                stopListViewModel = stopListViewModel,
                stopListType = stopListType,
                onStopSelected = onStopSelected,
            )
        }
    }
}
