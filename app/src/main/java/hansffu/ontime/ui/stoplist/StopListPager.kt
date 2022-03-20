package hansffu.ontime.ui.stoplist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.wear.compose.material.*
import com.google.accompanist.pager.ExperimentalPagerApi
import hansffu.ontime.StopListViewModel
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.LocalRotatingInputConsumer
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
        val focusRequesters = remember {
            StopListType.values().associateWith { FocusRequester() }
        }

        Pager(
            pages = StopListType.values().asList(),
            onFocusChange = { focusRequesters[it]?.requestFocus() }
        ) { stopListType ->
            CompositionLocalProvider(LocalRotatingInputConsumer provides focusRequesters[stopListType]) {
                StopListUi(
                    stopListViewModel = stopListViewModel,
                    stopListType = stopListType,
                    onStopSelected = onStopSelected,
                )
            }
        }
    }
}

