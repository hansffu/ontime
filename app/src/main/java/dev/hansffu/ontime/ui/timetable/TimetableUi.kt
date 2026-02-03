package dev.hansffu.ontime.ui.timetable

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.hansffu.ontime.ui.components.timetable.Timetable
import dev.hansffu.ontime.viewmodels.TimetableUiState
import dev.hansffu.ontime.viewmodels.TimetableViewModel


@OptIn(ExperimentalHorologistApi::class, ExperimentalMaterialApi::class)
@Composable
fun TimetableUi(
    timetableViewModel: TimetableViewModel,
    stopId: String,
    stopName: String,
) {

    val state by timetableViewModel.uiState.collectAsState()

    val columnState = rememberResponsiveColumnState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.refreshing,
        refreshThreshold = 40.dp,
        onRefresh = { timetableViewModel.loadDepartures() }
    )
    LaunchedEffect(stopId) {
        timetableViewModel.loadDepartures()
    }

    ScreenScaffold(
        scrollState = columnState,
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        PullRefreshIndicator(
            refreshing = state.refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Timetable(
            uiState = state,
            scalingLazyColumnState = columnState,
            toggleFavorite = {
                timetableViewModel.toggleFavoriteStop(
                    id = stopId,
                    name = stopName
                )
            },
            toggleFavoriteDeparture = {
                timetableViewModel.toggleFavoriteDeparture(it, stopId)
            }
        )


    }
}
