package hansffu.ontime.ui.stoplist

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.*
import com.google.accompanist.pager.ExperimentalPagerApi
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.components.Pager

@ExperimentalPagerApi
@ExperimentalWearMaterialApi
@Composable
fun StopListPager(
    stopListViewModel: StopListViewModel,
    onStopSelected: (Stop) -> Unit,
) {

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
    ) {
        Pager(StopListType.values().asList()) { stopListType ->
            StopListUi(
                stopListViewModel = stopListViewModel,
                stopListType = stopListType,
                onStopSelected = onStopSelected,
            )
        }
    }
}
